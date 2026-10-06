package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.charge.vo.AdmissionBrief;
import com.his.charge.support.ChargeDeptResolver;
import com.his.charge.vo.PatientBrief;
import com.his.charge.service.PatientGateway;
import com.his.patient.entity.BizAdmission;
import com.his.patient.entity.BizInpatientOrder;
import com.his.patient.entity.BizPatient;
import com.his.patient.mapper.BizAdmissionMapper;
import com.his.patient.mapper.BizInpatientOrderMapper;
import com.his.patient.mapper.BizPatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {@link PatientGateway} 在 his-patient 侧的实现。
 *
 * <p>接口由收费域 his-charge 声明、这里负责实现，是为了让依赖方向单一：
 * 患者域依赖收费域（记账能力在 charge），收费域反过来不依赖患者域。
 * 若把接口放在本模块，charge 就得依赖 patient，而 patient 又要依赖 charge 记账，
 * Maven reactor 判定成环，直接拒绝构建。
 *
 * <p>实体 → 摘要的映射只在这里发生：charge 拿到的是 {@link PatientBrief} /
 * {@link AdmissionBrief}，看不到 {@link BizPatient} / {@link BizAdmission}，
 * 对方实体加字段、改字段名都不会传导成收费域的编译错误。
 *
 * <p>走 Mapper 而非 Service：这两个方法只是单行主键查询，
 * 走 {@code PatientService} 会把整个住院域 service 拖进 charge 的编译依赖里。
 * 本模块内部怎么取数是本模块自己的事，端口只对外面暴露只读语义。
 */
@Service
@RequiredArgsConstructor
public class PatientGatewayImpl implements PatientGateway {

    private final BizPatientMapper patientMapper;
    private final BizAdmissionMapper admissionMapper;
    private final BizInpatientOrderMapper inpatientOrderMapper;

    @Override
    public PatientBrief findPatient(Long patientId) {
        if (patientId == null) {
            return null;
        }
        return toBrief(patientMapper.selectById(patientId));
    }

    @Override
    public AdmissionBrief findAdmission(Long admissionId) {
        if (admissionId == null) {
            return null;
        }
        return toBrief(admissionMapper.selectById(admissionId));
    }

    @Override
    public ChargeDeptResolver.DeptRef findDeptByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            return null;
        }
        BizInpatientOrder order = inpatientOrderMapper.selectOne(new LambdaQueryWrapper<BizInpatientOrder>()
                .eq(BizInpatientOrder::getOrderNo, orderNo)
                .last("LIMIT 1"));
        return order == null ? null
                : new ChargeDeptResolver.DeptRef(order.getDeptId(), order.getDeptName());
    }

    private PatientBrief toBrief(BizPatient e) {
        if (e == null) {
            return null;
        }
        PatientBrief brief = new PatientBrief();
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

    private AdmissionBrief toBrief(BizAdmission e) {
        if (e == null) {
            return null;
        }
        AdmissionBrief brief = new AdmissionBrief();
        brief.setAdmissionId(e.getAdmissionId());
        brief.setAdmissionNo(e.getAdmissionNo());
        brief.setPatientId(e.getPatientId());
        brief.setAdmitTime(e.getAdmitTime());
        brief.setAdmitDoctorId(e.getAdmitDoctorId());
        brief.setDiagnosis(e.getDiagnosis());
        return brief;
    }
}
