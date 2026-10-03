package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.mapper.BizMedicalRecordMapper;
import com.his.appoint.service.MedicalRecordRefGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link MedicalRecordRefGateway} 的 his-emr 侧实现（批次E/E6）。
 * <p>
 * 接口定义在调用方 his-appoint，实现在提供病历数据的 his-emr ——
 * 依赖方向 his-emr → his-appoint，单向，无环。
 * <p>
 * 只读：按ID取简要信息、按患者取最近就诊，不改任何数据（原病历一律不改）。
 */
@Component
@RequiredArgsConstructor
public class MedicalRecordRefGatewayImpl implements MedicalRecordRefGateway {

    private final BizMedicalRecordMapper medicalRecordMapper;

    @Override
    public RecordBrief getRecord(Long recordId) {
        if (recordId == null) {
            return null;
        }
        BizMedicalRecord record = medicalRecordMapper.selectById(recordId);
        return record == null ? null : toBrief(record);
    }

    @Override
    public List<RecordBrief> listRecentByPatient(Long patientId, int limit) {
        if (patientId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<BizMedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizMedicalRecord::getPatientId, patientId)
                // 没有挂号ID的病历是「还没看过病」的草稿，拿它当复诊基准判不出间隔天数
                .isNotNull(BizMedicalRecord::getRegistId)
                .orderByDesc(BizMedicalRecord::getVisitDate)
                .orderByDesc(BizMedicalRecord::getId)
                // limit 是 int 常量拼接，不走用户输入，无注入面
                .last("LIMIT " + (limit <= 0 ? 20 : limit));
        return medicalRecordMapper.selectList(wrapper).stream().map(this::toBrief).collect(Collectors.toList());
    }

    private RecordBrief toBrief(BizMedicalRecord record) {
        RecordBrief brief = new RecordBrief();
        brief.setId(record.getId());
        brief.setRecordNo(record.getRecordNo());
        brief.setPatientId(record.getPatientId());
        brief.setPatientName(record.getPatientName());
        brief.setVisitDate(record.getVisitDate());
        brief.setVisitType(record.getVisitType());
        brief.setDeptId(record.getDeptId());
        brief.setDoctorId(record.getDoctorId());
        brief.setDeptName(record.getDeptName());
        brief.setDoctorName(record.getDoctorName());
        brief.setDiagnosisName(record.getDiagnosisName());
        return brief;
    }
}
