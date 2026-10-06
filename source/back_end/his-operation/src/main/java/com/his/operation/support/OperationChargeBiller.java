package com.his.operation.support;

import com.his.common.enums.EncounterTypeEnum;
import com.his.common.enums.FeeSourceTypeEnum;
import com.his.common.enums.PaymentItemTypeEnum;
import com.his.fee.dto.FeeBookDTO;
import com.his.fee.entity.BizFeeRecord;
import com.his.fee.support.FeeCatalogResolver;
import com.his.operation.support.AnesthesiaCalcs;
import com.his.operation.entity.BizAnesthesiaPacu;
import com.his.operation.entity.BizAnesthesiaRecord;
import com.his.operation.entity.BizOperationChargeItem;
import com.his.operation.enums.AirwayDeviceEnum;
import com.his.operation.enums.AnesthesiaChargeStatusEnum;
import com.his.operation.enums.ChargeSourceEnum;
import com.his.operation.enums.OperationAnesthesiaMethodEnum;
import com.his.operation.mapper.BizOperationChargeItemMapper;
import com.his.operation.vo.OperationChargeSummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 手术麻醉计费：把"麻醉/复苏做了什么"翻译成"这几项该收多少钱"，并把每一项的落地结果留痕。
 *
 * <p><b>为什么不直接调一次收费服务就完事</b>：调用成功但没留痕，等于"我说我计费了"。
 * 手术麻醉计费明细每一行对应一条费用记账流水记账行，
 * 缺任何一项都能立刻回答"这笔钱有没有真的进账、没进账的原因是什么"。
 *
 * <p>三条硬口径：
 * <ol>
 *   <li><b>取不到单价就不计费</b>：项目编码在价目表里查不到（或已停用）→
 *       记账行 status=2、写 fail_reason，**绝不塞一个"看起来合理的价格"**。</li>
 *   <li><b>算不出的数量也报错而不是取 0</b>：监护费按小时收，
 *       如果连麻醉起止时间都没有，"这台手术监测了多久"是答不上来的——
 *       0 元明细会让日清单出现"免费监测"，那是一次虚假陈述。</li>
 *   <li><b>幂等</b>：同一 source + item_code 若已有记账行，直接跳过
 *       （重复点击"/charge"不会钉出第二笔费用）。</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationChargeBiller {

    /**
     * 金额统一两位小数（元）
     */
    private static final int AMOUNT_SCALE = 2;

    /**
     * 麻醉监护费项目编码（按小时收，与麻醉方式无关）
     */
    private static final String ITEM_MONITOR = "AN006";
    private static final String ITEM_INTUBATION = "AN007";
    private static final String ITEM_PACU = "AN008";

    private final BizOperationChargeItemMapper chargeItemMapper;
    private final OperationChargeInvoker invoker;

    /**
     * 分钟差（两端都要有值；顺序反了或为负 → null，不替业务圆回来）
     */
    private static Long minutesBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return null;
        }
        long m = Duration.between(from, to).toMinutes();
        return m < 0 ? null : m;
    }

    private static BigDecimal toDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(value));
    }

    // 内部：单项计费（幂等 + 落痕）

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 按麻醉记录单计费（麻醉费 + 麻醉监护 + 气管插管）。
     */
    @Transactional(rollbackFor = Exception.class)
    public OperationChargeSummaryVO billRecord(BizAnesthesiaRecord record, String patientNo) {
        OperationChargeSummaryVO summary = new OperationChargeSummaryVO();
        if (record == null || record.getId() == null) {
            summary.getMessages().add("麻醉记录单不存在，无法计费");
            return summary;
        }

        String basis = "麻醉记录 " + record.getRecordNo();

        // ① 麻醉费：方式 → 项目。方式没登记就不能收费（不知道做的是什么麻醉）
        OperationAnesthesiaMethodEnum method = OperationAnesthesiaMethodEnum.fromCode(record.getAnesthesiaType());
        if (method == null) {
            fail(summary, record, null, "未登记麻醉方式，无法确定麻醉费项目", basis, record.getRecordNo());
        } else {
            billOne(summary, record.getAdmissionId(), record.getPatientId(), patientNo, record.getPatientName(),
                    record.getApplyId(), record.getApplyNo(), ChargeSourceEnum.RECORD.getCode(), record.getId(), record.getRecordNo(),
                    method.getChargeItemCode(), BigDecimal.ONE, basis + " 麻醉费");
        }

        // ② 麻醉监护（按小时）
        Long minutes = minutesBetween(record.getAnesthesiaStartTime(), record.getAnesthesiaEndTime());
        if (minutes == null) {
            minutes = minutesBetween(record.getOperationStartTime(), record.getOperationEndTime());
        }
        if (minutes == null) {
            fail(summary, record, ITEM_MONITOR, "无麻醉/手术起止时间，算不出监护时长", basis, record.getRecordNo());
        } else {
            billOne(summary, record.getAdmissionId(), record.getPatientId(), patientNo, record.getPatientName(),
                    record.getApplyId(), record.getApplyNo(), ChargeSourceEnum.RECORD.getCode(), record.getId(), record.getRecordNo(),
                    ITEM_MONITOR, AnesthesiaCalcs.billHours(minutes),
                    basis + " 麻醉监护 " + AnesthesiaCalcs.durationText(minutes));
        }

        // ③ 气管插管（只有 flag 明确是插管才收；不确定（null）不收也不报错）
        if (Integer.valueOf(AirwayDeviceEnum.ENDOTRACHEAL_TUBE.getCode()).equals(record.getAirwayDevice())) {
            billOne(summary, record.getAdmissionId(), record.getPatientId(), patientNo, record.getPatientName(),
                    record.getApplyId(), record.getApplyNo(), ChargeSourceEnum.RECORD.getCode(), record.getId(), record.getRecordNo(),
                    ITEM_INTUBATION, BigDecimal.ONE, basis + " 气管插管");
        }
        return summary;
    }

    /**
     * 按 PACU 停留计费（麻醉后监测治疗，按小时）。
     */
    @Transactional(rollbackFor = Exception.class)
    public OperationChargeSummaryVO billPacu(BizAnesthesiaPacu pacu) {
        OperationChargeSummaryVO summary = new OperationChargeSummaryVO();
        if (pacu == null || pacu.getId() == null) {
            summary.getMessages().add("PACU 记录不存在，无法计费");
            return summary;
        }
        Long minutes = minutesBetween(pacu.getEnterTime(), pacu.getLeaveTime());
        if (minutes == null) {
            failPacu(summary, pacu, "PACU 尚未登记入室/出室时间，算不出复苏时长");
            return summary;
        }
        billOne(summary, pacu.getAdmissionId(), pacu.getPatientId(), null, pacu.getPatientName(),
                pacu.getApplyId(), null, ChargeSourceEnum.PACU.getCode(), pacu.getId(), pacu.getPacuNo(),
                ITEM_PACU, AnesthesiaCalcs.billHours(minutes),
                "PACU 复苏 " + pacu.getPacuNo() + " " + AnesthesiaCalcs.durationText(minutes));
        return summary;
    }

    private void billOne(OperationChargeSummaryVO summary, Long admissionId, Long patientId, String patientNo,
                         String patientName, Long applyId, String applyNo,
                         int sourceType, Long sourceId, String sourceNo,
                         String itemCode, BigDecimal quantity, String remark) {
        summary.setTotalItems(summary.getTotalItems() + 1);

        BizOperationChargeItem row = upsert(admissionId, patientId, patientNo, patientName,
                applyId, applyNo, sourceType, sourceId, sourceNo, itemCode);
        if (row == null) {
            // 已经成功计过费的同一项 → 幂等跳过，不再向收费明细里钉第二笔
            summary.getMessages().add(itemCode + " 此前已计费，本次跳过（同一来源同一项目只收一次）");
            return;
        }

        Map<String, Object> price = chargeItemMapper.selectTreatmentItem(itemCode);
        if (price == null || price.isEmpty()) {
            markFail(summary, row, "价目表中不存在可用项目 " + itemCode + "（未取到单价，不计费）", itemCode);
            return;
        }
        BigDecimal unitPrice = toDecimal(price.get("price"));
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            markFail(summary, row, "项目 " + itemCode + " 单价为 " + unitPrice + "，按'金额不明即不计费'不落账", itemCode);
            return;
        }
        String unit = price.get("unit") == null ? null : String.valueOf(price.get("unit"));
        String name = price.get("item_name") == null ? null : String.valueOf(price.get("item_name"));
        String spec = price.get("spec") == null ? null : String.valueOf(price.get("spec"));

        BigDecimal amount = unitPrice.multiply(quantity).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);

        row.setItemName(name);
        row.setSpec(spec);
        row.setUnit(StringUtils.hasText(unit) ? unit : "次");
        row.setQuantity(quantity);
        row.setPrice(unitPrice);
        row.setAmount(amount);
        row.setRemark(remark);

        if (admissionId == null) {
            markFail(summary, row, "缺少入院ID，这笔费用无法归属这次住院，本次不计费", itemCode);
            return;
        }
        // 没有手术锚点的麻醉费用 = 一笔说不清来源的账，宁可先不记
        if (!StringUtils.hasText(applyNo) && applyId == null) {
            markFail(summary, row, "缺少手术申请单，本次不计费", itemCode);
            return;
        }
        if (patientId == null || !StringUtils.hasText(patientName)) {
            markFail(summary, row, "缺少患者快照，这笔费用落不到人，本次不计费", itemCode);
            return;
        }

        // 一个计费项目 = 一条记账行，落库即「1-待结算」，等出院结算时被锁进账单
        FeeBookDTO fee = new FeeBookDTO();
        fee.setPatientId(patientId);
        fee.setPatientNo(patientNo);
        fee.setPatientName(patientName);
        fee.setEncounterType(EncounterTypeEnum.INPATIENT.getCode());
        fee.setEncounterId(admissionId);
        // 科室留空（已知缺口）：准确的归属应是手术申请单上的手术科室，而麻醉记录与 PACU 单上
        // 都没有这个快照，兜底成「麻醉科」或患者所在病区都会让科室收入表凭空多出一块。
        // 补法在开单侧把科室带进来；日结会把它单列成「无科室归属」差异项。
        // 项目类型一律 7-治疗：麻醉/复苏/监护在费用类型字典里没有专属类目，
        // 归「治疗」符合字典口径，也不会像自己发明一个 9 那样让收入报表出现无定义类目。
        fee.setItemType(PaymentItemTypeEnum.TREATMENT.getCode());
        fee.setItemCode(itemCode);
        fee.setItemName(row.getItemName());
        fee.setSpecification(row.getSpec());
        fee.setUnit(row.getUnit());
        fee.setPrice(row.getPrice());
        fee.setQuantity(quantity);
        fee.setSourceType(FeeSourceTypeEnum.OPERATION.getCode());
        // ★ 幂等锚点 = 本次计费的项目台账行（一行 = 一个可计费项目）。
        //   若直接用文档ID，麻醉记录与 PACU 记录会共用同一个「来源类型+来源ID」空间而互相误判。
        fee.setSourceId(row.getId());
        // ★ 四核对锚点：这台手术。麻醉费不属于任何一条医嘱，锚到那台手术才答得出「这是哪台手术的钱」；
        //   来源编号不能留空（该列 NOT NULL 且无默认）。
        fee.setSourceNo(StringUtils.hasText(applyNo) ? applyNo : ("APPLY-" + applyId));
        fee.setCatalogType(FeeCatalogResolver.byItemType(fee.getItemType()));
        fee.setRemark(StringUtils.hasText(remark) ? remark
                : ("手术麻醉记账（项目 " + itemCode + "，手术申请 " + fee.getSourceNo() + "）"));

        BizFeeRecord booked;
        try {
            booked = invoker.book(fee);
        } catch (Exception e) {
            markFail(summary, row, "记账异常：" + e.getMessage(), itemCode);
            return;
        }

        row.setChargeStatus(AnesthesiaChargeStatusEnum.DONE.getCode());
        // fee_record_id / fee_no：L1 记账行的指针（sql/141 之前是 charge_id + charge_detail_id 两列同值）；
        // 补计/红冲要按它找回那一行，不留旧行号的悬空值。
        row.setFeeRecordId(booked.getId());
        row.setFeeNo(booked.getFeeNo());
        row.setFailReason(null);
        chargeItemMapper.updateById(row);

        summary.setSuccessItems(summary.getSuccessItems() + 1);
        summary.setAmount(summary.getAmount().add(nz(booked.getAmount())));
        summary.setFeeNo(booked.getFeeNo());
        summary.getMessages().add(String.format("%s「%s」× %s = %s 元 → %s",
                itemCode, name, quantity.stripTrailingZeros().toPlainString(),
                amount.stripTrailingZeros().toPlainString(), booked.getFeeNo()));
    }

    /**
     * 取/建幂等记账行。
     *
     * <p>已标记为"已计费"的行<b>直接不再处理</b>（返回 null 让调用方跳过）——
     * 这就是"点两次收费只收一次"的实现处，比在 Controller 上加一个开关可靠。
     *
     * @return 可继续处理的记账行；返回 {@code null} 表示该项此前已计费成功，本次跳过
     */
    private BizOperationChargeItem upsert(Long admissionId, Long patientId, String patientNo, String patientName,
                                          Long applyId, String applyNo, int sourceType, Long sourceId,
                                          String sourceNo, String itemCode) {
        List<BizOperationChargeItem> existing = chargeItemMapper.selectBySource(sourceType, sourceId).stream()
                .filter(r -> itemCode.equals(r.getItemCode()))
                .toList();
        if (!existing.isEmpty()) {
            BizOperationChargeItem row = existing.get(0);
            if (Integer.valueOf(AnesthesiaChargeStatusEnum.DONE.getCode()).equals(row.getChargeStatus())) {
                return null;
            }
            return row;
        }
        BizOperationChargeItem row = new BizOperationChargeItem();
        row.setApplyId(applyId);
        row.setApplyNo(applyNo);
        row.setAdmissionId(admissionId);
        row.setPatientId(patientId);
        row.setPatientNo(patientNo);
        row.setPatientName(patientName);
        row.setSourceType(sourceType);
        row.setSourceId(sourceId);
        row.setSourceNo(sourceNo);
        row.setItemCode(itemCode);
        row.setItemType(7);
        row.setChargeStatus(AnesthesiaChargeStatusEnum.PENDING.getCode());
        chargeItemMapper.insert(row);
        return row;
    }

    // 工具

    private void markFail(OperationChargeSummaryVO summary, BizOperationChargeItem row, String reason, String itemCode) {
        summary.setFailedItems(summary.getFailedItems() + 1);
        summary.getMessages().add(itemCode + " 计费失败：" + reason);
        if (row != null) {
            row.setChargeStatus(AnesthesiaChargeStatusEnum.FAILED.getCode());
            // ★ 截到列宽：失败原因里带着原始 SQL 异常文本，超长会把这次 update 打成
            //   Data too long → "记账失败"升级成 500，反而看不到失败原因了
            row.setFailReason(AnesthesiaCalcs.clipReason(reason));
            chargeItemMapper.updateById(row);
        }
        log.warn("手术麻醉计费失败：{}", reason);
    }

    /**
     * 连"该收哪个项目"都定不下来的失败：这种项目<b>没有记账行可落</b>
     * （没有 item_code 就没法命中幂等唯一键），所以只在汇总里留一条失败说明。
     */
    private void fail(OperationChargeSummaryVO summary, BizAnesthesiaRecord record, String itemCode,
                      String reason, String basis, String recordNo) {
        summary.setTotalItems(summary.getTotalItems() + 1);
        summary.setFailedItems(summary.getFailedItems() + 1);
        summary.getMessages().add((itemCode == null ? "麻醉费" : itemCode) + " 计费失败：" + reason);
        log.warn("{}：{}", basis, reason);
    }

    private void failPacu(OperationChargeSummaryVO summary, BizAnesthesiaPacu pacu, String reason) {
        summary.setTotalItems(summary.getTotalItems() + 1);
        summary.setFailedItems(summary.getFailedItems() + 1);
        summary.getMessages().add("AN008 计费失败：" + reason);
        log.warn("PACU {}：{}", pacu.getPacuNo(), reason);
    }
}
