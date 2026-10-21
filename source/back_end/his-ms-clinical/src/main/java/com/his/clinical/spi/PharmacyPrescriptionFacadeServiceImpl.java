package com.his.clinical.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.PrescriptionStatusEnum;
import com.his.common.util.TimeUtil;
import com.his.clinical.entity.BizPrescription;
import com.his.clinical.entity.BizPrescriptionDetail;
import com.his.clinical.mapper.BizPrescriptionDetailMapper;
import com.his.clinical.mapper.BizPrescriptionMapper;
import com.his.spi.service.PharmacyPrescriptionFacadeService;
import com.his.spi.vo.PrescriptionPharmacyDetailVO;
import com.his.spi.vo.PrescriptionPharmacyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 药房域处方端口实现：处方头/明细只读快照 + 整单发完/退药状态联动。
 * 叶子实现，只注入本域 Mapper，不编排 Service。
 */
@Service
@RequiredArgsConstructor
public class PharmacyPrescriptionFacadeServiceImpl implements PharmacyPrescriptionFacadeService {

    private final BizPrescriptionMapper bizPrescriptionMapper;

    private final BizPrescriptionDetailMapper bizPrescriptionDetailMapper;

    @Override
    public PrescriptionPharmacyVO findPrescription(Long prescriptionId) {
        if (prescriptionId == null) {
            return null;
        }
        BizPrescription rx = bizPrescriptionMapper.selectById(prescriptionId);
        return rx == null ? null : toHead(rx);
    }

    @Override
    public List<PrescriptionPharmacyDetailVO> listDetails(Long prescriptionId) {
        if (prescriptionId == null) {
            return Collections.emptyList();
        }
        return bizPrescriptionDetailMapper.selectList(new LambdaQueryWrapper<BizPrescriptionDetail>()
                        .eq(BizPrescriptionDetail::getPrescriptionId, prescriptionId)
                        .orderByAsc(BizPrescriptionDetail::getId)).stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markDispensed(Long prescriptionId, String pharmacistName) {
        if (prescriptionId == null) {
            return;
        }
        BizPrescription rx = bizPrescriptionMapper.selectById(prescriptionId);
        if (rx == null) {
            return;
        }
        rx.setPrescriptionStatus(PrescriptionStatusEnum.DISPENSED.getCode());
        rx.setDispenseTime(TimeUtil.nowSeconds());
        rx.setDispenseBy(pharmacistName);
        bizPrescriptionMapper.updateById(rx);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markReturned(Long prescriptionId, String operatorName, String reason) {
        if (prescriptionId == null) {
            return;
        }
        BizPrescription rx = bizPrescriptionMapper.selectById(prescriptionId);
        if (rx == null) {
            return;
        }
        rx.setPrescriptionStatus(PrescriptionStatusEnum.RETURNED.getCode());
        rx.setRefundTime(TimeUtil.nowSeconds());
        rx.setRefundBy(operatorName);
        rx.setRefundReason(reason);
        bizPrescriptionMapper.updateById(rx);
    }

    private PrescriptionPharmacyVO toHead(BizPrescription rx) {
        PrescriptionPharmacyVO vo = new PrescriptionPharmacyVO();
        vo.setId(rx.getId());
        vo.setPrescriptionNo(rx.getPrescriptionNo());
        vo.setPrescriptionStatus(rx.getPrescriptionStatus());
        vo.setDiagnosis(rx.getDiagnosis());
        vo.setGender(rx.getGender());
        vo.setAge(rx.getAge());
        vo.setDeptId(rx.getDeptId());
        vo.setDeptName(rx.getDeptName());
        vo.setDoctorId(rx.getDoctorId());
        vo.setDoctorName(rx.getDoctorName());
        vo.setAuditBy(rx.getAuditBy());
        vo.setPatientId(rx.getPatientId());
        vo.setPatientNo(rx.getPatientNo());
        vo.setPatientName(rx.getPatientName());
        vo.setPrescriptionType(rx.getPrescriptionType());
        vo.setDecoctFlag(rx.getDecoctFlag());
        vo.setDoseCount(rx.getDoseCount());
        return vo;
    }

    private PrescriptionPharmacyDetailVO toDetail(BizPrescriptionDetail d) {
        PrescriptionPharmacyDetailVO vo = new PrescriptionPharmacyDetailVO();
        vo.setId(d.getId());
        vo.setPrescriptionId(d.getPrescriptionId());
        vo.setDrugId(d.getDrugId());
        vo.setDrugCode(d.getDrugCode());
        vo.setDrugName(d.getDrugName());
        vo.setSpecification(d.getSpecification());
        vo.setDosageForm(d.getDosageForm());
        vo.setQuantity(d.getQuantity());
        vo.setSingleDosage(d.getSingleDosage());
        vo.setFrequency(d.getFrequency());
        vo.setRoute(d.getRoute());
        vo.setDuration(d.getDuration());
        return vo;
    }
}
