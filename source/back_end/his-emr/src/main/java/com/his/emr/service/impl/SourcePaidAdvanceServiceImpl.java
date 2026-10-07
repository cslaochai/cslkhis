package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.ApplyStatusEnum;
import com.his.common.enums.PrescriptionPayStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.emr.entity.*;
import com.his.emr.enums.DispensingStatusEnum;
import com.his.emr.mapper.*;
import com.his.emr.service.SourcePaidAdvanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 来源单据缴费推进实现（见接口注释的容错口径）。
 *
 * <p>三处与旧 {@code ChargeController.markSourcesPaid} 不同的地方，都是它原来的缺陷：
 * <ol>
 *   <li><b>主表只在全部明细缴清后才置已缴费</b>。旧代码一张方三味药，收到第一味药的钱就把整张方
 *       标成已缴费，发药窗口按方发药会把没收钱的药一起发出去；</li>
 *   <li><b>pay_amount 写处方合计</b>而不是最后一条明细的金额（旧代码逐条覆盖，多明细方只剩最后一行的数）；</li>
 *   <li><b>发药记录按 prescription_detail_id 判重</b>，重复推进不再叠加一张待发药单。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SourcePaidAdvanceServiceImpl implements SourcePaidAdvanceService {

    private final BizPrescriptionMapper bizPrescriptionMapper;
    private final BizPrescriptionDetailMapper bizPrescriptionDetailMapper;
    private final BizInspectionApplyMapper bizInspectionApplyMapper;
    private final BizLaboratoryApplyMapper bizLaboratoryApplyMapper;
    private final BizDrugDispensingMapper bizDrugDispensingMapper;
    private final RedisSequenceService redisSequenceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void advancePrescriptionDetail(Long prescriptionDetailId, BigDecimal paidAmount, Integer payMethod) {
        BizPrescriptionDetail detail = bizPrescriptionDetailMapper.selectById(prescriptionDetailId);
        if (detail == null) {
            throw new BusinessException("处方明细不存在，无法推进缴费（明细ID：" + prescriptionDetailId + "）");
        }
        BizPrescription prescription = bizPrescriptionMapper.selectById(detail.getPrescriptionId());
        if (prescription == null) {
            throw new BusinessException("处方不存在，无法推进缴费（处方ID：" + detail.getPrescriptionId() + "）");
        }

        detail.setPaymentStatus(PrescriptionPayStatusEnum.PAID.getCode());
        bizPrescriptionDetailMapper.updateById(detail);
        createPendingDispensing(detail, prescription);
        syncPrescriptionPayStatus(prescription.getId(), paidAmount, payMethod);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revertPrescriptionDetail(Long prescriptionDetailId, String reason) {
        BizPrescriptionDetail detail = bizPrescriptionDetailMapper.selectById(prescriptionDetailId);
        if (detail == null) {
            log.warn("[退费推进] 处方明细 {} 已不存在，跳过（退款不因单据缺失回滚）", prescriptionDetailId);
            return;
        }
        detail.setPaymentStatus(PrescriptionPayStatusEnum.REFUNDED.getCode());
        bizPrescriptionDetailMapper.updateById(detail);

        LambdaQueryWrapper<BizDrugDispensing> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDrugDispensing::getPrescriptionDetailId, prescriptionDetailId);
        for (BizDrugDispensing dispensing : bizDrugDispensingMapper.selectList(wrapper)) {
            if (dispensing.getDispensingStatus() == null || dispensing.getDispensingStatus() != DispensingStatusEnum.PENDING.getCode()) {
                // 已经发出去的药不在这里回库存：那是「退药」动作（DrugDispensingService.returnDrug），
                // 要药师实物验收并落库存流水。退费只把钱退掉，绝不静默替药师办退药。
                continue;
            }
            dispensing.setDispensingStatus(DispensingStatusEnum.CANCELLED.getCode());
            dispensing.setRemark(TextUtil.cut("退费取消：" + reason, 500));
            bizDrugDispensingMapper.updateById(dispensing);
        }
        syncPrescriptionPayStatus(detail.getPrescriptionId(), null, null);
    }

    @Override
    public void assertNoDrugPendingReturn(List<Long> prescriptionDetailIds, String scene) {
        if (prescriptionDetailIds == null || prescriptionDetailIds.isEmpty()) {
            return;
        }
        List<BizDrugDispensing> stuck = bizDrugDispensingMapper.selectDispensedByDetailIds(prescriptionDetailIds);
        if (stuck.isEmpty()) {
            return;
        }
        BizDrugDispensing first = stuck.get(0);
        String more = stuck.size() == 1 ? "" : " 等 " + stuck.size() + " 条发药记录";
        throw new BusinessException("药品「" + first.getDrugName() + "」已发药、尚未办退药，不能" + scene
                + "（发药单 " + first.getDispensingNo() + "，处方 " + first.getPrescriptionNo() + more + "，"
                + "发药人 " + first.getPharmacistName() + "）。"
                + "请先在药房发药台办「退药」（药师实物验收 + 库存回库）再退钱："
                + "退费只退钱、不会替药师把药收回架，钱先退了库存就永久对不上");
    }

    @Override
    public void advanceInspectionApply(Long applyId) {
        BizInspectionApply apply = bizInspectionApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("检查申请单不存在，无法推进缴费（申请ID：" + applyId + "）");
        }
        if (ApplyStatusEnum.CANCELLED.getCode() == NumUtil.orZero(apply.getApplyStatus())) {
            throw new BusinessException("检查申请单已取消，不能缴费（申请号：" + apply.getApplyNo() + "）");
        }
        if (ApplyStatusEnum.PAID.getCode() == NumUtil.orZero(apply.getApplyStatus())) {
            return;
        }
        apply.setApplyStatus(ApplyStatusEnum.PAID.getCode());
        bizInspectionApplyMapper.updateById(apply);
    }

    @Override
    public void revertInspectionApply(Long applyId) {
        BizInspectionApply apply = bizInspectionApplyMapper.selectById(applyId);
        if (apply == null) {
            log.warn("[退费推进] 检查申请单 {} 已不存在，跳过", applyId);
            return;
        }
        if (ApplyStatusEnum.PAID.getCode() != NumUtil.orZero(apply.getApplyStatus())) {
            return;
        }
        apply.setApplyStatus(ApplyStatusEnum.SUBMITTED.getCode());
        bizInspectionApplyMapper.updateById(apply);
    }

    @Override
    public void advanceLaboratoryApply(Long applyId) {
        BizLaboratoryApply apply = bizLaboratoryApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("检验申请单不存在，无法推进缴费（申请ID：" + applyId + "）");
        }
        if (ApplyStatusEnum.CANCELLED.getCode() == NumUtil.orZero(apply.getApplyStatus())) {
            throw new BusinessException("检验申请单已取消，不能缴费（申请号：" + apply.getApplyNo() + "）");
        }
        if (ApplyStatusEnum.PAID.getCode() == NumUtil.orZero(apply.getApplyStatus())) {
            return;
        }
        apply.setApplyStatus(ApplyStatusEnum.PAID.getCode());
        bizLaboratoryApplyMapper.updateById(apply);
    }

    @Override
    public void revertLaboratoryApply(Long applyId) {
        BizLaboratoryApply apply = bizLaboratoryApplyMapper.selectById(applyId);
        if (apply == null) {
            log.warn("[退费推进] 检验申请单 {} 已不存在，跳过", applyId);
            return;
        }
        if (ApplyStatusEnum.PAID.getCode() != NumUtil.orZero(apply.getApplyStatus())) {
            return;
        }
        apply.setApplyStatus(ApplyStatusEnum.SUBMITTED.getCode());
        bizLaboratoryApplyMapper.updateById(apply);
    }

    /**
     * 处方主表缴费状态由明细现算：全部已缴 → 已缴，全部已退 → 已退，否则保持不动。
     *
     * <p>不写成"来一条明细就刷一次主表为已缴"：主表状态是发药窗口能否发药的依据，
     * 刷早了等于允许欠费发药。
     */
    private void syncPrescriptionPayStatus(Long prescriptionId, BigDecimal paidAmount, Integer payMethod) {
        BizPrescription prescription = bizPrescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            return;
        }
        LambdaQueryWrapper<BizPrescriptionDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizPrescriptionDetail::getPrescriptionId, prescriptionId);
        List<BizPrescriptionDetail> details = bizPrescriptionDetailMapper.selectList(wrapper);
        if (details.isEmpty()) {
            return;
        }
        int paid = PrescriptionPayStatusEnum.PAID.getCode();
        int refunded = PrescriptionPayStatusEnum.REFUNDED.getCode();
        boolean allPaid = details.stream().allMatch(d -> d.getPaymentStatus() != null && d.getPaymentStatus() == paid);
        boolean allRefunded = details.stream().allMatch(d -> d.getPaymentStatus() != null && d.getPaymentStatus() == refunded);
        if (allPaid) {
            if (prescription.getPaymentStatus() == null || prescription.getPaymentStatus() != paid) {
                prescription.setPayTime(LocalDateTime.now());
            }
            prescription.setPaymentStatus(paid);
            // 实缴写处方合计：一张方三味药，只写最后一味的金额，收费小票和处方对不上
            BigDecimal total = BigDecimal.ZERO;
            for (BizPrescriptionDetail d : details) {
                total = total.add(d.getAmount() == null ? BigDecimal.ZERO : d.getAmount());
            }
            prescription.setPayAmount(total);
            prescription.setPayMethod(payMethod);
        } else if (allRefunded) {
            prescription.setPaymentStatus(refunded);
        } else {
            return;
        }
        bizPrescriptionMapper.updateById(prescription);
        if (paidAmount != null) {
            log.debug("[缴费推进] 处方 {} 本次账单内实缴 ¥{}", prescription.getPrescriptionNo(), paidAmount.toPlainString());
        }
    }

    /**
     * 为一条处方明细生成待发药记录（同一条明细已有记录就不再叠加）
     */
    private void createPendingDispensing(BizPrescriptionDetail detail, BizPrescription prescription) {
        LambdaQueryWrapper<BizDrugDispensing> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDrugDispensing::getPrescriptionDetailId, detail.getId());
        if (bizDrugDispensingMapper.selectCount(wrapper) > 0) {
            return;
        }
        BizDrugDispensing dispensing = new BizDrugDispensing();
        dispensing.setDispensingNo(redisSequenceService.generateDispensingNo());
        dispensing.setPrescriptionId(prescription.getId());
        dispensing.setPrescriptionNo(prescription.getPrescriptionNo());
        dispensing.setPrescriptionDetailId(detail.getId());
        dispensing.setPatientId(prescription.getPatientId());
        dispensing.setPatientNo(prescription.getPatientNo());
        dispensing.setPatientName(prescription.getPatientName());
        dispensing.setDrugId(detail.getDrugId());
        dispensing.setDrugCode(detail.getDrugCode());
        dispensing.setDrugName(detail.getDrugName());
        dispensing.setSpecification(detail.getSpecification());
        dispensing.setUnit(detail.getUnit());
        // 饮片行的 quantity 已是「每剂克数 × 剂数」（sql/139），原样抄，发药侧再按库存档案单位换算
        dispensing.setQuantity(detail.getQuantity());
        dispensing.setPrice(detail.getPrice());
        dispensing.setAmount(detail.getAmount());
        dispensing.setDispensingStatus(DispensingStatusEnum.PENDING.getCode());
        bizDrugDispensingMapper.insert(dispensing);
    }
}
