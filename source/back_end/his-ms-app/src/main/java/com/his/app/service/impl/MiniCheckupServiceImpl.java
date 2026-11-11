package com.his.app.service.impl;

import com.his.app.service.MiniCheckupService;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.spi.service.CheckupFacadeService;
import com.his.spi.service.PatientFacadeService;
import com.his.spi.vo.PatientCheckupRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MiniCheckupServiceImpl implements MiniCheckupService {

    private final CheckupFacadeService checkupFacadeService;
    private final PatientFacadeService patientFacadeService;

    @Override
    public PageResult<PatientCheckupRecordVO> myRecords(Long patientId, int pageNum, int pageSize) {
        requireAccessible(patientId);
        return checkupFacadeService.myRecords(patientId, pageNum, pageSize);
    }

    @Override
    public PatientCheckupRecordVO recordDetail(Long patientId, Long recordId) {
        requireAccessible(patientId);
        PatientCheckupRecordVO vo = checkupFacadeService.recordDetail(recordId);
        if (vo == null) {
            throw new BusinessException("体检记录不存在或已删除");
        }
        if (!Objects.equals(patientId, vo.getPatientId())) {
            throw new BusinessException("无权查看该体检报告");
        }
        return vo;
    }

    private void requireAccessible(Long patientId) {
        if (patientId == null) {
            throw new BusinessException("就诊人不能为空");
        }
        if (!patientFacadeService.canAccessPatient(patientId)) {
            throw new BusinessException("无权查看该就诊人的体检报告");
        }
    }
}
