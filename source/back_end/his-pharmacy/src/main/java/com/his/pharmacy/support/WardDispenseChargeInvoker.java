package com.his.pharmacy.support;

import com.his.charge.dto.FeeBookDTO;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.service.FeeRecordService;
import com.his.charge.support.FeeCatalogResolver;
import com.his.common.enums.EncounterTypeEnum;
import com.his.common.enums.FeeSourceTypeEnum;
import com.his.common.enums.PaymentItemTypeEnum;
import com.his.pharmacy.entity.BizWardDispenseItem;
import com.his.pharmacy.mapper.BizWardDispenseItemMapper;
import com.his.pharmacy.vo.WardDispenseOrderDeptVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 住院摆药记账 —— <b>一条摆药明细 = 一笔记账行；退药 = 把那一行红冲</b>。
 *
 * <p>REQUIRES_NEW 独立事务：药已经摆出去了（扣库存、明细状态推进是真动作），计费失败只是账
 * 没记上，不该把扣库存一起拖回滚。独立成 Bean 是因为 Spring 的 {@code @Transactional} 基于代理，
 * 同类内部自调用注解不生效。
 *
 * <p>失败语义：返回 {@code null} = 未记账，调用方必须让明细上的记账行ID留空并打 warn，
 * 绝不静默当作已计费。
 *
 * <p>三条口径：
 * <ol>
 *   <li><b>幂等锚点来源ID = 摆药明细ID</b>，不是医嘱ID：长期医嘱会反复摆药，每次都是真药真钱。
 *       医嘱这条边由来源单号（医嘱号）表达，四核对按医嘱号回查。</li>
 *   <li><b>退药找回不到原行就返回 {@code null}</b>：当初就没记上账，凭空写一条负数只会多出
 *       一笔无主的红字应收。</li>
 *   <li><b>金额一律单价 × 数量现算</b>，明细上的金额只用于比对告警 —— 不一致时以现算为准，
 *       否则长期医嘱的每日量与摆药量对不上时会静默记错账。</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WardDispenseChargeInvoker {

    private final FeeRecordService feeRecordService;
    private final BizWardDispenseItemMapper bizWardDispenseItemMapper;

    /**
     * 配药记账（独立事务）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public BizFeeRecord charge(BizWardDispenseItem item) {
        if (item.getAdmissionId() == null) {
            log.warn("住院摆药记账缺少 admissionId，记账行无法归属这次住院，本次不记账（摆药明细 {}）", item.getId());
            return null;
        }
        if (item.getPatientId() == null || !StringUtils.hasText(item.getPatientName())) {
            log.warn("住院摆药记账缺少患者快照，本次不记账（admissionId={} 摆药明细 {}）",
                    item.getAdmissionId(), item.getId());
            return null;
        }

        BigDecimal quantity = item.getQuantity() == null ? BigDecimal.ONE : item.getQuantity();
        BigDecimal price = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
        BigDecimal computed = price.multiply(quantity);
        if (item.getAmount() != null && item.getAmount().compareTo(computed) != 0) {
            log.warn("摆药明细 {} 金额 ¥{} 与 单价×数量 ¥{} 不一致，按后者记账",
                    item.getId(), item.getAmount().toPlainString(), computed.toPlainString());
        }

        FeeBookDTO dto = new FeeBookDTO();
        dto.setPatientId(item.getPatientId());
        dto.setPatientNo(item.getPatientNo());
        dto.setPatientName(item.getPatientName());
        dto.setEncounterType(EncounterTypeEnum.INPATIENT.getCode());
        dto.setEncounterId(item.getAdmissionId());
        // 归科按医嘱号反查开立科室，反查不到就留空由日结单列「无科室归属」，不兜底成病区
        WardDispenseOrderDeptVO dept = StringUtils.hasText(item.getOrderNo())
                ? bizWardDispenseItemMapper.selectOrderDept(item.getOrderNo()) : null;
        if (dept != null) {
            dto.setDeptId(dept.getDeptId());
            dto.setDeptName(dept.getDeptName());
        }
        // 摆药只会是药品医嘱
        Integer itemType = PaymentItemTypeEnum.WESTERN_MEDICINE.getCode();
        dto.setItemType(itemType);
        dto.setItemCode(StringUtils.hasText(item.getItemCode()) ? item.getItemCode() : item.getOrderNo());
        dto.setItemName(item.getItemName());
        dto.setSpecification(item.getSpec());
        dto.setUnit(item.getUnit());
        dto.setPrice(price);
        dto.setQuantity(quantity);
        dto.setSourceType(FeeSourceTypeEnum.DISPENSE.getCode());
        dto.setSourceId(item.getId());
        dto.setSourceNo(StringUtils.hasText(item.getOrderNo())
                ? item.getOrderNo() : ("DISP-" + item.getId()));
        dto.setCatalogType(FeeCatalogResolver.byItemType(itemType));
        dto.setRemark("住院摆药配药记账（摆药单 " + item.getDispenseNo()
                + "，明细 " + item.getId()
                + (item.getOrderId() == null ? "" : "，医嘱 " + item.getOrderId()) + "）");

        BizFeeRecord row = feeRecordService.book(dto);
        log.info("住院摆药记账成功 dispenseNo={} 明细 {} {} 金额=¥{}",
                item.getDispenseNo(), item.getId(), row.getFeeNo(), row.getAmount().toPlainString());
        return row;
    }

    /**
     * 退药红冲（独立事务）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public BizFeeRecord refund(BizWardDispenseItem item, String reason) {
        String flushReason = StringUtils.hasText(reason)
                ? reason
                : ("住院摆药退药冲账（摆药单 " + item.getDispenseNo() + "，明细 " + item.getId() + "）");
        BizFeeRecord orig = feeRecordService.findBookedBySource(
                FeeSourceTypeEnum.DISPENSE.getCode(), item.getId(), null);
        if (orig == null) {
            log.warn("住院摆药退药未找到原记账行（摆药明细 {}），本次没有冲账：这笔药当初就没记上费用", item.getId());
            return null;
        }
        BizFeeRecord neg = feeRecordService.reverse(orig.getId(), flushReason);
        log.info("住院摆药退药红冲成功 dispenseNo={} 原行 {} → 负行 {} 净冲 ¥{}",
                item.getDispenseNo(), orig.getFeeNo(), neg.getFeeNo(), neg.getAmount().toPlainString());
        return neg;
    }
}
