package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.charge.api.PatientGateway;
import com.his.charge.support.ChargeDeptResolver;
import com.his.charge.vo.AdmissionBriefVO;
import com.his.charge.vo.PatientBriefVO;
import com.his.common.util.TextUtil;
import com.his.patient.entity.BizAdmission;
import com.his.patient.entity.BizInpatientOrder;
import com.his.patient.entity.BizPatient;
import com.his.patient.mapper.BizAdmissionMapper;
import com.his.patient.mapper.BizInpatientOrderMapper;
import com.his.patient.mapper.BizPatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * PatientGateway 在 his-patient 侧的实现。
 */
@Service
@RequiredArgsConstructor
public class PatientGatewayImpl implements PatientGateway {

    private final BizPatientMapper bizPatientMapper;
    private final BizAdmissionMapper bizAdmissionMapper;
    private final BizInpatientOrderMapper bizInpatientOrderMapper;

    @Override
    public PatientBriefVO findPatient(Long patientId) {
        if (patientId == null) {
            return null;
        }
        return toBrief(bizPatientMapper.selectById(patientId));
    }

    @Override
    public AdmissionBriefVO findAdmission(Long admissionId) {
        if (admissionId == null) {
            return null;
        }
        return toBrief(bizAdmissionMapper.selectById(admissionId));
    }

    @Override
    public ChargeDeptResolver.DeptRef findDeptByOrderNo(String orderNo) {
        if (!TextUtil.hasText(orderNo)) {
            return null;
        }
        BizInpatientOrder order = bizInpatientOrderMapper.selectOne(new LambdaQueryWrapper<BizInpatientOrder>()
                .eq(BizInpatientOrder::getOrderNo, orderNo)
                .last("LIMIT 1"));
        return order == null ? null
                : new ChargeDeptResolver.DeptRef(order.getDeptId(), order.getDeptName());
    }

    private PatientBriefVO toBrief(BizPatient e) {
        if (e == null) {
            return null;
        }
        PatientBriefVO brief = new PatientBriefVO();
        brief.setId(e.getId());
        brief.setPatientNo(e.getPatientNo());
        brief.setPatientName(e.getPatientName());
        brief.setGender(e.getGender());
        brief.setAge(e.getAge());
        brief.setPhone(e.getPhone());
        brief.setIdCard(e.getIdCard());
        brief.setPatientType(e.getPatientType());
        brief.setMedicalInsuranceType(e.getMedicalInsuranceType());
        brief.setMedicalInsuranceNo(e.getMedicalInsuranceNo());
        return brief;
    }

    private AdmissionBriefVO toBrief(BizAdmission e) {
        if (e == null) {
            return null;
        }
        AdmissionBriefVO brief = new AdmissionBriefVO();
        brief.setAdmissionId(e.getAdmissionId());
        brief.setAdmissionNo(e.getAdmissionNo());
        brief.setPatientId(e.getPatientId());
        brief.setAdmitTime(e.getAdmitTime());
        brief.setAdmitDoctorId(e.getAdmitDoctorId());
        brief.setDiagnosis(e.getDiagnosis());
        return brief;
    }
}
