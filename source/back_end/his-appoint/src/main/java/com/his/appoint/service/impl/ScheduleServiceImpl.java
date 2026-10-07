package com.his.appoint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.appoint.dto.OnDutyQueryDTO;
import com.his.appoint.dto.ScheduleQueryDTO;
import com.his.appoint.dto.ScheduleSelectQueryDTO;
import com.his.appoint.dto.ScheduleUpsertDTO;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.entity.BizSchedule;
import com.his.appoint.mapper.BizAppointInfoMapper;
import com.his.appoint.mapper.BizQueueMapper;
import com.his.appoint.mapper.BizScheduleMapper;
import com.his.appoint.service.AppointService;
import com.his.appoint.service.ScheduleService;
import com.his.appoint.service.ScheduleSlotService;
import com.his.appoint.vo.OnDutyStaffVO;
import com.his.appoint.vo.ScheduleDetailVO;
import com.his.appoint.vo.ScheduleSelectListVO;
import com.his.appoint.vo.StopImpactItemVO;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.util.ShiftCoverUtil;
import com.his.common.util.TextUtil;
import com.his.system.dto.StaffScheduleUpsertDTO;
import com.his.system.entity.BizShift;
import com.his.system.entity.BizStaffSchedule;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.DeptScopeProvider;
import com.his.system.service.ShiftService;
import com.his.system.service.StaffScheduleService;
import com.his.system.service.SysClinicRoomService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 挂号服务实现
 */
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl extends ServiceImpl<BizScheduleMapper, BizSchedule> implements ScheduleService {

    private final DeptScopeProvider deptScopeProvider;

    private final BizScheduleMapper bizScheduleMapper;

    private final BizAppointInfoMapper bizAppointInfoMapper;

    private final AppointService appointService;

    private final ScheduleSlotService scheduleSlotService;

    private final BizQueueMapper bizQueueMapper;

    private final SysClinicRoomService sysClinicRoomService;

    private final ShiftService shiftService;

    private final StaffScheduleService staffScheduleService;

    @Override
    public List<BizSchedule> listPage(ScheduleQueryDTO queryDTO) {
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<>();
        Long scopedDeptId = deptScopeProvider.resolveDeptId(queryDTO.getDeptId());
        if (scopedDeptId != null) {
            wrapper.eq(BizSchedule::getDeptId, scopedDeptId);
        } else if (deptScopeProvider.isScoped()) {
            wrapper.in(BizSchedule::getDeptId, deptScopeProvider.allowedDeptIds());
        }
        wrapper.eq(queryDTO.getStaffType() != null, BizSchedule::getStaffType, queryDTO.getStaffType())
                .eq(queryDTO.getDoctorId() != null, BizSchedule::getDoctorId, queryDTO.getDoctorId())
                .ge(queryDTO.getStartDate() != null, BizSchedule::getScheduleDate, queryDTO.getStartDate())
                .le(queryDTO.getEndDate() != null, BizSchedule::getScheduleDate, queryDTO.getEndDate());
        wrapper.last("ORDER BY schedule_date ASC, start_time ASC, doctor_id ASC, id ASC");
        return bizScheduleMapper.selectList(wrapper);
    }

    @Override
    public List<BizSchedule> scheduleSelectList(ScheduleSelectQueryDTO scheduleQueryDTO) {
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<>();
        Long scopedDeptId = deptScopeProvider.resolveDeptId(scheduleQueryDTO.getDeptId());
        if (scopedDeptId != null) {
            wrapper.eq(BizSchedule::getDeptId, scopedDeptId);
        } else if (deptScopeProvider.isScoped()) {
            wrapper.in(BizSchedule::getDeptId, deptScopeProvider.allowedDeptIds());
        }
        // 号源下拉只认医生出诊排班（sql/195）：护士/技师/收费员这些岗位是**出勤排班**，
        // 号源恒 0 且没有诊室，一旦混进这个下拉，挂号员能选到「张三（收费员）」并挂出一个没有号源的号。
        // 这里硬编码 1-医生而不是透传入参：能挂号的只有医生，这不是筛选条件，是业务前提。
        wrapper.eq(BizSchedule::getStaffType, StaffTypeEnum.DOCTOR.getCode())
                .eq(scheduleQueryDTO.getVisitDate() != null,
                        BizSchedule::getScheduleDate, scheduleQueryDTO.getVisitDate())
                .eq(scheduleQueryDTO.getDoctorId() != null,
                        BizSchedule::getDoctorId, scheduleQueryDTO.getDoctorId())
                .gt(BizSchedule::getAvailableSource, 0)
                .eq(BizSchedule::getStatus, ScheduleStatusEnum.NORMAL.getCode())
                .orderByAsc(BizSchedule::getStartTime);
        return bizScheduleMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSchedule(BizSchedule schedule) {
        applyShift(schedule);
        assertNotPast(schedule.getScheduleDate(), "新增排班");
        bindCoreSchedule(schedule, StaffScheduleSourceEnum.MANUAL);
        applyStaffType(schedule);
        checkRoomRequired(schedule);
        checkScheduleOverlap(schedule, null);

        schedule.setUsedSource(0);
        schedule.setAvailableSource(schedule.getTotalSource());
        if (schedule.getStatus() == null) {
            schedule.setStatus(ScheduleStatusEnum.NORMAL.getCode());
        }
        boolean ok = bizScheduleMapper.insert(schedule) > 0;
        if (ok && StaffTypeEnum.hasSource(schedule.getStaffType())) {
            scheduleSlotService.generateSlots(schedule.getId(), schedule.getStartTime(), schedule.getEndTime(),
                    schedule.getTotalSource(), schedule.getAppointmentSource());
        }
        return ok;
    }

    @Override
    public void bindCoreSchedule(BizSchedule schedule, StaffScheduleSourceEnum source) {
        Integer submittedStaffType = schedule.getStaffType();
        StaffScheduleUpsertDTO dto = new StaffScheduleUpsertDTO();
        dto.setId(schedule.getStaffScheduleId());
        dto.setScheduleDate(schedule.getScheduleDate());
        dto.setOrgType(OrgUnitTypeEnum.DEPT.getCode());
        dto.setOrgId(schedule.getDeptId());
        dto.setEmployeeId(schedule.getDoctorId());
        dto.setShiftId(schedule.getShiftId());
        // 停诊 ≠ 停班：号源池的状态与人今天上不上班是两件事，出诊计划只按「上班」派生事实
        dto.setDutyStatus(StaffDutyStatusEnum.WORK.getCode());
        dto.setAttendMode(AttendModeEnum.ON_SITE.getCode());
        dto.setClinicFlag(YesOrNoEnum.YES.getCode());
        dto.setRemark(schedule.getRemark());
        BizStaffSchedule core = staffScheduleService.ensureForClinic(dto, source);
        if (submittedStaffType != null && !Objects.equals(submittedStaffType, core.getStaffType())) {
            throw new BusinessException("「" + core.getEmployeeName() + "」在"
                    + OrgUnitTypeEnum.getText(core.getOrgType()) + "「" + core.getDeptName()
                    + "」的岗位类别是" + StaffTypeEnum.getText(core.getStaffType())
                    + "，不是" + StaffTypeEnum.getText(submittedStaffType) + "：请先修正这个人的岗位配置，排班表不接受手填类别");
        }
        schedule.setStaffScheduleId(core.getId());
        schedule.setWeekDay(core.getWeekDay());
        schedule.setStaffType(core.getStaffType());
        schedule.setDoctorName(core.getEmployeeName());
        schedule.setDeptName(core.getDeptName());
        schedule.setStartTime(core.getStartTime());
        schedule.setEndTime(core.getEndTime());
    }

    /**
     * 投影行不再引用这条出勤事实时，把事实一起收掉。
     */
    private void releaseCoreSchedule(Long staffScheduleId) {
        if (staffScheduleId == null) {
            return;
        }
        Long holding = bizScheduleMapper.selectCount(new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getStaffScheduleId, staffScheduleId));
        if (holding == null || holding == 0) {
            staffScheduleService.deleteById(staffScheduleId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSchedule(BizSchedule schedule) {
        BizSchedule existing = bizScheduleMapper.selectById(schedule.getId());
        if (existing == null) {
            throw new BusinessException("排班记录不存在");
        }
        // 已过去的排班一律只读：历史班次的真实就诊结果以门诊日志/挂号记录为准，改排班表等于改历史。
        // 既禁"改一条过去的"，也禁"把未来的改到过去"。
        assertNotPast(existing.getScheduleDate(), "修改");
        assertNotPast(schedule.getScheduleDate(), "修改");
        // 已挂过号的排班不允许换岗位：医生岗改出勤岗会把「能挂号的号源池」直接抹掉，
        // 出勤岗改医生岗则让一条没有号源/诊室配置的排班突然出现在挂号下拉里。
        if (!Objects.equals(existing.getStaffType(), schedule.getStaffType())
                && existing.getUsedSource() != null && existing.getUsedSource() > 0) {
            throw new BusinessException("该排班已有 " + existing.getUsedSource() + " 条挂号记录，不能变更岗位类别");
        }
        applyShift(schedule);
        bindCoreSchedule(schedule, StaffScheduleSourceEnum.MANUAL);
        applyStaffType(schedule);
        checkRoomRequired(schedule);
        checkScheduleOverlap(schedule, schedule.getId());

        int delta = schedule.getTotalSource() - existing.getTotalSource();
        schedule.setAvailableSource(existing.getAvailableSource() + delta);
        if (schedule.getAvailableSource() < 0) {
            throw new BusinessException("号源数量不能小于已使用数量");
        }
        boolean roomChanged = !Objects.equals(existing.getRoomId(), schedule.getRoomId())
                || !Objects.equals(existing.getRoomName(), schedule.getRoomName());
        boolean ok = bizScheduleMapper.updateById(schedule) > 0;
        if (ok) {
            if (StaffTypeEnum.hasSource(schedule.getStaffType())) {
                scheduleSlotService.regenerateForSchedule(schedule, schedule.getTotalSource(), schedule.getAppointmentSource());
            } else {
                scheduleSlotService.physicalDeleteByScheduleId(schedule.getId());
            }
            if (roomChanged) {
                syncRoomToTodayWorklist(schedule);
            }
            releaseCoreSchedule(existing.getStaffScheduleId());
        }
        return ok;
    }

    /**
     * 排班的诊室变了
     */
    private void syncRoomToTodayWorklist(BizSchedule schedule) {
        LocalDate today = LocalDate.now();
        if (schedule.getScheduleDate() != null && !schedule.getScheduleDate().equals(today)) {
            // 只动今天的班次：历史/未来日期的队列不该被追改
            return;
        }
        Long roomId = schedule.getRoomId();
        String roomName = schedule.getRoomName();
        if (roomId != null && !TextUtil.hasText(roomName)) {
            var room = sysClinicRoomService.getById(roomId);
            if (room != null) {
                roomName = room.getName();
                schedule.setRoomName(roomName);
                bizScheduleMapper.updateById(schedule);
            }
        }
        bizQueueMapper.updateRoomBySchedule(schedule.getId(), today, roomId, roomName);
        bizAppointInfoMapper.updateRoomBySchedule(schedule.getId(), today, roomId, roomName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSchedule(Long id) {
        BizSchedule existing = bizScheduleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("排班记录不存在");
        }
        if (existing.getUsedSource() > 0) {
            throw new BusinessException("该排班已有挂号记录，无法删除");
        }
        assertNotPast(existing.getScheduleDate(), "删除");
        scheduleSlotService.physicalDeleteByScheduleId(id);
        boolean ok = bizScheduleMapper.physicalDeleteById(id) > 0;
        if (ok) {
            releaseCoreSchedule(existing.getStaffScheduleId());
        }
        return ok;
    }

    /**
     * 校验：启用态的排班必须排诊室。
     */
    private void checkRoomRequired(BizSchedule schedule) {
        boolean enabled = !ScheduleStatusEnum.stopped(schedule.getStatus());
        // B类（条件必填）：必填性取决于同一请求里的状态与岗位类别，声明式注解做不到一刀切
        if (enabled && StaffTypeEnum.hasSource(schedule.getStaffType())
                && schedule.getRoomId() == null && !TextUtil.hasText(schedule.getRoomName())) {
            throw new BusinessException("启用状态的医生排班必须指定诊室：分诊台按诊室编号发号，缺诊室患者找不到房间");
        }
    }

    /**
     * 岗位类别收口（sql/195）：写库前按岗位裁剪字段，前端传什么都不算。
     */
    private void applyStaffType(BizSchedule schedule) {
        StaffTypeEnum.assertValid(schedule.getStaffType());
        if (StaffTypeEnum.hasSource(schedule.getStaffType())) {
            int total = schedule.getTotalSource() == null ? 0 : schedule.getTotalSource();
            if (total < 1) {
                throw new BusinessException("医生排班必须配置号源数量（至少 1）：没有号源的出诊班挂不出号");
            }
            int appt = schedule.getAppointmentSource() == null ? 0 : schedule.getAppointmentSource();
            if (appt > total) {
                throw new BusinessException("预约号源数不能大于号源总数");
            }
            schedule.setTotalSource(total);
            return;
        }
        schedule.setTotalSource(0);
        schedule.setAppointmentSource(0);
        schedule.setUsedAppointmentSource(0);
        schedule.setIsAppointment(0);
        schedule.setIsExpert(0);
        schedule.setExpertFee(BigDecimal.ZERO);
        schedule.setRegistFee(BigDecimal.ZERO);
        schedule.setDiagnosisFee(BigDecimal.ZERO);
        schedule.setRoomId(null);
        schedule.setRoomName(null);
    }

    /**
     * 班次是时间段与班别的<b>唯一</b>来源：前端传什么都不认，一律按班次字典覆盖
     * （原来「标准班次」和「班次类型」两个并列字段可以互相矛盾，就是这么来的）。
     */
    private void applyShift(BizSchedule schedule) {
        BizShift shift = shiftService.resolveForScheduling(schedule.getShiftId(), schedule.getDeptId());
        schedule.setStartTime(shift.getStartTime());
        schedule.setEndTime(shift.getEndTime());
        schedule.setShiftId(shift.getId());
    }

    @Override
    public void fillShiftDisplay(List<ScheduleDetailVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        Map<Long, BizShift> shiftMap = shiftService.mapByIds(voList.stream()
                .map(ScheduleDetailVO::getShiftId).filter(Objects::nonNull).toList());
        for (ScheduleDetailVO vo : voList) {
            BizShift shift = vo.getShiftId() == null ? null : shiftMap.get(vo.getShiftId());
            if (shift != null) {
                vo.setShiftName(shift.getShiftName());
                vo.setScheduleType(shift.getScheduleType());
            }
        }
    }

    @Override
    public List<ScheduleDetailVO> listDetail(ScheduleQueryDTO queryDTO) {
        return toVOList(listPage(queryDTO));
    }

    @Override
    public List<ScheduleSelectListVO> selectListVO(ScheduleSelectQueryDTO scheduleQueryDTO) {
        return toVOList(scheduleSelectList(scheduleQueryDTO)).stream().map(detail -> {
            ScheduleSelectListVO vo = new ScheduleSelectListVO();
            BeanUtils.copyProperties(detail, vo);
            return vo;
        }).toList();
    }

    @Override
    public List<ScheduleDetailVO> todayScheduleOfCurrentDept() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getDeptId() == null) {
            return List.of();
        }
        return toVOList(getTodaySchedule(user.getDeptId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scheduleUpsert(ScheduleUpsertDTO upsertDTO) {
        BizSchedule schedule = convertToScheduleEntity(upsertDTO);
        try {
            boolean success = upsertDTO.getId() == null
                    ? addSchedule(schedule)
                    : updateSchedule(schedule);
            if (!success) {
                throw new BusinessException(upsertDTO.getId() == null ? "新增失败" : "修改失败");
            }
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该医生在此时间段已有排班，时间冲突");
        }
    }

    private List<ScheduleDetailVO> toVOList(List<BizSchedule> list) {
        List<ScheduleDetailVO> voList = list.stream().map(this::convertToScheduleVO).toList();
        fillShiftDisplay(voList);
        return voList;
    }

    private BizSchedule convertToScheduleEntity(ScheduleUpsertDTO upsertDTO) {
        BizSchedule entity = new BizSchedule();
        entity.setId(upsertDTO.getId());
        entity.setDeptId(upsertDTO.getDeptId());
        entity.setDeptName(upsertDTO.getDeptName());
        entity.setDoctorId(upsertDTO.getDoctorId());
        entity.setDoctorName(upsertDTO.getDoctorName());
        entity.setStaffType(upsertDTO.getStaffType());
        entity.setScheduleDate(upsertDTO.getScheduleDate());
        entity.setStartTime(upsertDTO.getStartTime());
        entity.setEndTime(upsertDTO.getEndTime());
        entity.setShiftId(upsertDTO.getShiftId());
        entity.setTotalSource(upsertDTO.getTotalSource());
        entity.setRegistFee(upsertDTO.getRegistFee());
        entity.setDiagnosisFee(upsertDTO.getDiagnosisFee());
        entity.setRoomId(upsertDTO.getRoomId());
        entity.setRoomName(upsertDTO.getRoomName());
        entity.setIsExpert(upsertDTO.getIsExpert());
        entity.setExpertFee(upsertDTO.getExpertFee());
        entity.setIsAppointment(upsertDTO.getIsAppointment());
        entity.setAppointmentSource(upsertDTO.getAppointmentSource());
        entity.setStatus(upsertDTO.getStatus());
        return entity;
    }

    private ScheduleDetailVO convertToScheduleVO(BizSchedule entity) {
        if (entity == null) {
            return null;
        }
        ScheduleDetailVO vo = new ScheduleDetailVO();
        vo.setId(entity.getId());
        vo.setDeptId(entity.getDeptId());
        vo.setDeptName(entity.getDeptName());
        vo.setDoctorId(entity.getDoctorId());
        vo.setDoctorName(entity.getDoctorName());
        vo.setStaffType(entity.getStaffType());
        vo.setStaffTypeName(StaffTypeEnum.getText(entity.getStaffType()));
        vo.setScheduleDate(entity.getScheduleDate());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setShiftId(entity.getShiftId());
        vo.setTotalSource(entity.getTotalSource());
        vo.setUsedSource(entity.getUsedSource());
        vo.setAvailableSource(entity.getAvailableSource());
        vo.setRegistFee(entity.getRegistFee());
        vo.setDiagnosisFee(entity.getDiagnosisFee());
        vo.setIsExpert(entity.getIsExpert());
        vo.setExpertFee(entity.getExpertFee());
        vo.setIsAppointment(entity.getIsAppointment());
        vo.setAppointmentSource(entity.getAppointmentSource());
        vo.setUsedAppointmentSource(entity.getUsedAppointmentSource());
        vo.setStatus(entity.getStatus());
        vo.setConsultStatus(entity.getConsultStatus());
        vo.setRoomId(entity.getRoomId() != null ? String.valueOf(entity.getRoomId()) : null);
        vo.setRoomName(entity.getRoomName());
        return vo;
    }

    /**
     * 排班日期不得早于今天：已过日期的班次一律只读。
     *
     * <p>过去的日期上患者已就诊完，改号源/换诊室/停诊都不会再影响任何真实就诊，
     * 只会让「排班表」和「门诊日志」两个口径分家（历史以日志为准）；新增过去的日期
     * 更等于往号源池里开一个「能挂历史号」的口子。所以新增、修改、停诊、删除走同一条线。
     *
     * <p>粒度只到<b>日期</b>，不到时刻：今天 08:00~12:00 这种"时段已过"的班次当天仍可停诊
     * （医生临时走人必须当天停），所以放行今天。
     */
    private void assertNotPast(LocalDate date, String action) {
        if (date == null || date.compareTo(LocalDate.now()) >= 0) {
            return;
        }
        throw new BusinessException("排班日期 " + date + " 已过，不允许" + action + "（历史排班以门诊日志为准）");
    }

    /**
     * 校验同一医生同一天的时间段是否冲突
     * 时间重叠条件：startTime1 < endTime2 且 startTime2 < endTime1
     */
    private void checkScheduleOverlap(BizSchedule schedule, Long excludeId) {
        if (schedule.getDoctorId() == null || schedule.getScheduleDate() == null
                || !TextUtil.hasText(schedule.getStartTime()) || !TextUtil.hasText(schedule.getEndTime())) {
            return;
        }
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizSchedule::getDoctorId, schedule.getDoctorId())
                .eq(BizSchedule::getScheduleDate, schedule.getScheduleDate())
                .ne(excludeId != null, BizSchedule::getId, excludeId);
        List<BizSchedule> existing = bizScheduleMapper.selectList(wrapper);
        for (BizSchedule s : existing) {
            if (s.getStartTime().compareTo(schedule.getEndTime()) < 0
                    && schedule.getStartTime().compareTo(s.getEndTime()) < 0) {
                throw new BusinessException("该医生在此时间段已有排班，时间冲突");
            }
        }
    }

    @Override
    public List<BizSchedule> getTodaySchedule(Long deptId) {
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizSchedule::getDeptId, deptId)
                .eq(BizSchedule::getScheduleDate, LocalDate.now())
                .eq(BizSchedule::getStaffType, StaffTypeEnum.DOCTOR.getCode())
                .eq(BizSchedule::getStatus, ScheduleStatusEnum.NORMAL.getCode())
                .orderByAsc(BizSchedule::getStartTime);
        return bizScheduleMapper.selectList(wrapper);
    }

    @Override
    public boolean updateConsultStatus(Long scheduleId, Integer consultStatus) {
        // 值域在服务端收口：码值只允许 0/1/2，越界值（如 99）不能落库
        if (ConsultStatusEnum.fromCode(consultStatus) == null) {
            throw new BusinessException("就诊状态只允许 " + ConsultStatusEnum.whitelistText());
        }
        BizSchedule schedule = bizScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排班记录不存在");
        }
        schedule.setConsultStatus(consultStatus);
        return bizScheduleMapper.updateById(schedule) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long scheduleId, Integer status) {
        BizSchedule old = bizScheduleMapper.selectById(scheduleId);
        if (old == null) {
            throw new BusinessException("排班记录不存在");
        }
        assertNotPast(old.getScheduleDate(), ScheduleStatusEnum.stopped(status) ? "停诊" : "启用");
        BizSchedule schedule = new BizSchedule();
        schedule.setId(scheduleId);
        schedule.setStatus(status);
        boolean ok = bizScheduleMapper.updateById(schedule) > 0;
        if (ok) {
            // 段状态随主表联动：停诊 → 段全停（扣减 SQL 带 status=1，停用段不可再挂）；启用 → 段恢复
            scheduleSlotService.syncStatusToSlots(scheduleId, status);
        }
        return ok;
    }

    @Override
    public List<StopImpactItemVO> stopImpact(Long scheduleId) {
        if (bizScheduleMapper.selectById(scheduleId) == null) {
            throw new BusinessException("排班记录不存在");
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
    public String batchCancel(List<Long> registIds, String reason) {
        String cancelReason = TextUtil.hasText(reason) ? reason : "班次停诊，批量退号";
        int ok = 0;
        for (Long id : registIds) {
            try {
                if (appointService.cancelRegist(id, cancelReason)) {
                    ok++;
                }
            } catch (Exception ignored) {
                // 单条失败（如已支付需先退费）不中断整批，结论里如实计数
            }
        }
        return "批量退号完成：成功 " + ok + " 条 / 共 " + registIds.size() + " 条"
                + (ok < registIds.size()
                ? "（失败多为：已缴费需先到收费处退费；或已接诊/已就诊——诊疗已发生，号源锁死）"
                : "");
    }

    // 今日在岗（排班的下游出口）

    @Override
    public List<OnDutyStaffVO> onDuty(OnDutyQueryDTO queryDTO) {
        OnDutyQueryDTO cond = queryDTO == null ? new OnDutyQueryDTO() : queryDTO;
        Long deptId = cond.getDeptId() != null ? cond.getDeptId() : currentDeptId();
        if (deptId == null) {
            return List.of();
        }
        LocalDate date = cond.getDate() != null ? cond.getDate() : LocalDate.now();
        LocalTime moment = cond.getMoment() == null
                ? LocalTime.now()
                : ShiftCoverUtil.parseShiftTime(cond.getMoment());
        boolean onlyOnDuty = !Boolean.FALSE.equals(cond.getOnDutyOnly());

        List<BizSchedule> rows = loadDaySchedule(deptId, date, cond.getStaffType());
        List<OnDutyStaffVO> all = rows.stream().map(s -> toOnDutyVO(s, moment)).toList();
        if (!onlyOnDuty) {
            return all;
        }
        return all.stream().filter(vo -> Boolean.TRUE.equals(vo.getOnDutyNow())).toList();
    }

    @Override
    public OnDutyStaffVO pickDutyStaff(Long deptId, Integer staffType, Long preferEmpId) {
        if (deptId == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        List<BizSchedule> rows = loadDaySchedule(deptId, today, staffType);
        if (rows.isEmpty()) {
            return null;
        }
        List<BizSchedule> onDuty = rows.stream()
                .filter(s -> ShiftCoverUtil.covers(now, s.getStartTime(), s.getEndTime()))
                .toList();
        if (!onDuty.isEmpty()) {
            // 同科室多人同时在职：优先本人（当班护士自己操作，理应记她），不是随便抓一个
            for (BizSchedule s : onDuty) {
                if (preferEmpId != null && Objects.equals(s.getDoctorId(), preferEmpId)) {
                    return toOnDutyVO(s, now);
                }
            }
            // 交接班时段两班重叠 → 取最近开班的那一班（接班人），
            // 否则凌晨分诊会被记到已经下班的上一班头上
            BizSchedule latest = onDuty.stream()
                    .max(Comparator.comparing(s -> ShiftCoverUtil.parseShiftTime(s.getStartTime()),
                            Comparator.nullsFirst(Comparator.naturalOrder())))
                    .orElse(null);
            return latest == null ? null : toOnDutyVO(latest, now);
        }
        // 交接班空档：此刻谁都不在班里，退而取当日最早一班（当天在岗过的人）。
        // 仍然不是「登录人兜底」——身份凭证 ≠ 当班责任
        BizSchedule earliest = rows.stream()
                .min(Comparator.comparing(s -> ShiftCoverUtil.parseShiftTime(s.getStartTime()),
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        return earliest == null ? null : toOnDutyVO(earliest, now);
    }

    /**
     * 当日该科室全部有效排班（默认全岗位）。
     * 与 getTodaySchedule 不是一回事：那个是「今日医生出诊名单」（硬编码 staff_type=1），
     * 这里是「今日全部/某岗位的出勤表」，两者不能混用。
     */
    private List<BizSchedule> loadDaySchedule(Long deptId, LocalDate date, Integer staffType) {
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizSchedule::getDeptId, deptId)
                .eq(BizSchedule::getScheduleDate, date)
                .eq(BizSchedule::getStatus, ScheduleStatusEnum.NORMAL.getCode())
                .isNotNull(BizSchedule::getDoctorId);
        if (staffType != null) {
            wrapper.eq(BizSchedule::getStaffType, staffType);
        }
        wrapper.orderByAsc(BizSchedule::getStartTime);
        return bizScheduleMapper.selectList(wrapper);
    }

    private OnDutyStaffVO toOnDutyVO(BizSchedule s, LocalTime moment) {
        OnDutyStaffVO vo = new OnDutyStaffVO();
        vo.setScheduleId(s.getId());
        vo.setStaffId(s.getDoctorId());
        vo.setStaffName(s.getDoctorName());
        vo.setStaffType(s.getStaffType());
        vo.setStaffTypeName(StaffTypeEnum.getText(s.getStaffType()));
        vo.setDeptId(s.getDeptId());
        vo.setDeptName(s.getDeptName());
        vo.setShiftId(s.getShiftId());
        vo.setStartTime(s.getStartTime());
        vo.setEndTime(s.getEndTime());
        vo.setRoomId(s.getRoomId());
        vo.setRoomName(s.getRoomName());
        vo.setStatus(s.getStatus());
        vo.setOnDutyNow(ShiftCoverUtil.covers(moment, s.getStartTime(), s.getEndTime()));
        if (s.getShiftId() != null && shiftService != null) {
            BizShift shift = shiftService.getById(s.getShiftId());
            vo.setShiftName(shift == null ? null : shift.getShiftName());
        }
        return vo;
    }

    /**
     * 当前登录人所属科室；取不到返回 null（由调用方决定空结果还是报错）
     */
    private Long currentDeptId() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getDeptId() == null) {
            return null;
        }
        return Long.valueOf(user.getDeptId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSource(Long scheduleId, Integer addNum, String reason) {
        BizSchedule schedule = bizScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排班记录不存在");
        }
        if (schedule.getScheduleDate().isBefore(LocalDate.now())) {
            throw new BusinessException("已过期的班次不允许加号");
        }
        if (ScheduleStatusEnum.stopped(schedule.getStatus())) {
            throw new BusinessException("停诊中的班次不允许加号，请先启用");
        }
        // 加号是给医生出诊班加号源：出勤岗压根没有号源池，加了也挂不出去（sql/195）
        if (!StaffTypeEnum.hasSource(schedule.getStaffType())) {
            throw new BusinessException("只有医生出诊排班能加号："
                    + StaffTypeEnum.getText(schedule.getStaffType()) + "岗位是出勤排班，不对外放号");
        }
        int added = schedule.getAddedSource() == null ? 0 : schedule.getAddedSource();
        String stamp = LocalDate.now() + " 加号" + addNum + "（" + reason + "）";
        String newRemark = TextUtil.hasText(schedule.getRemark()) ? schedule.getRemark() + "；" + stamp : stamp;
        BizSchedule update = new BizSchedule();
        update.setId(scheduleId);
        update.setTotalSource((schedule.getTotalSource() == null ? 0 : schedule.getTotalSource()) + addNum);
        update.setAvailableSource((schedule.getAvailableSource() == null ? 0 : schedule.getAvailableSource()) + addNum);
        update.setAddedSource(added + addNum);
        update.setRemark(newRemark);
        boolean ok = bizScheduleMapper.updateById(update) > 0;
        if (ok) {
            // 加号摊到段（余数给前面的段），Σ段写回主表——号源事实在段上，主表只是汇总
            scheduleSlotService.spreadAddSource(scheduleId, addNum);
        }
        return ok;
    }
}
