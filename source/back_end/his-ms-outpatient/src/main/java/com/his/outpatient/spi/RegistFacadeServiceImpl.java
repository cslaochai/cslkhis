package com.his.outpatient.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.outpatient.entity.AppointInfo;
import com.his.outpatient.entity.Emergency;
import com.his.outpatient.entity.Queue;
import com.his.outpatient.mapper.AppointInfoMapper;
import com.his.outpatient.mapper.EmergencyMapper;
import com.his.outpatient.mapper.QueueMapper;
import com.his.common.enums.AppointStatusEnum;
import com.his.common.enums.QueueStatusEnum;
import com.his.common.entity.CurrentUser;
import com.his.spi.service.RegistFacadeService;
import com.his.spi.vo.BizAppointInfoListVO;
import com.his.spi.vo.EmergencyBriefVO;
import com.his.spi.vo.RegistBriefVO;
import com.his.spi.vo.StopImpactItemVO;
import com.his.spi.vo.TodayVisitVO;
import com.his.common.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 挂号事实端口实现：只读本模块挂号/队列/急诊行并转摘要，不依赖挂号与排队的编排服务，
 * 因此收费、医技、AI、排班、患者域依赖本类不会与挂号域构成 Bean 环。
 */
@Service
@RequiredArgsConstructor
public class RegistFacadeServiceImpl implements RegistFacadeService {

    private final AppointInfoMapper appointInfoMapper;

    private final QueueMapper queueMapper;

    private final EmergencyMapper emergencyMapper;

    @Override
    public RegistBriefVO findRegist(Long registId) {
        return registId == null ? null : toBrief(appointInfoMapper.selectById(registId));
    }

    @Override
    public Map<Long, TodayVisitVO> todayVisits() {
        LambdaQueryWrapper<Queue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Queue::getVisitDate, LocalDate.now())
                .in(Queue::getQueueStatus,
                        QueueStatusEnum.WAITING.getCode(),
                        QueueStatusEnum.CONSULTING.getCode(),
                        QueueStatusEnum.COMPLETED.getCode())
                .orderByDesc(Queue::getArriveTime);
        List<Queue> list = queueMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }

        CurrentUser currentUser = UserUtils.getCurrentUser();
        Long myEmpId = currentUser != null ? currentUser.getEmployeeId() : null;
        Long myDeptId = currentUser != null ? currentUser.getDeptId() : null;

        Map<Long, TodayVisitVO> result = new HashMap<>();
        for (Queue q : list) {
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

    @Override
    public List<StopImpactItemVO> listRegsBySchedule(Long scheduleId) {
        if (scheduleId == null) {
            return List.of();
        }
        List<AppointInfo> regs = appointInfoMapper.selectList(
                new LambdaQueryWrapper<AppointInfo>()
                        .eq(AppointInfo::getScheduleId, scheduleId)
                        .notIn(AppointInfo::getRegistStatus,
                                AppointStatusEnum.CANCELLED.getCode(), AppointStatusEnum.OVERDUE.getCode())
                        .orderByAsc(AppointInfo::getRegistStatus)
                        .orderByAsc(AppointInfo::getSlotTime));
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
    public List<BizAppointInfoListVO> listRegists(String startDate, String endDate) {
        LambdaQueryWrapper<AppointInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(startDate != null, AppointInfo::getVisitDate, startDate)
                .le(endDate != null, AppointInfo::getVisitDate, endDate);
        return appointInfoMapper.selectList(wrapper).stream()
                .map(this::toListVO).toList();
    }

    @Override
    public Long patientIdOfRegist(Long registId) {
        if (registId == null) {
            return null;
        }
        AppointInfo appointInfo = appointInfoMapper.selectById(registId);
        return appointInfo == null ? null : appointInfo.getPatientId();
    }

    @Override
    public void updateQueueRoomBySchedule(Long scheduleId, LocalDate visitDate, Long roomId, String roomName) {
        queueMapper.updateRoomBySchedule(scheduleId, visitDate, roomId, roomName);
    }

    @Override
    public void updateAppointRoomBySchedule(Long scheduleId, LocalDate visitDate, Long roomId, String roomName) {
        appointInfoMapper.updateRoomBySchedule(scheduleId, visitDate, roomId, roomName);
    }

    @Override
    public EmergencyBriefVO findEmergency(Long emergencyId) {
        if (emergencyId == null) {
            return null;
        }
        Emergency e = emergencyMapper.selectById(emergencyId);
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

    private TodayVisitVO toVisit(Queue q, Long myEmpId, Long myDeptId) {
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

    private BizAppointInfoListVO toListVO(AppointInfo entity) {
        if (entity == null) {
            return null;
        }
        BizAppointInfoListVO vo = new BizAppointInfoListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private RegistBriefVO toBrief(AppointInfo e) {
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
