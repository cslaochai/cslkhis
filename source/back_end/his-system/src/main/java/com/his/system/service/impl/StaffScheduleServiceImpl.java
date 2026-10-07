package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.AttendModeEnum;
import com.his.common.enums.EnableStatusEnum;
import com.his.common.enums.OrgUnitTypeEnum;
import com.his.common.enums.ScheduleChangeTypeEnum;
import com.his.common.enums.StaffDutyStatusEnum;
import com.his.common.enums.StaffScheduleSourceEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.ShiftCoverUtil;
import com.his.system.provider.DeptScopeProvider;
import com.his.system.dto.StaffScheduleCopyDTO;
import com.his.system.dto.StaffScheduleQueryPageDTO;
import com.his.system.dto.StaffScheduleSwapDTO;
import com.his.system.dto.StaffScheduleUpsertDTO;
import com.his.system.entity.BizShift;
import com.his.system.entity.BizStaffSchedule;
import com.his.system.entity.SysDepartment;
import com.his.system.entity.SysEmployee;
import com.his.system.entity.SysEmployeePost;
import com.his.system.entity.SysRole;
import com.his.system.entity.SysWard;
import com.his.system.mapper.BizStaffScheduleMapper;
import com.his.system.mapper.SysDepartmentMapper;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.mapper.SysEmployeePostMapper;
import com.his.system.mapper.SysRoleMapper;
import com.his.system.mapper.SysWardMapper;
import com.his.system.service.ScheduleChangeLogService;
import com.his.system.service.ShiftService;
import com.his.system.service.StaffPlanRuleService;
import com.his.system.service.StaffScheduleService;
import com.his.system.vo.StaffOnDutyVO;
import com.his.system.vo.StaffScheduleVO;
import com.his.system.vo.StaffTypeDayWorkingVO;
import com.his.system.vo.StaffWorkingGroupVO;
import com.his.system.vo.UnitDayWorkingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 全院岗位排班服务实现 —— 排班事实的唯一写入口。
 *
 * <p><b>三条铁律</b>：
 * <ol>
 *   <li><b>时间只由班次带出</b>：前端传的起止时间一律不认，休息/请假/培训/停班必须无班次、无时间、无工时；</li>
 *   <li><b>同一个人同一天不能有两个重叠的班</b>：跨零点班归开始日，所以重叠判定要按
 *       「昨天夜里延伸进来的那段 + 今天 + 明天凌晨被延伸到的那段」三个自然日一起算，
 *       只比日期会把「昨晚上到今早 8 点」与「今早 6 点到 10 点」判成不冲突；</li>
 *   <li><b>岗位类别由人事岗位派生</b>：不是前端选一个类别就存一个类别，
 *       否则「这个人到底以什么身份在岗」会有两套答案。</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class StaffScheduleServiceImpl extends ServiceImpl<BizStaffScheduleMapper, BizStaffSchedule>
        implements StaffScheduleService {
    private final DeptScopeProvider deptScopeProvider;

    /** 无班次（休息/请假/培训/停班）与全院级的单元ID都用 0 表达，NULL 会让唯一键失效 */
    private static final long ID_NONE = 0L;
    /** 一周七天，复制周期按星期对齐 */
    private static final int DAYS_OF_WEEK = 7;
    private static final String[] WEEK_DAY_TEXTS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    private final SysEmployeeMapper employeeMapper;
    private final SysEmployeePostMapper employeePostMapper;
    private final SysRoleMapper roleMapper;
    private final SysDepartmentMapper departmentMapper;
    private final SysWardMapper wardMapper;
    private final ShiftService shiftService;
    private final StaffPlanRuleService planRuleService;
    private final ScheduleChangeLogService changeLogService;

    @Override
    public PageResult<StaffScheduleVO> pageVO(StaffScheduleQueryPageDTO dto) {
        LambdaQueryWrapper<BizStaffSchedule> wrapper = scoped(new LambdaQueryWrapper<BizStaffSchedule>()
                .ge(dto.getStartDate() != null, BizStaffSchedule::getScheduleDate, dto.getStartDate())
                .le(dto.getEndDate() != null, BizStaffSchedule::getScheduleDate, dto.getEndDate())
                .eq(dto.getOrgType() != null, BizStaffSchedule::getOrgType, dto.getOrgType())
                .eq(dto.getOrgId() != null, BizStaffSchedule::getOrgId, dto.getOrgId())
                .eq(dto.getDeptId() != null, BizStaffSchedule::getDeptId, dto.getDeptId())
                .eq(dto.getStaffType() != null, BizStaffSchedule::getStaffType, dto.getStaffType())
                .eq(dto.getDutyStatus() != null, BizStaffSchedule::getDutyStatus, dto.getDutyStatus())
                .eq(dto.getClinicFlag() != null, BizStaffSchedule::getClinicFlag, dto.getClinicFlag()));
        if (StringUtils.hasText(dto.getKeyword())) {
            String keyword = dto.getKeyword().trim();
            wrapper.and(w -> w.like(BizStaffSchedule::getEmployeeName, keyword)
                    .or().like(BizStaffSchedule::getEmpCode, keyword));
        }
        // 二级键 id：同人同日多班时翻页顺序才稳定（否则翻页会重复同一行、漏掉另一行）
        wrapper.orderByAsc(BizStaffSchedule::getScheduleDate).orderByAsc(BizStaffSchedule::getStartTime)
                .orderByAsc(BizStaffSchedule::getId);
        Page<BizStaffSchedule> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        Map<Long, BizShift> shifts = shiftService.mapByIds(shiftIdsOf(page.getRecords()));
        List<StaffScheduleVO> vos = new ArrayList<>(page.getRecords().size());
        for (BizStaffSchedule row : page.getRecords()) {
            vos.add(toVO(row, shifts.get(row.getShiftId())));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String upsert(StaffScheduleUpsertDTO dto) {
        BizStaffSchedule schedule = compose(dto, StaffScheduleSourceEnum.MANUAL);
        if (isDuplicated(schedule)) {
            throw new BusinessException("「" + schedule.getEmployeeName() + "」在 "
                    + schedule.getScheduleDate() + " 已经排过「" + shiftLabelOf(schedule) + "」，不要重复排同一个班");
        }
        assertTimeFree(schedule);
        // 改之前长什么样要先记住：人力标准的下限只在「这次操作让某个班少了一个人」时才拦，
        // 新增和「改成上班」是往里加人，越加越多，拿下限去卡等于让排班员排不出第一版。
        BizStaffSchedule before = dto.getId() == null ? null : getById(dto.getId());
        saveOrUpdate(schedule);
        String unitTip = planRuleService.reviewAfterChange(schedule.getOrgType(), schedule.getOrgId(),
                schedule.getShiftId(), schedule.getStaffType(), schedule.getScheduleDate(),
                headcountDrops(before, schedule));
        return tipJoin(unitTip, reviewEmployee(schedule));
    }

    /** 人力闸门的两半：单元够不够人 + 这个人被排得狠不狠，两句话合成一段提示给页面 */
    private String reviewEmployee(BizStaffSchedule schedule) {
        return planRuleService.reviewEmployee(schedule.getId(), schedule.getEmployeeId(),
                schedule.getOrgType(), schedule.getOrgId(), schedule.getStaffType(), schedule.getScheduleDate());
    }

    private static String tipJoin(String... tips) {
        StringBuilder sb = new StringBuilder();
        for (String t : tips) {
            if (t == null || t.isBlank()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("；");
            }
            sb.append(t);
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    /**
     * 这次保存有没有让某个班净减一个人。
     *
     * <p>三种减员：上班 → 休息/请假/停班；换了个班次（原班少一人）；换了个排班单元（原单元少一人）。
     * 新增一行（before 为空）一律不算减员 —— 空表上排第一个人被下限拦住是最典型的一种卡死。
     */
    private boolean headcountDrops(BizStaffSchedule before, BizStaffSchedule now) {
        if (before == null || !StaffDutyStatusEnum.isWorking(before.getDutyStatus())) {
            return false;
        }
        if (!StaffDutyStatusEnum.isWorking(now.getDutyStatus())) {
            return true;
        }
        return !Objects.equals(before.getShiftId(), now.getShiftId())
                || !Objects.equals(before.getOrgType(), now.getOrgType())
                || !Objects.equals(before.getOrgId(), now.getOrgId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizStaffSchedule ensureForClinic(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source) {
        return ensureAttendance(dto, source);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizStaffSchedule ensureAttendance(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source) {
        BizStaffSchedule schedule = compose(dto, source);
        return reuseOrSave(dto, schedule, sameShiftRow(schedule));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizStaffSchedule ensureForUnit(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source) {
        requireOwnedRow(dto);
        BizStaffSchedule schedule = compose(dto, source);
        return reuseOrSave(dto, schedule, sameUnitShiftRow(schedule));
    }

    /**
     * 入参带 id 时，那条事实必须是这个人这一天的，否则 saveOrUpdate 会把<b>别人</b>那条行改掉。
     *
     * <p>护理/值守改格子时会把自己上次回写的事实 id 传回来（这样改班不至于换一条新 id，
     * 外部引用行不会漂），传错人过去等于给了个可以改任意排班的后门。
     */
    private void requireOwnedRow(StaffScheduleUpsertDTO dto) {
        if (dto.getId() == null) {
            return;
        }
        BizStaffSchedule owned = getById(dto.getId());
        if (owned == null) {
            throw new BusinessException("这条排班事实已不存在（可能已被删除），请刷新后重试");
        }
        if (!Objects.equals(owned.getEmployeeId(), dto.getEmployeeId())
                || !Objects.equals(owned.getScheduleDate(), dto.getScheduleDate())) {
            throw new BusinessException("排班事实与人员/日期对不上，请刷新后重试");
        }
        dto.setOrgType(owned.getOrgType());
        dto.setOrgId(owned.getOrgId());
    }

    /**
     * 键上没有冲突 → 落这条；键上已有行 → 校验它跟本次要表达的是不是同一件事，是就复用。
     *
     * <p>「撞已存在的行不报错而是复用」的原因写在 {@link #ensureForClinic} 上：
     * 号源层不允许自己决定「这个人今天上不上班」，它只能借用已有事实。
     */
    private BizStaffSchedule reuseOrSave(StaffScheduleUpsertDTO dto, BizStaffSchedule schedule,
                                         BizStaffSchedule exist) {
        if (exist == null || Objects.equals(exist.getId(), dto.getId())) {
            // 目标键上要么没人、要么就是自己（出诊计划换了人/换班 = 键本身在动）：直接落这条事实
            assertTimeFree(schedule);
            saveOrUpdate(schedule);
            // 落完立刻把人力闸门走一遍：单元够不够人（reviewAfterChange）+ 这个人被排得狠不狠（reviewEmployee）
            planRuleService.reviewAfterChange(schedule.getOrgType(), schedule.getOrgId(),
                    schedule.getShiftId(), schedule.getStaffType(), schedule.getScheduleDate(), false);
            String personTip = reviewEmployee(schedule);
            if (personTip != null) {
                // 这条线的返回值是实体不是提示文案（出诊/护理要的是那条事实），
                // 超工时这类「只提示不拦」的结论在这里留痕，等 P3 做 backdrop 报表时一并纳管
                log.warn("[排班] " + schedule.getEmployeeName() + " " + schedule.getScheduleDate()
                        + " " + schedule.getOrgName() + "：" + personTip);
            }
            return schedule;
        }
        // 复用已有的那条事实，但前提是它跟本次要表达的是同一件事：
        // 这个键已经排成休息或听班，再挂一条出诊/值守就是同一时刻两种在岗状态
        if (!Objects.equals(exist.getDutyStatus(), schedule.getDutyStatus())) {
            throw new BusinessException("「" + exist.getEmployeeName() + "」在 " + exist.getScheduleDate()
                    + " 的这个班已经排成" + StaffDutyStatusEnum.getText(exist.getDutyStatus())
                    + "，不能同时排成" + StaffDutyStatusEnum.getText(schedule.getDutyStatus())
                    + "；出诊与值守都认这条排班，要改请到「全院排班」里改它");
        }
        if (!AttendModeEnum.releasesSource(exist.getAttendMode())) {
            throw new BusinessException("「" + exist.getEmployeeName() + "」在 " + exist.getScheduleDate()
                    + " 是" + AttendModeEnum.getText(exist.getAttendMode())
                    + "，人不在院，排不出诊也接不了值守；要在岗请先改这条排班的响应形态");
        }
        return exist;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizStaffSchedule replaceDayAttendance(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source) {
        // 入参带 id 时留下那条行：改的是同一条出勤事实（改班次/改状态 = 改属性），
        // 不是换一条新的。id 漂了，护理格子回写的 staff_schedule_id、将来要挂的实际出勤
        // 全部会指向一条被物理删掉的行。
        requireOwnedRow(dto);
        if (dto.getOrgType() != null && dto.getOrgId() != null && dto.getEmployeeId() != null
                && dto.getScheduleDate() != null) {
            OrgUnitTypeEnum unitType = OrgUnitTypeEnum.fromCode(dto.getOrgType());
            Long unitId = unitType == OrgUnitTypeEnum.HOSPITAL ? ID_NONE : dto.getOrgId();
            baseMapper.purgeByDay(unitType == null ? dto.getOrgType() : unitType.getCode(),
                    unitId, dto.getEmployeeId(), dto.getScheduleDate(), dto.getId());
        }
        // 「清」是按单元清的，「查」就必须按单元查：
        // 用不含单元的 ensureAttendance 会查到这个人在别的单元那条事实并复用它，
        // 于是新单元的事实根本没生成 —— 护理表说她在病区、底座说她在门诊（G-10 错位）。
        return ensureForUnit(dto, source);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void purgeDayAttendance(Integer orgType, Long orgId, Long employeeId, LocalDate date) {
        if (employeeId == null || date == null) {
            return;
        }
        OrgUnitTypeEnum unitType = OrgUnitTypeEnum.fromCode(orgType);
        if (unitType == null) {
            throw new BusinessException("排班单元类型只允许 " + OrgUnitTypeEnum.whitelistText());
        }
        Long unitId = unitType == OrgUnitTypeEnum.HOSPITAL ? ID_NONE : orgId;
        baseMapper.purgeByDay(unitType.getCode(), unitId, employeeId, date, null);
    }

    /**
     * 提交值 → 排班行：单元、人、班次三段全部服务端重查，前端传的快照一律不认。
     */
    private BizStaffSchedule compose(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source) {
        BizStaffSchedule schedule = new BizStaffSchedule();
        schedule.setId(dto.getId());
        schedule.setScheduleDate(dto.getScheduleDate());
        schedule.setWeekDay(dto.getScheduleDate().getDayOfWeek().getValue());
        schedule.setDutyStatus(dto.getDutyStatus());
        schedule.setAttendMode(dto.getAttendMode() == null ? AttendModeEnum.ON_SITE.getCode() : dto.getAttendMode());
        schedule.setClinicFlag(dto.getClinicFlag() == null ? YesOrNoEnum.NO.getCode() : dto.getClinicFlag());
        schedule.setScheduleSource(source.getCode());
        schedule.setRemark(dto.getRemark());

        StaffDutyStatusEnum status = StaffDutyStatusEnum.fromCode(schedule.getDutyStatus());
        if (status == null) {
            throw new BusinessException("出勤状态只允许 " + StaffDutyStatusEnum.whitelistText());
        }
        if (AttendModeEnum.fromCode(schedule.getAttendMode()) == null) {
            throw new BusinessException("响应形态只允许 " + AttendModeEnum.whitelistText());
        }
        applyUnit(schedule, dto.getOrgType(), dto.getOrgId());
        applyEmployee(schedule, dto.getEmployeeId(), dto.getEmployeePostId());
        applyShift(schedule, dto.getShiftId(), status);
        return schedule;
    }

    private BizStaffSchedule sameShiftRow(BizStaffSchedule schedule) {
        return getOne(new LambdaQueryWrapper<BizStaffSchedule>()
                .eq(BizStaffSchedule::getEmployeeId, schedule.getEmployeeId())
                .eq(BizStaffSchedule::getScheduleDate, schedule.getScheduleDate())
                .eq(BizStaffSchedule::getShiftId, schedule.getShiftId()), false);
    }

    /**
     * 单元内的查重键 = 人 × 日 × 班 × 单元（{@link #ensureForUnit} 用）。
     *
     * <p>班次为空（休息/请假行没有班次）必须走 {@code IS NULL}：
     * {@code eq(shiftId, null)} 拼出来是 {@code shift_id = NULL}，在 SQL 里恒不成立。
     */
    private BizStaffSchedule sameUnitShiftRow(BizStaffSchedule schedule) {
        LambdaQueryWrapper<BizStaffSchedule> wrapper = new LambdaQueryWrapper<BizStaffSchedule>()
                .eq(BizStaffSchedule::getEmployeeId, schedule.getEmployeeId())
                .eq(BizStaffSchedule::getScheduleDate, schedule.getScheduleDate())
                .eq(BizStaffSchedule::getOrgType, schedule.getOrgType())
                .eq(BizStaffSchedule::getOrgId, schedule.getOrgId());
        if (schedule.getShiftId() == null) {
            wrapper.isNull(BizStaffSchedule::getShiftId);
        } else {
            wrapper.eq(BizStaffSchedule::getShiftId, schedule.getShiftId());
        }
        return getOne(wrapper, false);
    }

    /**
     * 这条排班落库前，这个人这一天/这一时段是否还是空的。
     */
    private void assertTimeFree(BizStaffSchedule schedule) {
        assertDayExclusive(schedule);
        if (StaffDutyStatusEnum.isWorking(schedule.getDutyStatus())) {
            assertNoOverlap(schedule);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        BizStaffSchedule row = requireById(id);
        assertDeptAccessible(row.getDeptId());
        // 唯一键不含删除标志 → 物理删（软删行继续占键，「删了再重排同一天同一班」必撞重复键）
        baseMapper.purgeById(id);
        // 删完再核一次人力：把人删到「这个班没人接」必须当场拦住，而不是等明天出事故
        planRuleService.reviewAfterChange(row.getOrgType(), row.getOrgId(),
                row.getShiftId(), row.getStaffType(), row.getScheduleDate(), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void swap(StaffScheduleSwapDTO dto) {
        BizStaffSchedule from = requireById(dto.getFromScheduleId());
        assertDeptAccessible(from.getDeptId());
        String reason = dto.getReason().trim();

        if (dto.getToScheduleId() != null) {
            BizStaffSchedule to = requireById(dto.getToScheduleId());
            if (Objects.equals(from.getEmployeeId(), to.getEmployeeId())) {
                throw new BusinessException("换班双方不能是同一个人");
            }
            // 换班是两个「在岗的人」互换，休息/请假行没有班次与时间可对调
            if (!StaffDutyStatusEnum.isWorking(from.getDutyStatus())
                    || !StaffDutyStatusEnum.isWorking(to.getDutyStatus())) {
                throw new BusinessException("换班只能在两条上班排班之间进行；休息/请假请直接改那条记录的出勤状态");
            }
            if (!Objects.equals(from.getScheduleDate(), to.getScheduleDate())
                    && !Objects.equals(from.getShiftId(), to.getShiftId())) {
                // 不同日互换要各自重算星期与工时快照，跨日又不同班会让「谁欠谁一个班」算不清
                throw new BusinessException("互换换班请选择同一天的两条排班；跨天顶班请改用代班");
            }
            Long fromEmpId = from.getEmployeeId();
            Long fromShiftId = from.getShiftId();
            Long toShiftId = to.getShiftId();
            // 两条排班的**持有人**互换，班次留在各自那条排班上不跟着走：
            // 一行是「人 × 日 × 班」这个键，把 to 的时刻抄到 from 上、又不改 shift_id，
            // 行自己就矛盾了（shift_id=上午门诊却记着下午门诊的时刻），
            // 紧接着的重叠校验还会把老王这台虚构的双重排班当成真冲突拦下来。
            // applyShiftForSwap 传**自己那条**的班次：只按新人的岗位类别复核并重写时刻快照。
            applyEmployee(from, to.getEmployeeId(), null);
            applyShiftForSwap(from, fromShiftId);
            applyEmployee(to, fromEmpId, null);
            applyShiftForSwap(to, toShiftId);
            assertSwappable(from, to.getId());
            assertSwappable(to, from.getId());
            // 两条都在改，数据库的旧行此刻还占着旧人：必须把对手排除掉，否则必然自己跟自己判重叠
            assertNoOverlap(from, to.getId());
            assertNoOverlap(to, from.getId());
            updateById(from);
            updateById(to);
            changeLogService.record(from.getId(), ScheduleChangeTypeEnum.SWAP, fromEmpId,
                    to.getEmployeeId(), fromShiftId, from.getShiftId(), null, reason);
            changeLogService.record(to.getId(), ScheduleChangeTypeEnum.SWAP, to.getEmployeeId(),
                    fromEmpId, toShiftId, to.getShiftId(), null, reason);
            return;
        }

        if (dto.getSubstituteEmployeeId() == null) {
            throw new BusinessException("代班请选择承接的人");
        }
        if (!StaffDutyStatusEnum.isWorking(from.getDutyStatus())) {
            throw new BusinessException("代班是找人接一个在岗的班，这条排班不是上班状态");
        }
        Long originEmpId = from.getEmployeeId();
        Long shiftId = from.getShiftId();
        applyEmployee(from, dto.getSubstituteEmployeeId(), null);
        applyShiftForSwap(from, shiftId);
        assertSwappable(from);
        assertNoOverlap(from);
        updateById(from);
        changeLogService.record(from.getId(), ScheduleChangeTypeEnum.SUBSTITUTE, originEmpId,
                from.getEmployeeId(), shiftId, shiftId, null, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int copyRange(StaffScheduleCopyDTO dto) {
        OrgUnitTypeEnum unitType = OrgUnitTypeEnum.fromCode(dto.getOrgType());
        if (unitType == null) {
            throw new BusinessException("排班单元类型只允许 " + OrgUnitTypeEnum.whitelistText());
        }
        if (dto.getFromStartDate().isAfter(dto.getFromEndDate())) {
            throw new BusinessException("来源区间开始日不能晚于结束日");
        }
        long spanDays = dto.getFromEndDate().toEpochDay() - dto.getFromStartDate().toEpochDay() + 1;
        LocalDate toEndDate = dto.getToStartDate().plusDays(spanDays - 1);
        if (!dto.getToStartDate().isAfter(dto.getFromEndDate()) && !dto.getFromStartDate().isAfter(toEndDate)) {
            throw new BusinessException("目标区间与来源区间重叠，复制会把同一批班排两遍");
        }
        Long orgId = unitType == OrgUnitTypeEnum.HOSPITAL ? ID_NONE : dto.getOrgId();
        if (unitType != OrgUnitTypeEnum.HOSPITAL && (orgId == null || orgId == ID_NONE)) {
            throw new BusinessException("请选择" + unitType.getLabel());
        }
        List<BizStaffSchedule> sources = list(new LambdaQueryWrapper<BizStaffSchedule>()
                .eq(BizStaffSchedule::getOrgType, dto.getOrgType())
                .eq(BizStaffSchedule::getOrgId, orgId)
                .ge(BizStaffSchedule::getScheduleDate, dto.getFromStartDate())
                .le(BizStaffSchedule::getScheduleDate, dto.getFromEndDate())
                .orderByAsc(BizStaffSchedule::getScheduleDate).orderByAsc(BizStaffSchedule::getId));
        if (sources.isEmpty()) {
            throw new BusinessException("来源区间内没有排班可复制");
        }
        int copied = 0;
        for (BizStaffSchedule source : sources) {
            LocalDate targetDate = dto.getToStartDate().plusDays(
                    source.getScheduleDate().toEpochDay() - dto.getFromStartDate().toEpochDay());
            BizStaffSchedule exist = getOne(new LambdaQueryWrapper<BizStaffSchedule>()
                    .eq(BizStaffSchedule::getEmployeeId, source.getEmployeeId())
                    .eq(BizStaffSchedule::getScheduleDate, targetDate)
                    .eq(BizStaffSchedule::getShiftId, source.getShiftId()), false);
            if (exist != null) {
                // 已经排过就跳过：整周复制是「补齐空缺」，不是把人家的班覆盖掉
                continue;
            }
            BizStaffSchedule copy = new BizStaffSchedule();
            copy.setScheduleDate(targetDate);
            copy.setWeekDay(targetDate.getDayOfWeek().getValue());
            copy.setOrgType(source.getOrgType());
            copy.setOrgId(source.getOrgId());
            copy.setOrgName(source.getOrgName());
            copy.setDeptId(source.getDeptId());
            copy.setDeptName(source.getDeptName());
            copy.setEmployeeId(source.getEmployeeId());
            copy.setEmpCode(source.getEmpCode());
            copy.setEmployeeName(source.getEmployeeName());
            copy.setEmployeePostId(source.getEmployeePostId());
            copy.setStaffType(source.getStaffType());
            copy.setShiftId(source.getShiftId());
            copy.setStartTime(source.getStartTime());
            copy.setEndTime(source.getEndTime());
            copy.setDutyStatus(source.getDutyStatus());
            copy.setAttendMode(source.getAttendMode());
            copy.setClinicFlag(source.getClinicFlag());
            copy.setWorkMinutes(source.getWorkMinutes());
            copy.setScheduleSource(StaffScheduleSourceEnum.COPY.getCode());
            copy.setRemark(source.getRemark());
            if (countDayConflict(copy) > 0) {
                // 目标周这天这个人已经是休息/请假了，复制不能把请好的假改成上班
                continue;
            }
            assertNoOverlap(copy);
            save(copy);
            copied++;
        }
        if (copied == 0) {
            throw new BusinessException("目标区间已经排满，没有需要补的空缺");
        }
        return copied;
    }

    @Override
    public List<StaffOnDutyVO> onDutyAt(LocalDateTime at, Integer orgType, Long orgId, Integer staffType) {
        LocalDate today = at.toLocalDate();
        LocalTime now = at.toLocalTime();
        // 跨零点班归开始日：凌晨两点在岗的人是「昨天夜班」的那一行，只查今天会查不到责任人
        List<BizStaffSchedule> rows = list(new LambdaQueryWrapper<BizStaffSchedule>()
                .in(BizStaffSchedule::getScheduleDate, Arrays.asList(today.minusDays(1), today))
                .eq(BizStaffSchedule::getDutyStatus, StaffDutyStatusEnum.WORK.getCode())
                .eq(orgType != null, BizStaffSchedule::getOrgType, orgType)
                .eq(orgId != null, BizStaffSchedule::getOrgId, orgId)
                .eq(staffType != null, BizStaffSchedule::getStaffType, staffType)
                .orderByAsc(BizStaffSchedule::getStartTime).orderByAsc(BizStaffSchedule::getId));
        List<StaffOnDutyVO> vos = new ArrayList<>();
        for (BizStaffSchedule row : rows) {
            if (!ShiftCoverUtil.covers(now, row.getStartTime(), row.getEndTime())) {
                continue;
            }
            StaffOnDutyVO vo = new StaffOnDutyVO();
            vo.setEmployeeId(row.getEmployeeId());
            vo.setEmpCode(row.getEmpCode());
            vo.setEmployeeName(row.getEmployeeName());
            vo.setStaffType(row.getStaffType());
            vo.setStaffTypeName(StaffTypeEnum.getText(row.getStaffType()));
            vo.setOrgType(row.getOrgType());
            vo.setOrgTypeText(OrgUnitTypeEnum.getText(row.getOrgType()));
            vo.setOrgId(row.getOrgId());
            vo.setOrgName(row.getOrgName());
            vo.setDeptId(row.getDeptId());
            vo.setDeptName(row.getDeptName());
            vo.setShiftId(row.getShiftId());
            vo.setStartTime(row.getStartTime());
            vo.setEndTime(row.getEndTime());
            vo.setAttendMode(row.getAttendMode());
            vo.setAttendModeText(AttendModeEnum.getText(row.getAttendMode()));
            vo.setClinicFlag(row.getClinicFlag());
            vo.setScheduleDate(row.getScheduleDate() == null ? null : row.getScheduleDate().toString());
            vos.add(vo);
        }
        fillShiftNames(vos);
        return vos;
    }

    @Override
    public List<BizStaffSchedule> listDay(LocalDate date, Integer orgType, Long orgId, Integer staffType) {
        return list(scoped(new LambdaQueryWrapper<BizStaffSchedule>()
                .eq(BizStaffSchedule::getScheduleDate, date)
                .eq(orgType != null, BizStaffSchedule::getOrgType, orgType)
                .eq(orgId != null, BizStaffSchedule::getOrgId, orgId)
                .eq(staffType != null, BizStaffSchedule::getStaffType, staffType)
                .orderByAsc(BizStaffSchedule::getStartTime).orderByAsc(BizStaffSchedule::getId)));
    }

    @Override
    public boolean releasesClinicSource(BizStaffSchedule schedule) {
        if (schedule == null) {
            return false;
        }
        return StaffTypeEnum.hasSource(schedule.getStaffType())
                && StaffDutyStatusEnum.isWorking(schedule.getDutyStatus())
                && AttendModeEnum.releasesSource(schedule.getAttendMode())
                && Objects.equals(YesOrNoEnum.YES.getCode(), schedule.getClinicFlag());
    }

    @Override
    public List<UnitDayWorkingVO> listUnitDayWorking(LocalDate begin, LocalDate end) {
        // 单元 × 日 = 聚合行再按 (date, org) 折叠（班次与岗位两个维度在这里合掉）
        Map<String, UnitDayWorkingVO> merged = new LinkedHashMap<>();
        for (StaffWorkingGroupVO row : baseMapper.groupWorkingByUnitShift(begin, end)) {
            LocalDate date = row.getScheduleDate();
            Integer orgType = row.getOrgType();
            Long orgId = row.getOrgId();
            String key = date + "|" + orgType + "|" + orgId;
            UnitDayWorkingVO vo = merged.get(key);
            if (vo == null) {
                vo = new UnitDayWorkingVO();
                vo.setOrgType(orgType);
                vo.setOrgId(orgId);
                vo.setOrgName(row.getOrgName());
                vo.setScheduleDate(date);
                vo.setWorkingCount(0L);
                merged.put(key, vo);
            }
            vo.setWorkingCount(vo.getWorkingCount() + row.getCnt());
        }
        return new ArrayList<>(merged.values());
    }

    @Override
    public List<StaffTypeDayWorkingVO> listStaffTypeDayWorking(LocalDate begin, LocalDate end) {
        Map<String, StaffTypeDayWorkingVO> merged = new LinkedHashMap<>();
        for (StaffWorkingGroupVO row : baseMapper.groupWorkingByUnitShift(begin, end)) {
            LocalDate date = row.getScheduleDate();
            Integer staffType = row.getStaffType();
            String key = date + "|" + staffType;
            StaffTypeDayWorkingVO vo = merged.get(key);
            if (vo == null) {
                vo = new StaffTypeDayWorkingVO();
                vo.setStaffType(staffType);
                vo.setScheduleDate(date);
                vo.setWorkingCount(0L);
                merged.put(key, vo);
            }
            vo.setWorkingCount(vo.getWorkingCount() + row.getCnt());
        }
        return new ArrayList<>(merged.values());
    }

    // 写入口的内部收口：单元 / 人 / 班 三段快照全部服务端重查

    private BizStaffSchedule requireById(Long id) {
        BizStaffSchedule row = id == null ? null : getById(id);
        if (row == null) {
            throw new BusinessException("排班记录不存在或已删除，请刷新后重试");
        }
        return row;
    }

    /**
     * 单元归属：病区取它所属科室，科室取自己，全院固定为 0。
     * <br>全院级一定要写成 0 而不是留空 —— 数据范围收口靠「科室在授权集合内或单元类型为全院」，
     * 科室为空时这个或式子永远不成立，受限角色会静默看不到全院班。
     */
    private void applyUnit(BizStaffSchedule schedule, Integer orgType, Long orgId) {
        OrgUnitTypeEnum unitType = OrgUnitTypeEnum.fromCode(orgType);
        if (unitType == null) {
            throw new BusinessException("排班单元类型只允许 " + OrgUnitTypeEnum.whitelistText());
        }
        schedule.setOrgType(unitType.getCode());
        if (unitType == OrgUnitTypeEnum.HOSPITAL) {
            schedule.setOrgId(ID_NONE);
            schedule.setOrgName(unitType.getLabel());
            schedule.setDeptId(ID_NONE);
            schedule.setDeptName(unitType.getLabel());
            return;
        }
        if (orgId == null || orgId == ID_NONE) {
            throw new BusinessException("请选择" + unitType.getLabel());
        }
        if (unitType == OrgUnitTypeEnum.WARD) {
            SysWard ward = wardMapper.selectById(orgId);
            if (ward == null || isDisabled(ward.getStatus())) {
                throw new BusinessException("所选" + unitType.getLabel() + "不存在或已停用");
            }
            SysDepartment dept = departmentMapper.selectById(ward.getDeptId());
            if (dept == null || isDisabled(dept.getStatus())) {
                throw new BusinessException("病区所属科室不存在或已停用，请先修正主数据");
            }
            schedule.setOrgId(ward.getWardId());
            schedule.setOrgName(ward.getWardName());
            schedule.setDeptId(dept.getId());
            schedule.setDeptName(dept.getDeptName());
            return;
        }
        SysDepartment dept = departmentMapper.selectById(orgId);
        if (dept == null || isDisabled(dept.getStatus())) {
            throw new BusinessException("所选" + unitType.getLabel() + "不存在或已停用");
        }
        schedule.setOrgId(dept.getId());
        schedule.setOrgName(dept.getDeptName());
        schedule.setDeptId(dept.getId());
        schedule.setDeptName(dept.getDeptName());
    }

    /**
     * 排班对象与岗位类别：姓名/工号取员工快照，岗位类别由「人 × 科室 × 角色」上的角色派生。
     */
    private void applyEmployee(BizStaffSchedule schedule, Long employeeId, Long employeePostId) {
        SysEmployee employee = employeeId == null ? null : employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new BusinessException("所选排班对象不存在或已删除，请重新选择");
        }
        if (isDisabled(employee.getStatus())) {
            throw new BusinessException("「" + employee.getEmpName() + "」已停用，不能排班");
        }
        SysEmployeePost post = resolvePost(employee, schedule.getDeptId(), employeePostId);
        schedule.setEmployeeId(employee.getId());
        schedule.setEmpCode(employee.getEmpCode());
        schedule.setEmployeeName(employee.getEmpName());
        schedule.setEmployeePostId(post == null ? null : post.getId());
        schedule.setStaffType(staffTypeOf(post, employee));
    }

    private SysEmployeePost resolvePost(SysEmployee employee, Long deptId, Long employeePostId) {
        if (employeePostId != null) {
            SysEmployeePost post = employeePostMapper.selectById(employeePostId);
            if (post == null || !Objects.equals(post.getEmployeeId(), employee.getId())) {
                throw new BusinessException("所选岗位不属于这个人，请重新选择");
            }
            return post;
        }
        List<SysEmployeePost> posts = employeePostMapper.selectList(new LambdaQueryWrapper<SysEmployeePost>()
                .eq(SysEmployeePost::getEmployeeId, employee.getId())
                .orderByAsc(SysEmployeePost::getId));
        if (posts.isEmpty()) {
            throw new BusinessException("「" + employee.getEmpName() + "」还没有分配岗位，"
                    + "请先在员工档案里配置岗位（岗位类别决定这个班能不能排给他）");
        }
        if (deptId != null && deptId != ID_NONE) {
            for (SysEmployeePost post : posts) {
                if (Objects.equals(post.getDeptId(), deptId)) {
                    return post;
                }
            }
        }
        for (SysEmployeePost post : posts) {
            if (Objects.equals(YesOrNoEnum.YES.getCode(), post.getIsPrimary())) {
                return post;
            }
        }
        return posts.get(0);
    }

    private Integer staffTypeOf(SysEmployeePost post, SysEmployee employee) {
        if (post == null || post.getRoleId() == null) {
            throw new BusinessException("「" + employee.getEmpName() + "」的岗位没有挂角色，"
                    + "无法判定岗位类别，请先在岗位配置里补齐角色");
        }
        SysRole role = roleMapper.selectById(post.getRoleId());
        if (role == null) {
            throw new BusinessException("岗位上的角色不存在或已删除，请先修正岗位配置");
        }
        if (StaffTypeEnum.fromCode(role.getStaffType()) == null) {
            throw new BusinessException("角色「" + role.getRoleName() + "」没有配置岗位类别，"
                    + "请在角色管理里指定（岗位类别决定这个班要不要放号）");
        }
        return role.getStaffType();
    }

    /**
     * 班次带出时间与班别：上班必须有班次，非上班一律清空班次与时间。
     */
    private void applyShift(BizStaffSchedule schedule, Long shiftId, StaffDutyStatusEnum status) {
        if (status != StaffDutyStatusEnum.WORK) {
            schedule.setShiftId(ID_NONE);
            schedule.setStartTime(null);
            schedule.setEndTime(null);
            schedule.setWorkMinutes(0);
            schedule.setClinicFlag(YesOrNoEnum.NO.getCode());
            return;
        }
        if (shiftId == null || shiftId == ID_NONE) {
            throw new BusinessException("上班状态必须选一个班次：排班的时间段与工时都由班次带出");
        }
        BizShift shift = requireUsableShift(shiftId, schedule.getStaffType(), schedule.getEmployeeName());
        schedule.setShiftId(shift.getId());
        schedule.setStartTime(shift.getStartTime());
        schedule.setEndTime(shift.getEndTime());
        schedule.setWorkMinutes(shift.getDurationMinutes() == null ? 0 : shift.getDurationMinutes());
        applyClinicFlag(schedule);
    }

    /**
     * 换班/代班时班次不变，只需按新人的岗位类别复核这条班次还能不能给他用。
     */
    private void applyShiftForSwap(BizStaffSchedule schedule, Long shiftId) {
        if (shiftId == null || shiftId == ID_NONE) {
            return;
        }
        BizShift shift = requireUsableShift(shiftId, schedule.getStaffType(), schedule.getEmployeeName());
        schedule.setStartTime(shift.getStartTime());
        schedule.setEndTime(shift.getEndTime());
        schedule.setWorkMinutes(shift.getDurationMinutes() == null ? 0 : shift.getDurationMinutes());
        applyClinicFlag(schedule);
    }

    private BizShift requireUsableShift(Long shiftId, Integer staffType, String employeeName) {
        BizShift shift = shiftService.getById(shiftId);
        if (shift == null) {
            throw new BusinessException("所选班次不存在或已删除，请重新选择");
        }
        if (isDisabled(shift.getStatus())) {
            throw new BusinessException("班次「" + shift.getShiftName() + "」已停用，不能用于排班");
        }
        if (shift.getApplyStaffType() != null && !Objects.equals(shift.getApplyStaffType(), staffType)) {
            throw new BusinessException("班次「" + shift.getShiftName() + "」是"
                    + StaffTypeEnum.getText(shift.getApplyStaffType()) + "用的班次，"
                    + employeeName + "当前的岗位类别是" + StaffTypeEnum.getText(staffType) + "，排不进这个班");
        }
        return shift;
    }

    /**
     * 出诊标记只在「岗位有号源属性 + 非听班」时才可能为是。
     * <br>听班不放号：挂号系统给一个在家待命的人放号，患者到了没人看，那是投诉不是数据问题。
     */
    private void applyClinicFlag(BizStaffSchedule schedule) {
        boolean mayRelease = StaffTypeEnum.hasSource(schedule.getStaffType())
                && AttendModeEnum.releasesSource(schedule.getAttendMode());
        if (!mayRelease) {
            schedule.setClinicFlag(YesOrNoEnum.NO.getCode());
        }
    }

    private boolean isDuplicated(BizStaffSchedule schedule, Long... exceptIds) {
        List<Long> skip = new ArrayList<>();
        if (schedule.getId() != null) {
            skip.add(schedule.getId());
        }
        for (Long id : exceptIds) {
            if (id != null && !skip.contains(id)) {
                skip.add(id);
            }
        }
        // 换班要连对方那条一起排除：它此刻还占着「对方 + 这天 + 这个班」这个键，写完才让开
        return count(new LambdaQueryWrapper<BizStaffSchedule>()
                .eq(BizStaffSchedule::getEmployeeId, schedule.getEmployeeId())
                .eq(BizStaffSchedule::getScheduleDate, schedule.getScheduleDate())
                .eq(BizStaffSchedule::getShiftId, schedule.getShiftId())
                .notIn(!skip.isEmpty(), BizStaffSchedule::getId, skip)) > 0;
    }

    /** 换班/代班落库前：承接的人不能已经排过这个班，那天也不能已经请了假 */
    private void assertSwappable(BizStaffSchedule schedule, Long... exceptIds) {
        if (isDuplicated(schedule, exceptIds)) {
            throw new BusinessException("「" + schedule.getEmployeeName() + "」在 " + schedule.getScheduleDate()
                    + " 已经排过「" + shiftLabelOf(schedule) + "」，不能再接这个班");
        }
        assertDayExclusive(schedule);
    }

    /**
     * 休息/请假/培训/停班按<b>整天</b>占位，与「上班」互斥：同一天既请假又出诊，
     * 号源照放、人却不在，这是投诉不是数据问题。
     */
    private void assertDayExclusive(BizStaffSchedule schedule) {
        if (countDayConflict(schedule) == 0) {
            return;
        }
        if (StaffDutyStatusEnum.isWorking(schedule.getDutyStatus())) {
            throw new BusinessException("「" + schedule.getEmployeeName() + "」在 " + schedule.getScheduleDate()
                    + " 已排休息/请假，先删掉那条记录才能排上班");
        }
        throw new BusinessException("「" + schedule.getEmployeeName() + "」在 " + schedule.getScheduleDate()
                + " 已经排了班，改成" + StaffDutyStatusEnum.getText(schedule.getDutyStatus())
                + "要先删掉那条排班（他的号源也得跟着停）");
    }

    /**
     * 同一<b>排班单元</b>内与本次状态互斥的行数（上班 ↔ 非上班）。
     *
     * <p>互斥的边界是<b>单元</b>而不是「人 × 日」：一格只允许一个走向说的是这个人在这个单元这一天，
     * 不是这个人这一天。同一个护士同一天可以在病区格子上填「休」（这个病区今天没她的班）
     * 同时又在门诊科室上一班 —— 这两件事不矛盾，时间冲突由 {@link #assertNoOverlap} 按班次时段兜。
     * 按「人 × 日」判互斥会让任何一个跨单元支援都写不进去。
     */
    private long countDayConflict(BizStaffSchedule schedule) {
        LambdaQueryWrapper<BizStaffSchedule> wrapper = new LambdaQueryWrapper<BizStaffSchedule>()
                .eq(BizStaffSchedule::getEmployeeId, schedule.getEmployeeId())
                .eq(BizStaffSchedule::getScheduleDate, schedule.getScheduleDate())
                .eq(BizStaffSchedule::getOrgType, schedule.getOrgType())
                .eq(BizStaffSchedule::getOrgId, schedule.getOrgId())
                .ne(schedule.getId() != null, BizStaffSchedule::getId, schedule.getId());
        if (StaffDutyStatusEnum.isWorking(schedule.getDutyStatus())) {
            wrapper.ne(BizStaffSchedule::getDutyStatus, StaffDutyStatusEnum.WORK.getCode());
        } else {
            wrapper.eq(BizStaffSchedule::getDutyStatus, StaffDutyStatusEnum.WORK.getCode());
        }
        return count(wrapper);
    }

    /**
     * 同一个人时间重叠校验：把每条排班摊成「日期 + 起止时刻」的时间段（跨零点班的结束算到次日），
     * 再与相邻自然日的行比对。命中重叠直接拦，报错带上两边的人可读信息。
     */
    private void assertNoOverlap(BizStaffSchedule schedule, Long... exceptIds) {
        List<Long> skip = new ArrayList<>(Arrays.asList(exceptIds));
        if (schedule.getId() != null) {
            skip.add(schedule.getId());
        }
        if (!StaffDutyStatusEnum.isWorking(schedule.getDutyStatus())) {
            return;
        }
        LocalDate date = schedule.getScheduleDate();
        List<BizStaffSchedule> neighbours = list(new LambdaQueryWrapper<BizStaffSchedule>()
                .eq(BizStaffSchedule::getEmployeeId, schedule.getEmployeeId())
                .eq(BizStaffSchedule::getDutyStatus, StaffDutyStatusEnum.WORK.getCode())
                .ge(BizStaffSchedule::getScheduleDate, date.minusDays(1))
                .le(BizStaffSchedule::getScheduleDate, date.plusDays(1))
                .notIn(!skip.isEmpty(), BizStaffSchedule::getId, skip));
        TimeRange mine = intervalOf(schedule, date);
        if (mine == null) {
            return;
        }
        for (BizStaffSchedule other : neighbours) {
            if (Objects.equals(other.getShiftId(), schedule.getShiftId())
                    && Objects.equals(other.getScheduleDate(), date)) {
                continue;
            }
            TimeRange theirs = intervalOf(other, other.getScheduleDate());
            if (theirs == null) {
                continue;
            }
            if (theirs.from().isBefore(mine.to()) && theirs.to().isAfter(mine.from())) {
                throw new BusinessException("「" + schedule.getEmployeeName() + "」在 "
                        + other.getScheduleDate() + " 已排「" + timeText(other) + "」，"
                        + "与本次的「" + timeText(schedule) + "」时间重叠：一个人同一时刻只能在一个班上当岗");
            }
        }
    }

    /** 一条排班摊开后的时间段 */
    private record TimeRange(LocalDateTime from, LocalDateTime to) {
    }

    /** 排班行摊成时间段：跨零点（结束不晚于开始）的结束点算到次日；时间脏了返回 null（不猜） */
    private TimeRange intervalOf(BizStaffSchedule row, LocalDate date) {
        LocalTime start = ShiftCoverUtil.parseShiftTime(row.getStartTime());
        LocalTime end = ShiftCoverUtil.parseShiftTime(row.getEndTime());
        if (start == null || end == null || date == null) {
            return null;
        }
        LocalDateTime from = LocalDateTime.of(date, start);
        LocalDateTime to = end.isAfter(start) ? LocalDateTime.of(date, end) : LocalDateTime.of(date.plusDays(1), end);
        return new TimeRange(from, to);
    }

    private String timeText(BizStaffSchedule row) {
        return shiftLabelOf(row) + " " + row.getStartTime() + "~" + row.getEndTime();
    }

    private String shiftNameOf(Long shiftId) {
        BizShift shift = shiftService.getById(shiftId);
        return shift == null ? null : shift.getShiftName();
    }

    private void assertDeptAccessible(Long deptId) {
        if (deptId == null || deptId == ID_NONE) {
            // 全院级不属于任何科室，人人可见（见 #scoped）
            return;
        }
        if (!deptScopeProvider.canAccessDept(deptId)) {
            throw new BusinessException("没有该科室的排班权限（不在当前岗位的数据范围内）");
        }
    }

    /**
     * 数据范围收口：受限岗位只能看自己科室的行；全院级行对所有人开放 ——
     * 「今天全院谁负责」不是敏感信息，收掉等于让人半夜找不到打电话的对象。
     */
    private LambdaQueryWrapper<BizStaffSchedule> scoped(LambdaQueryWrapper<BizStaffSchedule> wrapper) {
        if (!deptScopeProvider.isScoped()) {
            return wrapper;
        }
        Set<Long> allowed = deptScopeProvider.allowedDeptIds();
        if (allowed == null || allowed.isEmpty()) {
            wrapper.eq(BizStaffSchedule::getOrgType, OrgUnitTypeEnum.HOSPITAL.getCode());
            return wrapper;
        }
        wrapper.and(w -> w.in(BizStaffSchedule::getDeptId, allowed)
                .or().eq(BizStaffSchedule::getOrgType, OrgUnitTypeEnum.HOSPITAL.getCode()));
        return wrapper;
    }

    private StaffScheduleVO toVO(BizStaffSchedule row, BizShift shift) {
        StaffScheduleVO vo = new StaffScheduleVO();
        vo.setId(row.getId());
        vo.setScheduleDate(row.getScheduleDate());
        vo.setWeekDay(row.getWeekDay());
        vo.setWeekDayText(weekDayText(row.getWeekDay()));
        vo.setOrgType(row.getOrgType());
        vo.setOrgTypeText(OrgUnitTypeEnum.getText(row.getOrgType()));
        vo.setOrgId(row.getOrgId());
        vo.setOrgName(row.getOrgName());
        vo.setDeptId(row.getDeptId());
        vo.setDeptName(row.getDeptName());
        vo.setEmployeeId(row.getEmployeeId());
        vo.setEmpCode(row.getEmpCode());
        vo.setEmployeeName(row.getEmployeeName());
        vo.setEmployeePostId(row.getEmployeePostId());
        vo.setStaffType(row.getStaffType());
        vo.setStaffTypeName(StaffTypeEnum.getText(row.getStaffType()));
        vo.setShiftId(row.getShiftId());
        vo.setShiftName(shift == null ? null : shift.getShiftName());
        vo.setStartTime(row.getStartTime());
        vo.setEndTime(row.getEndTime());
        vo.setCrossDay(isCrossDay(row.getStartTime(), row.getEndTime()));
        vo.setDutyStatus(row.getDutyStatus());
        vo.setDutyStatusText(StaffDutyStatusEnum.getText(row.getDutyStatus()));
        vo.setAttendMode(row.getAttendMode());
        vo.setAttendModeText(AttendModeEnum.getText(row.getAttendMode()));
        vo.setClinicFlag(row.getClinicFlag());
        vo.setWorkMinutes(row.getWorkMinutes());
        vo.setWorkHours(row.getWorkMinutes() == null ? null
                : String.format("%.1f", row.getWorkMinutes() / 60.0));
        vo.setScheduleSource(row.getScheduleSource());
        vo.setScheduleSourceText(StaffScheduleSourceEnum.getText(row.getScheduleSource()));
        vo.setTemplateId(row.getTemplateId());
        vo.setRemark(row.getRemark());
        return vo;
    }

    private String weekDayText(Integer weekDay) {
        return weekDay == null || weekDay < 1 || weekDay > DAYS_OF_WEEK ? null : WEEK_DAY_TEXTS[weekDay - 1];
    }

    private static boolean isCrossDay(String startTime, String endTime) {
        LocalTime start = ShiftCoverUtil.parseShiftTime(startTime);
        LocalTime end = ShiftCoverUtil.parseShiftTime(endTime);
        return start != null && end != null && !end.isAfter(start);
    }

    private static boolean isDisabled(Integer status) {
        return status != null && status == EnableStatusEnum.DISABLED.getCode();
    }

    private String shiftLabelOf(BizStaffSchedule schedule) {
        if (schedule.getShiftId() == null || schedule.getShiftId() == ID_NONE) {
            return StaffDutyStatusEnum.getText(schedule.getDutyStatus());
        }
        String name = shiftNameOf(schedule.getShiftId());
        return name == null ? "未命名班次" : name;
    }

    private Set<Long> shiftIdsOf(List<BizStaffSchedule> rows) {
        Set<Long> ids = new HashSet<>();
        for (BizStaffSchedule row : rows) {
            if (row.getShiftId() != null && row.getShiftId() != ID_NONE) {
                ids.add(row.getShiftId());
            }
        }
        return ids;
    }

    private void fillShiftNames(List<StaffOnDutyVO> vos) {
        Set<Long> ids = new HashSet<>();
        for (StaffOnDutyVO vo : vos) {
            if (vo.getShiftId() != null && vo.getShiftId() != ID_NONE) {
                ids.add(vo.getShiftId());
            }
        }
        Map<Long, BizShift> shifts = shiftService.mapByIds(ids);
        for (StaffOnDutyVO vo : vos) {
            BizShift shift = shifts.get(vo.getShiftId());
            vo.setShiftName(shift == null ? null : shift.getShiftName());
        }
    }
}
