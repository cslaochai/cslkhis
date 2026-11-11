package com.his.app.service.impl;

import com.his.app.dto.MiniExamBookDTO;
import com.his.app.dto.MiniExamCancelDTO;
import com.his.app.service.MiniExamService;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.spi.service.ExamAppointFacadeService;
import com.his.spi.service.PatientFacadeService;
import com.his.spi.vo.PatientExamApptVO;
import com.his.spi.vo.PatientExamApplyVO;
import com.his.spi.vo.PatientExamSlotVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MiniExamServiceImpl implements MiniExamService {

    private final ExamAppointFacadeService examAppointFacadeService;
    private final PatientFacadeService patientFacadeService;

    @Override
    public PageResult<PatientExamApplyVO> pendingApplies(Long patientId, int pageNum, int pageSize) {
        requireAccessible(patientId);
        return examAppointFacadeService.pendingApplies(patientId, pageNum, pageSize);
    }

    @Override
    public List<PatientExamSlotVO> recommendSlots(Long patientId, Long applyId, String examDate, String afterTime) {
        requireAccessible(patientId);
        if (applyId == null) {
            throw new BusinessException("检查申请单不能为空");
        }
        return examAppointFacadeService.recommendSlots(patientId, applyId, parseDate(examDate),
                TextUtil.trimToNull(afterTime));
    }

    @Override
    public PatientExamApptVO book(MiniExamBookDTO dto) {
        requireAccessible(dto.getPatientId());
        return examAppointFacadeService.book(dto.getPatientId(), dto.getApplyId(), dto.getDeviceId(),
                dto.getExamDate(), dto.getStartTime());
    }

    @Override
    public void cancel(MiniExamCancelDTO dto) {
        requireAccessible(dto.getPatientId());
        examAppointFacadeService.cancel(dto.getPatientId(), dto.getApptId(), dto.getReason());
    }

    @Override
    public PageResult<PatientExamApptVO> myAppointments(Long patientId, int pageNum, int pageSize) {
        requireAccessible(patientId);
        return examAppointFacadeService.myAppointments(patientId, pageNum, pageSize);
    }

    /**
     * 就诊人归属校验：小程序可代家人操作，但只能是当前账号绑定过的就诊人。
     */
    private void requireAccessible(Long patientId) {
        if (patientId == null) {
            throw new BusinessException("就诊人不能为空");
        }
        if (!patientFacadeService.canAccessPatient(patientId)) {
            throw new BusinessException("无权操作该就诊人的检查预约");
        }
    }

    private LocalDate parseDate(String value) {
        if (!TextUtil.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new BusinessException("检查日期格式不正确，应为 yyyy-MM-dd");
        }
    }
}
