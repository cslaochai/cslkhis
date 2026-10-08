package com.his.pharmacy.support;

import com.his.charge.dto.FeeBookDTO;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.service.FeeRecordService;
import com.his.charge.support.FeeCatalogResolver;
import com.his.common.enums.EncounterTypeEnum;
import com.his.common.enums.FeeSourceTypeEnum;
import com.his.common.enums.PaymentItemTypeEnum;
import com.his.common.util.TextUtil;
import com.his.pharmacy.entity.BizWardDispenseItem;
import com.his.pharmacy.mapper.BizWardDispenseItemMapper;
import com.his.pharmacy.vo.WardDispenseOrderDeptVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 住院摆药记账 —— 一条摆药明细 = 一笔记账行；退药 = 把那一行红冲。
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
        if (item.getPatientId() == null || !TextUtil.hasText(item.getPatientName())) {
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
        WardDispenseOrderDeptVO dept = TextUtil.hasText(item.getOrderNo())
                ? bizWardDispenseItemMapper.selectOrderDept(item.getOrderNo()) : null;
        if (dept != null) {
            dto.setDeptId(dept.getDeptId());
            dto.setDeptName(dept.getDeptName());
        }
        // 摆药只会是药品医嘱
        Integer itemType = PaymentItemTypeEnum.WESTERN_MEDICINE.getCode();
        dto.setItemType(itemType);
        dto.setItemCode(TextUtil.hasText(item.getItemCode()) ? item.getItemCode() : item.getOrderNo());
        dto.setItemName(item.getItemName());
        dto.setSpecification(item.getSpec());
        dto.setUnit(item.getUnit());
        dto.setPrice(price);
        dto.setQuantity(quantity);
        dto.setSourceType(FeeSourceTypeEnum.DISPENSE.getCode());
        dto.setSourceId(item.getId());
        dto.setSourceNo(TextUtil.hasText(item.getOrderNo())
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
        String flushReason = TextUtil.hasText(reason)
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
