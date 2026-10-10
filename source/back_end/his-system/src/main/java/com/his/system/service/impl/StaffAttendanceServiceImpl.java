package com.his.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.dto.AttendanceDTO;
import com.his.system.entity.BizShift;
import com.his.system.entity.BizStaffAttendance;
import com.his.system.entity.BizStaffSchedule;
import com.his.system.entity.CurrentUser;
import com.his.system.enums.StaffAttendanceStatusEnum;
import com.his.system.mapper.BizStaffAttendanceMapper;
import com.his.system.service.StaffAttendanceService;
import com.his.system.utils.UserUtils;
import com.his.system.vo.CalibrationAdviceVO;
import com.his.system.vo.StaffAttendanceVO;
import com.his.system.vo.StaffWorktimeVO;
import com.his.system.vo.WorktimeSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 实际出勤服务实现（闭环第 3、4 步）。
 */
@Service
@RequiredArgsConstructor
public class StaffAttendanceServiceImpl extends ServiceImpl<BizStaffAttendanceMapper, BizStaffAttendance>
        implements StaffAttendanceService {

    /**
     * 一次最多对照多少天（全量回溯没有意义，也拖不动）
     */
    private static final int MAX_QUERY_DAYS = 92;

    @Override
    public StaffAttendanceVO checkIn(AttendanceDTO dto) {
        Long employeeId = requireEmployee(dto);
        LocalDate date = dto.getWorkDate() == null ? LocalDate.now() : dto.getWorkDate();
        LocalDateTime at = dto.getCheckIn() == null ? LocalDateTime.now() : dto.getCheckIn();

        List<BizStaffSchedule> plans = baseMapper.selectDayPlanOfEmployee(employeeId, date);
        // 签到时有打卡时刻，一天多班可以按离哪个班最近来认；缺勤确认/工时修正没有时刻，
        // 那些场合必须把班次说清楚
        BizStaffSchedule plan = matchPlan(plans, dto, date, true);

        Long planId;
        Integer orgType;
        Long orgId;
        String orgName;
        Integer staffType;
        Long shiftId;
        Integer planned;
        Integer status;
        String employeeName = null;
        String empCode = null;

        if (plan != null) {
            planId = plan.getId();
            planned = nvlZero(plan.getWorkMinutes());
            staffType = plan.getStaffType();
            shiftId = dto.getShiftId() == null ? plan.getShiftId() : dto.getShiftId();
            employeeName = plan.getEmployeeName();
            empCode = plan.getEmpCode();
            if (dto.getOrgType() != null && dto.getOrgId() != null
                    && !(Objects.equals(dto.getOrgType(), plan.getOrgType())
                    && Objects.equals(dto.getOrgId(), plan.getOrgId()))) {
                // 支援：人被派去别的单元干活。单元按**实际**写（在哪儿干活是事实），
                // 计划那根线还牵着（staff_schedule_id 指向原计划），这样两边都对得上。
                orgType = dto.getOrgType();
                orgId = dto.getOrgId();
                orgName = null;
                status = StaffAttendanceStatusEnum.SUPPORT.getCode();
            } else {
                orgType = plan.getOrgType();
                orgId = plan.getOrgId();
                orgName = plan.getOrgName();
                status = StaffAttendanceStatusEnum.NORMAL.getCode();
            }
        } else {
            // 没排班却来上班：临时加班。这时必须说清人在哪儿干活 —— 没有计划可认，
            // 不给单元就只能通过互斥判断硬猜，那条猜测迟早变成脏数据。
            planId = null;
            planned = 0;
            staffType = dto.getStaffType();
            shiftId = dto.getShiftId() == null ? 0L : dto.getShiftId();
            orgType = requireValue(dto.getOrgType(), "没有排班计划的出勤（加班/支援）必须指定单元类型");
            orgId = requireValue(dto.getOrgId(), "没有排班计划的出勤（加班/支援）必须指定单元ID");
            orgName = null;
            status = StaffAttendanceStatusEnum.OVERTIME.getCode();
        }
        if (dto.getSubstituteFor() != null) {
            status = StaffAttendanceStatusEnum.SUBSTITUTE.getCode();
        }

        BizStaffAttendance row = baseMapper.selectOneAttend(employeeId, date, orgType, orgId, shiftId);
        if (row != null && row.getCheckIn() != null) {
            // 幂等：重复刷卡不代表两个事实，也不该把迟到刷成准时（保留最早那次签到）
            return toVO(row, checkInMessage(row.getAttendanceStatus()));
        }
        if (row == null) {
            row = new BizStaffAttendance();
            row.setScheduleDate(date);
            row.setEmployeeId(employeeId);
            row.setOrgType(orgType);
            row.setOrgId(orgId);
            row.setShiftId(shiftId);
            row.setDelFlag(0);
            row.setStatus(1);
        }
        // 走到这儿要么是新签到，要么是之前被登记成缺勤/手工登记而现在人真来了 ——
        // 人来了就把那条结论推翻，否则名单上会同时挂着"缺勤"和"签到时间"两件矛盾的事。
        row.setStaffScheduleId(planId);
        row.setEmployeeName(employeeName);
        row.setEmpCode(empCode);
        row.setOrgName(orgName);
        row.setStaffType(staffType);
        row.setPlannedMinutes(planned);
        row.setCheckIn(at);
        row.setSubstituteFor(dto.getSubstituteFor());
        row.setAttendanceStatus(status);
        row.setDataSource(dto.getCheckIn() == null ? 1 : 2);
        row.setRemark(normalizeRemark(dto.getRemark()));
        row.setAttendanceStatus(judgeStatus(row, plan, date));
        if (row.getId() == null) {
            baseMapper.insert(row);
        } else {
            // 已经有一条了：要么是被登记成缺勤、要么是手工登记过，现在人真来了 ——
            // 以打卡事实把它扶正，而不是再插一条去撞唯一键。
            baseMapper.updateById(row);
        }
        return toVO(row, checkInMessage(row.getAttendanceStatus()));
    }

    @Override
    public StaffAttendanceVO checkOut(AttendanceDTO dto) {
        BizStaffAttendance row = locateRow(dto, true);
        if (row.getCheckIn() == null) {
            throw new BusinessException("这条出勤没有签到时间，算不出实际工时：没有打卡数据的日子请走「工时修正」由护士长补登");
        }
        LocalDateTime out = dto.getCheckOut() == null ? LocalDateTime.now() : dto.getCheckOut();
        long minutes = Duration.between(row.getCheckIn(), out).toMinutes();
        if (minutes <= 0) {
            throw new BusinessException("签退时间必须晚于签到时间");
        }
        int actual = (int) Math.min(minutes, 1440L);
        row.setCheckOut(out);
        row.setActualMinutes(actual);
        row.setOvertimeMinutes(Math.max(0, actual - nvlZero(row.getPlannedMinutes())));
        BizStaffSchedule plan = row.getStaffScheduleId() == null ? null
                : baseMapper.selectPlanById(row.getStaffScheduleId());
        row.setAttendanceStatus(judgeStatus(row, plan, row.getScheduleDate()));
        if (TextUtil.hasText(dto.getRemark())) {
            row.setRemark(normalizeRemark(dto.getRemark()));
        }
        baseMapper.updateById(row);
        return toVO(row, checkOutMessage(row));
    }

    @Override
    public StaffAttendanceVO markAbsent(AttendanceDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        Long employeeId = requireEmployee(dto);
        LocalDate date = dto.getWorkDate() == null ? LocalDate.now() : dto.getWorkDate();
        List<BizStaffSchedule> plans = baseMapper.selectDayPlanOfEmployee(employeeId, date);
        if (plans.isEmpty()) {
            throw new BusinessException("该员工当天没有「上班」排班，不存在缺勤");
        }
        BizStaffSchedule plan = matchPlan(plans, dto, date, false);
        Integer orgType = dto.getOrgType() == null ? plan.getOrgType() : dto.getOrgType();
        Long orgId = dto.getOrgId() == null ? plan.getOrgId() : dto.getOrgId();
        Long shiftId = dto.getShiftId() == null ? plan.getShiftId() : dto.getShiftId();

        BizStaffAttendance row = baseMapper.selectOneAttend(employeeId, date, orgType, orgId, shiftId);
        if (row != null && row.getCheckIn() != null) {
            throw new BusinessException("这个人当天已经签到过了，不能再确认缺勤；确属异常请先撤销那条出勤登记");
        }
        if (row == null) {
            row = new BizStaffAttendance();
            row.setScheduleDate(date);
            row.setEmployeeId(employeeId);
            row.setEmployeeName(plan.getEmployeeName());
            row.setEmpCode(plan.getEmpCode());
            row.setOrgType(orgType);
            row.setOrgId(orgId);
            row.setOrgName(plan.getOrgName());
            row.setShiftId(shiftId);
            row.setStaffType(plan.getStaffType());
            row.setStaffScheduleId(plan.getId());
            row.setDelFlag(0);
            row.setStatus(1);
        }
        // 缺勤的三个字段必须同时是"零值"：没有签到、没有签退、没有工时。
        // 少清一个，下游就会算出一个"签到时间为空但工时 480 分钟"的行。
        row.setCheckIn(null);
        row.setCheckOut(null);
        row.setActualMinutes(0);
        row.setOvertimeMinutes(0);
        row.setPlannedMinutes(nvlZero(plan.getWorkMinutes()));
        row.setAttendanceStatus(StaffAttendanceStatusEnum.ABSENT.getCode());
        row.setDataSource(1);
        row.setConfirmStatus(1);
        row.setConfirmBy(operatorUser.getRealName());
        row.setConfirmTime(LocalDateTime.now());
        row.setRemark(normalizeRemark(dto.getRemark()));
        if (row.getId() == null) {
            baseMapper.insert(row);
        } else {
            baseMapper.updateById(row);
        }
        return toVO(row, "已确认为缺勤");
    }

    @Override
    public StaffAttendanceVO adjust(AttendanceDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        Long employeeId = requireEmployee(dto);
        LocalDate date = dto.getWorkDate() == null ? LocalDate.now() : dto.getWorkDate();
        if (dto.getActualMinutes() == null || dto.getActualMinutes() < 0) {
            throw new BusinessException("请填写实际工时（分钟，不小于 0）");
        }
        List<BizStaffSchedule> plans = baseMapper.selectDayPlanOfEmployee(employeeId, date);
        BizStaffSchedule plan = matchPlan(plans, dto, date, false);

        Integer orgType = plan != null ? plan.getOrgType()
                : requireValue(dto.getOrgType(), "无排班的补登必须指定单元类型");
        Long orgId = plan != null ? plan.getOrgId()
                : requireValue(dto.getOrgId(), "无排班的补登必须指定单元ID");
        Long shiftId = nvlZero(dto.getShiftId() == null ? (plan == null ? 0L : plan.getShiftId()) : dto.getShiftId());

        BizStaffAttendance row = baseMapper.selectOneAttend(employeeId, date, orgType, orgId, shiftId);
        if (row == null) {
            row = new BizStaffAttendance();
            row.setScheduleDate(date);
            row.setEmployeeId(employeeId);
            row.setEmployeeName(plan == null ? null : plan.getEmployeeName());
            row.setEmpCode(plan == null ? null : plan.getEmpCode());
            row.setOrgType(orgType);
            row.setOrgId(orgId);
            row.setOrgName(plan == null ? null : plan.getOrgName());
            row.setShiftId(shiftId);
            row.setStaffType(dto.getStaffType() == null ? (plan == null ? null : plan.getStaffType()) : dto.getStaffType());
            row.setStaffScheduleId(plan == null ? null : plan.getId());
            row.setDelFlag(0);
            row.setStatus(1);
        }
        int actual = dto.getActualMinutes();
        row.setPlannedMinutes(plan == null ? 0 : nvlZero(plan.getWorkMinutes()));
        row.setActualMinutes(actual);
        row.setOvertimeMinutes(Math.max(0, actual - nvlZero(row.getPlannedMinutes())));
        if (dto.getAttendanceStatus() != null) {
            row.setAttendanceStatus(dto.getAttendanceStatus());
        } else if (row.getAttendanceStatus() == null) {
            row.setAttendanceStatus(plan == null ? StaffAttendanceStatusEnum.OVERTIME.getCode() : StaffAttendanceStatusEnum.NORMAL.getCode());
        }
        row.setSubstituteFor(dto.getSubstituteFor());
        row.setDataSource(1);
        // 这一步本身就是人在登记，不必再走一遍科室确认
        row.setConfirmStatus(1);
        row.setConfirmBy(operatorUser.getRealName());
        row.setConfirmTime(LocalDateTime.now());
        row.setRemark(normalizeRemark(dto.getRemark()));
        if (row.getId() == null) {
            baseMapper.insert(row);
        } else {
            baseMapper.updateById(row);
        }
        return toVO(row, "工时已登记");
    }

    @Override
    public void confirm(Long id, Integer confirmStatus) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizStaffAttendance row = requireRow(id);
        if (confirmStatus == null || confirmStatus < 0 || confirmStatus > 2) {
            throw new BusinessException("确认状态只能是 0-待确认、1-已确认、2-有异议");
        }
        row.setConfirmStatus(confirmStatus);
        row.setConfirmBy(operatorUser.getRealName());
        row.setConfirmTime(LocalDateTime.now());
        baseMapper.updateById(row);
    }

    @Override
    public void remove(Long id) {
        BizStaffAttendance row = requireRow(id);
        // 物理删而非软删：唯一键不含 del_flag，软删行继续占键，撤销后重新签到必然撞重复键
        baseMapper.purgeById(row.getId());
    }

    @Override
    public List<StaffWorktimeVO> comparison(LocalDate startDate, LocalDate endDate, Integer orgType, Long orgId,
                                            Integer staffType, Integer diffType) {
        requireRange(startDate, endDate);
        requireUnitType(orgId, orgType);
        return baseMapper.selectComparison(startDate, endDate, orgType, orgId, staffType, null, diffType);
    }

    @Override
    public List<WorktimeSummaryVO> summary(LocalDate startDate, LocalDate endDate, Integer orgType, Long orgId,
                                           Integer staffType) {
        requireRange(startDate, endDate);
        requireUnitType(orgId, orgType);
        return baseMapper.selectSummary(startDate, endDate, orgType, orgId, staffType);
    }

    @Override
    public List<CalibrationAdviceVO> advice(Integer orgType, Long orgId, Integer staffType) {
        requireUnitType(orgId, orgType);
        List<CalibrationAdviceVO> rows = baseMapper.selectAdvice(orgType, orgId, staffType);
        return rows == null ? Collections.emptyList() : rows;
    }

    // -------------------------------------------------------------------------
    // 内部：判定与定位
    // -------------------------------------------------------------------------

    private StaffAttendanceVO toVO(BizStaffAttendance row, String message) {
        StaffAttendanceVO vo = new StaffAttendanceVO();
        BeanUtils.copyProperties(row, vo);
        vo.setAttendanceStatusText(StaffAttendanceStatusEnum.getText(row.getAttendanceStatus()));
        vo.setMessage(message);
        return vo;
    }

    /**
     * 签到回执只点出「这次签到不是普通到岗」的那四种（迟到/替班/加班/支援）；
     * 正常与早退照旧只报「已签到」—— 早退是签退那一刻才成立的事实，签到时说它属于抢跑结论。
     */
    private String checkInMessage(Integer status) {
        StaffAttendanceStatusEnum item = StaffAttendanceStatusEnum.fromCode(status);
        if (item == null) {
            return "已签到";
        }
        return switch (item) {
            case LATE -> "已签到（迟到）";
            case SUBSTITUTE -> "已签到（替班）";
            case OVERTIME -> "已签到（加班：当天没有排班计划）";
            case SUPPORT -> "已签到（支援：实际出勤单元与计划不同）";
            default -> "已签到";
        };
    }

    private String checkOutMessage(BizStaffAttendance row) {
        return row.getActualMinutes() == null
                ? "已签退" : "已签退，实际工时 " + row.getActualMinutes() + " 分钟";
    }

    /**
     * 迟到/早退判定。<b>与 sql/214 视图 {@code v_staff_worktime} 的 diff_type 是同一套口径</b>。
     *
     * @param plan 这次出勤对应的计划事实（加班/支援这类无计划的传 null，此时不做时间判定）
     */
    private Integer judgeStatus(BizStaffAttendance row, BizStaffSchedule plan, LocalDate date) {
        Integer cur = row.getAttendanceStatus();
        // 替班/加班/支援是按业务性质定性的（替别人、没排班、跨单元），打卡时间改写不了它
        if (cur != null && (StaffAttendanceStatusEnum.SUBSTITUTE.is(cur) || StaffAttendanceStatusEnum.OVERTIME.is(cur) || StaffAttendanceStatusEnum.SUPPORT.is(cur))) {
            return cur;
        }
        // 缺勤是"人没来"的结论；只要签到时间还在，这条结论就被推翻，重新按时间判
        if (row.getCheckIn() == null) {
            return cur == null ? StaffAttendanceStatusEnum.NORMAL.getCode() : cur;
        }
        if (plan == null || plan.getShiftId() == null || plan.getShiftId() <= 0) {
            return StaffAttendanceStatusEnum.NORMAL.getCode();
        }
        BizShift shift = baseMapper.selectShift(plan.getShiftId());
        if (shift == null || shift.getStartTime() == null) {
            return StaffAttendanceStatusEnum.NORMAL.getCode();
        }
        int grace = shift.getLateGraceMinutes() == null ? 0 : shift.getLateGraceMinutes();
        LocalDateTime shiftStart = LocalDateTime.of(date, LocalTime.parse(shift.getStartTime()));
        if (row.getCheckIn().isAfter(shiftStart.plusMinutes(grace))) {
            return StaffAttendanceStatusEnum.LATE.getCode();
        }
        if (row.getCheckOut() != null && shift.getEndTime() != null) {
            LocalDateTime shiftEnd = LocalDateTime.of(date, LocalTime.parse(shift.getEndTime()));
            if (Integer.valueOf(1).equals(shift.getCrossDay())) {
                // 跨零点的班，结束时间是次日 —— 不加这一天，夜班 22:00~08:00 提前跑路判不出来
                shiftEnd = shiftEnd.plusDays(1);
            }
            if (row.getCheckOut().isBefore(shiftEnd)) {
                return StaffAttendanceStatusEnum.EARLY_LEAVE.getCode();
            }
        }
        return StaffAttendanceStatusEnum.NORMAL.getCode();
    }

    /**
     * 找到这次操作针对的那条出勤记录
     */
    private BizStaffAttendance locateRow(AttendanceDTO dto, boolean requireExist) {
        Long employeeId = requireEmployee(dto);
        LocalDate date = dto.getWorkDate() == null ? LocalDate.now() : dto.getWorkDate();
        List<BizStaffAttendance> rows = baseMapper.selectDayOfEmployee(employeeId, date);
        if (rows.isEmpty()) {
            if (requireExist) {
                throw new BusinessException("当天没有出勤记录：请先签到；没有打卡数据的日子请走「工时修正」由护士长补登");
            }
            return null;
        }
        final List<BizStaffAttendance> byShift;
        if (dto.getShiftId() != null) {
            byShift = rows.stream().filter(r -> Objects.equals(dto.getShiftId(), r.getShiftId())).toList();
        } else {
            byShift = rows;
        }
        final List<BizStaffAttendance> hit;
        if (dto.getOrgType() != null && dto.getOrgId() != null) {
            hit = byShift.stream().filter(r -> Objects.equals(dto.getOrgType(), r.getOrgType())
                    && Objects.equals(dto.getOrgId(), r.getOrgId())).toList();
        } else {
            hit = byShift;
        }
        // 优先落在「还没签退」的那条：一个人一天两个班，先签退的应当是前一个班
        BizStaffAttendance row = hit.stream().filter(r -> r.getCheckOut() == null).findFirst()
                .orElseGet(() -> hit.stream().findFirst().orElse(null));
        if (row == null && requireExist) {
            throw new BusinessException("当天没有这个单元/班次的出勤记录：请先签到");
        }
        return row;
    }

    /**
     * 从这个人当天的计划里挑出对得上入参的那条。
     *
     * <p><b>难点是「一天多个班」</b>：一个人同一天可以既排病区护理、又排门诊出诊。
     * 这时候不能默认挑第一条 —— 静默认会把工时记到另一个单元另一个班上，
     * 绩效发完了才发现错。这里的处理分三种情况：
     * <ol>
     *   <li>调用方指定了班次或单元 → 按指定来；</li>
     *   <li>当天只有一个班 → 就是它；</li>
     *   <li>多个班时：签到这种有打卡时刻的动作，按「离哪个班开始时间最近」认
     *       （考勤机的通行做法，也解释得通）；缺勤确认/工时修正这类没有时刻的，
     *       <b>直接报错要求指定班次</b> —— 歧义面前不接受猜测。</li>
     * </ol>
     */
    private BizStaffSchedule matchPlan(List<BizStaffSchedule> plans, AttendanceDTO dto,
                                       LocalDate date, boolean allowNearest) {
        if (plans == null || plans.isEmpty()) {
            return null;
        }
        if (dto.getShiftId() != null) {
            BizStaffSchedule hit = plans.stream()
                    .filter(p -> Objects.equals(dto.getShiftId(), p.getShiftId())).findFirst().orElse(null);
            if (hit == null) {
                throw new BusinessException("该员工当天没有排这个班次的上班计划");
            }
            return hit;
        }
        if (dto.getOrgType() != null && dto.getOrgId() != null) {
            BizStaffSchedule hit = plans.stream()
                    .filter(p -> Objects.equals(dto.getOrgType(), p.getOrgType())
                            && Objects.equals(dto.getOrgId(), p.getOrgId()))
                    .findFirst().orElse(null);
            // ⚠ 指定了单元却在这个单元没有计划时，**不能就此返回 null**。
            // 那是「排了 A 班、实际在 B 干活」的支援场景 —— 走 null 分支会被当成加班，
            // 既丢了计划那根线（staff_schedule_id），又把支援记成了加班。这里继续往下认。
            if (hit != null) {
                return hit;
            }
        }
        if (plans.size() == 1) {
            return plans.get(0);
        }
        if (!allowNearest) {
            throw new BusinessException("这个人当天排了 " + plans.size() + " 个班，请指定是哪个班次");
        }
        LocalDateTime at = dto.getCheckIn() == null ? LocalDateTime.now() : dto.getCheckIn();
        BizStaffSchedule best = null;
        long bestDiff = Long.MAX_VALUE;
        for (BizStaffSchedule candidate : plans) {
            LocalDateTime start = shiftStartOf(candidate.getShiftId(), date);
            if (start == null) {
                continue;
            }
            long diff = Math.abs(Duration.between(start, at).toMinutes());
            if (diff < bestDiff) {
                bestDiff = diff;
                best = candidate;
            }
        }
        return best == null ? plans.get(0) : best;
    }

    /**
     * 班次在指定日期上的开始时刻（跨零点班的结束时间顺延一天，开始时间不变）
     */
    private LocalDateTime shiftStartOf(Long shiftId, LocalDate date) {
        if (shiftId == null || shiftId <= 0) {
            return null;
        }
        BizShift shift = baseMapper.selectShift(shiftId);
        if (shift == null || shift.getStartTime() == null) {
            return null;
        }
        return LocalDateTime.of(date, LocalTime.parse(shift.getStartTime()));
    }

    // -------------------------------------------------------------------------
    // 内部：参数校验与小工具
    // -------------------------------------------------------------------------

    private Long requireEmployee(AttendanceDTO dto) {
        if (dto.getEmployeeId() == null) {
            throw new BusinessException("请先选择要登记出勤的员工");
        }
        return dto.getEmployeeId();
    }

    private BizStaffAttendance requireRow(Long id) {
        if (id == null) {
            throw new BusinessException("请指定要处理的出勤登记");
        }
        BizStaffAttendance row = baseMapper.selectById(id);
        if (row == null || Integer.valueOf(1).equals(row.getDelFlag())) {
            throw new BusinessException("这条出勤登记不存在或已被撤销");
        }
        return row;
    }

    private void requireRange(LocalDate startDate, LocalDate endDate) {
        // C-非 web 入参：私有日期区间守卫，被 comparison/summary 等多个入口共用（与跨字段规则混在同一段），Bean Validation 不覆盖内部调用，保留
        if (startDate == null || endDate == null) {
            throw new BusinessException("请选择日期区间");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        if (startDate.plusDays(MAX_QUERY_DAYS).isBefore(endDate)) {
            throw new BusinessException("一次最多对照 " + MAX_QUERY_DAYS + " 天");
        }
    }

    private void requireUnitType(Long orgId, Integer orgType) {
        // 病区和科室的 id 不在同一个空间，只给 id 等于让系统猜（sql/209 的教训）
        if (orgId != null && orgType == null) {
            throw new BusinessException("查某个单元的出勤时必须同时给单元类型（1-科室 2-病区）");
        }
    }

    private Integer requireValue(Integer value, String message) {
        if (value == null) {
            throw new BusinessException(message);
        }
        return value;
    }

    private Long requireValue(Long value, String message) {
        if (value == null) {
            throw new BusinessException(message);
        }
        return value;
    }

    private int nvlZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Long nvlZero(Long value) {
        return value == null ? 0L : value;
    }

    private String normalizeRemark(String remark) {
        return !TextUtil.hasText(remark) ? null : remark.trim();
    }

}
