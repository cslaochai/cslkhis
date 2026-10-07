package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.EnableStatusEnum;
import com.his.common.enums.OrgUnitTypeEnum;
import com.his.common.enums.StaffDutyStatusEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.dto.StaffPlanRuleQueryPageDTO;
import com.his.system.dto.StaffPlanRuleUpsertDTO;
import com.his.system.entity.*;
import com.his.system.mapper.BizStaffPlanRuleMapper;
import com.his.system.mapper.BizStaffScheduleMapper;
import com.his.system.mapper.SysDepartmentMapper;
import com.his.system.mapper.SysWardMapper;
import com.his.system.service.ShiftService;
import com.his.system.service.StaffPlanRuleService;
import com.his.system.vo.StaffPlanRuleVO;
import com.his.system.vo.StaffShortfallVO;
import com.his.system.vo.StaffWorkingGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 人力配置标准服务实现。
 */
@Service
@RequiredArgsConstructor
public class StaffPlanRuleServiceImpl extends ServiceImpl<BizStaffPlanRuleMapper, BizStaffPlanRule>
        implements StaffPlanRuleService {

    /**
     * 标准上「该单元全部班次共用」的班次位置取值
     */
    private static final long SHIFT_ANY = 0L;
    /**
     * 人的约束往前看几天、往后看几天。
     *
     * <p>取 14 天而不是 7 天：连续上班上限最大可能配到 7 天以上，
     * 而「是不是连续」要从这天向两侧数到断口为止，窗口必须比上限大。
     */
    private static final int PERSON_WINDOW_DAYS = 14;
    private final BizStaffScheduleMapper bizStaffScheduleMapper;
    private final ShiftService shiftService;
    private final SysWardMapper sysWardMapper;
    private final SysDepartmentMapper sysDepartmentMapper;

    private static boolean isNight(BizShift shift) {
        return shift.getIsNight() != null && shift.getIsNight() == 1;
    }

    private static double restHoursOf(BizShift shift) {
        return shift.getNeedRestHours() == null ? 0D : shift.getNeedRestHours().doubleValue();
    }

    private static LocalDateTime startAt(LocalDate date, BizShift shift) {
        return LocalDateTime.of(date, LocalTime.parse(shift.getStartTime()));
    }

    private static LocalDateTime endAt(LocalDate date, BizShift shift) {
        LocalDateTime end = LocalDateTime.of(date, LocalTime.parse(shift.getEndTime()));
        return Integer.valueOf(1).equals(shift.getCrossDay()) ? end.plusDays(1) : end;
    }

    @Override
    public PageResult<StaffPlanRuleVO> pageVO(StaffPlanRuleQueryPageDTO dto) {
        LambdaQueryWrapper<BizStaffPlanRule> wrapper = new LambdaQueryWrapper<BizStaffPlanRule>()
                .eq(dto.getOrgType() != null, BizStaffPlanRule::getOrgType, dto.getOrgType())
                .eq(dto.getOrgId() != null, BizStaffPlanRule::getOrgId, dto.getOrgId())
                .eq(dto.getStaffType() != null, BizStaffPlanRule::getStaffType, dto.getStaffType())
                .eq(dto.getStatus() != null, BizStaffPlanRule::getStatus, dto.getStatus())
                // 二级键 id：同单元同岗位的多条标准排序才稳定
                .orderByAsc(BizStaffPlanRule::getOrgType).orderByAsc(BizStaffPlanRule::getOrgId)
                .orderByAsc(BizStaffPlanRule::getShiftId).orderByAsc(BizStaffPlanRule::getId);
        Page<BizStaffPlanRule> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        Map<Long, BizShift> shifts = shiftService.mapByIds(shiftIdsOf(page.getRecords()));
        List<StaffPlanRuleVO> vos = new ArrayList<>(page.getRecords().size());
        for (BizStaffPlanRule row : page.getRecords()) {
            BizShift shift = row.getShiftId() == null ? null : shifts.get(row.getShiftId());
            StaffPlanRuleVO vo = new StaffPlanRuleVO();
            vo.setId(row.getId());
            vo.setOrgType(row.getOrgType());
            vo.setOrgTypeText(OrgUnitTypeEnum.getText(row.getOrgType()));
            vo.setOrgId(row.getOrgId());
            vo.setOrgName(unitNameOf(row.getOrgType(), row.getOrgId()));
            vo.setShiftId(row.getShiftId());
            vo.setShiftName(shiftNameOf(row.getShiftId(), shift));
            vo.setStaffType(row.getStaffType());
            vo.setStaffTypeName(StaffTypeEnum.getText(row.getStaffType()));
            vo.setMinStaff(row.getMinStaff());
            vo.setMaxStaff(row.getMaxStaff());
            vo.setMaxWeekHours(row.getMaxWeekHours());
            vo.setMaxConsecutiveNightDays(row.getMaxConsecutiveNightDays());
            vo.setMaxConsecutiveWorkDays(row.getMaxConsecutiveWorkDays());
            vo.setStatus(row.getStatus());
            vo.setRemark(row.getRemark());
            vos.add(vo);
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Override
    public Long upsert(StaffPlanRuleUpsertDTO dto) {
        BizStaffPlanRule rule = new BizStaffPlanRule();
        rule.setId(dto.getId());
        rule.setOrgType(dto.getOrgType());
        rule.setOrgId(dto.getOrgId() == null ? 0L : dto.getOrgId());
        rule.setShiftId(dto.getShiftId() == null ? SHIFT_ANY : dto.getShiftId());
        rule.setStaffType(dto.getStaffType());
        rule.setMinStaff(dto.getMinStaff() == null ? 0 : dto.getMinStaff());
        rule.setMaxStaff(dto.getMaxStaff() == null ? 0 : dto.getMaxStaff());
        rule.setMaxWeekHours(dto.getMaxWeekHours());
        rule.setMaxConsecutiveNightDays(dto.getMaxConsecutiveNightDays());
        rule.setMaxConsecutiveWorkDays(dto.getMaxConsecutiveWorkDays());
        rule.setStatus(dto.getStatus() == null ? EnableStatusEnum.ENABLED.getCode() : dto.getStatus());
        rule.setRemark(dto.getRemark());

        if (OrgUnitTypeEnum.fromCode(rule.getOrgType()) == null) {
            throw new BusinessException("排班单元类型只允许 " + OrgUnitTypeEnum.whitelistText());
        }
        StaffTypeEnum.assertValid(rule.getStaffType());
        if (rule.getMinStaff() < 0 || rule.getMaxStaff() < 0) {
            throw new BusinessException("人数标准不能为负数");
        }
        if (rule.getMaxStaff() > 0 && rule.getMinStaff() > rule.getMaxStaff()) {
            throw new BusinessException("最低在岗人数不能大于最高在岗人数");
        }
        // 单元归属与班次都要落到真实对象上，标准才有比对的对象；全院级固定为 0
        rule.setOrgName(applyUnit(rule.getOrgType(), rule.getOrgId()));
        if (rule.getShiftId() != SHIFT_ANY) {
            BizShift shift = shiftService.getById(rule.getShiftId());
            if (shift == null) {
                throw new BusinessException("所选班次不存在或已删除，请重新选择");
            }
        }
        LambdaQueryWrapper<BizStaffPlanRule> dup = new LambdaQueryWrapper<BizStaffPlanRule>()
                .eq(BizStaffPlanRule::getOrgType, rule.getOrgType())
                .eq(BizStaffPlanRule::getOrgId, rule.getOrgId())
                .eq(BizStaffPlanRule::getShiftId, rule.getShiftId())
                .eq(BizStaffPlanRule::getStaffType, rule.getStaffType())
                .ne(rule.getId() != null, BizStaffPlanRule::getId, rule.getId());
        if (count(dup) > 0) {
            throw new BusinessException("该单元在这个班次上对这个岗位类别的标准已存在（同班次可留空表示全部班次共用）");
        }
        saveOrUpdate(rule);
        return rule.getId();
    }

    @Override
    public List<BizStaffPlanRule> listByUnit(Integer orgType, Long orgId, Integer staffType) {
        return list(new LambdaQueryWrapper<BizStaffPlanRule>()
                .eq(BizStaffPlanRule::getOrgType, orgType)
                .eq(BizStaffPlanRule::getOrgId, orgId == null ? 0L : orgId)
                .eq(staffType != null, BizStaffPlanRule::getStaffType, staffType)
                .orderByAsc(BizStaffPlanRule::getShiftId));
    }

    @Override
    public BizStaffPlanRule requireById(Long id) {
        BizStaffPlanRule rule = id == null ? null : getById(id);
        if (rule == null) {
            throw new BusinessException("人力标准不存在或已删除");
        }
        return rule;
    }

    @Override
    public void deleteById(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("人力标准不存在或已删除");
        }
        // 唯一键不含删除标志 → 物理删（软删行会继续占着「单元 × 班次 × 岗位」，重建同一条必撞键）
        baseMapper.purgeById(id);
    }

    @Override
    public String reviewAfterChange(Integer orgType, Long orgId, Long shiftId, Integer staffType, LocalDate date,
                                    boolean enforceMin) {
        BizStaffPlanRule rule = matchRule(orgType, orgId, shiftId, staffType);
        if (rule == null) {
            return null;
        }
        long actual = countOnDuty(orgType, orgId, staffType, date, rule);
        String unitText = unitNameOf(orgType, orgId);
        String shiftText = shiftNameOf(rule.getShiftId(), null);
        String postText = StaffTypeEnum.getText(staffType);
        if (enforceMin && rule.getMinStaff() != null && rule.getMinStaff() > 0 && actual < rule.getMinStaff()) {
            throw new BusinessException("「" + unitText + "」" + date + " " + shiftText + " 的"
                    + postText + "最低需在岗 " + rule.getMinStaff() + " 人，现在只剩 " + actual
                    + " 人：这个班没人接得住，请先补排再改这一条");
        }
        if (rule.getMaxStaff() != null && rule.getMaxStaff() > 0 && actual > rule.getMaxStaff()) {
            return "提示：「" + unitText + "」" + date + " " + shiftText + " 的" + postText
                    + "已排 " + actual + " 人，超出标准上限 " + rule.getMaxStaff() + " 人（不影响保存）";
        }
        return null;
    }

    @Override
    public String reviewEmployee(Long currentScheduleId, Long employeeId, Integer orgType, Long orgId,
                                 Integer staffType, LocalDate date) {
        BizStaffPlanRule rule = matchRule(orgType, orgId, null, staffType);
        if (rule == null || currentScheduleId == null || employeeId == null || date == null) {
            return null;
        }
        LocalDate from = date.minusDays(PERSON_WINDOW_DAYS);
        LocalDate to = date.plusDays(PERSON_WINDOW_DAYS);
        List<BizStaffSchedule> rows = bizStaffScheduleMapper.selectList(new LambdaQueryWrapper<BizStaffSchedule>()
                .eq(BizStaffSchedule::getEmployeeId, employeeId)
                .ge(BizStaffSchedule::getScheduleDate, from)
                .le(BizStaffSchedule::getScheduleDate, to)
                .orderByAsc(BizStaffSchedule::getScheduleDate));
        Map<Long, BizShift> shifts = shiftService.mapByIds(rows.stream()
                .map(BizStaffSchedule::getShiftId).filter(Objects::nonNull).toList());
        String nurse = rows.stream().filter(r -> Objects.equals(r.getId(), currentScheduleId))
                .map(BizStaffSchedule::getEmployeeName).findFirst().orElse("该员工");

        assertRestAfterNight(currentScheduleId, rows, shifts, nurse);
        assertNightStreak(currentScheduleId, rule, rows, shifts, nurse, date);

        List<String> tips = new ArrayList<>();
        String workTip = weekHoursTip(rule, rows, shifts, nurse, date);
        if (workTip != null) {
            tips.add(workTip);
        }
        String streakTip = workStreakTip(currentScheduleId, rule, rows, nurse, date);
        if (streakTip != null) {
            tips.add(streakTip);
        }
        return tips.isEmpty() ? null : String.join("；", tips);
    }

    /**
     * 岗后最短休息：一个班的结束时刻 + 它的 need_rest_hours 之前，不能给这个人排下一个班。
     *
     * <p>只判「本次这条事实参与的那两个组合」—— 存量里躺着的历史违规不在这件事的账上，
     * 一并判会让排班员被前人的排班卡死，什么都改不动。
     */
    private void assertRestAfterNight(Long currentId, List<BizStaffSchedule> rows,
                                      Map<Long, BizShift> shifts, String nurse) {
        for (BizStaffSchedule night : rows) {
            if (!StaffDutyStatusEnum.isWorking(night.getDutyStatus())) {
                continue;
            }
            BizShift nightShift = shifts.get(night.getShiftId());
            if (nightShift == null || !isNight(nightShift) || restHoursOf(nightShift) <= 0) {
                continue;
            }
            LocalDateTime freeFrom = endAt(night.getScheduleDate(), nightShift)
                    .plusMinutes((long) (restHoursOf(nightShift) * 60));
            for (BizStaffSchedule next : rows) {
                if (!StaffDutyStatusEnum.isWorking(next.getDutyStatus())) {
                    continue;
                }
                if (!Objects.equals(next.getId(), currentId) && !Objects.equals(night.getId(), currentId)) {
                    continue;
                }
                if (!next.getScheduleDate().isAfter(night.getScheduleDate())) {
                    continue;
                }
                BizShift nextShift = shifts.get(next.getShiftId());
                if (nextShift == null) {
                    continue;
                }
                LocalDateTime startAt = startAt(next.getScheduleDate(), nextShift);
                if (startAt.isBefore(freeFrom)) {
                    throw new BusinessException("「" + nurse + "」" + night.getScheduleDate() + " 的「"
                            + nightShift.getShiftName() + "」要休到 "
                            + freeFrom.toLocalDate() + " " + freeFrom.toLocalTime()
                            + "（岗后最短休息 " + restHoursOf(nightShift) + " 小时），"
                            + next.getScheduleDate() + " 的「" + nextShift.getShiftName()
                            + "」接不上：先调这一条，或者改班后休息时长");
                }
            }
        }
    }

    /**
     * 连续夜班天数上限（劳动安全，拦）
     */
    private void assertNightStreak(Long currentId, BizStaffPlanRule rule, List<BizStaffSchedule> rows,
                                   Map<Long, BizShift> shifts, String nurse, LocalDate date) {
        int limit = rule.getMaxConsecutiveNightDays() == null ? 0 : rule.getMaxConsecutiveNightDays();
        if (limit <= 0) {
            return;
        }
        if (rows.stream().noneMatch(r -> Objects.equals(r.getId(), currentId) && workingNight(r, shifts))) {
            return;
        }
        int streak = streakOf(rows, shifts, date, true);
        if (streak > limit) {
            throw new BusinessException("「" + nurse + "」截至 " + date + " 已连续排夜班 " + streak
                    + " 天，超过该单元的上限 " + limit + " 天：这段时间里至少要有一天白班或休息");
        }
    }

    /**
     * 连续上班天数上限（总量控制，提示）
     */
    private String workStreakTip(Long currentId, BizStaffPlanRule rule, List<BizStaffSchedule> rows,
                                 String nurse, LocalDate date) {
        int limit = rule.getMaxConsecutiveWorkDays() == null ? 0 : rule.getMaxConsecutiveWorkDays();
        if (limit <= 0) {
            return null;
        }
        if (rows.stream().noneMatch(r -> Objects.equals(r.getId(), currentId)
                && StaffDutyStatusEnum.isWorking(r.getDutyStatus()))) {
            return null;
        }
        int streak = streakOf(rows, null, date, false);
        if (streak > limit) {
            return "「" + nurse + "」截至 " + date + " 已连续上班 " + streak + " 天，超过上限 " + limit
                    + " 天（不影响保存，建议安排一天休息）";
        }
        return null;
    }

    /**
     * 单周工时上限（总量控制，提示）
     */
    private String weekHoursTip(BizStaffPlanRule rule, List<BizStaffSchedule> rows,
                                Map<Long, BizShift> shifts, String nurse, LocalDate date) {
        BigDecimal limit = rule.getMaxWeekHours();
        if (limit == null || limit.signum() <= 0) {
            return null;
        }
        LocalDate monday = date.minusDays(date.getDayOfWeek().getValue() - 1L);
        LocalDate sunday = monday.plusDays(6);
        long minutes = 0;
        for (BizStaffSchedule row : rows) {
            if (!StaffDutyStatusEnum.isWorking(row.getDutyStatus())
                    || row.getScheduleDate().isBefore(monday) || row.getScheduleDate().isAfter(sunday)) {
                continue;
            }
            BizShift shift = shifts.get(row.getShiftId());
            if (shift != null && shift.getDurationMinutes() != null) {
                minutes += shift.getDurationMinutes();
            }
        }
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);
        if (hours.compareTo(limit) > 0) {
            return "「" + nurse + "」" + monday + "~" + sunday + " 已排 " + hours + " 小时，超过单周上限 "
                    + limit + " 小时（不影响保存）";
        }
        return null;
    }

    /**
     * 以 date 为中心向两侧数到断口为止的连续天数（date 那天本身不算在内就返回 0）。
     *
     * @param nightOnly true=只数夜班，false=数所有上班日
     */
    private int streakOf(List<BizStaffSchedule> rows, Map<Long, BizShift> shifts, LocalDate date,
                         boolean nightOnly) {
        Set<LocalDate> hit = new HashSet<>();
        for (BizStaffSchedule row : rows) {
            if (!StaffDutyStatusEnum.isWorking(row.getDutyStatus())) {
                continue;
            }
            if (nightOnly && !workingNight(row, shifts)) {
                continue;
            }
            hit.add(row.getScheduleDate());
        }
        if (!hit.contains(date)) {
            return 0;
        }
        int streak = 1;
        LocalDate cur = date.minusDays(1);
        while (hit.contains(cur)) {
            streak++;
            cur = cur.minusDays(1);
        }
        cur = date.plusDays(1);
        while (hit.contains(cur)) {
            streak++;
            cur = cur.plusDays(1);
        }
        return streak;
    }

    private boolean workingNight(BizStaffSchedule row, Map<Long, BizShift> shifts) {
        if (!StaffDutyStatusEnum.isWorking(row.getDutyStatus())) {
            return false;
        }
        BizShift shift = shifts.get(row.getShiftId());
        return shift != null && isNight(shift);
    }

    /**
     * 命中的标准：先认「这个班次」的那条，没有才退到「全部班次共用」的那条。
     * 顺序不能反——共用标准是兜底，具体班次的标准才是事实。
     */
    private BizStaffPlanRule matchRule(Integer orgType, Long orgId, Long shiftId, Integer staffType) {
        LambdaQueryWrapper<BizStaffPlanRule> wrapper = new LambdaQueryWrapper<BizStaffPlanRule>()
                .eq(BizStaffPlanRule::getOrgType, orgType)
                .eq(BizStaffPlanRule::getOrgId, orgId == null ? 0L : orgId)
                .eq(BizStaffPlanRule::getStaffType, staffType)
                .eq(BizStaffPlanRule::getStatus, EnableStatusEnum.ENABLED.getCode())
                .in(BizStaffPlanRule::getShiftId, shiftId == null ? SHIFT_ANY : shiftId, SHIFT_ANY)
                .orderByDesc(BizStaffPlanRule::getShiftId)
                .last("LIMIT 1");
        return getOne(wrapper, false);
    }

    private long countOnDuty(Integer orgType, Long orgId, Integer staffType, LocalDate date, BizStaffPlanRule rule) {
        LambdaQueryWrapper<BizStaffSchedule> wrapper = new LambdaQueryWrapper<BizStaffSchedule>()
                .eq(BizStaffSchedule::getOrgType, orgType)
                .eq(BizStaffSchedule::getOrgId, orgId == null ? 0L : orgId)
                .eq(BizStaffSchedule::getStaffType, staffType)
                .eq(BizStaffSchedule::getScheduleDate, date)
                .eq(BizStaffSchedule::getDutyStatus, StaffDutyStatusEnum.WORK.getCode());
        if (rule.getShiftId() != null && rule.getShiftId() != SHIFT_ANY) {
            wrapper.eq(BizStaffSchedule::getShiftId, rule.getShiftId());
        }
        return bizStaffScheduleMapper.selectCount(wrapper);
    }

    private String applyUnit(Integer orgType, Long orgId) {
        if (OrgUnitTypeEnum.HOSPITAL.getCode() == orgType) {
            return OrgUnitTypeEnum.HOSPITAL.getLabel();
        }
        if (orgId == null || orgId == 0L) {
            throw new BusinessException("请选择" + OrgUnitTypeEnum.getText(orgType));
        }
        String name = unitNameOf(orgType, orgId);
        if (name == null) {
            throw new BusinessException(OrgUnitTypeEnum.getText(orgType) + "不存在或已停用");
        }
        return name;
    }

    private String unitNameOf(Integer orgType, Long orgId) {
        if (orgType == null) {
            return null;
        }
        if (OrgUnitTypeEnum.HOSPITAL.getCode() == orgType || orgId == null || orgId == 0L) {
            return OrgUnitTypeEnum.HOSPITAL.getLabel();
        }
        if (OrgUnitTypeEnum.WARD.getCode() == orgType) {
            SysWard ward = sysWardMapper.selectById(orgId);
            return ward == null ? null : ward.getWardName();
        }
        SysDepartment dept = sysDepartmentMapper.selectById(orgId);
        return dept == null ? null : dept.getDeptName();
    }

    @Override
    public List<StaffShortfallVO> listShortfalls(LocalDate begin, LocalDate end) {
        List<BizStaffPlanRule> rules = this.list(new LambdaQueryWrapper<BizStaffPlanRule>()
                .eq(BizStaffPlanRule::getStatus, EnableStatusEnum.ENABLED.getCode())
                .gt(BizStaffPlanRule::getMinStaff, 0));
        if (rules.isEmpty()) {
            return List.of();
        }
        // 一次扫描建两份在岗索引：标准行指明班次的按 (日|单元|岗位|班次) 查，
        // shiftId=0 全班次共用的按 (日|单元|岗位) 跨班次合计。COUNT(*) 按班次行数，
        // 同一人同日两班算两份人力 —— 最低在岗本来就是「每个班要几个人」。
        Map<String, Long> byShift = new HashMap<>();
        Map<String, Long> byUnit = new HashMap<>();
        Map<String, String> orgNames = new HashMap<>();
        for (StaffWorkingGroupVO row : bizStaffScheduleMapper.groupWorkingByUnitShift(begin, end)) {
            LocalDate date = row.getScheduleDate();
            Integer orgType = row.getOrgType();
            Long orgId = row.getOrgId();
            Long shiftId = row.getShiftId();
            Integer staffType = row.getStaffType();
            long cnt = row.getCnt();
            String unitKey = date + "|" + orgType + "|" + orgId + "|" + staffType;
            byUnit.merge(unitKey, cnt, Long::sum);
            if (shiftId != null) {
                byShift.merge(unitKey + "|" + shiftId, cnt, Long::sum);
            }
            orgNames.putIfAbsent(orgType + "|" + orgId, row.getOrgName());
        }
        List<Long> namedShiftIds = rules.stream()
                .map(BizStaffPlanRule::getShiftId)
                .filter(id -> id != null && id != SHIFT_ANY).distinct().toList();
        Map<Long, BizShift> shifts = namedShiftIds.isEmpty()
                ? Map.of() : shiftService.mapByIds(namedShiftIds);
        List<StaffShortfallVO> vos = new ArrayList<>();
        for (BizStaffPlanRule rule : rules) {
            String orgKey = rule.getOrgType() + "|" + rule.getOrgId();
            for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
                String unitKey = date + "|" + orgKey + "|" + rule.getStaffType();
                Long actual = rule.getShiftId() == null || rule.getShiftId() == SHIFT_ANY
                        ? byUnit.get(unitKey) : byShift.get(unitKey + "|" + rule.getShiftId());
                long actualCount = actual == null ? 0L : actual;
                if (actualCount >= rule.getMinStaff()) {
                    continue;
                }
                StaffShortfallVO vo = new StaffShortfallVO();
                vo.setScheduleDate(date);
                vo.setOrgType(rule.getOrgType());
                vo.setOrgId(rule.getOrgId());
                vo.setOrgName(TextUtil.hasText(rule.getOrgName())
                        ? rule.getOrgName() : orgNames.getOrDefault(orgKey, unitNameOf(rule.getOrgType(), rule.getOrgId())));
                vo.setShiftId(rule.getShiftId());
                vo.setShiftName(shiftNameOf(rule.getShiftId(), shifts.get(rule.getShiftId())));
                vo.setStaffType(rule.getStaffType());
                vo.setMinStaff(rule.getMinStaff());
                vo.setActualCount(actualCount);
                vo.setShortfall(rule.getMinStaff() - (int) actualCount);
                vos.add(vo);
            }
        }
        return vos;
    }

    /**
     * 班次名：0 表示全部班次共用，出参要能看出来是「整册标准」而不是某个班
     */
    private String shiftNameOf(Long shiftId, BizShift shift) {
        if (shiftId == null || shiftId == SHIFT_ANY) {
            return "全部班次";
        }
        if (shift != null) {
            return shift.getShiftName();
        }
        BizShift loaded = shiftService.getById(shiftId);
        return loaded == null ? null : loaded.getShiftName();
    }

    private Set<Long> shiftIdsOf(List<BizStaffPlanRule> rows) {
        Set<Long> ids = new HashSet<>();
        for (BizStaffPlanRule row : rows) {
            if (row.getShiftId() != null && row.getShiftId() != SHIFT_ANY) {
                ids.add(row.getShiftId());
            }
        }
        return ids;
    }
}
