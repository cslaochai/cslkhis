package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.OrgUnitTypeEnum;
import com.his.common.enums.StaffDutyStatusEnum;
import com.his.common.enums.StaffScheduleSourceEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.patient.dto.NurseScheduleDTO;
import com.his.patient.entity.BizNurseSchedule;
import com.his.patient.mapper.BizNurseScheduleMapper;
import com.his.patient.service.NurseScheduleService;
import com.his.patient.vo.NurseScheduleVO;
import com.his.system.dto.StaffPlanRuleUpsertDTO;
import com.his.system.dto.StaffScheduleUpsertDTO;
import com.his.system.entity.BizStaffPlanRule;
import com.his.system.provider.DeptScopeProvider;
import com.his.system.service.StaffPlanRuleService;
import com.his.system.service.StaffScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;

/**
 * 病区护理排班服务实现（sql/166）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NurseScheduleServiceImpl extends ServiceImpl<BizNurseScheduleMapper, BizNurseSchedule> implements NurseScheduleService {
    private static final int REMARK_MAX = 500;
    private static final int NURSE_LIMIT = 500;
    /**
     * 夜班口径：开始时刻早于 08:00（后夜班）或不早于 16:00（前夜班/大夜）
     */
    private static final String NIGHT_LATEST_MORNING = "08:00";
    private static final String NIGHT_FROM_AFTERNOON = "16:00";
    /**
     * 病区级规则行的 shift_id（与 sql/166 一致）
     */
    private static final long WARD_LEVEL_SHIFT = 0L;
    /**
     * 排班单元类型：1-病区（sql/209）
     */
    private static final int UNIT_WARD = 1;
    /**
     * 排班单元类型：2-门诊科室（sql/209）
     */
    private static final int UNIT_CLINIC = 2;
    private final DeptScopeProvider deptScopeProvider;
    private final BizNurseScheduleMapper bizNurseScheduleMapper;
    private final StaffScheduleService staffScheduleService;
    /**
     * 人力配置标准的唯一维护口（G-08：护理排班页不再自己建一套 biz_nurse_schedule_rule，
     * 改维护 biz_staff_plan_rule 里「病区 × 护理岗」那一批，与全院排班的人力闸门同源）。
     */
    private final StaffPlanRuleService staffPlanRuleService;

    // 参照数据

    private static int unitTypeOf(NurseScheduleVO.Ward ward) {
        return ward.getUnitType() == null || ward.getUnitType() == 0 ? UNIT_WARD : ward.getUnitType();
    }

    /**
     * 这个护理单元在出勤底座里落的排班单元类型：病区→2，门诊科室→1
     */
    private static Integer coreOrgTypeOf(NurseScheduleVO.Ward ward) {
        return unitTypeOf(ward) == UNIT_CLINIC ? OrgUnitTypeEnum.DEPT.getCode() : OrgUnitTypeEnum.WARD.getCode();
    }

    private static Integer coreOrgTypeOfByType(Integer unitType) {
        return unitType != null && unitType == UNIT_CLINIC
                ? OrgUnitTypeEnum.DEPT.getCode() : OrgUnitTypeEnum.WARD.getCode();
    }

    // 周矩阵 / 台账

    private static boolean isNightShift(String startTime) {
        if (startTime == null || startTime.length() < 5) {
            return false;
        }
        return startTime.compareTo(NIGHT_LATEST_MORNING) < 0 || startTime.compareTo(NIGHT_FROM_AFTERNOON) >= 0;
    }

    /**
     * 周一为周首：排班周、周工时上限都按它算（不按周日起始的美式口径）
     */
    private static LocalDate mondayOf(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1L);
    }

    // 点格 / 删除 / 复制上周

    /**
     * 日期字符串（yyyy-MM-dd）在该周里的第几天：1-周一 ... 7-周日
     */
    private static int weekdayOf(String scheduleDate) {
        return LocalDate.parse(scheduleDate).getDayOfWeek().getValue();
    }

    private static BigDecimal hours(Integer minutes) {
        return BigDecimal.valueOf(minutes == null ? 0 : minutes)
                .divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);
    }

    private static YearMonth parseMonth(String month) {
        if (month == null || month.trim().isEmpty()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (DateTimeParseException e) {
            throw new BusinessException("月份格式必须为 yyyy-MM");
        }
    }

    // 规则校验 / 工时

    private static void zeroFill(NurseScheduleVO.Workload w) {
        w.setWorkDays(nvlInt(w.getWorkDays()));
        w.setRestDays(nvlInt(w.getRestDays()));
        w.setLeaveDays(nvlInt(w.getLeaveDays()));
        w.setTrainingDays(nvlInt(w.getTrainingDays()));
        w.setSuspendedDays(nvlInt(w.getSuspendedDays()));
        w.setNightDays(nvlInt(w.getNightDays()));
        w.setWorkMinutes(nvlInt(w.getWorkMinutes()));
        if (w.getWorkHours() == null) {
            w.setWorkHours(BigDecimal.ZERO);
        }
    }

    private static int countLevel(List<NurseScheduleVO.Warning> warnings, int level) {
        return (int) warnings.stream().filter(w -> w.getLevel() != null && w.getLevel() == level).count();
    }

    // 人力配置标准

    private static int nvlInt(Integer value) {
        return value == null ? 0 : value;
    }

    @Override
    public List<NurseScheduleVO.Ward> wardSelectList(String keyword) {
        return bizNurseScheduleMapper.selectWardOptions(scopedDeptIds(null), TextUtil.trimToNull(keyword));
    }

    @Override
    public List<NurseScheduleVO.Ward> unitSelectList(String keyword) {
        List<NurseScheduleVO.Ward> units = new ArrayList<>();
        units.addAll(bizNurseScheduleMapper.selectWardOptions(scopedDeptIds(null), TextUtil.trimToNull(keyword)));
        units.addAll(bizNurseScheduleMapper.selectDeptOptions(scopedDeptIds(null), TextUtil.trimToNull(keyword)));
        return units;
    }

    @Override
    public List<NurseScheduleVO.Nurse> nurseSelectList(Integer unitType, Long unitId, String keyword) {
        NurseScheduleVO.Ward ward = requireUnit(unitType, unitId);
        return bizNurseScheduleMapper.selectNurses(ward.getDeptId(), TextUtil.trimToNull(keyword), NURSE_LIMIT);
    }

    @Override
    public NurseScheduleVO.Matrix weekMatrix(NurseScheduleDTO.MatrixQuery query) {
        NurseScheduleVO.Ward ward = requireUnit(query.getUnitType(), query.getWardId());
        LocalDate start = mondayOf(query.getWeekStart() == null ? LocalDate.now() : query.getWeekStart());
        LocalDate end = start.plusDays(6);

        List<NurseScheduleVO.Nurse> nurses = bizNurseScheduleMapper.selectNurses(ward.getDeptId(), null, NURSE_LIMIT);
        List<NurseScheduleVO.Cell> cells = selectCells(ward, start, end);
        cells.forEach(c -> c.setScheduleStatusText(StaffDutyStatusEnum.getText(c.getScheduleStatus())));
        List<NurseScheduleVO.ShiftOption> shifts = bizNurseScheduleMapper.selectNursingShifts();
        shifts.forEach(s -> s.setNight(isNightShift(s.getStartTime())));

        NurseScheduleVO.Matrix matrix = new NurseScheduleVO.Matrix();
        matrix.setWardId(ward.getWardId());
        matrix.setWardName(ward.getWardName());
        matrix.setDeptId(ward.getDeptId());
        matrix.setDeptName(ward.getDeptName());
        matrix.setWeekStart(start.toString());
        matrix.setWeekEnd(end.toString());
        List<String> days = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            days.add(start.plusDays(i).toString());
        }
        matrix.setDays(days);
        matrix.setNurses(nurses);
        matrix.setShifts(shifts);
        matrix.setCells(cells);
        matrix.setStaffing(buildStaffing(ward, start, end, cells));
        matrix.setWarnings(buildWarnings(ward, start, end, nurses, cells));
        return matrix;
    }

    @Override
    public PageResult<NurseScheduleVO.Row> listPage(NurseScheduleDTO.QueryPage query) {
        IPage<NurseScheduleVO.Row> page = new Page<>(query.getPageNum(), query.getPageSize());
        Long deptId = deptScopeProvider.resolveDeptId(query.getDeptId());
        List<Long> deptIds = scopedDeptIds(query.getDeptId());
        List<NurseScheduleVO.Row> records = bizNurseScheduleMapper.selectSchedulePage(page, TextUtil.trimToNull(query.getKeyword()),
                query.getWardId(), deptId, query.getScheduleStatus(), query.getStartDate(), query.getEndDate(), deptIds);
        for (NurseScheduleVO.Row row : records) {
            row.setScheduleStatusText(StaffDutyStatusEnum.getText(row.getScheduleStatus()));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NurseScheduleVO.SaveResult upsert(NurseScheduleDTO.CellUpsert dto) {
        NurseScheduleVO.Ward ward = requireUnit(dto.getUnitType(), dto.getWardId());
        StaffDutyStatusEnum status = StaffDutyStatusEnum.fromCode(dto.getScheduleStatus());
        if (status == null) {
            throw new BusinessException("排班状态只允许 " + StaffDutyStatusEnum.whitelistText());
        }
        NurseScheduleVO.Nurse nurse = bizNurseScheduleMapper.selectNurse(dto.getEmployeeId());
        if (nurse == null) {
            throw new BusinessException("该员工不在在册护士名单里（需为在职的护士/护师，工号可在员工档案核对）");
        }
        if (!Objects.equals(ward.getDeptId(), nurse.getDeptId())) {
            throw new BusinessException("护士「" + nurse.getNurseName() + "」属于「" + nurse.getDeptName()
                    + "」，不能排进「" + ward.getWardName() + "」——跨单元支援请先在员工档案调整所属科室");
        }

        NurseScheduleVO.ShiftOption shift = null;
        if (status == StaffDutyStatusEnum.WORK) {
            if (dto.getShiftId() == null) {
                throw new BusinessException("排「上班」必须选班次：不然这一天既没有时段也没有工时，月度统计无从算起");
            }
            shift = bizNurseScheduleMapper.selectNursingShift(dto.getShiftId());
            if (shift == null) {
                throw new BusinessException("所选班次不存在、已停用或不是病区护理班次（护理班次在班次字典里按「病区护理」册维护）");
            }
        }

        LocalDate date = dto.getScheduleDate();
        Integer workMinutes = shift == null ? 0 : shift.getDurationMinutes();
        String remark = TextUtil.cut(TextUtil.trimToNull(dto.getRemark()), REMARK_MAX);
        BizNurseSchedule prevRow = bizNurseScheduleMapper.selectOne(new LambdaQueryWrapper<BizNurseSchedule>()
                .eq(BizNurseSchedule::getEmployeeId, dto.getEmployeeId())
                .eq(BizNurseSchedule::getScheduleDate, date)
                .last("LIMIT 1"));
        Long coreScheduleId = syncDayAttendance(ward, nurse.getEmployeeId(), date, status,
                shift == null ? null : shift.getShiftId(), remark, StaffScheduleSourceEnum.MANUAL,
                prevRow == null ? null : prevRow.getStaffScheduleId());
        NurseScheduleVO.Cell exist = bizNurseScheduleMapper.selectByKey(dto.getEmployeeId(), date);

        Long rowId;
        if (exist == null) {
            BizNurseSchedule row = new BizNurseSchedule();
            fillSnapshot(row, ward, nurse, shift, status, date, workMinutes, remark,
                    StaffScheduleSourceEnum.MANUAL.getCode(), coreScheduleId);
            bizNurseScheduleMapper.insert(row);
            rowId = row.getId();
        } else {
            rowId = exist.getId();
            // 显式 set 全列：改成休息时要能把 shift_* 洗成 NULL，
            // 而 MP 的 updateById 默认忽略 null 字段（等于改了状态却留着夜班的起止时间）
            LambdaUpdateWrapper<BizNurseSchedule> upd = new LambdaUpdateWrapper<BizNurseSchedule>()
                    .eq(BizNurseSchedule::getId, rowId)
                    .set(BizNurseSchedule::getWardId, ward.getWardId())
                    .set(BizNurseSchedule::getWardName, ward.getWardName())
                    .set(BizNurseSchedule::getDeptId, ward.getDeptId())
                    .set(BizNurseSchedule::getDeptName, ward.getDeptName())
                    .set(BizNurseSchedule::getUnitType, unitTypeOf(ward))
                    .set(BizNurseSchedule::getUnitId, ward.getWardId())
                    .set(BizNurseSchedule::getScheduleDate, date)
                    .set(BizNurseSchedule::getWeekDay, date.getDayOfWeek().getValue())
                    .set(BizNurseSchedule::getEmployeeId, nurse.getEmployeeId())
                    .set(BizNurseSchedule::getEmpCode, nurse.getEmpCode())
                    .set(BizNurseSchedule::getNurseName, nurse.getNurseName())
                    .set(BizNurseSchedule::getNurseTitle, nurse.getNurseTitle())
                    .set(BizNurseSchedule::getShiftId, shift == null ? null : shift.getShiftId())
                    .set(BizNurseSchedule::getShiftName, shift == null ? null : shift.getShiftName())
                    .set(BizNurseSchedule::getStartTime, shift == null ? null : shift.getStartTime())
                    .set(BizNurseSchedule::getEndTime, shift == null ? null : shift.getEndTime())
                    .set(BizNurseSchedule::getWorkMinutes, workMinutes)
                    .set(BizNurseSchedule::getScheduleStatus, status.getCode())
                    .set(BizNurseSchedule::getScheduleSource, StaffScheduleSourceEnum.MANUAL.getCode())
                    .set(BizNurseSchedule::getStaffScheduleId, coreScheduleId)
                    .set(BizNurseSchedule::getRemark, remark);
            bizNurseScheduleMapper.update(null, upd);
        }

        LocalDate weekStart = mondayOf(date);
        BigDecimal weekHours = hours(bizNurseScheduleMapper.selectWorkMinutes(nurse.getEmployeeId(), weekStart, weekStart.plusDays(6)));
        NurseScheduleVO.SaveResult result = new NurseScheduleVO.SaveResult();
        result.setId(rowId);
        result.setScheduleDate(date.toString());
        result.setShiftName(shift == null ? null : shift.getShiftName());
        result.setScheduleStatus(status.getCode());
        result.setWeekHours(weekHours);
        List<NurseScheduleVO.Warning> all = warningsOf(ward, weekStart, weekStart.plusDays(6));
        List<NurseScheduleVO.Warning> related = new ArrayList<>();
        for (NurseScheduleVO.Warning w : all) {
            if (Objects.equals(w.getEmployeeId(), nurse.getEmployeeId())
                    || date.toString().equals(w.getScheduleDate())) {
                related.add(w);
            }
        }
        result.setWarnings(related);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NurseScheduleVO.DeleteResult deleteById(Long id) {
        if (id == null) {
            throw new BusinessException("缺少排班行ID");
        }
        BizNurseSchedule row = bizNurseScheduleMapper.selectById(id);
        if (row == null) {
            throw new BusinessException("排班行不存在或已删除");
        }
        if (!deptScopeProvider.canAccessDept(row.getDeptId())) {
            throw new BusinessException("无权删除该病区的排班（不在当前岗位的数据范围内）");
        }
        bizNurseScheduleMapper.purgeById(id);
        // 格子删掉 = 这个人这天在这个病区没班了：那条在岗事实一并收掉，
        // 否则「今日在岗」还会把他算进病区人数，而他其实已经排空了
        staffScheduleService.purgeDayAttendance(coreOrgTypeOfByType(row.getUnitType()),
                row.getWardId(), row.getEmployeeId(), row.getScheduleDate());
        NurseScheduleVO.DeleteResult result = new NurseScheduleVO.DeleteResult();
        result.setId(id);
        result.setNurseName(row.getNurseName());
        result.setScheduleDate(row.getScheduleDate() == null ? null : row.getScheduleDate().toString());
        return result;
    }

    // 内部：校验与工具

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NurseScheduleVO.CopyResult copyWeek(NurseScheduleDTO.CopyWeek dto) {
        NurseScheduleVO.Ward ward = requireUnit(dto.getUnitType(), dto.getWardId());
        LocalDate sourceStart = mondayOf(dto.getSourceWeekStart());
        LocalDate targetStart = mondayOf(dto.getTargetWeekStart());
        if (sourceStart.equals(targetStart)) {
            throw new BusinessException("来源周与目标周是同一周，复制没有意义");
        }
        List<NurseScheduleVO.Nurse> nurses = bizNurseScheduleMapper.selectNurses(ward.getDeptId(), null, NURSE_LIMIT);
        List<NurseScheduleVO.Cell> source = selectCells(ward, sourceStart, sourceStart.plusDays(6));
        List<NurseScheduleVO.Cell> target = selectCells(ward, targetStart, targetStart.plusDays(6));

        Map<String, NurseScheduleVO.Cell> sourceMap = new HashMap<>();
        for (NurseScheduleVO.Cell c : source) {
            sourceMap.put(c.getEmployeeId() + "|" + weekdayOf(c.getScheduleDate()), c);
        }
        Set<String> written = new HashSet<>();
        for (NurseScheduleVO.Cell c : target) {
            written.add(c.getEmployeeId() + "|" + weekdayOf(c.getScheduleDate()));
        }

        int copied = 0;
        int skippedFilled = 0;
        int blankSource = 0;
        for (NurseScheduleVO.Nurse nurse : nurses) {
            for (int day = 1; day <= 7; day++) {
                String key = nurse.getEmployeeId() + "|" + day;
                if (written.contains(key)) {
                    skippedFilled++;
                    continue;
                }
                NurseScheduleVO.Cell from = sourceMap.get(key);
                if (from == null) {
                    blankSource++;
                    continue;
                }
                BizNurseSchedule row = new BizNurseSchedule();
                LocalDate date = targetStart.plusDays(day - 1L);
                StaffDutyStatusEnum status = StaffDutyStatusEnum.fromCode(from.getScheduleStatus());
                if (status == null) {
                    throw new BusinessException("来源周存在非法排班状态（" + from.getScheduleStatus() + "），请先修正再复制");
                }
                row.setWardId(ward.getWardId());
                row.setWardName(ward.getWardName());
                row.setDeptId(ward.getDeptId());
                row.setDeptName(ward.getDeptName());
                row.setUnitType(unitTypeOf(ward));
                row.setUnitId(ward.getWardId());
                row.setScheduleDate(date);
                row.setWeekDay(day);
                row.setEmployeeId(nurse.getEmployeeId());
                row.setEmpCode(nurse.getEmpCode());
                row.setNurseName(nurse.getNurseName());
                row.setNurseTitle(nurse.getNurseTitle());
                // 班次快照按「今天仍可用的班次」重取：来源周的班次若已停用，照抄会让新周带着废班次
                if (status == StaffDutyStatusEnum.WORK && from.getShiftId() != null) {
                    NurseScheduleVO.ShiftOption shift = bizNurseScheduleMapper.selectNursingShift(from.getShiftId());
                    if (shift != null) {
                        row.setShiftId(shift.getShiftId());
                        row.setShiftName(shift.getShiftName());
                        row.setStartTime(shift.getStartTime());
                        row.setEndTime(shift.getEndTime());
                        row.setWorkMinutes(shift.getDurationMinutes());
                    } else {
                        row.setScheduleStatus(StaffDutyStatusEnum.REST.getCode());
                        row.setWorkMinutes(0);
                        row.setRemark("复制上周：原班次「" + from.getShiftName() + "」已停用，按休息落");
                    }
                } else {
                    row.setWorkMinutes(0);
                }
                if (row.getScheduleStatus() == null) {
                    row.setScheduleStatus(status.getCode());
                }
                row.setScheduleSource(StaffScheduleSourceEnum.COPY.getCode());
                // 回指出勤事实：复制周期走的是「先落事实再 insert 格子」，同一条路径有同一份指路牌
                row.setStaffScheduleId(syncDayAttendance(ward, nurse.getEmployeeId(), date,
                        StaffDutyStatusEnum.fromCode(row.getScheduleStatus()), row.getShiftId(),
                        row.getRemark(), StaffScheduleSourceEnum.COPY, null));
                bizNurseScheduleMapper.insert(row);
                copied++;
            }
        }

        NurseScheduleVO.CopyResult result = new NurseScheduleVO.CopyResult();
        result.setCopiedCount(copied);
        result.setSkippedCount(skippedFilled);
        result.setMessage("已复制 " + copied + " 格；目标周已有 " + skippedFilled + " 格未覆盖；来源周空白 "
                + blankSource + " 格无内容可复制");
        return result;
    }

    /**
     * 一格同步一条在岗事实（覆盖语义）。班次、岗位类别、起止时间、工时都由事实层按人事与班次字典重算，
     * 这里只递交「哪个病区、哪个人、哪天、什么状态、哪个班」。
     *
     * @return 底座那条事实的ID —— 格子必须回指它（sql/205），否则「改了护理格子，底座的行在哪」
     * 只能靠 (人,日,病区) 反查，而这个反查在多单元场景下会指到别的单元那一条上去。
     */
    private Long syncDayAttendance(NurseScheduleVO.Ward ward, Long employeeId, LocalDate date,
                                   StaffDutyStatusEnum status, Long shiftId, String remark,
                                   StaffScheduleSourceEnum source, Long existStaffScheduleId) {
        StaffScheduleUpsertDTO core = new StaffScheduleUpsertDTO();
        // 带上一次落地的事实 id：改的是同一条出勤事实，不是删了重建。
        // 不带的话每次改动都会换一个新 id，护理格子回写的引用、将来要挂的实际出勤全部指向已删的行。
        core.setId(existStaffScheduleId);
        core.setScheduleDate(date);
        // 病区护理格落在病区单元(2)，门诊护理格落在科室单元(1) ——
        // 单元类型错了，这条事实用「全院排班」看就会出现在错误的地点上
        core.setOrgType(coreOrgTypeOf(ward));
        core.setOrgId(ward.getWardId());
        core.setEmployeeId(employeeId);
        core.setDutyStatus(status.getCode());
        core.setShiftId(shiftId);
        core.setRemark(remark);
        return staffScheduleService.replaceDayAttendance(core, source).getId();
    }

    @Override
    public NurseScheduleVO.CheckResult check(NurseScheduleDTO.CheckQuery query) {
        NurseScheduleVO.Ward ward = requireUnit(query.getUnitType(), query.getWardId());
        LocalDate start = query.getStartDate() != null ? query.getStartDate()
                : mondayOf(query.getEndDate() == null ? LocalDate.now() : query.getEndDate());
        LocalDate end = query.getEndDate() != null ? query.getEndDate() : start.plusDays(6);
        if (end.isBefore(start)) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        List<NurseScheduleVO.Warning> warnings = warningsOf(ward, start, end);
        NurseScheduleVO.CheckResult result = new NurseScheduleVO.CheckResult();
        result.setWardId(ward.getWardId());
        result.setWardName(ward.getWardName());
        result.setStartDate(start.toString());
        result.setEndDate(end.toString());
        result.setAlertCount(countLevel(warnings, 1));
        result.setHintCount(countLevel(warnings, 2));
        result.setWarnings(warnings);
        return result;
    }

    @Override
    public List<NurseScheduleVO.Warning> warningsOf(Long wardId, LocalDate startDate, LocalDate endDate) {
        return warningsOf(requireUnit(null, wardId), startDate, endDate);
    }

    /**
     * 已收过权限的病区直接算告警（省掉一次重复的病区回查）
     */
    private List<NurseScheduleVO.Warning> warningsOf(NurseScheduleVO.Ward ward, LocalDate startDate, LocalDate endDate) {
        List<NurseScheduleVO.Cell> cells = selectCells(ward, startDate, endDate);
        List<NurseScheduleVO.Nurse> nurses = bizNurseScheduleMapper.selectNurses(ward.getDeptId(), null, NURSE_LIMIT);
        return buildWarnings(ward, startDate, endDate, nurses, cells);
    }

    @Override
    public NurseScheduleVO.MonthWorkload monthWorkload(NurseScheduleDTO.WorkloadQuery query) {
        NurseScheduleVO.Ward ward = requireUnit(query.getUnitType(),
                query.getWardId());
        YearMonth ym = parseMonth(query.getMonth());
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        int days = ym.lengthOfMonth();

        List<NurseScheduleVO.Nurse> nurses = bizNurseScheduleMapper.selectNurses(ward.getDeptId(), null, NURSE_LIMIT);
        Map<Long, NurseScheduleVO.Workload> byEmployee = new LinkedHashMap<>();
        for (NurseScheduleVO.Workload w : bizNurseScheduleMapper.selectWorkload(
                unitTypeOf(ward), ward.getWardId(), start, end)) {
            byEmployee.put(w.getEmployeeId(), w);
        }
        List<NurseScheduleVO.Workload> rows = new ArrayList<>();
        int totalWorkDays = 0;
        int totalNightDays = 0;
        BigDecimal totalHours = BigDecimal.ZERO;
        for (NurseScheduleVO.Nurse nurse : nurses) {
            NurseScheduleVO.Workload w = byEmployee.get(nurse.getEmployeeId());
            if (w == null) {
                w = new NurseScheduleVO.Workload();
                w.setEmployeeId(nurse.getEmployeeId());
                w.setEmpCode(nurse.getEmpCode());
                w.setNurseName(nurse.getNurseName());
                w.setNurseTitle(nurse.getNurseTitle());
                w.setWrittenDays(0);
            }
            zeroFill(w);
            w.setMissingDays(Math.max(0, days - nvlInt(w.getWrittenDays())));
            rows.add(w);
            totalWorkDays += nvlInt(w.getWorkDays());
            totalNightDays += nvlInt(w.getNightDays());
            totalHours = totalHours.add(w.getWorkHours() == null ? BigDecimal.ZERO : w.getWorkHours());
        }
        rows.sort(Comparator.comparing(NurseScheduleVO.Workload::getEmpCode,
                Comparator.nullsLast(Comparator.naturalOrder())));

        NurseScheduleVO.MonthWorkload result = new NurseScheduleVO.MonthWorkload();
        result.setWardId(ward.getWardId());
        result.setWardName(ward.getWardName());
        result.setMonth(ym.toString());
        result.setNurseCount(rows.size());
        result.setTotalWorkDays(totalWorkDays);
        result.setTotalNightDays(totalNightDays);
        result.setTotalWorkHours(totalHours.setScale(1, RoundingMode.HALF_UP));
        result.setMaxWeekHours(wardRule(ward.getWardId()).map(NurseScheduleVO.Rule::getMaxWeekHours).orElse(null));
        result.setRows(rows);
        return result;
    }

    @Override
    public List<NurseScheduleVO.Rule> ruleList(Long wardId) {
        return ruleList(null, wardId);
    }

    @Override
    public List<NurseScheduleVO.Rule> ruleList(Integer unitType, Long unitId) {
        NurseScheduleVO.Ward ward = requireUnit(unitType, unitId);
        return rulesOfWard(ward.getWardId());
    }

    /**
     * 某病区的护理人力标准 —— 唯一来源是 {@code biz_staff_plan_rule} 里
     * 「org_type=2 病区 × org_id=wardId × staff_type=2 护理」那一批。
     *
     * <p>旧的 {@code biz_nurse_schedule_rule} 停止写入（sql/206 起留作历史）：两份标准各管一摊的结果是
     * 护理页配的下限不进底座校验、底座配的护理页看不见。
     */
    private List<NurseScheduleVO.Rule> rulesOfWard(Long wardId) {
        return toRules(wardId, staffPlanRuleService.listByUnit(OrgUnitTypeEnum.WARD.getCode(), wardId,
                StaffTypeEnum.NURSE.getCode()));
    }

    /**
     * 只看启用的那几条（告警比对不用停用值的标准）
     */
    private List<NurseScheduleVO.Rule> enabledRulesOfWard(Long wardId) {
        List<NurseScheduleVO.Rule> out = new ArrayList<>();
        for (NurseScheduleVO.Rule r : rulesOfWard(wardId)) {
            if (Objects.equals(r.getStatus(), 1)) {
                out.add(r);
            }
        }
        return out;
    }

    private List<NurseScheduleVO.Rule> toRules(Long wardId, List<BizStaffPlanRule> rows) {
        NurseScheduleVO.Ward ward = bizNurseScheduleMapper.selectWard(wardId);
        String wardName = ward == null ? null : ward.getWardName();
        List<NurseScheduleVO.Rule> out = new ArrayList<>(rows.size());
        for (BizStaffPlanRule r : rows) {
            NurseScheduleVO.Rule vo = new NurseScheduleVO.Rule();
            vo.setId(r.getId());
            vo.setWardId(wardId);
            vo.setWardName(wardName);
            vo.setShiftId(r.getShiftId());
            vo.setShiftName(shiftLabelOf(r.getShiftId()));
            vo.setMinStaff(r.getMinStaff());
            vo.setMaxStaff(r.getMaxStaff());
            vo.setMaxWeekHours(r.getMaxWeekHours());
            vo.setMaxConsecutiveNightDays(r.getMaxConsecutiveNightDays());
            vo.setMaxConsecutiveWorkDays(r.getMaxConsecutiveWorkDays());
            vo.setStatus(r.getStatus());
            vo.setRemark(r.getRemark());
            out.add(vo);
        }
        return out;
    }

    /**
     * 班次名按「今天仍可用的护理班次」现取：新表里没有冗余 shift_name，字典改名不该让历史标准带着旧名
     */
    private String shiftLabelOf(Long shiftId) {
        if (shiftId == null || shiftId == WARD_LEVEL_SHIFT) {
            return "病区合计";
        }
        NurseScheduleVO.ShiftOption shift = bizNurseScheduleMapper.selectNursingShift(shiftId);
        return shift == null ? "班次已失效" : shift.getShiftName();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ruleUpsert(NurseScheduleDTO.RuleUpsert dto) {
        NurseScheduleVO.Ward ward = requireUnit(dto.getUnitType(), dto.getWardId());
        long shiftId = dto.getShiftId() == null ? WARD_LEVEL_SHIFT : dto.getShiftId();
        boolean wardLevel = shiftId == WARD_LEVEL_SHIFT;
        String shiftName;
        if (wardLevel) {
            shiftName = "病区合计";
        } else {
            NurseScheduleVO.ShiftOption shift = bizNurseScheduleMapper.selectNursingShift(shiftId);
            if (shift == null) {
                throw new BusinessException("所选班次不存在、已停用或不是病区护理班次");
            }
            shiftName = shift.getShiftName();
        }

        Integer minStaff = dto.getMinStaff();
        if (minStaff == null || minStaff < 0) {
            throw new BusinessException("最低在岗人数必须是 0 或正整数");
        }
        Integer maxStaff = dto.getMaxStaff() == null ? 0 : dto.getMaxStaff();
        if (maxStaff < 0) {
            throw new BusinessException("最高在岗人数不能为负（0=不限）");
        }
        if (maxStaff > 0 && maxStaff < minStaff) {
            throw new BusinessException("最高在岗人数不能小于最低在岗人数");
        }
        Integer status = dto.getStatus() == null ? 1 : dto.getStatus();
        if (status != 0 && status != 1) {
            throw new BusinessException("状态只允许 1-启用 / 0-停用");
        }

        // 工时/连班是「人」的约束，只落在病区级那一行；班次行填了会被忽略，避免两处口径打架
        BigDecimal maxWeekHours = wardLevel ? dto.getMaxWeekHours() : null;
        Integer maxNight = wardLevel ? dto.getMaxConsecutiveNightDays() : null;
        Integer maxWork = wardLevel ? dto.getMaxConsecutiveWorkDays() : null;
        if (maxWeekHours != null && (maxWeekHours.signum() < 0 || maxWeekHours.compareTo(BigDecimal.valueOf(168)) > 0)) {
            throw new BusinessException("单周工时上限必须在 0~168 小时之间");
        }

        String remark = TextUtil.cut(TextUtil.trimToNull(dto.getRemark()), REMARK_MAX);
        // 病区护理页维护的只是「病区 × 护理岗」这一段格子：托给唯一维护口去落，
        // 查重、单元名与班次真实性的校验都在那边，这里不再自己造一份规则。
        StaffPlanRuleUpsertDTO core = new StaffPlanRuleUpsertDTO();
        core.setId(dto.getId());
        core.setOrgType(OrgUnitTypeEnum.WARD.getCode());
        core.setOrgId(ward.getWardId());
        core.setShiftId(shiftId);
        core.setStaffType(StaffTypeEnum.NURSE.getCode());
        core.setMinStaff(minStaff);
        core.setMaxStaff(maxStaff);
        core.setMaxWeekHours(maxWeekHours);
        core.setMaxConsecutiveNightDays(maxNight);
        core.setMaxConsecutiveWorkDays(maxWork);
        core.setStatus(status);
        core.setRemark(remark);
        staffPlanRuleService.upsert(core);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ruleDeleteById(Long id) {
        if (id == null) {
            throw new BusinessException("缺少标准行ID");
        }
        BizStaffPlanRule rule = staffPlanRuleService.requireById(id);
        if (!Objects.equals(rule.getOrgType(), OrgUnitTypeEnum.WARD.getCode())) {
            throw new BusinessException("这条标准不是病区护理标准，请到「人力配置标准」里维护");
        }
        // 标准只存单元 id，科室归属从病区现取（不在标准行上冗余 dept 列，避免调科后两处不一致）
        NurseScheduleVO.Ward ward = bizNurseScheduleMapper.selectWard(rule.getOrgId());
        if (ward == null || !deptScopeProvider.canAccessDept(ward.getDeptId())) {
            throw new BusinessException("无权维护该病区的人力标准（不在当前岗位的数据范围内）");
        }
        staffPlanRuleService.deleteById(id);
    }

    /**
     * 区间告警（一次取数、纯内存判定）：
     * 人力缺口/超配看「日 × 班次」，周工时/连班/连夜班看「人」，未排班看「人 × 日」的空缺。
     */
    private List<NurseScheduleVO.Warning> buildWarnings(NurseScheduleVO.Ward ward, LocalDate start, LocalDate end,
                                                        List<NurseScheduleVO.Nurse> nurses,
                                                        List<NurseScheduleVO.Cell> cells) {
        List<NurseScheduleVO.Rule> rules = selectEnabledRules(ward.getWardId());
        NurseScheduleVO.Rule wardRule = null;
        List<NurseScheduleVO.Rule> shiftRules = new ArrayList<>();
        for (NurseScheduleVO.Rule r : rules) {
            if (r.getShiftId() != null && r.getShiftId() == WARD_LEVEL_SHIFT) {
                wardRule = r;
            } else if (r.getShiftId() != null) {
                shiftRules.add(r);
            }
        }

        Map<Long, NurseScheduleVO.Nurse> nurseIndex = new HashMap<>();
        for (NurseScheduleVO.Nurse n : nurses) {
            nurseIndex.put(n.getEmployeeId(), n);
        }
        Map<Long, List<NurseScheduleVO.Cell>> byNurse = new LinkedHashMap<>();
        Map<String, Integer> dayTotal = new HashMap<>();
        Map<String, Integer> dayShift = new HashMap<>();
        for (NurseScheduleVO.Cell c : cells) {
            byNurse.computeIfAbsent(c.getEmployeeId(), k -> new ArrayList<>()).add(c);
            if (Objects.equals(c.getScheduleStatus(), StaffDutyStatusEnum.WORK.getCode())) {
                dayTotal.merge(c.getScheduleDate(), 1, Integer::sum);
                if (c.getShiftId() != null) {
                    dayShift.merge(c.getScheduleDate() + "|" + c.getShiftId(), 1, Integer::sum);
                }
            }
        }

        List<NurseScheduleVO.Warning> out = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            dates.add(d.toString());
        }

        // 1) 每日人力对照标准（班次级 + 病区级）
        for (String date : dates) {
            int total = dayTotal.getOrDefault(date, 0);
            if (wardRule != null && nvlInt(wardRule.getMinStaff()) > 0 && total < wardRule.getMinStaff()) {
                out.add(staffWarning(ward, date, WARD_LEVEL_SHIFT, "病区合计", total, wardRule.getMinStaff(), true));
            }
            if (wardRule != null && nvlInt(wardRule.getMaxStaff()) > 0 && total > wardRule.getMaxStaff()) {
                out.add(staffWarning(ward, date, WARD_LEVEL_SHIFT, "病区合计", total, wardRule.getMaxStaff(), false));
            }
            for (NurseScheduleVO.Rule r : shiftRules) {
                int actual = dayShift.getOrDefault(date + "|" + r.getShiftId(), 0);
                if (nvlInt(r.getMinStaff()) > 0 && actual < r.getMinStaff()) {
                    out.add(staffWarning(ward, date, r.getShiftId(), r.getShiftName(), actual, r.getMinStaff(), true));
                }
                if (nvlInt(r.getMaxStaff()) > 0 && actual > r.getMaxStaff()) {
                    out.add(staffWarning(ward, date, r.getShiftId(), r.getShiftName(), actual, r.getMaxStaff(), false));
                }
            }
        }

        // 2) 按人：周工时、连续上班、连续夜班、非上班却带班次
        for (Map.Entry<Long, List<NurseScheduleVO.Cell>> entry : byNurse.entrySet()) {
            Long employeeId = entry.getKey();
            NurseScheduleVO.Nurse nurse = nurseIndex.get(employeeId);
            String nurseName = nurse == null ? "非本病区在册护士" : nurse.getNurseName();
            List<NurseScheduleVO.Cell> rows = new ArrayList<>(entry.getValue());
            rows.sort(Comparator.comparing(NurseScheduleVO.Cell::getScheduleDate));

            for (NurseScheduleVO.Cell c : rows) {
                if (!Objects.equals(c.getScheduleStatus(), StaffDutyStatusEnum.WORK.getCode())
                        && c.getShiftId() != null) {
                    out.add(personWarning(1, "SHIFT_ON_NON_WORK", c.getScheduleDate(), null,
                            employeeId, nurseName, "「" + nurseName + "」" + c.getScheduleDate()
                                    + " 状态是" + StaffDutyStatusEnum.getText(c.getScheduleStatus())
                                    + "却仍挂着「" + c.getShiftName() + "」，请重新点格修正"));
                }
            }

            streakWarning(out, rows, nurseName, employeeId,
                    c -> Objects.equals(c.getScheduleStatus(), StaffDutyStatusEnum.WORK.getCode()),
                    wardRule == null ? null : wardRule.getMaxConsecutiveWorkDays(),
                    "WORK_STREAK", "连续上班");
            streakWarning(out, rows, nurseName, employeeId,
                    c -> Objects.equals(c.getScheduleStatus(), StaffDutyStatusEnum.WORK.getCode())
                            && isNightShift(c.getStartTime()),
                    wardRule == null ? null : wardRule.getMaxConsecutiveNightDays(),
                    "NIGHT_STREAK", "连续夜班");

            if (wardRule != null && wardRule.getMaxWeekHours() != null
                    && wardRule.getMaxWeekHours().signum() > 0) {
                Map<LocalDate, Integer> minutesByWeek = new LinkedHashMap<>();
                Map<LocalDate, String> sampleDate = new HashMap<>();
                for (NurseScheduleVO.Cell c : rows) {
                    if (!Objects.equals(c.getScheduleStatus(), StaffDutyStatusEnum.WORK.getCode())) {
                        continue;
                    }
                    LocalDate monday = mondayOf(LocalDate.parse(c.getScheduleDate()));
                    minutesByWeek.merge(monday, nvlInt(c.getWorkMinutes()), Integer::sum);
                    sampleDate.putIfAbsent(monday, c.getScheduleDate());
                }
                for (Map.Entry<LocalDate, Integer> w : minutesByWeek.entrySet()) {
                    // 跨病区也要算上：本病区取到的行可能只是这个人当周的一部分，所以按人回查全周
                    int realMinutes = bizNurseScheduleMapper.selectWorkMinutes(employeeId, w.getKey(), w.getKey().plusDays(6));
                    BigDecimal hours = hours(realMinutes);
                    if (hours.compareTo(wardRule.getMaxWeekHours()) > 0) {
                        out.add(personWarning(1, "WEEK_HOURS", sampleDate.get(w.getKey()), null,
                                employeeId, nurseName, "「" + nurseName + "」" + w.getKey() + " 起一周工时 "
                                        + hours + " 小时，超过上限 " + wardRule.getMaxWeekHours() + " 小时"));
                    }
                }
            }
        }

        // 3) 未排班空缺（整段空白只报一条，逐日缺格才逐日报）
        for (NurseScheduleVO.Nurse nurse : nurses) {
            List<NurseScheduleVO.Cell> rows = byNurse.get(nurse.getEmployeeId());
            if (rows == null || rows.isEmpty()) {
                out.add(personWarning(2, "NOT_SCHEDULED", start.toString(), null,
                        nurse.getEmployeeId(), nurse.getNurseName(), "「" + nurse.getNurseName()
                                + "」" + start + "~" + end + " 整段未排班"));
                continue;
            }
            Set<String> has = new HashSet<>();
            for (NurseScheduleVO.Cell c : rows) {
                has.add(c.getScheduleDate());
            }
            for (String date : dates) {
                if (!has.contains(date)) {
                    out.add(personWarning(2, "NOT_SCHEDULED", date, null,
                            nurse.getEmployeeId(), nurse.getNurseName(), "「" + nurse.getNurseName()
                                    + "」" + date + " 未排班（既没上班也没休息）"));
                }
            }
        }

        out.sort(Comparator.comparing(NurseScheduleVO.Warning::getScheduleDate,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return out;
    }

    /**
     * 每日在岗人数明细（矩阵底部「N 人 / 应 M 人」）
     */
    private List<NurseScheduleVO.Staffing> buildStaffing(NurseScheduleVO.Ward ward, LocalDate start, LocalDate end,
                                                         List<NurseScheduleVO.Cell> cells) {
        List<NurseScheduleVO.Rule> rules = selectEnabledRules(ward.getWardId());
        NurseScheduleVO.Rule wardRule = null;
        List<NurseScheduleVO.Rule> shiftRules = new ArrayList<>();
        for (NurseScheduleVO.Rule r : rules) {
            if (r.getShiftId() != null && r.getShiftId() == WARD_LEVEL_SHIFT) {
                wardRule = r;
            } else if (r.getShiftId() != null) {
                shiftRules.add(r);
            }
        }
        Map<String, Integer> dayTotal = new HashMap<>();
        Map<String, Integer> dayShift = new HashMap<>();
        for (NurseScheduleVO.Cell c : cells) {
            if (!Objects.equals(c.getScheduleStatus(), StaffDutyStatusEnum.WORK.getCode())) {
                continue;
            }
            dayTotal.merge(c.getScheduleDate(), 1, Integer::sum);
            if (c.getShiftId() != null) {
                dayShift.merge(c.getScheduleDate() + "|" + c.getShiftId(), 1, Integer::sum);
            }
        }
        List<NurseScheduleVO.Staffing> out = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            String date = d.toString();
            if (wardRule != null) {
                NurseScheduleVO.Staffing s = new NurseScheduleVO.Staffing();
                s.setScheduleDate(date);
                s.setShiftId(WARD_LEVEL_SHIFT);
                s.setShiftName("病区合计");
                s.setStaffCount(dayTotal.getOrDefault(date, 0));
                s.setMinStaff(wardRule.getMinStaff());
                s.setMaxStaff(wardRule.getMaxStaff());
                out.add(s);
            }
            for (NurseScheduleVO.Rule r : shiftRules) {
                NurseScheduleVO.Staffing s = new NurseScheduleVO.Staffing();
                s.setScheduleDate(date);
                s.setShiftId(r.getShiftId());
                s.setShiftName(r.getShiftName());
                s.setStaffCount(dayShift.getOrDefault(date + "|" + r.getShiftId(), 0));
                s.setMinStaff(r.getMinStaff());
                s.setMaxStaff(r.getMaxStaff());
                out.add(s);
            }
        }
        return out;
    }

    private NurseScheduleVO.Warning staffWarning(NurseScheduleVO.Ward ward, String date, long shiftId,
                                                 String shiftName, int actual, int required, boolean gap) {
        NurseScheduleVO.Warning w = new NurseScheduleVO.Warning();
        w.setLevel(1);
        w.setType(gap ? "STAFF_GAP" : "STAFF_OVER");
        w.setScheduleDate(date);
        w.setShiftId(shiftId);
        w.setShiftName(shiftName);
        w.setActual(actual);
        w.setRequired(required);
        w.setMessage(ward.getWardName() + " " + date + "「" + shiftName + "」在岗 " + actual + " 人，"
                + (gap ? "低于标准 " : "超过上限 ") + required + " 人");
        return w;
    }

    private NurseScheduleVO.Warning personWarning(int level, String type, String date, Long shiftId,
                                                  Long employeeId, String nurseName, String message) {
        NurseScheduleVO.Warning w = new NurseScheduleVO.Warning();
        w.setLevel(level);
        w.setType(type);
        w.setScheduleDate(date);
        w.setShiftId(shiftId);
        w.setEmployeeId(employeeId);
        w.setNurseName(nurseName);
        w.setMessage(message);
        return w;
    }

    /**
     * 最长连班判定：命中必须<b>日历连续</b>才算连班 —— 中间有一天没写行（漏排）或写成休息，
     * 连班就重新起算。「没写行」按缺排班另行告警，不在这里替它圆场。
     */
    private void streakWarning(List<NurseScheduleVO.Warning> out, List<NurseScheduleVO.Cell> sortedRows,
                               String nurseName, Long employeeId,
                               Predicate<NurseScheduleVO.Cell> predicate,
                               Integer limit, String type, String label) {
        if (limit == null || limit <= 0) {
            return;
        }
        int streak = 0;
        int best = 0;
        String bestDate = null;
        LocalDate prevHitDate = null;
        for (NurseScheduleVO.Cell c : sortedRows) {
            LocalDate d = LocalDate.parse(c.getScheduleDate());
            if (!predicate.test(c)) {
                streak = 0;
                prevHitDate = null;
                continue;
            }
            streak = prevHitDate != null && prevHitDate.equals(d.minusDays(1)) ? streak + 1 : 1;
            prevHitDate = d;
            if (streak > best) {
                best = streak;
                bestDate = c.getScheduleDate();
            }
        }
        if (best > limit) {
            out.add(personWarning(1, type, bestDate, null, employeeId, nurseName,
                    "「" + nurseName + "」截至 " + bestDate + " 连续" + label + " " + best + " 天，超过上限 " + limit + " 天"));
        }
    }

    /**
     * 病区合计那一行规则（工时/连班上限的唯一来源）
     */
    private Optional<NurseScheduleVO.Rule> wardRule(Long wardId) {
        return enabledRulesOfWard(wardId).stream()
                .filter(r -> r.getShiftId() != null && r.getShiftId() == WARD_LEVEL_SHIFT)
                .findFirst();
    }

    private List<NurseScheduleVO.Rule> selectEnabledRules(Long wardId) {
        return enabledRulesOfWard(wardId);
    }

    /**
     * 排班单元（sql/209）：1-病区 / 2-门诊科室。不传类型按病区处理，存量调用不用跟着改一轮。
     *
     * <p><b>两种单元的 id 不在同一个空间</b>，所以类型必须跟着 id 一起走 ——
     * 只拿 id 去查会把「门诊科室的 id」当成「病区的 id」，查出来的东西张冠李戴。
     */
    private NurseScheduleVO.Ward requireUnit(Integer unitType, Long unitId) {
        int type = unitType == null || unitType == 0 ? UNIT_WARD : unitType;
        // C-非 web 入参：私有 helper，被本类多处排班流程直接以裸 unitId 调用，Bean Validation 不覆盖，保留
        if (unitId == null) {
            throw new BusinessException("请选择排班单元");
        }
        if (type != UNIT_WARD && type != UNIT_CLINIC) {
            throw new BusinessException("排班单元类型只允许 1-病区 / 2-门诊科室");
        }
        NurseScheduleVO.Ward unit = bizNurseScheduleMapper.selectUnit(type, unitId);
        if (unit == null) {
            throw new BusinessException(type == UNIT_WARD ? "病区不存在或已停用" : "门诊科室不存在或已停用");
        }
        if (!deptScopeProvider.canAccessDept(unit.getDeptId())) {
            throw new BusinessException("无权操作「" + unit.getWardName() + "」的排班（不在当前岗位的数据范围内）");
        }
        return unit;
    }

    /**
     * 单元矩阵取格：类型 + id 一起给，避免只认 id 造成的串单元
     */
    private List<NurseScheduleVO.Cell> selectCells(NurseScheduleVO.Ward ward, LocalDate startDate, LocalDate endDate) {
        return bizNurseScheduleMapper.selectCells(unitTypeOf(ward), ward.getWardId(), startDate, endDate);
    }

    private NurseScheduleVO.Ward requireWard(Long wardId) {
        // C-非 web 入参：私有 helper，多个 service 方法共用的参数守卫，Bean Validation 不覆盖（HTTP 侧必填已由各 DTO @NotNull + @Valid 收口），保留
        if (wardId == null) {
            throw new BusinessException("请选择病区");
        }
        NurseScheduleVO.Ward ward = bizNurseScheduleMapper.selectWard(wardId);
        if (ward == null) {
            throw new BusinessException("病区不存在或已停用");
        }
        if (!deptScopeProvider.canAccessDept(ward.getDeptId())) {
            throw new BusinessException("无权操作「" + ward.getWardName() + "」的排班（不在当前岗位的数据范围内）");
        }
        return ward;
    }

    private void fillSnapshot(BizNurseSchedule row, NurseScheduleVO.Ward ward, NurseScheduleVO.Nurse nurse,
                              NurseScheduleVO.ShiftOption shift, StaffDutyStatusEnum status, LocalDate date,
                              Integer workMinutes, String remark, int source, Long staffScheduleId) {
        row.setWardId(ward.getWardId());
        row.setWardName(ward.getWardName());
        row.setDeptId(ward.getDeptId());
        row.setDeptName(ward.getDeptName());
        row.setUnitType(unitTypeOf(ward));
        row.setUnitId(ward.getWardId());
        row.setScheduleDate(date);
        row.setWeekDay(date.getDayOfWeek().getValue());
        row.setEmployeeId(nurse.getEmployeeId());
        row.setEmpCode(nurse.getEmpCode());
        row.setNurseName(nurse.getNurseName());
        row.setNurseTitle(nurse.getNurseTitle());
        row.setShiftId(shift == null ? null : shift.getShiftId());
        row.setShiftName(shift == null ? null : shift.getShiftName());
        row.setStartTime(shift == null ? null : shift.getStartTime());
        row.setEndTime(shift == null ? null : shift.getEndTime());
        row.setWorkMinutes(workMinutes == null ? 0 : workMinutes);
        row.setScheduleStatus(status.getCode());
        row.setScheduleSource(source);
        row.setStaffScheduleId(staffScheduleId);
        row.setRemark(remark);
    }

    /**
     * 当前岗位可见科室；null=不收口（全院），空集合用 -1 兜住，避免 IN () 语法错
     */
    private List<Long> scopedDeptIds(Long requestedDeptId) {
        Long resolved = deptScopeProvider.resolveDeptId(requestedDeptId);
        if (resolved != null) {
            return null;
        }
        Set<Long> allowed = deptScopeProvider.allowedDeptIds();
        if (allowed == null) {
            return null;
        }
        return allowed.isEmpty() ? List.of(-1L) : List.copyOf(allowed);
    }
}
