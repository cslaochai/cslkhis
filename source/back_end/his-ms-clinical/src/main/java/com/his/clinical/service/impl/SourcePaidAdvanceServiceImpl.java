package com.his.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.ApplyStatusEnum;
import com.his.common.enums.PrescriptionPayStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.clinical.entity.*;
import com.his.clinical.mapper.*;
import com.his.clinical.service.SourcePaidAdvanceService;
import com.his.spi.dto.PendingDispensingCreateDTO;
import com.his.spi.service.DispensingFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 来源单据缴费推进实现（见接口注释的容错口径）。
 * 待发药记录的生成/取消/未退药闸门委托药房域 DispensingFacadeService，本类只推进临床单据自身状态。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SourcePaidAdvanceServiceImpl implements SourcePaidAdvanceService {

    private final BizPrescriptionMapper bizPrescriptionMapper;
    private final BizPrescriptionDetailMapper bizPrescriptionDetailMapper;
    private final BizInspectionApplyMapper bizInspectionApplyMapper;
    private final BizLaboratoryApplyMapper bizLaboratoryApplyMapper;
    private final DispensingFacadeService dispensingFacadeService;

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
        dispensingFacadeService.createPendingDispensing(toPendingDispensing(detail, prescription));
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
        // 只取消待发药记录；已经发出去的药不在这里回库存，那是药师实物验收的退药动作
        dispensingFacadeService.cancelPendingDispensingByDetailId(prescriptionDetailId, reason);
        syncPrescriptionPayStatus(detail.getPrescriptionId(), null, null);
    }

    @Override
    public void assertNoDrugPendingReturn(List<Long> prescriptionDetailIds, String scene) {
        dispensingFacadeService.assertNoDrugPendingReturn(prescriptionDetailIds, scene);
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
     * 把处方明细 + 处方主表抄成待发药快照交给药房域（饮片行 quantity 已是「每剂克数 × 剂数」，原样抄）。
     */
    private PendingDispensingCreateDTO toPendingDispensing(BizPrescriptionDetail detail, BizPrescription prescription) {
        PendingDispensingCreateDTO dto = new PendingDispensingCreateDTO();
        dto.setPrescriptionId(prescription.getId());
        dto.setPrescriptionNo(prescription.getPrescriptionNo());
        dto.setPrescriptionDetailId(detail.getId());
        dto.setPatientId(prescription.getPatientId());
        dto.setPatientNo(prescription.getPatientNo());
        dto.setPatientName(prescription.getPatientName());
        dto.setDrugId(detail.getDrugId());
        dto.setDrugCode(detail.getDrugCode());
        dto.setDrugName(detail.getDrugName());
        dto.setSpecification(detail.getSpecification());
        dto.setUnit(detail.getUnit());
        dto.setQuantity(detail.getQuantity());
        dto.setPrice(detail.getPrice());
        dto.setAmount(detail.getAmount());
        return dto;
    }
}
