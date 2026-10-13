package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.system.api.service.AppointApi;
import com.his.system.dto.BizClinicSourceBatchItemDTO;
import com.his.system.dto.BizClinicSourceBatchUpsertDTO;
import com.his.system.dto.BizClinicSourceReplicateDTO;
import com.his.system.dto.NurseDutyRow;
import com.his.system.dto.OnDutyQueryDTO;
import com.his.system.dto.ScheduleQueryDTO;
import com.his.system.dto.ScheduleSelectQueryDTO;
import com.his.system.dto.ScheduleUpsertDTO;
import com.his.system.entity.BizClinicSource;
import com.his.system.mapper.BizClinicSourceMapper;
import com.his.system.mapper.BizScheduleMapper;
import com.his.system.service.BizClinicSourceService;
import com.his.system.service.BizClinicSourceSlotService;
import com.his.system.vo.BizClinicSourceBatchResultVO;
import com.his.system.vo.OnDutyStaffVO;
import com.his.system.vo.ScheduleDetailVO;
import com.his.system.vo.ScheduleSelectListVO;
import com.his.system.vo.StopImpactItemVO;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.ShiftCoverUtil;
import com.his.common.util.TextUtil;
import com.his.system.dto.StaffScheduleUpsertDTO;
import com.his.system.entity.BizShift;
import com.his.system.entity.BizSchedule;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.DeptScopeService;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 挂号服务实现
 */
@Service
@RequiredArgsConstructor
public class BizClinicSourceServiceImpl extends ServiceImpl<BizClinicSourceMapper, BizClinicSource> implements BizClinicSourceService {

    /** 复刻的默认后移天数（一周） */
    private static final int REPLICATE_DEFAULT_OFFSET = 7;

    private final DeptScopeService deptScopeService;


    private final BizClinicSourceSlotService bizScheduleSlotService;

    private final SysClinicRoomService sysClinicRoomService;

    private final ShiftService shiftService;

    private final StaffScheduleService staffScheduleService;

    private final BizClinicSourceMapper clinicSourceMapper;

    private final BizScheduleMapper bizScheduleMapper;

    private final AppointApi appointApi;



    @Override
    public List<BizClinicSource> listPage(ScheduleQueryDTO queryDTO) {
        LambdaQueryWrapper<BizClinicSource> wrapper = new LambdaQueryWrapper<>();
        Long scopedDeptId = deptScopeService.resolveDeptId(queryDTO.getDeptId());
        if (scopedDeptId != null) {
            wrapper.eq(BizClinicSource::getDeptId, scopedDeptId);
        } else if (deptScopeService.isScoped()) {
            wrapper.in(BizClinicSource::getDeptId, deptScopeService.allowedDeptIds());
        }
        wrapper.eq(queryDTO.getStaffType() != null, BizClinicSource::getStaffType, queryDTO.getStaffType())
                .eq(queryDTO.getDoctorId() != null, BizClinicSource::getDoctorId, queryDTO.getDoctorId())
                .ge(queryDTO.getStartDate() != null, BizClinicSource::getScheduleDate, queryDTO.getStartDate())
                .le(queryDTO.getEndDate() != null, BizClinicSource::getScheduleDate, queryDTO.getEndDate());
        wrapper.last("ORDER BY schedule_date ASC, start_time ASC, doctor_id ASC, id ASC");
        return clinicSourceMapper.selectList(wrapper);
    }

    @Override
    public List<BizClinicSource> scheduleSelectList(ScheduleSelectQueryDTO scheduleQueryDTO) {
        return clinicSourceMapper.selectList(buildScheduleSelectWrapper(scheduleQueryDTO, true));
    }

    @Override
    public List<BizClinicSource> scheduleSelectListUnbounded(ScheduleSelectQueryDTO scheduleQueryDTO) {
        return clinicSourceMapper.selectList(buildScheduleSelectWrapper(scheduleQueryDTO, false));
    }

    /**
     * 选号查询的 SQL 收口（员工端套科室权限，患者端按所选条件直接取数）。
     *
     * @param applyDeptScope true = 员工端，套 {@link DeptScopeService} 的科室收口；
     *                       false = 患者端，跳过院内科室权限，仅按查询参数里的 deptId/doctorId/visitDate 过滤
     */
    private LambdaQueryWrapper<BizClinicSource> buildScheduleSelectWrapper(ScheduleSelectQueryDTO scheduleQueryDTO, boolean applyDeptScope) {
        LambdaQueryWrapper<BizClinicSource> wrapper = new LambdaQueryWrapper<>();
        if (applyDeptScope) {
            Long scopedDeptId = deptScopeService.resolveDeptId(scheduleQueryDTO.getDeptId());
            if (scopedDeptId != null) {
                wrapper.eq(BizClinicSource::getDeptId, scopedDeptId);
            } else if (deptScopeService.isScoped()) {
                wrapper.in(BizClinicSource::getDeptId, deptScopeService.allowedDeptIds());
            }
        } else if (scheduleQueryDTO.getDeptId() != null) {
            wrapper.eq(BizClinicSource::getDeptId, scheduleQueryDTO.getDeptId());
        }
        wrapper.eq(BizClinicSource::getStaffType, StaffTypeEnum.DOCTOR.getCode())
                .eq(scheduleQueryDTO.getVisitDate() != null,
                        BizClinicSource::getScheduleDate, scheduleQueryDTO.getVisitDate())
                .eq(scheduleQueryDTO.getDoctorId() != null,
                        BizClinicSource::getDoctorId, scheduleQueryDTO.getDoctorId())
                .gt(BizClinicSource::getAvailableSource, 0)
                .eq(BizClinicSource::getStatus, ScheduleStatusEnum.NORMAL.getCode())
                .orderByAsc(BizClinicSource::getStartTime);
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSchedule(BizClinicSource schedule) {
        applyShift(schedule);
        assertNotPast(schedule.getScheduleDate(), "新增排班");
        bindCoreSchedule(schedule, StaffScheduleSourceEnum.MANUAL);
        applyStaffType(schedule);
        checkRoomRequired(schedule);
        checkScheduleOverlap(schedule, null);

        initNewScheduleRow(schedule);
        boolean ok = clinicSourceMapper.insert(schedule) > 0;
        if (ok && StaffTypeEnum.hasSource(schedule.getStaffType())) {
            bizScheduleSlotService.generateSlots(schedule.getId(), schedule.getStartTime(), schedule.getEndTime(),
                    schedule.getTotalSource(), schedule.getAppointmentSource());
        }
        return ok;
    }

    /**
     * 新建号源行的初值收口（手工新增与模板生成共用）。
     *
     * <p>本表除 del_flag 外没有任何列默认值，而 MyBatis-Plus 遇到 null 字段会把**整个列从 INSERT 里省掉**，
     * MySQL 严格模式下就报 1364「doesn't have a default value」——连可空列也一样（可空≠可省略）。
     * 所以「这条号源池刚建出来是什么状态」只能在这里给死，不许靠库兜。
     */
    @Override
    public void initNewScheduleRow(BizClinicSource schedule) {
        int total = NumUtil.orZero(schedule.getTotalSource());
        schedule.setTotalSource(total);
        schedule.setUsedSource(0);
        schedule.setAddedSource(0);
        schedule.setUsedAppointmentSource(0);
        schedule.setAvailableSource(total);
        schedule.setAppointmentSource(NumUtil.orZero(schedule.getAppointmentSource()));
        schedule.setIsAppointment(NumUtil.orZero(schedule.getIsAppointment()));
        schedule.setConsultStatus(ConsultStatusEnum.NOT_STARTED.getCode());
        if (schedule.getStatus() == null) {
            schedule.setStatus(ScheduleStatusEnum.NORMAL.getCode());
        }
        schedule.setIsExpert(NumUtil.orZero(schedule.getIsExpert()));
        schedule.setRegistFee(NumUtil.orZero(schedule.getRegistFee()));
        schedule.setDiagnosisFee(NumUtil.orZero(schedule.getDiagnosisFee()));
        schedule.setExpertFee(NumUtil.orZero(schedule.getExpertFee()));
    }

    @Override
    public void bindCoreSchedule(BizClinicSource schedule, StaffScheduleSourceEnum source) {
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
        BizSchedule core = staffScheduleService.ensureForClinic(dto, source);
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
        Long holding = clinicSourceMapper.selectCount(new LambdaQueryWrapper<BizClinicSource>()
                .eq(BizClinicSource::getStaffScheduleId, staffScheduleId));
        if (holding == null || holding == 0) {
            staffScheduleService.deleteById(staffScheduleId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSchedule(BizClinicSource schedule) {
        BizClinicSource existing = clinicSourceMapper.selectById(schedule.getId());
        if (existing == null) {
            throw new BusinessException("排班记录不存在");
        }
        assertNotPast(existing.getScheduleDate(), "修改");
        assertNotPast(schedule.getScheduleDate(), "修改");
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
        boolean ok = clinicSourceMapper.updateById(schedule) > 0;
        if (ok) {
            if (StaffTypeEnum.hasSource(schedule.getStaffType())) {
                bizScheduleSlotService.regenerateForSchedule(schedule, schedule.getTotalSource(), schedule.getAppointmentSource());
            } else {
                bizScheduleSlotService.physicalDeleteByScheduleId(schedule.getId());
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
    private void syncRoomToTodayWorklist(BizClinicSource schedule) {
        LocalDate today = LocalDate.now();
        if (schedule.getScheduleDate() != null && !schedule.getScheduleDate().equals(today)) {
            return;
        }
        Long roomId = schedule.getRoomId();
        String roomName = schedule.getRoomName();
        if (roomId != null && !TextUtil.hasText(roomName)) {
            var room = sysClinicRoomService.getById(roomId);
            if (room != null) {
                roomName = room.getName();
                schedule.setRoomName(roomName);
                clinicSourceMapper.updateById(schedule);
            }
        }
        appointApi.updateQueueRoomBySchedule(schedule.getId(), today, roomId, roomName);
        appointApi.updateAppointRoomBySchedule(schedule.getId(), today, roomId, roomName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSchedule(Long id) {
        BizClinicSource existing = clinicSourceMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("排班记录不存在");
        }
        if (existing.getUsedSource() > 0) {
            throw new BusinessException("该排班已有挂号记录，无法删除");
        }
        assertNotPast(existing.getScheduleDate(), "删除");
        bizScheduleSlotService.physicalDeleteByScheduleId(id);
        boolean ok = clinicSourceMapper.physicalDeleteById(id) > 0;
        if (ok) {
            releaseCoreSchedule(existing.getStaffScheduleId());
        }
        return ok;
    }

    /**
     * 校验：启用态的排班必须排诊室。
     */
    private void checkRoomRequired(BizClinicSource schedule) {
        boolean enabled = !ScheduleStatusEnum.stopped(schedule.getStatus());
        // B类（条件必填）：必填性取决于同一请求里的状态与岗位类别，声明式注解做不到一刀切
        if (enabled && StaffTypeEnum.hasSource(schedule.getStaffType())
                && schedule.getRoomId() == null && !TextUtil.hasText(schedule.getRoomName())) {
            throw new BusinessException("启用状态的医生排班必须指定诊室：分诊台按诊室编号发号，缺诊室患者找不到房间");
        }
    }

    /**
     * 岗位类别收口
     */
    private void applyStaffType(BizClinicSource schedule) {
        StaffTypeEnum.assertValid(schedule.getStaffType());
        if (!StaffTypeEnum.isDoctor(schedule.getStaffType())) {
            throw new BusinessException("门诊号源只排医生：「" + schedule.getDoctorName() + "」的岗位类别是「"
                    + StaffTypeEnum.getText(schedule.getStaffType())
                    + "」，他的出勤请到「岗位出勤」登记（这里承接医生出诊与号源，"
                    + "排进来不会挂号源和诊室；若他确实是出诊医生，请先到员工档案修正岗位）");
        }
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
     */
    private void applyShift(BizClinicSource schedule) {
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
        BizClinicSource schedule = convertToScheduleEntity(upsertDTO);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizClinicSourceBatchResultVO batchUpsert(BizClinicSourceBatchUpsertDTO batchDTO) {
        List<BizClinicSourceBatchItemDTO> items = batchDTO.getItems();
        Set<Long> distinctDoctorIds = new HashSet<>();
        for (BizClinicSourceBatchItemDTO item : items) {
            if (!distinctDoctorIds.add(item.getDoctorId())) {
                throw new BusinessException("排班人员重复选择：" + item.getDoctorName());
            }
        }
        int created = 0;
        for (BizClinicSourceBatchItemDTO item : items) {
            for (LocalDate date : batchDTO.getScheduleDates()) {
                BizClinicSource schedule = toBatchSchedule(batchDTO, item, date);
                try {
                    addSchedule(schedule);
                } catch (BusinessException | DuplicateKeyException e) {
                    // 整批已经回滚，所以报错必须点名是「哪个人、哪一天」，只说「时间冲突」没人知道该改哪一条
                    String reason = e instanceof DuplicateKeyException ? "该医生在此时间段已有排班，时间冲突" : e.getMessage();
                    throw new BusinessException(item.getDoctorName() + " " + date + "：" + reason);
                }
                created++;
            }
        }
        BizClinicSourceBatchResultVO vo = new BizClinicSourceBatchResultVO();
        vo.setCreated(created);
        vo.setSkipped(0);
        vo.setMessage("成功创建 " + created + " 条排班（" + items.size() + " 位"
                + StaffTypeEnum.getText(batchDTO.getStaffType()) + "人员 × " + batchDTO.getScheduleDates().size() + " 天）");
        return vo;
    }

    /**
     * 批量表头 + 人员明细 + 单个日期 → 一条号源行。时间段不取入参，由 addSchedule 按班次覆盖。
     */
    private BizClinicSource toBatchSchedule(BizClinicSourceBatchUpsertDTO batch, BizClinicSourceBatchItemDTO item, LocalDate date) {
        BizClinicSource schedule = new BizClinicSource();
        schedule.setDeptId(batch.getDeptId());
        schedule.setDeptName(batch.getDeptName());
        schedule.setStaffType(batch.getStaffType());
        schedule.setShiftId(batch.getShiftId());
        schedule.setStatus(batch.getStatus());
        schedule.setScheduleDate(date);
        schedule.setDoctorId(item.getDoctorId());
        schedule.setDoctorName(item.getDoctorName());
        schedule.setRoomId(item.getRoomId());
        schedule.setRoomName(item.getRoomName());
        schedule.setTotalSource(item.getTotalSource());
        schedule.setRegistFee(item.getRegistFee());
        schedule.setDiagnosisFee(item.getDiagnosisFee());
        schedule.setIsExpert(item.getIsExpert());
        schedule.setExpertFee(item.getExpertFee());
        schedule.setIsAppointment(item.getIsAppointment());
        schedule.setAppointmentSource(item.getAppointmentSource());
        return schedule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizClinicSourceBatchResultVO replicate(BizClinicSourceReplicateDTO dto) {
        int offset = dto.getDayOffset() == null ? REPLICATE_DEFAULT_OFFSET : dto.getDayOffset();
        LocalDate today = LocalDate.now();
        if (dto.getEndDate().isBefore(today)) {
            throw new BusinessException("复刻区间 " + dto.getStartDate() + " ~ " + dto.getEndDate()
                    + " 已全部过去，历史排班只读（以门诊日志为准）");
        }
        ScheduleQueryDTO sourceQuery = new ScheduleQueryDTO();
        sourceQuery.setDeptId(dto.getDeptId());
        sourceQuery.setStartDate(dto.getStartDate());
        sourceQuery.setEndDate(dto.getEndDate());
        sourceQuery.setStaffType(dto.getStaffType());
        List<BizClinicSource> sources = listPage(sourceQuery).stream()
                .filter(s -> !ScheduleStatusEnum.stopped(s.getStatus()))
                .toList();

        // 目标区间的既有排班一次捞回来按「人×日」分组：逐条查重就是 N 次查询，而复刻是整周操作
        List<BizClinicSource> targets = clinicSourceMapper.selectList(new LambdaQueryWrapper<BizClinicSource>()
                .ge(BizClinicSource::getScheduleDate, dto.getStartDate().plusDays(offset))
                .le(BizClinicSource::getScheduleDate, dto.getEndDate().plusDays(offset)));
        Map<Long, List<BizClinicSource>> targetByDoctor = new LinkedHashMap<>();
        for (BizClinicSource t : targets) {
            targetByDoctor.computeIfAbsent(t.getDoctorId(), k -> new ArrayList<>()).add(t);
        }

        int created = 0;
        int skippedPast = 0;
        int skippedExist = 0;
        for (BizClinicSource s : sources) {
            LocalDate targetDate = s.getScheduleDate().plusDays(offset);
            if (targetDate.isBefore(today)) {
                skippedPast++;
                continue;
            }
            boolean occupied = targetByDoctor.getOrDefault(s.getDoctorId(), List.of()).stream()
                    .anyMatch(t -> targetDate.equals(t.getScheduleDate())
                            && overlaps(s.getStartTime(), s.getEndTime(), t.getStartTime(), t.getEndTime()));
            if (occupied) {
                skippedExist++;
                continue;
            }
            BizClinicSource copy = toReplicateSchedule(s, targetDate);
            try {
                addSchedule(copy);
            } catch (BusinessException | DuplicateKeyException e) {
                String reason = e instanceof DuplicateKeyException ? "该医生在此时间段已有排班，时间冲突" : e.getMessage();
                throw new BusinessException(s.getDoctorName() + " " + targetDate + "：" + reason);
            }
            created++;
        }
        BizClinicSourceBatchResultVO vo = new BizClinicSourceBatchResultVO();
        vo.setCreated(created);
        vo.setSkipped(skippedPast + skippedExist);
        vo.setMessage("复刻到 " + dto.getStartDate().plusDays(offset) + " ~ " + dto.getEndDate().plusDays(offset)
                + "：成功 " + created + " 条"
                + (skippedExist > 0 ? "，跳过 " + skippedExist + " 条（目标时段已有排班）" : "")
                + (skippedPast > 0 ? "，" + skippedPast + " 条目标日期已过未复制" : ""));
        return vo;
    }

    /**
     * 复刻行只带「排的是哪个班」的字段：号源池、费用、状态按源行原样搬，
     * used/added/remark 这类运行期痕迹必须清零，否则下一周的班次一建出来就带着上周的挂号数。
     */
    private BizClinicSource toReplicateSchedule(BizClinicSource source, LocalDate targetDate) {
        BizClinicSource copy = new BizClinicSource();
        copy.setDeptId(source.getDeptId());
        copy.setDeptName(source.getDeptName());
        copy.setDoctorId(source.getDoctorId());
        copy.setDoctorName(source.getDoctorName());
        copy.setStaffType(source.getStaffType());
        copy.setScheduleDate(targetDate);
        copy.setShiftId(source.getShiftId());
        copy.setStartTime(source.getStartTime());
        copy.setEndTime(source.getEndTime());
        copy.setTotalSource(source.getTotalSource());
        copy.setRegistFee(source.getRegistFee());
        copy.setDiagnosisFee(source.getDiagnosisFee());
        copy.setIsExpert(source.getIsExpert());
        copy.setExpertFee(source.getExpertFee());
        copy.setIsAppointment(source.getIsAppointment());
        copy.setAppointmentSource(source.getAppointmentSource());
        copy.setRoomId(source.getRoomId());
        copy.setRoomName(source.getRoomName());
        copy.setStatus(ScheduleStatusEnum.NORMAL.getCode());
        return copy;
    }

    private boolean overlaps(String startA, String endA, String startB, String endB) {
        if (startA == null || endA == null || startB == null || endB == null) {
            return false;
        }
        return startA.compareTo(endB) < 0 && startB.compareTo(endA) < 0;
    }

    private List<ScheduleDetailVO> toVOList(List<BizClinicSource> list) {
        List<ScheduleDetailVO> voList = list.stream().map(this::convertToScheduleVO).toList();
        fillShiftDisplay(voList);
        return voList;
    }

    private BizClinicSource convertToScheduleEntity(ScheduleUpsertDTO upsertDTO) {
        BizClinicSource entity = new BizClinicSource();
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

    private ScheduleDetailVO convertToScheduleVO(BizClinicSource entity) {
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
        vo.setRoomId(entity.getRoomId());
        vo.setRoomName(entity.getRoomName());
        return vo;
    }

    /**
     * 排班日期不得早于今天：已过日期的班次一律只读。
     */
    private void assertNotPast(LocalDate date, String action) {
        if (date == null || date.compareTo(LocalDate.now()) >= 0) {
            return;
        }
        throw new BusinessException("排班日期 " + date + " 已过，不允许" + action + "（历史排班以门诊日志为准）");
    }

    /**
     * 校验同一医生同一天的时间段是否冲突
     */
    private void checkScheduleOverlap(BizClinicSource schedule, Long excludeId) {
        if (schedule.getDoctorId() == null || schedule.getScheduleDate() == null
                || !TextUtil.hasText(schedule.getStartTime()) || !TextUtil.hasText(schedule.getEndTime())) {
            return;
        }
        LambdaQueryWrapper<BizClinicSource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizClinicSource::getDoctorId, schedule.getDoctorId())
                .eq(BizClinicSource::getScheduleDate, schedule.getScheduleDate())
                .ne(excludeId != null, BizClinicSource::getId, excludeId);
        List<BizClinicSource> existing = clinicSourceMapper.selectList(wrapper);
        for (BizClinicSource s : existing) {
            if (overlaps(s.getStartTime(), s.getEndTime(), schedule.getStartTime(), schedule.getEndTime())) {
                throw new BusinessException("该医生在此时间段已有排班，时间冲突");
            }
        }
    }

    @Override
    public List<BizClinicSource> getTodaySchedule(Long deptId) {
        LambdaQueryWrapper<BizClinicSource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizClinicSource::getDeptId, deptId)
                .eq(BizClinicSource::getScheduleDate, LocalDate.now())
                .eq(BizClinicSource::getStaffType, StaffTypeEnum.DOCTOR.getCode())
                .eq(BizClinicSource::getStatus, ScheduleStatusEnum.NORMAL.getCode())
                .orderByAsc(BizClinicSource::getStartTime);
        return clinicSourceMapper.selectList(wrapper);
    }

    @Override
    public boolean updateConsultStatus(Long scheduleId, Integer consultStatus) {
        // 值域在服务端收口：码值只允许 0/1/2，越界值（如 99）不能落库
        if (ConsultStatusEnum.fromCode(consultStatus) == null) {
            throw new BusinessException("就诊状态只允许 " + ConsultStatusEnum.whitelistText());
        }
        BizClinicSource schedule = clinicSourceMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排班记录不存在");
        }
        schedule.setConsultStatus(consultStatus);
        return clinicSourceMapper.updateById(schedule) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long scheduleId, Integer status) {
        BizClinicSource old = clinicSourceMapper.selectById(scheduleId);
        if (old == null) {
            throw new BusinessException("排班记录不存在");
        }
        assertNotPast(old.getScheduleDate(), ScheduleStatusEnum.stopped(status) ? "停诊" : "启用");
        BizClinicSource schedule = new BizClinicSource();
        schedule.setId(scheduleId);
        schedule.setStatus(status);
        boolean ok = clinicSourceMapper.updateById(schedule) > 0;
        if (ok) {
            // 段状态随主表联动：停诊 → 段全停（扣减 SQL 带 status=1，停用段不可再挂）；启用 → 段恢复
            bizScheduleSlotService.syncStatusToSlots(scheduleId, status);
        }
        return ok;
    }

    @Override
    public List<StopImpactItemVO> stopImpact(Long scheduleId) {
        if (clinicSourceMapper.selectById(scheduleId) == null) {
            throw new BusinessException("排班记录不存在");
        }
        return appointApi.listRegsBySchedule(scheduleId);
    }

    @Override
    public String batchCancel(List<Long> registIds, String reason) {
        String cancelReason = TextUtil.hasText(reason) ? reason : "班次停诊，批量退号";
        int ok = 0;
        for (Long id : registIds) {
            try {
                if (appointApi.cancelRegist(id, cancelReason)) {
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
        Long deptId = queryDTO.getDeptId() != null ? queryDTO.getDeptId() : currentDeptId();
        if (deptId == null) {
            return List.of();
        }
        LocalDate date = queryDTO.getDate() != null ? queryDTO.getDate() : LocalDate.now();
        LocalTime moment = queryDTO.getMoment() == null
                ? LocalTime.now()
                : ShiftCoverUtil.parseShiftTime(queryDTO.getMoment());
        boolean onlyOnDuty = !Boolean.FALSE.equals(queryDTO.getOnDutyOnly());

        // 护士权威来源是护理排班表，必须并入（否则护士在分诊/在岗列表里看不到）
        List<DutyRow> rows = collectDutyRows(deptId, date, queryDTO.getStaffType());
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
        List<DutyRow> rows = collectDutyRows(deptId, today, staffType);
        if (rows.isEmpty()) {
            return null;
        }
        List<DutyRow> onDuty = rows.stream()
                .filter(r -> ShiftCoverUtil.covers(now, r.startTime, r.endTime))
                .toList();
        if (!onDuty.isEmpty()) {
            for (DutyRow r : onDuty) {
                if (preferEmpId != null && Objects.equals(r.staffId, preferEmpId)) {
                    return toOnDutyVO(r, now);
                }
            }
            DutyRow latest = onDuty.stream()
                    .max(Comparator.comparing(r -> ShiftCoverUtil.parseShiftTime(r.startTime),
                            Comparator.nullsFirst(Comparator.naturalOrder())))
                    .orElse(null);
            return latest == null ? null : toOnDutyVO(latest, now);
        }
        DutyRow earliest = rows.stream()
                .min(Comparator.comparing(r -> ShiftCoverUtil.parseShiftTime(r.startTime),
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        return earliest == null ? null : toOnDutyVO(earliest, now);
    }

    /**
     * 当日该科室门诊号源表里的有效出勤行（默认全岗位），转成统一的 DutyRow。
     */
    private List<DutyRow> loadDaySchedule(Long deptId, LocalDate date, Integer staffType) {
        LambdaQueryWrapper<BizClinicSource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizClinicSource::getDeptId, deptId)
                .eq(BizClinicSource::getScheduleDate, date)
                .eq(BizClinicSource::getStatus, ScheduleStatusEnum.NORMAL.getCode())
                .isNotNull(BizClinicSource::getDoctorId);
        if (staffType != null) {
            wrapper.eq(BizClinicSource::getStaffType, staffType);
        }
        wrapper.orderByAsc(BizClinicSource::getStartTime);
        List<BizClinicSource> src = clinicSourceMapper.selectList(wrapper);
        List<DutyRow> out = new ArrayList<>(src.size());
        for (BizClinicSource s : src) {
            out.add(toDutyRow(s));
        }
        return out;
    }

    private DutyRow toDutyRow(BizClinicSource s) {
        DutyRow r = new DutyRow();
        r.scheduleId = s.getId();
        r.staffId = s.getDoctorId();
        r.staffName = s.getDoctorName();
        r.staffType = s.getStaffType();
        r.deptId = s.getDeptId();
        r.deptName = s.getDeptName();
        r.shiftId = s.getShiftId();
        r.startTime = s.getStartTime();
        r.endTime = s.getEndTime();
        r.status = s.getStatus();
        r.roomId = s.getRoomId();
        r.roomName = s.getRoomName();
        return r;
    }

    /**
     * 当日该科室护理排班表里的在岗护士（权威来源），转成统一的 DutyRow。
     */
    private List<DutyRow> loadNurseDaySchedule(Long deptId, LocalDate date) {
        List<NurseDutyRow> ns = bizScheduleMapper.listOnDuty(deptId, date);
        if (ns == null || ns.isEmpty()) {
            return List.of();
        }
        List<DutyRow> out = new ArrayList<>(ns.size());
        for (NurseDutyRow n : ns) {
            DutyRow r = new DutyRow();
            r.scheduleId = n.getId();
            r.staffId = n.getEmployeeId();
            r.staffName = n.getNurseName();
            r.staffType = StaffTypeEnum.NURSE.getCode();
            r.deptId = n.getDeptId();
            r.deptName = n.getDeptName();
            r.shiftId = n.getShiftId();
            r.startTime = n.getStartTime();
            r.endTime = n.getEndTime();
            r.status = n.getScheduleStatus();
            out.add(r);
        }
        return out;
    }

    /**
     * 合并门诊号源表与护理排班表，得到某科室当日全部候选用人在岗记录。
     */
    private List<DutyRow> collectDutyRows(Long deptId, LocalDate date, Integer staffType) {
        List<DutyRow> rows = loadDaySchedule(deptId, date, staffType);
        boolean includeNurse = staffType == null || staffType == StaffTypeEnum.NURSE.getCode();
        if (!includeNurse) {
            return rows;
        }
        LinkedHashMap<Long, DutyRow> byEmp = new LinkedHashMap<>();
        for (DutyRow r : rows) {
            if (r.staffId != null) {
                byEmp.putIfAbsent(r.staffId, r);
            }
        }
        for (DutyRow n : loadNurseDaySchedule(deptId, date)) {
            if (n.staffId != null) {
                byEmp.put(n.staffId, n); // 护理排班表权威：冲突时覆盖
            }
        }
        return new ArrayList<>(byEmp.values());
    }

    private OnDutyStaffVO toOnDutyVO(DutyRow r, LocalTime moment) {
        OnDutyStaffVO vo = new OnDutyStaffVO();
        vo.setScheduleId(r.scheduleId);
        vo.setStaffId(r.staffId);
        vo.setStaffName(r.staffName);
        vo.setStaffType(r.staffType);
        vo.setStaffTypeName(StaffTypeEnum.getText(r.staffType));
        vo.setDeptId(r.deptId);
        vo.setDeptName(r.deptName);
        vo.setShiftId(r.shiftId);
        vo.setStartTime(r.startTime);
        vo.setEndTime(r.endTime);
        vo.setRoomId(r.roomId);
        vo.setRoomName(r.roomName);
        vo.setStatus(r.status);
        vo.setOnDutyNow(ShiftCoverUtil.covers(moment, r.startTime, r.endTime));
        if (r.shiftId != null && shiftService != null) {
            BizShift shift = shiftService.getById(r.shiftId);
            vo.setShiftName(shift == null ? null : shift.getShiftName());
        }
        return vo;
    }

    /**
     * 统一的候选用人在岗行（抹平门诊号源表 / 护理排班表两张源的差异）。
     */
    private static class DutyRow {
        Long scheduleId;
        Long staffId;
        String staffName;
        Integer staffType;
        Long deptId;
        String deptName;
        Long shiftId;
        String startTime;
        String endTime;
        Integer status;
        Long roomId;
        String roomName;
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
        BizClinicSource schedule = clinicSourceMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排班记录不存在");
        }
        if (schedule.getScheduleDate().isBefore(LocalDate.now())) {
            throw new BusinessException("已过期的班次不允许加号");
        }
        if (ScheduleStatusEnum.stopped(schedule.getStatus())) {
            throw new BusinessException("停诊中的班次不允许加号，请先启用");
        }
        // 加号是给医生出诊班加号源：出勤岗压根没有号源池，加了也挂不出去
        if (!StaffTypeEnum.hasSource(schedule.getStaffType())) {
            throw new BusinessException("只有医生出诊排班能加号："
                    + StaffTypeEnum.getText(schedule.getStaffType()) + "岗位是出勤排班，不对外放号");
        }
        int added = schedule.getAddedSource() == null ? 0 : schedule.getAddedSource();
        String stamp = LocalDate.now() + " 加号" + addNum + "（" + reason + "）";
        String newRemark = TextUtil.hasText(schedule.getRemark()) ? schedule.getRemark() + "；" + stamp : stamp;
        BizClinicSource update = new BizClinicSource();
        update.setId(scheduleId);
        update.setTotalSource((schedule.getTotalSource() == null ? 0 : schedule.getTotalSource()) + addNum);
        update.setAvailableSource((schedule.getAvailableSource() == null ? 0 : schedule.getAvailableSource()) + addNum);
        update.setAddedSource(added + addNum);
        update.setRemark(newRemark);
        boolean ok = clinicSourceMapper.updateById(update) > 0;
        if (ok) {
            // 加号摊到段（余数给前面的段），Σ段写回主表——号源事实在段上，主表只是汇总
            bizScheduleSlotService.spreadAddSource(scheduleId, addNum);
        }
        return ok;
    }
}
