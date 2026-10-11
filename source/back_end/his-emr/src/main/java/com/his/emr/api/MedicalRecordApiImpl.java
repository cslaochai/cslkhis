package com.his.emr.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.system.api.service.MedicalRecordApi;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.mapper.BizMedicalRecordMapper;
import com.his.system.api.vo.RecordBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MedicalRecordRefGateway 的 his-emr 侧实现（批次E/E6）。
 */
@Component
@RequiredArgsConstructor
public class MedicalRecordApiImpl implements MedicalRecordApi {

    private final BizMedicalRecordMapper bizMedicalRecordMapper;

    @Override
    public RecordBriefVO getRecord(Long recordId) {
        if (recordId == null) {
            return null;
        }
        BizMedicalRecord record = bizMedicalRecordMapper.selectById(recordId);
        return record == null ? null : toBrief(record);
    }

    @Override
    public List<RecordBriefVO> listRecentByPatient(Long patientId, int limit) {
        if (patientId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<BizMedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizMedicalRecord::getPatientId, patientId)
                .isNotNull(BizMedicalRecord::getRegistId)
                .orderByDesc(BizMedicalRecord::getVisitDate)
                .orderByDesc(BizMedicalRecord::getId)
                .last("LIMIT " + (limit <= 0 ? 20 : limit));
        return bizMedicalRecordMapper.selectList(wrapper).stream().map(this::toBrief).collect(Collectors.toList());
    }

    private RecordBriefVO toBrief(BizMedicalRecord record) {
        RecordBriefVO brief = new RecordBriefVO();
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
