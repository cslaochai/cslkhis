package com.his.outpatient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.ShiftCoverUtil;
import com.his.common.util.TextUtil;
import com.his.common.utils.UserUtils;
import com.his.outpatient.dto.*;
import com.his.outpatient.entity.AppointInfo;
import com.his.outpatient.entity.BizClinicSource;
import com.his.outpatient.entity.BizSchedule;
import com.his.outpatient.entity.BizShift;
import com.his.outpatient.mapper.AppointInfoMapper;
import com.his.outpatient.mapper.BizClinicSourceMapper;
import com.his.outpatient.mapper.BizScheduleMapper;
import com.his.outpatient.mapper.QueueMapper;
import com.his.outpatient.service.*;
import com.his.outpatient.vo.*;
import com.his.spi.service.DeptScopeService;
import com.his.spi.service.SystemFacadeService;
import com.his.spi.vo.DeptBriefVO;
import com.his.spi.vo.StopImpactItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 挂号服务实现
 */
@Service
@RequiredArgsConstructor
public class BizClinicSourceServiceImpl extends ServiceImpl<BizClinicSourceMapper, BizClinicSource> implements BizClinicSourceService {

    /**
     * 复刻的默认后移天数（一周）
     */
    private static final int REPLICATE_DEFAULT_OFFSET = 7;
    /**
     * 全局豁免审核角色：系统管理员、门诊办（不绑科室也能审）
     */
    private static final String ROLE_ADMIN = "10012";
    private static final String ROLE_OUTPATIENT_OFFICE = "10035";
    private final DeptScopeService deptScopeService;
    private final BizClinicSourceSlotService bizScheduleSlotService;
    private final ShiftService shiftService;
    private final StaffScheduleService staffScheduleService;
    private final BizClinicSourceMapper clinicSourceMapper;
    private final BizScheduleMapper bizScheduleMapper;
    private final AppointService appointService;
    private final AppointInfoMapper appointInfoMapper;
    private final QueueMapper queueMapper;
    private final ClinicSourceLogService clinicSourceLogService;
    /**
     * 值班白班并入「此刻在岗医生」聚合：门诊结束后仍能找到科室兜底值班人
     */
    private final DutyRosterService dutyRosterService;
    /**
     * 科室主档：取科室指定的门诊/护理排班审核人
     */
    private final SystemFacadeService systemFacadeService;

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
                .in(BizClinicSource::getPublishStatus,
                        PublishStatusEnum.PUBLISHED.getCode(), PublishStatusEnum.CHANGED.getCode())
                .orderByAsc(BizClinicSource::getStartTime);
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSchedule(BizClinicSource schedule) {
        applyShift(schedule);
        assertNotPast(schedule.getScheduleDate(), "新增排班");
        // 新增号源一律草稿态（出号逻辑在 initNewScheduleRow 之后；这里提前给 bindCoreSchedule 读，
        // 让派生的出勤事实跟号源行同一个发布态）
        schedule.setPublishStatus(PublishStatusEnum.DRAFT.getCode());
        bindCoreSchedule(schedule, StaffScheduleSourceEnum.MANUAL);
        applyStaffType(schedule);
        checkRoomRequired(schedule);
        checkScheduleOverlap(schedule, null);

        initNewScheduleRow(schedule);
        boolean ok = clinicSourceMapper.insert(schedule) > 0;
        // 仅生效态（已发布/已变更）才出号：新保存的号源默认草稿，审核发布后才会 generateSlots
        if (ok && StaffTypeEnum.hasSource(schedule.getStaffType())
                && PublishStatusEnum.isEffective(schedule.getPublishStatus())) {
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
        // 新建号源行一律草稿态：排班写完不算生效，要经「提交→审核发布」才出号。
        // 模板批量生成、批量新增、复刻都走这里，保证三条入口初态一致。
        schedule.setPublishStatus(PublishStatusEnum.DRAFT.getCode());
        schedule.setIsExpert(NumUtil.orZero(schedule.getIsExpert()));
        schedule.setRegistFee(NumUtil.orZero(schedule.getRegistFee()));
        schedule.setDiagnosisFee(NumUtil.orZero(schedule.getDiagnosisFee()));
        // 档位为空时兜底普通档（9 元），不留 null：出账与医保对账都要读这一列，
        //    null 会让「这号适用哪档加收」在报表里变成空白，追责时说不清。
        if (ClinicDiagnosisTierEnum.ofCode(schedule.getDiagnosisTier()) == null) {
            schedule.setDiagnosisTier(ClinicDiagnosisTierEnum.GENERAL.getCode());
        }
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
        // 出勤事实跟随号源行的发布态：草稿/待审核的出诊不该算进在岗（在岗聚合只统计 publish_status IN (2,4)）
        dto.setPublishStatus(schedule.getPublishStatus());
        BizSchedule core = staffScheduleService.ensureForClinic(dto, source);
        if (submittedStaffType != null && !Objects.equals(submittedStaffType, core.getStaffType())) {
            throw new BusinessException("「" + core.getEmployeeName() + "」在"
                    + OrgUnitTypeEnum.getLabel(core.getOrgType()) + "「" + core.getDeptName()
                    + "」的岗位类别是" + StaffTypeEnum.getLabel(core.getStaffType())
                    + "，不是" + StaffTypeEnum.getLabel(submittedStaffType) + "：请先修正这个人的岗位配置，排班表不接受手填类别");
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
        // 发布态流转（必须排在 bindCoreSchedule 之前：派生的出勤事实跟随号源行的发布态）
        // 草稿/待审核改单保持原态（不出号）；已发布/已变更或调班中改单 = 完成调班 → 已变更（直接生效、重新出号，不二次审核）
        Integer oldPublish = existing.getPublishStatus();
        Integer newPublish = PublishStatusEnum.ADJUSTING.getCode().equals(oldPublish)
                || PublishStatusEnum.isEffective(oldPublish)
                ? PublishStatusEnum.CHANGED.getCode() : oldPublish;
        schedule.setPublishStatus(newPublish);
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
            // 仅生效态出号/保留号；非生效态（草稿/待审核/调班中）清掉任何残留号段，避免患者侧漏出
            if (StaffTypeEnum.hasSource(schedule.getStaffType())
                    && PublishStatusEnum.isEffective(newPublish)) {
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
            var room = systemFacadeService.findClinicRoom(roomId);
            if (room != null) {
                roomName = room.getName();
                schedule.setRoomName(roomName);
                clinicSourceMapper.updateById(schedule);
            }
        }
        queueMapper.updateRoomBySchedule(schedule.getId(), today, roomId, roomName);
        appointInfoMapper.updateRoomBySchedule(schedule.getId(), today, roomId, roomName);
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
            clinicSourceLogService.record(id, ClinicSourceChangeTypeEnum.DELETE, null,
                    existing.getDoctorName() + " " + existing.getScheduleDate() + " "
                            + existing.getStartTime() + "-" + existing.getEndTime(), existing);
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
                    + StaffTypeEnum.getLabel(schedule.getStaffType())
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
        schedule.setDiagnosisTier(ClinicDiagnosisTierEnum.GENERAL.getCode());
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
            // 留痕：单条新增/修改在此落账；批量与复刻在各自循环里逐条落账（快照随行，删除/改期后仍可读）
            clinicSourceLogService.record(schedule.getId(),
                    upsertDTO.getId() == null ? ClinicSourceChangeTypeEnum.CREATE : ClinicSourceChangeTypeEnum.UPDATE,
                    null, schedule.getDoctorName() + " " + schedule.getScheduleDate() + " "
                            + schedule.getStartTime() + "-" + schedule.getEndTime(), schedule);
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
                clinicSourceLogService.record(schedule.getId(), ClinicSourceChangeTypeEnum.CREATE, null,
                        "批量新增：" + item.getDoctorName() + " " + date + " "
                                + schedule.getStartTime() + "-" + schedule.getEndTime(), schedule);
                created++;
            }
        }
        BizClinicSourceBatchResultVO vo = new BizClinicSourceBatchResultVO();
        vo.setCreated(created);
        vo.setSkipped(0);
        vo.setMessage("成功创建 " + created + " 条排班（" + items.size() + " 位"
                + StaffTypeEnum.getLabel(batchDTO.getStaffType()) + "人员 × " + batchDTO.getScheduleDates().size() + " 天）");
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
        schedule.setDiagnosisTier(item.getDiagnosisTier());
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
            clinicSourceLogService.record(copy.getId(), ClinicSourceChangeTypeEnum.REPLICATE, null,
                    "复刻：" + copy.getDoctorName() + " " + targetDate + " "
                            + copy.getStartTime() + "-" + copy.getEndTime(), copy);
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
        copy.setDiagnosisTier(source.getDiagnosisTier());
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
        entity.setDiagnosisTier(upsertDTO.getDiagnosisTier());
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
        vo.setStaffTypeName(StaffTypeEnum.getLabel(entity.getStaffType()));
        vo.setScheduleDate(entity.getScheduleDate());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setShiftId(entity.getShiftId());
        vo.setTotalSource(entity.getTotalSource());
        vo.setUsedSource(entity.getUsedSource());
        vo.setAvailableSource(entity.getAvailableSource());
        vo.setRegistFee(entity.getRegistFee());
        vo.setDiagnosisFee(entity.getDiagnosisFee());
        vo.setDiagnosisTier(entity.getDiagnosisTier());
        vo.setIsExpert(entity.getIsExpert());
        vo.setExpertFee(entity.getExpertFee());
        vo.setIsAppointment(entity.getIsAppointment());
        vo.setAppointmentSource(entity.getAppointmentSource());
        vo.setUsedAppointmentSource(entity.getUsedAppointmentSource());
        vo.setStatus(entity.getStatus());
        vo.setPublishStatus(entity.getPublishStatus());
        vo.setPublishStatusText(PublishStatusEnum.getLabel(entity.getPublishStatus()));
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
                .in(BizClinicSource::getPublishStatus,
                        PublishStatusEnum.PUBLISHED.getCode(), PublishStatusEnum.CHANGED.getCode())
                .orderByAsc(BizClinicSource::getStartTime);
        return clinicSourceMapper.selectList(wrapper);
    }

    @Override
    public boolean updateConsultStatus(Long scheduleId, Integer consultStatus) {
        // 值域在服务端收口：码值只允许 0/1/2，越界值（如 99）不能落库
        if (ConsultStatusEnum.fromCode(consultStatus) == null) {
            throw new BusinessException("就诊状态取值不合法");
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
            boolean stopping = ScheduleStatusEnum.stopped(status);
            clinicSourceLogService.record(scheduleId,
                    stopping ? ClinicSourceChangeTypeEnum.STOP : ClinicSourceChangeTypeEnum.ENABLE,
                    null, old.getDoctorName() + " " + old.getScheduleDate()
                            + "（" + (stopping ? "停诊" : "启用") + "）", old);
        }
        return ok;
    }

    @Override
    public List<StopImpactItemVO> stopImpact(Long scheduleId) {
        if (clinicSourceMapper.selectById(scheduleId) == null) {
            throw new BusinessException("排班记录不存在");
        }
        return listRegsBySchedule(scheduleId);
    }

    @Override
    public String batchCancel(Long scheduleId, List<Long> registIds, String reason) {
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
        if (ok > 0 && scheduleId != null) {
            clinicSourceLogService.record(scheduleId, ClinicSourceChangeTypeEnum.BATCH_CANCEL, ok, cancelReason, null);
        }
        return "批量退号完成：成功 " + ok + " 条 / 共 " + registIds.size() + " 条"
                + (ok < registIds.size()
                ? "（失败多为：已缴费需先到收费处退费；或已接诊/已就诊——诊疗已发生，号源锁死）"
                : "");
    }

    private List<StopImpactItemVO> listRegsBySchedule(Long scheduleId) {
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

    // ===== G-15 发布态流转（提交→审核→发布 / 调班 / 撤回）=====

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        BizClinicSource existing = clinicSourceMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("排班记录不存在");
        }
        assertNotPast(existing.getScheduleDate(), "提交审核");
        doSubmit(existing);
    }

    /**
     * 草稿 → 待审核（单条与整周提交共用）
     */
    private void doSubmit(BizClinicSource existing) {
        if (!PublishStatusEnum.DRAFT.getCode().equals(existing.getPublishStatus())) {
            throw new BusinessException("只有草稿状态的排班可以提交审核");
        }
        BizClinicSource update = new BizClinicSource();
        update.setId(existing.getId());
        update.setPublishStatus(PublishStatusEnum.PENDING.getCode());
        clinicSourceMapper.updateById(update);
        clinicSourceLogService.record(existing.getId(), ClinicSourceChangeTypeEnum.SUBMIT, null,
                existing.getDoctorName() + " " + existing.getScheduleDate() + " 提交审核", existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitWeek(ScheduleQueryDTO query) {
        LocalDate today = LocalDate.now();
        List<BizClinicSource> drafts = listPage(query).stream()
                .filter(s -> PublishStatusEnum.DRAFT.getCode().equals(s.getPublishStatus()))
                // 历史班次跳过（不整批回滚），与单条 assertNotPast 同口径
                .filter(s -> s.getScheduleDate() != null && !s.getScheduleDate().isBefore(today))
                .toList();
        if (drafts.isEmpty()) {
            throw new BusinessException("区间内没有待提交的草稿排班（待审核/已发布/历史班次不会提交）");
        }
        for (BizClinicSource s : drafts) {
            doSubmit(s);
        }
        return drafts.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPublish(Long id) {
        BizClinicSource existing = clinicSourceMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("排班记录不存在");
        }
        assertNotPast(existing.getScheduleDate(), "审核发布");
        doAudit(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPublishBatch(List<Long> ids) {
        for (Long id : ids) {
            auditPublish(id);
        }
    }

    /**
     * 待审核 → 已发布（单条与整周发布共用）：补号段 + 同步出勤事实 + 留痕
     */
    private void doAudit(BizClinicSource existing) {
        if (!PublishStatusEnum.PENDING.getCode().equals(existing.getPublishStatus())) {
            throw new BusinessException("只有待审核状态的排班可以审核发布");
        }
        assertOutpatientAuditor(existing);
        BizClinicSource update = new BizClinicSource();
        update.setId(existing.getId());
        update.setPublishStatus(PublishStatusEnum.PUBLISHED.getCode());
        clinicSourceMapper.updateById(update);
        // 审核发布后正式出号（仅医生号源；草稿期间不生成号段，这里一次性补齐）
        if (StaffTypeEnum.hasSource(existing.getStaffType())) {
            bizScheduleSlotService.regenerateForSchedule(existing, existing.getTotalSource(), existing.getAppointmentSource());
        }
        // 派生出勤事实同步生效：不然号源已发布、这个人却不计在岗
        syncCoreSchedulePublish(existing.getStaffScheduleId(), PublishStatusEnum.PUBLISHED.getCode());
        clinicSourceLogService.record(existing.getId(), ClinicSourceChangeTypeEnum.PUBLISH, null,
                existing.getDoctorName() + " " + existing.getScheduleDate() + " 审核发布", existing);
    }

    /**
     * 门诊排班审核人校验：系统管理员(10012)、门诊办(10035)全局豁免；
     * 其余当前登录人必须是该班所属科室在科室主档指定的门诊审核人(outpatient_auditor_id)。
     */
    private void assertOutpatientAuditor(BizClinicSource schedule) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("未登录或登录已过期，请重新登录");
        }
        String role = user.getCurrentRole();
        if (ROLE_ADMIN.equals(role) || ROLE_OUTPATIENT_OFFICE.equals(role)) {
            return;
        }
        DeptBriefVO dept = systemFacadeService.findDepartment(schedule.getDeptId());
        if (dept == null || dept.getOutpatientAuditorId() == null
                || !dept.getOutpatientAuditorId().equals(user.getEmployeeId())) {
            throw new BusinessException("您不是本科室指定的门诊排班审核人，无权审核发布");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditWeek(ScheduleQueryDTO query) {
        LocalDate today = LocalDate.now();
        List<BizClinicSource> pendings = listPage(query).stream()
                .filter(s -> PublishStatusEnum.PENDING.getCode().equals(s.getPublishStatus()))
                // 历史班次跳过（不整批回滚），与单条 assertNotPast 同口径
                .filter(s -> s.getScheduleDate() != null && !s.getScheduleDate().isBefore(today))
                .toList();
        if (pendings.isEmpty()) {
            throw new BusinessException("区间内没有待审核的排班（草稿需先提交，已发布/历史班次不会重复发布）");
        }
        for (BizClinicSource s : pendings) {
            doAudit(s);
        }
        return pendings.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recall(Long id) {
        BizClinicSource existing = clinicSourceMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("排班记录不存在");
        }
        assertNotPast(existing.getScheduleDate(), "撤回");
        // 任意态可撤回回草稿，并清掉已生成号源（撤回即不生效、不出号）
        BizClinicSource update = new BizClinicSource();
        update.setId(id);
        update.setPublishStatus(PublishStatusEnum.DRAFT.getCode());
        clinicSourceMapper.updateById(update);
        if (StaffTypeEnum.hasSource(existing.getStaffType())) {
            bizScheduleSlotService.physicalDeleteByScheduleId(id);
        }
        // 撤回后号源不生效，派生的出勤事实同步回草稿（在岗聚合要一起撤掉）
        syncCoreSchedulePublish(existing.getStaffScheduleId(), PublishStatusEnum.DRAFT.getCode());
        clinicSourceLogService.record(id, ClinicSourceChangeTypeEnum.RECALL, null,
                existing.getDoctorName() + " " + existing.getScheduleDate() + " 撤回", existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStart(Long id) {
        BizClinicSource existing = clinicSourceMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("排班记录不存在");
        }
        assertNotPast(existing.getScheduleDate(), "进入调班");
        if (!PublishStatusEnum.isEffective(existing.getPublishStatus())) {
            throw new BusinessException("只有已发布/已变更的排班可以进入调班");
        }
        // 进入调班中：锁定编辑，号源因 publishStatus 不在 (已发布/已变更) 自动冻结，患者侧不可约
        BizClinicSource update = new BizClinicSource();
        update.setId(id);
        update.setPublishStatus(PublishStatusEnum.ADJUSTING.getCode());
        clinicSourceMapper.updateById(update);
        clinicSourceLogService.record(id, ClinicSourceChangeTypeEnum.ADJUST, null,
                existing.getDoctorName() + " " + existing.getScheduleDate() + " 进入调班", existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PageResult<ScheduleDetailVO> pendingApprovalPage(SchedulePendingQueryDTO dto) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("未登录或登录已过期，请重新登录");
        }
        String role = user.getCurrentRole();
        Set<Long> deptIds = null;
        if (!ROLE_ADMIN.equals(role) && !ROLE_OUTPATIENT_OFFICE.equals(role)) {
            // 普通门诊审核人：只收主档 outpatient_auditor_id 指向自己的科室
            deptIds = systemFacadeService.listDepartmentsByOutpatientAuditor(user.getEmployeeId()).stream()
                    .map(DeptBriefVO::getId).collect(Collectors.toSet());
            if (deptIds.isEmpty()) {
                return PageResult.of(0, dto.getPageNum(), dto.getPageSize(), 0, List.of());
            }
        }
        LambdaQueryWrapper<BizClinicSource> wrapper = new LambdaQueryWrapper<BizClinicSource>()
                .eq(BizClinicSource::getPublishStatus, PublishStatusEnum.PENDING.getCode())
                .in(deptIds != null, BizClinicSource::getDeptId, deptIds)
                .orderByAsc(BizClinicSource::getScheduleDate)
                .orderByAsc(BizClinicSource::getStartTime)
                .orderByAsc(BizClinicSource::getId);
        Page<BizClinicSource> page = clinicSourceMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), toVOList(page.getRecords()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(ScheduleRejectDTO dto) {
        BizClinicSource existing = clinicSourceMapper.selectById(dto.getScheduleId());
        if (existing == null) {
            throw new BusinessException("排班记录不存在");
        }
        assertNotPast(existing.getScheduleDate(), "驳回");
        if (!PublishStatusEnum.PENDING.getCode().equals(existing.getPublishStatus())) {
            throw new BusinessException("只有待审核状态的排班可以驳回");
        }
        assertOutpatientAuditor(existing);
        BizClinicSource update = new BizClinicSource();
        update.setId(existing.getId());
        update.setPublishStatus(PublishStatusEnum.DRAFT.getCode());
        clinicSourceMapper.updateById(update);
        String reason = (dto.getReason() == null || dto.getReason().isBlank()) ? "" : "：" + dto.getReason();
        clinicSourceLogService.record(existing.getId(), ClinicSourceChangeTypeEnum.REJECT, null,
                existing.getDoctorName() + " " + existing.getScheduleDate() + " 驳回" + reason, existing);
    }

    /**
     * 把号源行的发布态同步到它派生的那条出勤事实（biz_schedule）。
     * 号源与出勤是两行，发布/撤回必须两边一起动，否则会出现「号源已发布、人却不计在岗」。
     */
    private void syncCoreSchedulePublish(Long staffScheduleId, Integer publishStatus) {
        if (staffScheduleId == null) {
            return;
        }
        BizSchedule update = new BizSchedule();
        update.setId(staffScheduleId);
        update.setPublishStatus(publishStatus);
        staffScheduleService.updateById(update);
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
                .in(BizClinicSource::getPublishStatus,
                        PublishStatusEnum.PUBLISHED.getCode(), PublishStatusEnum.CHANGED.getCode())
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
        List<DutyRow> rows = new ArrayList<>(loadDaySchedule(deptId, date, staffType));
        boolean doctorScope = staffType == null || staffType == StaffTypeEnum.DOCTOR.getCode();
        boolean includeNurse = staffType == null || staffType == StaffTypeEnum.NURSE.getCode();
        // 医生口径：再并入本科室当日白班值班医生——门诊结束后仍能找到替科室兜底的值班人
        if (doctorScope) {
            mergeDeptDuty(rows, deptId, date);
        }
        if (!includeNurse) {
            return rows;
        }
        LinkedHashMap<Long, DutyRow> byEmp = new LinkedHashMap<>();
        for (DutyRow r : rows) {
            if (r.staffId != null) {
                byEmp.putIfAbsent(r.staffId, r); // 门诊号源优先，已有的人（含值班）不覆盖
            }
        }
        for (DutyRow n : loadNurseDaySchedule(deptId, date)) {
            if (n.staffId != null) {
                byEmp.put(n.staffId, n); // 护理排班表权威：冲突时覆盖
            }
        }
        return new ArrayList<>(byEmp.values());
    }

    /**
     * 把本科室当日白班值班医生并入候选集；门诊号源已有的人不重复加，值班来源异常不阻断在岗解析。
     */
    private void mergeDeptDuty(List<DutyRow> rows, Long deptId, LocalDate date) {
        List<DutyRosterVO> duties;
        try {
            duties = dutyRosterService.listDeptDayOnDuty(deptId, date);
        } catch (Exception e) {
            return;
        }
        Set<Long> exist = new HashSet<>();
        for (DutyRow r : rows) {
            if (r.staffId != null) {
                exist.add(r.staffId);
            }
        }
        for (DutyRosterVO d : duties) {
            Long empId = d.getActualEmpId() != null ? d.getActualEmpId() : d.getEmployeeId();
            String name = d.getActualEmpName() != null ? d.getActualEmpName() : d.getEmployeeName();
            if (empId == null || exist.contains(empId)) {
                continue;
            }
            exist.add(empId);
            DutyRow r = new DutyRow();
            r.scheduleId = d.getId();
            r.staffId = empId;
            r.staffName = name;
            r.staffType = StaffTypeEnum.DOCTOR.getCode();
            r.deptId = d.getDeptId();
            r.deptName = d.getDeptName();
            r.shiftId = null;
            r.startTime = d.getStartTime();
            r.endTime = d.getEndTime();
            r.status = 1;
            rows.add(r);
        }
    }

    private OnDutyStaffVO toOnDutyVO(DutyRow r, LocalTime moment) {
        OnDutyStaffVO vo = new OnDutyStaffVO();
        vo.setScheduleId(r.scheduleId);
        vo.setStaffId(r.staffId);
        vo.setStaffName(r.staffName);
        vo.setStaffType(r.staffType);
        vo.setStaffTypeName(StaffTypeEnum.getLabel(r.staffType));
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
                    + StaffTypeEnum.getLabel(schedule.getStaffType()) + "岗位是出勤排班，不对外放号");
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
            clinicSourceLogService.record(scheduleId, ClinicSourceChangeTypeEnum.ADD_SOURCE, addNum, reason, schedule);
        }
        return ok;
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
}
