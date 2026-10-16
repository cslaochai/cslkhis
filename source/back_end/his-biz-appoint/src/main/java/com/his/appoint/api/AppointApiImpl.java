package com.his.appoint.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.entity.BizEmergency;
import com.his.appoint.entity.BizQueue;
import com.his.appoint.enums.QueueStatusEnum;
import com.his.appoint.mapper.BizAppointInfoMapper;
import com.his.appoint.mapper.BizEmergencyMapper;
import com.his.appoint.mapper.BizQueueMapper;
import com.his.appoint.service.BizAppointService;
import com.his.appoint.service.BizQueueService;
import com.his.appoint.service.DoctorStatusCacheService;
import com.his.common.base.PageResult;
import com.his.common.util.TextUtil;
import com.his.api.service.AppointApi;
import com.his.api.dto.AppointCheckInUpdateDTO;
import com.his.api.dto.AppointQueryDTO;
import com.his.api.dto.AppointUpsertDTO;
import com.his.api.dto.RevisitFeePreviewDTO;
import com.his.api.vo.BizAppointInfoListVO;
import com.his.api.vo.DeptRefVO;
import com.his.api.vo.EmergencyBriefVO;
import com.his.api.vo.PatientQueueVO;
import com.his.api.vo.RegistBriefVO;
import com.his.api.vo.RevisitFeePreviewVO;
import com.his.api.vo.RevisitRecordSelectVO;
import com.his.api.vo.TodayVisitVO;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import com.his.api.vo.StopImpactItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AppointApi 在 his-appoint 侧的实现（挂号域跨域动作的承接方）。
 */
@Service
@RequiredArgsConstructor
public class AppointApiImpl implements AppointApi {

    private final BizAppointInfoMapper bizAppointInfoMapper;
    private final BizQueueMapper bizQueueMapper;
    private final BizAppointService bizAppointService;
    private final BizQueueService bizQueueService;
    private final DoctorStatusCacheService doctorStatusCacheService;
    private final BizEmergencyMapper bizEmergencyMapper;

    @Override
    public RegistBriefVO findRegist(Long registId) {
        if (registId == null) {
            return null;
        }
        return toBrief(bizAppointInfoMapper.selectById(registId));
    }

    @Override
    public DeptRefVO findDeptByRegistNo(String registNo) {
        if (!TextUtil.hasText(registNo)) {
            return null;
        }
        BizAppointInfo row = bizAppointInfoMapper.selectOne(new LambdaQueryWrapper<BizAppointInfo>()
                .eq(BizAppointInfo::getRegistNo, registNo)
                .last("LIMIT 1"));
        return row == null ? null
                : new DeptRefVO(row.getDeptId(), row.getDeptName());
    }

    @Override
    public Map<Long, TodayVisitVO> todayVisits() {
        LambdaQueryWrapper<BizQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizQueue::getVisitDate, LocalDate.now())
                .in(BizQueue::getQueueStatus,
                        QueueStatusEnum.WAITING.getCode(),
                        QueueStatusEnum.CONSULTING.getCode(),
                        QueueStatusEnum.COMPLETED.getCode())
                .orderByDesc(BizQueue::getArriveTime);
        List<BizQueue> list = bizQueueMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }

        CurrentUser currentUser = UserUtils.getCurrentUser();
        Long myEmpId = currentUser != null ? currentUser.getEmployeeId() : null;
        Long myDeptId = currentUser != null ? currentUser.getDeptId() : null;

        Map<Long, TodayVisitVO> result = new HashMap<>();
        for (BizQueue q : list) {
            if (q.getPatientId() == null) {
                continue;
            }
            TodayVisitVO visit = toVisit(q, myEmpId, myDeptId);
            TodayVisitVO exist = result.get(q.getPatientId());
            if (exist == null) {
                result.put(q.getPatientId(), visit);
                continue;
            }
            if (Boolean.TRUE.equals(visit.getMine()) && !Boolean.TRUE.equals(exist.getMine())) {
                result.put(q.getPatientId(), visit);
            }
        }
        return result;
    }

    private TodayVisitVO toVisit(BizQueue q, Long myEmpId, Long myDeptId) {
        TodayVisitVO visit = new TodayVisitVO();
        visit.setPatientId(q.getPatientId());
        visit.setStatus(q.getQueueStatus());
        QueueStatusEnum status = q.getQueueStatus() == null
                ? null : QueueStatusEnum.fromCode(q.getQueueStatus());
        // 状态文案由提供方给：前端不再自造码值映射（未知码值宁可显示原始码，也不回落成看似合法的值）
        visit.setStatusText(status != null ? status.getLabel() : "状态(" + q.getQueueStatus() + ")");
        visit.setDeptName(q.getDeptName());
        visit.setDoctorName(q.getDoctorName());
        visit.setQueueNo(q.getSequenceNo() != null ? q.getSequenceNo() + " 号" : null);
        visit.setMine((myEmpId != null && myEmpId.equals(q.getDoctorId()))
                || (myDeptId != null && myDeptId.equals(q.getDeptId())));
        return visit;
    }

    @Override
    public List<StopImpactItemVO> listRegsBySchedule(Long scheduleId) {
        if (scheduleId == null) {
            return List.of();
        }
        List<BizAppointInfo> regs = bizAppointInfoMapper.selectList(
                new LambdaQueryWrapper<BizAppointInfo>()
                        .eq(BizAppointInfo::getScheduleId, scheduleId)
                        .notIn(BizAppointInfo::getRegistStatus, 5, 6) // 5-已退号 6-已爽约
                        .orderByAsc(BizAppointInfo::getRegistStatus)
                        .orderByAsc(BizAppointInfo::getSlotTime));
        return regs.stream().map(r -> {
            StopImpactItemVO vo = new StopImpactItemVO();
            vo.setRegistId(r.getId());
            vo.setPatientId(r.getPatientId());
            vo.setPatientName(r.getPatientName());
            vo.setPatientNo(r.getPatientNo());
            vo.setPhone(r.getPhone());
            vo.setRegistSource(r.getRegistSource());
            vo.setRegistStatus(r.getRegistStatus());
            vo.setSlotTime(r.getSlotTime());
            return vo;
        }).toList();
    }

    @Override
    public boolean cancelRegist(Long id, String reason) {
        return bizAppointService.cancelRegist(id, reason);
    }

    @Override
    public void updateQueueRoomBySchedule(Long scheduleId, LocalDate visitDate, Long roomId, String roomName) {
        bizQueueMapper.updateRoomBySchedule(scheduleId, visitDate, roomId, roomName);
    }

    @Override
    public void updateAppointRoomBySchedule(Long scheduleId, LocalDate visitDate, Long roomId, String roomName) {
        bizAppointInfoMapper.updateRoomBySchedule(scheduleId, visitDate, roomId, roomName);
    }

    // ---------- 挂号单读写 ----------

    @Override
    public BizAppointInfoListVO addAppoint(AppointUpsertDTO dto) {
        return toListVO(bizAppointService.addAppoint(dto));
    }

    @Override
    public PageResult<BizAppointInfoListVO> listRegistPage(AppointQueryDTO query) {
        return bizAppointService.listPage(query);
    }

    @Override
    public List<BizAppointInfoListVO> listRegists(String startDate, String endDate) {
        LambdaQueryWrapper<BizAppointInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(startDate != null, BizAppointInfo::getVisitDate, startDate)
                .le(endDate != null, BizAppointInfo::getVisitDate, endDate);
        return bizAppointInfoMapper.selectList(wrapper).stream()
                .map(this::toListVO).toList();
    }

    @Override
    public Long patientIdOfRegist(Long registId) {
        return bizQueueService.patientIdOfRegist(registId);
    }

    // ---------- 复诊 ----------

    @Override
    public RevisitFeePreviewVO revisitFeePreview(RevisitFeePreviewDTO dto) {
        return bizAppointService.revisitFeePreview(dto);
    }

    @Override
    public List<RevisitRecordSelectVO> revisitRecordSelectList(Long patientId, int limit) {
        return bizAppointService.revisitRecordSelectList(patientId, limit);
    }

    // ---------- 队列 ----------

    @Override
    public List<PatientQueueVO> myQueue(Long patientId) {
        return bizQueueService.myQueue(patientId);
    }

    @Override
    public boolean checkInByRegistId(AppointCheckInUpdateDTO dto) {
        return bizQueueService.checkInByRegistId(dto);
    }

    // ---------- 医生接诊状态 ----------

    @Override
    public void finishConsulting(Long doctorId) {
        doctorStatusCacheService.finishConsulting(doctorId);
    }

    // ---------- 急诊 ----------

    @Override
    public EmergencyBriefVO findEmergency(Long emergencyId) {
        if (emergencyId == null) {
            return null;
        }
        BizEmergency e = bizEmergencyMapper.selectById(emergencyId);
        if (e == null) {
            return null;
        }
        EmergencyBriefVO vo = new EmergencyBriefVO();
        vo.setId(e.getId());
        vo.setEmergencyNo(e.getEmergencyNo());
        vo.setPatientName(e.getPatientName());
        vo.setGender(e.getGender());
        vo.setAge(e.getAge());
        vo.setChiefComplaint(e.getChiefComplaint());
        vo.setVitalSigns(e.getVitalSigns());
        vo.setTriageLevel(e.getTriageLevel());
        vo.setZone(e.getZone());
        vo.setGreenChannel(e.getGreenChannel());
        return vo;
    }

    private BizAppointInfoListVO toListVO(BizAppointInfo entity) {
        if (entity == null) {
            return null;
        }
        BizAppointInfoListVO vo = new BizAppointInfoListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private RegistBriefVO toBrief(BizAppointInfo e) {
        if (e == null) {
            return null;
        }
        RegistBriefVO brief = new RegistBriefVO();
        brief.setId(e.getId());
        brief.setRegistNo(e.getRegistNo());
        brief.setPatientId(e.getPatientId());
        brief.setPatientName(e.getPatientName());
        brief.setGender(e.getGender());
        brief.setAge(e.getAge());
        brief.setDeptId(e.getDeptId());
        brief.setDeptName(e.getDeptName());
        brief.setDoctorId(e.getDoctorId());
        brief.setDoctorName(e.getDoctorName());
        brief.setVisitType(e.getVisitType());
        brief.setVisitDate(e.getVisitDate());
        brief.setPatientNo(e.getPatientNo());
        brief.setRegistStatus(e.getRegistStatus());
        brief.setBillId(e.getBillId());
        brief.setMedicalInsuranceType(e.getMedicalInsuranceType());
        brief.setMedicalInsuranceNo(e.getMedicalInsuranceNo());
        return brief;
    }
}
