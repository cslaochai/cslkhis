package com.his.medicaltech.spi;

import com.his.common.base.PageResult;
import com.his.medicaltech.service.ExamAppointmentService;
import com.his.medicaltech.vo.ExamApptVO;
import com.his.spi.service.ExamAppointFacadeService;
import com.his.spi.vo.PatientExamApptVO;
import com.his.spi.vo.PatientExamApplyVO;
import com.his.spi.vo.PatientExamSlotVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * 检查预约患者自助端口实现：把 medicaltech 内部出参转成患者侧 VO。
 *
 * <p>患者侧 ID 出参统一字符串化（{@code ToStringSerializer}），日期统一文本，
 * 避免小程序端 19 位 Long 丢精度与 ISO 日期格式差异。</p>
 */
@Service
@RequiredArgsConstructor
public class ExamAppointFacadeServiceImpl implements ExamAppointFacadeService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ExamAppointmentService examAppointmentService;

    @Override
    public PageResult<PatientExamApplyVO> pendingApplies(Long patientId, int pageNum, int pageSize) {
        PageResult<ExamApptVO.ApplyVO> page =
                examAppointmentService.pendingAppliesForPatient(patientId, pageNum, pageSize);
        return PageResult.of(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getPages(),
                page.getRecords().stream().map(this::toApply).toList());
    }

    @Override
    public List<PatientExamSlotVO> recommendSlots(Long patientId, Long applyId, LocalDate examDate, String afterTime) {
        ExamApptVO.RecommendVO recommend =
                examAppointmentService.recommendForPatient(patientId, applyId, examDate, afterTime);
        List<ExamApptVO.SlotOptionVO> options = recommend.getOptions();
        return options == null ? List.of() : options.stream().map(this::toSlot).toList();
    }

    @Override
    public PatientExamApptVO book(Long patientId, Long applyId, Long deviceId, LocalDate examDate, String startTime) {
        return toAppt(examAppointmentService.bookForPatient(patientId, applyId, deviceId, examDate, startTime));
    }

    @Override
    public void cancel(Long patientId, Long apptId, String reason) {
        examAppointmentService.cancelForPatient(patientId, apptId, reason);
    }

    @Override
    public PageResult<PatientExamApptVO> myAppointments(Long patientId, int pageNum, int pageSize) {
        PageResult<ExamApptVO.ApptVO> page =
                examAppointmentService.appointmentsForPatient(patientId, pageNum, pageSize);
        return PageResult.of(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getPages(),
                page.getRecords().stream().map(this::toAppt).toList());
    }

    private PatientExamApplyVO toApply(ExamApptVO.ApplyVO src) {
        PatientExamApplyVO vo = new PatientExamApplyVO();
        vo.setApplyId(src.getApplyId());
        vo.setApplyNo(src.getApplyNo());
        vo.setItemName(src.getItemName());
        vo.setBodyPart(src.getBodyPart());
        vo.setExamMinutes(src.getExamMinutes());
        vo.setPrice(src.getPrice());
        vo.setVisitDate(text(src.getVisitDate()));
        vo.setApplyDeptName(src.getApplyDeptName());
        vo.setDoctorName(src.getDoctorName());
        vo.setIsEmergency(src.getIsEmergency());
        vo.setApplyStatusText(src.getApplyStatusText());
        vo.setDeviceCount(src.getDeviceCount());
        return vo;
    }

    private PatientExamSlotVO toSlot(ExamApptVO.SlotOptionVO src) {
        PatientExamSlotVO vo = new PatientExamSlotVO();
        vo.setDeviceId(src.getDeviceId());
        vo.setDeviceName(src.getDeviceName());
        vo.setRoomName(src.getRoomName());
        vo.setExamDate(text(src.getExamDate()));
        vo.setStartTime(src.getStartTime());
        vo.setEndTime(src.getEndTime());
        vo.setAvailableSource(src.getAvailableSource());
        return vo;
    }

    private PatientExamApptVO toAppt(ExamApptVO.ApptVO src) {
        PatientExamApptVO vo = new PatientExamApptVO();
        vo.setApptId(src.getId());
        vo.setApptNo(src.getApptNo());
        vo.setApplyNo(src.getApplyNo());
        vo.setItemName(src.getItemName());
        vo.setBodyPart(src.getBodyPart());
        vo.setDeviceName(src.getDeviceName());
        vo.setRoomName(src.getRoomName());
        vo.setExamDeptName(src.getExamDeptName());
        vo.setExamDate(text(src.getExamDate()));
        vo.setStartTime(src.getStartTime());
        vo.setEndTime(src.getEndTime());
        vo.setTimeRange(Objects.toString(src.getTimeRange(),
                src.getStartTime() + "-" + src.getEndTime()));
        vo.setStatus(src.getStatus());
        vo.setStatusText(src.getStatusText());
        vo.setActiveFlag(src.getActiveFlag());
        vo.setIsEmergency(src.getIsEmergency());
        vo.setBookTime(dateTime(src.getBookTime()));
        vo.setBookBy(src.getBookBy());
        return vo;
    }

    private String text(LocalDate date) {
        return date == null ? null : date.toString();
    }

    private String dateTime(LocalDateTime value) {
        return value == null ? null : DATE_TIME.format(value);
    }
}
