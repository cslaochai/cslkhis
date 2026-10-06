package com.his.charge.service.impl;


import com.his.charge.api.MedicalTechGateway;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.entity.BizPaymentTxn;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.mapper.BizPaymentTxnMapper;
import com.his.charge.api.EmrGateway;
import com.his.charge.service.FeeRecordService;
import com.his.charge.service.SourceAdvanceService;
import com.his.common.enums.FeeSourceTypeEnum;
import com.his.common.enums.FeeStatusEnum;
import com.his.common.enums.PayDirectionEnum;
import com.his.common.enums.PayTxnStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 来源单据推进实现：把记账行的 {@code source_type + source_id} 翻译成"该动哪张临床单据"。
 *
 * <p>分派只看 source_type（记账时由开单方写死），不再像旧代码那样用 item_type 猜：
 * 同一类项目可以从不同单据来（手工补记的药品费用就没有处方锚点），用类型猜会去改一张没交钱的单子。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SourceAdvanceServiceImpl implements SourceAdvanceService {

    private final FeeRecordService feeRecordService;
    private final BizPaymentTxnMapper paymentTxnMapper;
    private final EmrGateway emrGateway;
    private final MedicalTechGateway medicalTechGateway;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void advanceByBill(BizSettlementBill bill) {
        if (bill == null || bill.getId() == null) {
            return;
        }
        Integer payMethod = primaryPayMethod(bill.getId());
        List<BizFeeRecord> rows = feeRecordService.listByBill(bill.getId());
        int advanced = 0;
        for (BizFeeRecord row : rows) {
            if (isNegative(row)) {
                continue;
            }
            FeeSourceTypeEnum source = FeeSourceTypeEnum.fromCode(row.getSourceType());
            if (source == null || row.getSourceId() == null) {
                continue;
            }
            switch (source) {
                case PRESCRIPTION -> {
                    emrGateway.advancePrescriptionDetail(row.getSourceId(), row.getAmount(), payMethod);
                    advanced++;
                }
                case EXAM_APPLY -> {
                    emrGateway.advanceInspectionApply(row.getSourceId());
                    // 执行记录由医技域按申请单幂等创建：收费只负责"钱到了"，不替医技决定怎么开工
                    medicalTechGateway.ensureInspectionRecordFromApply(row.getSourceId());
                    advanced++;
                }
                case LAB_APPLY -> {
                    emrGateway.advanceLaboratoryApply(row.getSourceId());
                    medicalTechGateway.ensureLaboratoryRecordFromApply(row.getSourceId());
                    advanced++;
                }
                default -> {
                    // 挂号 / 治疗 / 耗材 / 住院医嘱 …… 这些单据自己走各自的执行链，不由缴费推进
                }
            }
        }
        if (advanced > 0) {
            log.info("[缴费推进] 账单 {} 推进来源单据 {} 张，支付渠道={}", bill.getBillNo(), advanced, payMethod);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revertByBill(Long billId, String reason) {
        if (billId == null) {
            return;
        }
        revertByRows(feeRecordService.listByBill(billId), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revertByRows(List<BizFeeRecord> rows, String reason) {
        int reverted = 0;
        for (BizFeeRecord row : rows) {
            // 只看"原行被整行冲完"的：部分冲减后这条费用还在发生，撤了等于把没收钱的药也停了
            if (isNegative(row) || !FeeStatusEnum.REVERSED.getCode().equals(row.getFeeStatus())) {
                continue;
            }
            FeeSourceTypeEnum source = FeeSourceTypeEnum.fromCode(row.getSourceType());
            if (source == null || row.getSourceId() == null) {
                continue;
            }
            switch (source) {
                case PRESCRIPTION -> {
                    emrGateway.revertPrescriptionDetail(row.getSourceId(), reason);
                    reverted++;
                }
                case EXAM_APPLY -> {
                    // 先让医技把还没开始的执行记录置取消，再把申请单退回未缴费；
                    // 已经做过的检查不靠退费抹平（医技侧自己判断能不能取消）
                    medicalTechGateway.cancelInspectionByApplyId(row.getSourceId(), reason);
                    emrGateway.revertInspectionApply(row.getSourceId());
                    reverted++;
                }
                case LAB_APPLY -> {
                    medicalTechGateway.cancelLaboratoryByApplyId(row.getSourceId(), reason);
                    emrGateway.revertLaboratoryApply(row.getSourceId());
                    reverted++;
                }
                default -> {
                }
            }
        }
        if (reverted > 0) {
            log.info("[退费撤销] 退回来源单据 {} 张，原因：{}", reverted, reason);
        }
    }

    /**
     * 红冲负行不推进（它冲的是原行的应收，来源单据原行已经处理过）
     */
    private boolean isNegative(BizFeeRecord row) {
        BigDecimal amount = row.getAmount();
        return amount == null || amount.signum() <= 0;
    }

    @Override
    public void assertDrugReturnedForRefund(List<BizFeeRecord> rows, String scene) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        List<Long> detailIds = new ArrayList<>();
        for (BizFeeRecord row : rows) {
            // 负行跳过：它是红冲记录，对应的处方锚点与原行同一个，问一遍就够
            if (isNegative(row) || row.getSourceId() == null) {
                continue;
            }
            if (FeeSourceTypeEnum.fromCode(row.getSourceType()) != FeeSourceTypeEnum.PRESCRIPTION) {
                continue;
            }
            detailIds.add(row.getSourceId());
        }
        emrGateway.assertNoDrugPendingReturn(detailIds, scene);
    }

    /**
     * 本账单的主支付渠道：成功收款里金额最大的一笔。
     *
     * <p>四层之后一笔账单可以是「现金 + 个账 + 余额」三笔流水，而处方上只有一个 pay_method 列。
     * 取最大的一笔比"取最后一笔"更贴近患者实际掏钱方式，也不会因为流水插入顺序而漂。
     */
    private Integer primaryPayMethod(Long billId) {
        Map<Integer, BigDecimal> byMethod = new HashMap<>();
        for (BizPaymentTxn txn : paymentTxnMapper.selectByBill(billId)) {
            if (!PayDirectionEnum.CHARGE.getCode().equals(txn.getDirection())
                    || !PayTxnStatusEnum.SUCCESS.getCode().equals(txn.getTxnStatus())
                    || txn.getPayMethod() == null) {
                continue;
            }
            byMethod.merge(txn.getPayMethod(), txn.getAmount() == null ? BigDecimal.ZERO : txn.getAmount(), BigDecimal::add);
        }
        Integer best = null;
        BigDecimal bestAmount = null;
        for (Map.Entry<Integer, BigDecimal> e : byMethod.entrySet()) {
            if (bestAmount == null || e.getValue().compareTo(bestAmount) > 0) {
                best = e.getKey();
                bestAmount = e.getValue();
            }
        }
        return best;
    }
}
