package com.his.outpatient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.util.ShiftCoverUtil;
import com.his.common.util.TextUtil;
import com.his.common.utils.UserUtils;
import com.his.outpatient.dto.*;
import com.his.outpatient.entity.*;
import com.his.outpatient.mapper.BizClinicSourceMapper;
import com.his.outpatient.mapper.BizScheduleMapper;
import com.his.outpatient.mapper.BizStaffAttendanceMapper;
import com.his.outpatient.service.ScheduleChangeLogService;
import com.his.outpatient.service.ShiftService;
import com.his.outpatient.service.StaffPlanRuleService;
import com.his.outpatient.service.StaffScheduleService;
import com.his.outpatient.vo.*;
import com.his.spi.service.DeptScopeService;
import com.his.spi.service.SystemFacadeService;
import com.his.spi.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 全院岗位排班服务实现 —— 排班事实的唯一写入口。
 */
@Service
@RequiredArgsConstructor
public class StaffScheduleServiceImpl extends ServiceImpl<BizScheduleMapper, BizSchedule>
        implements StaffScheduleService {
    /**
     * 无班次（休息/请假/培训/停班）与全院级的单元ID都用 0 表达，NULL 会让唯一键失效
     */
    private static final long ID_NONE = 0L;
    /**
     * 一周七天，复制周期按星期对齐
     */
    private static final int DAYS_OF_WEEK = 7;
    private static final String[] WEEK_DAY_TEXTS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    /**
     * 系统管理员角色码：全院岗位排班审核的全局豁免者
     */
    private static final String ROLE_ADMIN = "10012";
    /**
     * 签到时间展示格式
     */
    private static final DateTimeFormatter ATTENDANCE_TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DeptScopeService deptScopeService;
    private final SystemFacadeService systemFacadeService;
    private final ShiftService shiftService;
    private final StaffPlanRuleService staffPlanRuleService;
    private final ScheduleChangeLogService scheduleChangeLogService;
    private final BizStaffAttendanceMapper staffAttendanceMapper;
    /**
     * 门诊号源 Mapper：员工→门诊 联动的只读引用判据。
     * 直接依赖 Mapper 而非 {@code BizClinicSourceService}，避免与门诊侧
     * {@code bindCoreSchedule → ensureForClinic} 形成循环依赖。
     */
    private final BizClinicSourceMapper clinicSourceMapper;

    private static String tipJoin(String... tips) {
        StringBuilder sb = new StringBuilder();
        for (String t : tips) {
            if (!TextUtil.hasText(t)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("；");
            }
            sb.append(t);
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private static boolean isCrossDay(String startTime, String endTime) {
        LocalTime start = ShiftCoverUtil.parseShiftTime(startTime);
        LocalTime end = ShiftCoverUtil.parseShiftTime(endTime);
        return start != null && end != null && !end.isAfter(start);
    }

    private static boolean isDisabled(Integer status) {
        return status != null && status == EnableStatusEnum.DISABLED.getCode();
    }

    /**
     * 该排班事实被门诊号源引用的条数（del_flag=0）。
     * 引用计数是「员工→门诊」联动的判据：&gt;0 时这条行归门诊侧管理，
     * 员工排班侧的删除/换班/停班/发布态动作不得擅自执行，否则产生孤儿号源或状态分叉。
     */
    private long clinicRefCount(Long staffScheduleId) {
        if (staffScheduleId == null) {
            return 0L;
        }
        return clinicSourceMapper.selectCount(new LambdaQueryWrapper<BizClinicSource>()
                .eq(BizClinicSource::getStaffScheduleId, staffScheduleId));
    }

    /**
     * 一批排班行的门诊引用数（按 staff_schedule_id 分组计数），避免逐行查造成 N+1。
     * staffScheduleId 为空的行（手工排班未派生）不参与计数。
     */
    private Map<Long, Long> clinicRefCountsOf(List<Long> staffScheduleIds) {
        if (staffScheduleIds == null || staffScheduleIds.isEmpty()) {
            return Map.of();
        }
        return clinicSourceMapper.selectList(new LambdaQueryWrapper<BizClinicSource>()
                        .select(BizClinicSource::getStaffScheduleId)
                        .in(BizClinicSource::getStaffScheduleId, staffScheduleIds))
                .stream()
                .map(BizClinicSource::getStaffScheduleId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));
    }

    /**
     * 被门诊号源引用的行，不支持员工排班侧的换班/代班/删除/发布态流转。
     */
    private void assertNotClinicBound(BizSchedule schedule) {
        long refCount = clinicRefCount(schedule.getId());
        if (refCount > 0) {
            throw new BusinessException("「" + schedule.getEmployeeName() + "」在 " + schedule.getScheduleDate()
                    + " 的这个班已挂 " + refCount + " 条门诊号源，由门诊排班管理：请到门诊排班页处理");
        }
    }

    /**
     * 出诊行被改为非上班态（停班/请假/培训/休息）时的门诊联动：
     * 把被引用的门诊号源置为停诊（不物理删，有在挂患者时由门诊排班页批量退号）。
     * 有在挂号时返回提示语（沿用「提示不拦」风格）；无需提示返回 null。
     */
    private String stopClinicSources(BizSchedule before) {
        List<BizClinicSource> sources = clinicSourceMapper.selectList(new LambdaQueryWrapper<BizClinicSource>()
                .eq(BizClinicSource::getStaffScheduleId, before.getId()));
        if (sources.isEmpty()) {
            return null;
        }
        int registered = 0;
        for (BizClinicSource source : sources) {
            if (!ScheduleStatusEnum.stopped(source.getStatus())) {
                if (source.getUsedSource() != null && source.getUsedSource() > 0) {
                    registered += source.getUsedSource();
                }
                BizClinicSource update = new BizClinicSource();
                update.setId(source.getId());
                update.setStatus(ScheduleStatusEnum.STOPPED.getCode());
                clinicSourceMapper.updateById(update);
            }
        }
        return registered > 0
                ? "「" + before.getEmployeeName() + "」在 " + before.getScheduleDate() + " 的出诊已随停班停诊，仍有 "
                + registered + " 条在挂号，请到门诊排班页批量退号"
                : "「" + before.getEmployeeName() + "」在 " + before.getScheduleDate() + " 的出诊已随停班停诊";
    }

    /**
     * 停班路径放行前提：单元/人员与改前完全一致。
     * <p>班次/时间不参与比对：applyShift 对非上班态会把 shift_id 归零、时间清空（落库口径就是
     * 「休息行没有班次」），拿归零后的行去跟出诊行比班次必然不等 —— 原实现因此把整条
     * 「上班→休息」停诊联动路径堵死（守卫恒假，转休永远报「请到门诊排班页操作」）。
     * 「顺手改班次」在转休路径上构造不出来（班次被归零），门诊侧要改时间/班次，
     * 先把出勤恢复成上班再走号源域。
     */
    private boolean fieldsUnchangedExceptDuty(BizSchedule before, BizSchedule now) {
        return Objects.equals(before.getEmployeeId(), now.getEmployeeId())
                && Objects.equals(before.getOrgType(), now.getOrgType())
                && Objects.equals(before.getOrgId(), now.getOrgId());
    }

    @Override
    public PageResult<StaffScheduleVO> pageVO(StaffSchedulePageQueryDTO dto) {
        LambdaQueryWrapper<BizSchedule> wrapper = scoped(new LambdaQueryWrapper<BizSchedule>()
                .ge(dto.getStartDate() != null, BizSchedule::getScheduleDate, dto.getStartDate())
                .le(dto.getEndDate() != null, BizSchedule::getScheduleDate, dto.getEndDate())
                .eq(dto.getOrgType() != null, BizSchedule::getOrgType, dto.getOrgType())
                .eq(dto.getOrgId() != null, BizSchedule::getOrgId, dto.getOrgId())
                .eq(dto.getDeptId() != null, BizSchedule::getDeptId, dto.getDeptId())
                .eq(dto.getStaffType() != null, BizSchedule::getStaffType, dto.getStaffType())
                .eq(dto.getDutyStatus() != null, BizSchedule::getDutyStatus, dto.getDutyStatus())
                .eq(dto.getClinicFlag() != null, BizSchedule::getClinicFlag, dto.getClinicFlag()));
        if (TextUtil.hasText(dto.getKeyword())) {
            String keyword = dto.getKeyword().trim();
            wrapper.and(w -> w.like(BizSchedule::getEmployeeName, keyword)
                    .or().like(BizSchedule::getEmpCode, keyword));
        }
        applyScope(wrapper, dto.getScope());
        // 二级键 id：同人同日多班时翻页顺序才稳定（否则翻页会重复同一行、漏掉另一行）
        wrapper.orderByAsc(BizSchedule::getScheduleDate).orderByAsc(BizSchedule::getStartTime)
                .orderByAsc(BizSchedule::getId);
        Page<BizSchedule> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        Map<Long, BizShift> shifts = shiftService.mapByIds(shiftIdsOf(page.getRecords()));
        // 员工→门诊联动：批量带出门诊引用数（>0 的行前端展示「门诊出诊」来源标记）
        Map<Long, Long> clinicRefCounts = clinicRefCountsOf(
                page.getRecords().stream().map(BizSchedule::getId).toList());
        List<StaffScheduleVO> vos = new ArrayList<>(page.getRecords().size());
        for (BizSchedule row : page.getRecords()) {
            StaffScheduleVO vo = toVO(row, shifts.get(row.getShiftId()));
            vo.setClinicSourceCount(clinicRefCounts.getOrDefault(row.getId(), 0L));
            vos.add(vo);
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String upsert(StaffScheduleUpsertDTO dto) {
        BizSchedule schedule = compose(dto, StaffScheduleSourceEnum.MANUAL);
        if (isDuplicated(schedule)) {
            throw new BusinessException("「" + schedule.getEmployeeName() + "」在 "
                    + schedule.getScheduleDate() + " 已经排过「" + shiftLabelOf(schedule) + "」，不要重复排同一个班");
        }
        assertTimeFree(schedule);
        assertQualified(schedule);
        // 改之前长什么样要先记住：人力标准的下限只在「这次操作让某个班少了一个人」时才拦，
        // 新增和「改成上班」是往里加人，越加越多，拿下限去卡等于让排班员排不出第一版。
        BizSchedule before = dto.getId() == null ? null : getById(dto.getId());
        // 员工→门诊联动：门诊绑定行（被号源引用）归门诊侧管理——
        // 只允许「上班 → 非上班」的纯停班路径（联动停诊），其余改动（班次/时间/单元/人员/出勤态回改）一律拒绝
        String clinicTip = null;
        if (before != null && clinicRefCount(before.getId()) > 0) {
            boolean leaving = StaffDutyStatusEnum.isWorking(before.getDutyStatus())
                    && !StaffDutyStatusEnum.isWorking(schedule.getDutyStatus());
            if (leaving && fieldsUnchangedExceptDuty(before, schedule)) {
                clinicTip = stopClinicSources(before);
            } else {
                throw new BusinessException("「" + before.getEmployeeName() + "」在 " + before.getScheduleDate()
                        + " 的这个班已挂门诊号源，由门诊排班管理：班次/时间/单元/出勤态变更请到门诊排班页操作");
            }
        }
        saveOrUpdate(schedule);
        // 门诊出诊域不适用「最低在岗」硬拦：出诊行转休 = 医生停诊请假，是门诊排班的常规操作，
        // 号源侧由 stopClinicSources 联动兜底；硬拦会把「今天只有他一个医生出诊」的科室卡死。
        // 最低在岗是岗位出勤域（病区/急诊值班）的口径，门诊域一律只提示不拦。
        boolean enforceMin = !StaffScheduleScopeEnum.OPD.getCode().equals(dto.getScope())
                && headcountDrops(before, schedule);
        String unitTip = staffPlanRuleService.reviewAfterChange(schedule.getOrgType(), schedule.getOrgId(),
                schedule.getShiftId(), schedule.getStaffType(), schedule.getScheduleDate(), enforceMin);
        return tipJoin(unitTip, reviewEmployee(schedule), clinicTip);
    }

    /**
     * 人力闸门的两半：单元够不够人 + 这个人被排得狠不狠，两句话合成一段提示给页面
     */
    private String reviewEmployee(BizSchedule schedule) {
        return staffPlanRuleService.reviewEmployee(schedule.getId(), schedule.getEmployeeId(),
                schedule.getOrgType(), schedule.getOrgId(), schedule.getStaffType(), schedule.getScheduleDate());
    }

    /**
     * 急诊科室单元全集：sys_department.dept_type 含 6-急诊科室（字典 his_dept_type，增量 SQL 295）。
     * <p>含虚父级（如急危重症中心）—— 资质校验要兜住排到父级上的班，漏一个 ID 就是漏一道闸。
     * 现查不缓存：科室档案上勾「急诊科室」是运行时行为，勾选应立即生效。
     */
    private Set<Long> erDeptIds() {
        return getBaseMapper().selectErDepts(DeptTypeEnum.ER.getCode()).stream()
                .map(ErDeptRow::getId)
                .collect(Collectors.toSet());
    }

    /**
     * 急诊叶子单元：全集内排除「被其他急诊科室指为父级」的虚父级（如急危重症中心）。
     * 值班公开表与固定医师占比只按实际出诊单元统计，虚父级不单列 —— 与字典化前口径一致。
     */
    private Set<Long> erLeafDeptIds() {
        List<ErDeptRow> rows = getBaseMapper().selectErDepts(DeptTypeEnum.ER.getCode());
        if (rows.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Long> parentIds = rows.stream().map(ErDeptRow::getParentId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        return rows.stream().map(ErDeptRow::getId)
                .filter(id -> !parentIds.contains(id))
                .collect(Collectors.toSet());
    }

    /**
     * 排班域过滤（列表侧）：scope=ER 只看急诊科室 + 医生岗；scope=STAFF 排除急诊科室、
     * 医生/护理/护工岗与出诊放号行（clinic_flag=1，归门诊排班页）。
     *
     * <p>MP 对空集合的 in/notIn 不拼条件（等于不过滤），所以 scope=ER 且没配任何急诊科室时
     * 必须显式压成空结果 —— 否则「没有急诊科室」会静默变成「全院的医生都算急诊值班」。
     */
    private void applyScope(LambdaQueryWrapper<BizSchedule> wrapper, Integer scope) {
        if (scope == null) {
            return;
        }
        Set<Long> erIds = erDeptIds();
        if (StaffScheduleScopeEnum.ER.getCode().equals(scope)) {
            if (erIds.isEmpty()) {
                wrapper.apply("1 = 0");
                return;
            }
            wrapper.in(BizSchedule::getOrgId, erIds)
                    .eq(BizSchedule::getStaffType, StaffTypeEnum.DOCTOR.getCode());
            return;
        }
        if (StaffScheduleScopeEnum.STAFF.getCode().equals(scope)) {
            if (!erIds.isEmpty()) {
                wrapper.notIn(BizSchedule::getOrgId, erIds);
            }
            wrapper.ne(BizSchedule::getStaffType, StaffTypeEnum.DOCTOR.getCode())
                    .ne(BizSchedule::getStaffType, StaffTypeEnum.NURSE.getCode())
                    .ne(BizSchedule::getStaffType, StaffTypeEnum.NURSING_ASSISTANT.getCode())
                    // 出诊放号行归门诊排班页（与 pendingApprovalPage 同口径，null 老数据不误伤）
                    .and(w -> w.isNull(BizSchedule::getClinicFlag).or().eq(BizSchedule::getClinicFlag, 0));
        }
        if (StaffScheduleScopeEnum.OPD.getCode().equals(scope)) {
            // 门诊出诊域（出勤侧）：非急诊科室 + 非护理/护工行；医生行（出诊/休息）都归这里
            if (!erIds.isEmpty()) {
                wrapper.notIn(BizSchedule::getOrgId, erIds);
            }
            wrapper.ne(BizSchedule::getStaffType, StaffTypeEnum.NURSE.getCode())
                    .ne(BizSchedule::getStaffType, StaffTypeEnum.NURSING_ASSISTANT.getCode());
        }
    }

    /**
     * 排班域越权拦截（保存侧）：scope=ER 只允许「急诊科室单元 + 医生岗」；
     * scope=STAFF 拦下急诊科室 / 医生 / 护理 / 护工 —— 那些域有专用页面，
     * 从员工排班页排不进去。直调接口绕得过前端藏选项，绕不过这里。
     *
     * <p>放在 compose 末尾：单元/人/班次都已按服务端重查的结果落好，这里判的是最终事实。
     */
    private void assertScope(BizSchedule schedule, Integer scope) {
        if (scope == null) {
            return;
        }
        if (StaffScheduleScopeEnum.ER.getCode().equals(scope)) {
            if (!Objects.equals(schedule.getOrgType(), OrgUnitTypeEnum.DEPT.getCode())
                    || !erDeptIds().contains(schedule.getOrgId())) {
                throw new BusinessException("急诊排班只能排急诊科室单元（科室档案勾选「急诊科室」类型后生效）");
            }
            if (!Objects.equals(schedule.getStaffType(), StaffTypeEnum.DOCTOR.getCode())) {
                throw new BusinessException("急诊排班只能排医生岗位");
            }
            return;
        }
        if (StaffScheduleScopeEnum.STAFF.getCode().equals(scope)) {
            if (erDeptIds().contains(schedule.getOrgId())) {
                throw new BusinessException("急诊科室排班请到「急诊排班」页操作");
            }
            if (Objects.equals(schedule.getStaffType(), StaffTypeEnum.DOCTOR.getCode())) {
                throw new BusinessException("医生排班请到「急诊排班」或「门诊排班」页操作");
            }
            if (Objects.equals(schedule.getStaffType(), StaffTypeEnum.NURSE.getCode())
                    || Objects.equals(schedule.getStaffType(), StaffTypeEnum.NURSING_ASSISTANT.getCode())) {
                throw new BusinessException("护理排班请到「护理排班」页操作");
            }
        }
        if (StaffScheduleScopeEnum.OPD.getCode().equals(scope)) {
            if (!Objects.equals(schedule.getOrgType(), OrgUnitTypeEnum.DEPT.getCode())) {
                throw new BusinessException("门诊排班只能排科室单元");
            }
            if (erDeptIds().contains(schedule.getOrgId())) {
                throw new BusinessException("急诊科室排班请到「急诊排班」页操作");
            }
            if (Objects.equals(schedule.getStaffType(), StaffTypeEnum.NURSE.getCode())
                    || Objects.equals(schedule.getStaffType(), StaffTypeEnum.NURSING_ASSISTANT.getCode())) {
                throw new BusinessException("护理排班请到「护理排班」页操作");
            }
        }
    }

    /**
     * 单元级域校验（整周提交/审核、周期复制用）：按单元定位的操作只许落在本域单元上。
     * 全院级（orgType=3，orgId=0）两个域都放行 —— 全院行本身不分域。
     */
    private void assertUnitScope(Integer orgType, Long orgId, Integer scope, String opName) {
        if (scope == null || !Objects.equals(orgType, OrgUnitTypeEnum.DEPT.getCode())) {
            return;
        }
        if (StaffScheduleScopeEnum.ER.getCode().equals(scope) && !erDeptIds().contains(orgId)) {
            throw new BusinessException(opName + "只能操作急诊科室");
        }
        if (StaffScheduleScopeEnum.STAFF.getCode().equals(scope) && erDeptIds().contains(orgId)) {
            throw new BusinessException("急诊科室的" + opName + "请到「急诊排班」页操作");
        }
        if (StaffScheduleScopeEnum.OPD.getCode().equals(scope) && erDeptIds().contains(orgId)) {
            throw new BusinessException("急诊科室的" + opName + "请到「急诊排班」页操作");
        }
    }

    /**
     * 换班/代班的域校验：只许动本域的行。scope=ER 只许急诊科室 + 医生岗的行；
     * scope=STAFF 不许碰急诊行与医生/护理行（那些域有专用页面）。
     * 与 {@link #assertScope} 同一份口径，只是一个判「将要保存的行」、一个判「已存在的行」。
     */
    private void assertRowScope(BizSchedule row, Integer scope, String opName) {
        if (scope == null) {
            return;
        }
        boolean erRow = Objects.equals(row.getOrgType(), OrgUnitTypeEnum.DEPT.getCode())
                && erDeptIds().contains(row.getOrgId());
        boolean clinicalRow = Objects.equals(row.getStaffType(), StaffTypeEnum.DOCTOR.getCode())
                || Objects.equals(row.getStaffType(), StaffTypeEnum.NURSE.getCode())
                || Objects.equals(row.getStaffType(), StaffTypeEnum.NURSING_ASSISTANT.getCode());
        if (StaffScheduleScopeEnum.ER.getCode().equals(scope)) {
            if (!erRow || !Objects.equals(row.getStaffType(), StaffTypeEnum.DOCTOR.getCode())) {
                throw new BusinessException(opName + "只能操作急诊科室的医生排班行");
            }
            return;
        }
        if (StaffScheduleScopeEnum.STAFF.getCode().equals(scope)) {
            if (erRow) {
                throw new BusinessException("急诊科室的排班" + opName + "请到「急诊排班」页操作");
            }
            if (clinicalRow) {
                throw new BusinessException("医生/护理排班的" + opName + "请到「急诊排班」「门诊排班」或「护理排班」页操作");
            }
        }
        if (StaffScheduleScopeEnum.OPD.getCode().equals(scope)) {
            if (erRow) {
                throw new BusinessException("急诊科室的排班" + opName + "请到「急诊排班」页操作");
            }
            if (Objects.equals(row.getStaffType(), StaffTypeEnum.NURSE.getCode())
                    || Objects.equals(row.getStaffType(), StaffTypeEnum.NURSING_ASSISTANT.getCode())) {
                throw new BusinessException("护理排班的" + opName + "请到「护理排班」页操作");
            }
        }
    }

    /**
     * 值班资质底线校验（《医疗质量安全核心制度要点》国卫医发〔2018〕8号）：
     * 非本机构注册执业人员不得单独值班。
     * <p>落地口径：急诊科室单元 + 医生岗位 + <b>夜班班次</b>（夜间单人值班风险最高）时，
     * 值班人 is_registered=0 拦截；NULL（未登记）放行 —— 档案未维护不卡死排班，
     * 数据维护见「20261020_急诊资质字段」增量 SQL。
     * <p>放在保存（草稿）阶段而非审核阶段：排班员在急诊排班时就该知道谁没资质，
     * 而不是等审核发布时才发现。
     */
    private void assertQualified(BizSchedule schedule) {
        if (schedule.getOrgType() == null
                || !Objects.equals(schedule.getOrgType(), OrgUnitTypeEnum.DEPT.getCode())) {
            return;
        }
        if (!erDeptIds().contains(schedule.getOrgId())) {
            return;
        }
        if (!Objects.equals(schedule.getStaffType(), StaffTypeEnum.DOCTOR.getCode())) {
            return;
        }
        BizShift shift = schedule.getShiftId() == null ? null : shiftService.getById(schedule.getShiftId());
        if (shift == null || !Objects.equals(shift.getIsNight(), 1)) {
            return;
        }
        EmployeeBriefVO emp = systemFacadeService.findEmployee(schedule.getEmployeeId());
        if (emp == null || emp.getIsRegistered() == null || Objects.equals(emp.getIsRegistered(), 1)) {
            return;
        }
        throw new BusinessException("「" + emp.getEmpName() + "」未标记本机构注册执业（is_registered=0），"
                + "不得单独值急诊夜班。请在员工档案维护注册执业标记后再排。");
    }

    /**
     * 这次保存有没有让某个班净减一个人。
     *
     * <p>三种减员：上班 → 休息/请假/停班；换了个班次（原班少一人）；换了个排班单元（原单元少一人）。
     * 新增一行（before 为空）一律不算减员 —— 空表上排第一个人被下限拦住是最典型的一种卡死。
     */
    private boolean headcountDrops(BizSchedule before, BizSchedule now) {
        if (before == null || !StaffDutyStatusEnum.isWorking(before.getDutyStatus())) {
            return false;
        }
        // 草稿/待审核的行本来就不计进在岗（countOnDuty 只认生效态），改它不构成减员，
        // 否则排班员连自己没发布的第一版都改不动
        if (!PublishStatusEnum.isEffective(before.getPublishStatus())) {
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
    public BizSchedule ensureForClinic(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source) {
        return ensureAttendance(dto, source);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizSchedule ensureAttendance(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source) {
        BizSchedule schedule = compose(dto, source);
        return reuseOrSave(dto, schedule, sameShiftRow(schedule));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizSchedule ensureForUnit(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source) {
        requireOwnedRow(dto);
        BizSchedule schedule = compose(dto, source);
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
        BizSchedule owned = getById(dto.getId());
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
    private BizSchedule reuseOrSave(StaffScheduleUpsertDTO dto, BizSchedule schedule,
                                    BizSchedule exist) {
        if (exist == null || Objects.equals(exist.getId(), dto.getId())) {
            // 目标键上要么没人、要么就是自己（出诊计划换了人/换班 = 键本身在动）：直接落这条事实
            assertTimeFree(schedule);
            saveOrUpdate(schedule);
            // 落完立刻把人力闸门走一遍：单元够不够人（reviewAfterChange）+ 这个人被排得狠不狠（reviewEmployee）
            staffPlanRuleService.reviewAfterChange(schedule.getOrgType(), schedule.getOrgId(),
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
                    + " 的这个班已经排成" + StaffDutyStatusEnum.getLabel(exist.getDutyStatus())
                    + "，不能同时排成" + StaffDutyStatusEnum.getLabel(schedule.getDutyStatus())
                    + "；出诊与值守都认这条排班，要改请到「全院排班」里改它");
        }
        if (!AttendModeEnum.releasesSource(exist.getAttendMode())) {
            throw new BusinessException("「" + exist.getEmployeeName() + "」在 " + exist.getScheduleDate()
                    + " 是" + AttendModeEnum.getLabel(exist.getAttendMode())
                    + "，人不在院，排不出诊也接不了值守；要在岗请先改这条排班的响应形态");
        }
        return exist;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizSchedule replaceDayAttendance(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source) {
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
            throw new BusinessException("排班单元类型取值不合法");
        }
        Long unitId = unitType == OrgUnitTypeEnum.HOSPITAL ? ID_NONE : orgId;
        baseMapper.purgeByDay(unitType.getCode(), unitId, employeeId, date, null);
    }

    /**
     * 提交值 → 排班行：单元、人、班次三段全部服务端重查，前端传的快照一律不认。
     */
    private BizSchedule compose(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source) {
        BizSchedule schedule = new BizSchedule();
        schedule.setId(dto.getId());
        schedule.setScheduleDate(dto.getScheduleDate());
        schedule.setWeekDay(dto.getScheduleDate().getDayOfWeek().getValue());
        schedule.setDutyStatus(dto.getDutyStatus());
        schedule.setAttendMode(dto.getAttendMode() == null ? AttendModeEnum.ON_SITE.getCode() : dto.getAttendMode());
        schedule.setClinicFlag(dto.getClinicFlag() == null ? YesOrNoEnum.NO.getCode() : dto.getClinicFlag());
        schedule.setScheduleSource(source.getCode());
        // 发布态一律在这里定，不留给库默认值：调用方显式带入的用带入值，
        // 没带的按「新行=草稿、改单按原态迁移」算，否则审核闸会被静默绕过。
        schedule.setPublishStatus(resolvePublishStatus(dto.getId(), dto.getPublishStatus()));
        schedule.setRemark(dto.getRemark());

        StaffDutyStatusEnum status = StaffDutyStatusEnum.fromCode(schedule.getDutyStatus());
        if (status == null) {
            throw new BusinessException("出勤状态取值不合法");
        }
        if (AttendModeEnum.fromCode(schedule.getAttendMode()) == null) {
            throw new BusinessException("响应形态取值不合法");
        }
        applyUnit(schedule, dto.getOrgType(), dto.getOrgId());
        applyEmployee(schedule, dto.getEmployeeId(), dto.getEmployeePostId());
        applyShift(schedule, dto.getShiftId(), status);
        assertScope(schedule, dto.getScope());
        return schedule;
    }

    /**
     * 排班事实的发布态归属：调用方带入的用带入值（出诊派生跟号源行、值守派生跟值守行），
     * 没带入的按「新行落草稿、改单按原态迁移」定 —— 已发布/已变更/调班中的行被改过就是已变更，
     * 草稿与待审核改完仍是原态（没发布过的东西不存在「变更」）。
     *
     * <p>这里必须给出值而不是留 null：本列 NOT NULL 且无库默认值，漏列会让整条 INSERT 报 1364。
     */
    private Integer resolvePublishStatus(Long id, Integer submitted) {
        if (submitted != null) {
            return submitted;
        }
        BizSchedule exist = id == null ? null : getById(id);
        if (exist == null) {
            return PublishStatusEnum.DRAFT.getCode();
        }
        Integer old = exist.getPublishStatus();
        if (old == null || PublishStatusEnum.ADJUSTING.getCode().equals(old)
                || PublishStatusEnum.isEffective(old)) {
            return PublishStatusEnum.CHANGED.getCode();
        }
        return old;
    }

    private BizSchedule sameShiftRow(BizSchedule schedule) {
        return getOne(new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getEmployeeId, schedule.getEmployeeId())
                .eq(BizSchedule::getScheduleDate, schedule.getScheduleDate())
                .eq(BizSchedule::getShiftId, schedule.getShiftId()), false);
    }

    /**
     * 单元内的查重键 = 人 × 日 × 班 × 单元（{@link #ensureForUnit} 用）。
     *
     * <p>班次为空（休息/请假行没有班次）必须走 {@code IS NULL}：
     * {@code eq(shiftId, null)} 拼出来是 {@code shift_id = NULL}，在 SQL 里恒不成立。
     */
    private BizSchedule sameUnitShiftRow(BizSchedule schedule) {
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getEmployeeId, schedule.getEmployeeId())
                .eq(BizSchedule::getScheduleDate, schedule.getScheduleDate())
                .eq(BizSchedule::getOrgType, schedule.getOrgType())
                .eq(BizSchedule::getOrgId, schedule.getOrgId());
        if (schedule.getShiftId() == null) {
            wrapper.isNull(BizSchedule::getShiftId);
        } else {
            wrapper.eq(BizSchedule::getShiftId, schedule.getShiftId());
        }
        return getOne(wrapper, false);
    }

    /**
     * 这条排班落库前，这个人这一天/这一时段是否还是空的。
     */
    private void assertTimeFree(BizSchedule schedule) {
        assertDayExclusive(schedule);
        if (StaffDutyStatusEnum.isWorking(schedule.getDutyStatus())) {
            assertNoOverlap(schedule);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        BizSchedule row = requireById(id);
        assertDeptAccessible(row.getDeptId());
        // 员工→门诊联动：被门诊号源引用的行归门诊侧管理，物理删会留下孤儿号源
        assertNotClinicBound(row);
        // 唯一键不含删除标志 → 物理删（软删行继续占键，「删了再重排同一天同一班」必撞重复键）
        baseMapper.purgeById(id);
        // 删完再核一次人力：把人删到「这个班没人接」必须当场拦住，而不是等明天出事故。
        // 只对「生效中的上班行」生效——两层的行删了都不改变在岗数：
        //   ① 休息/请假/培训/停班行：本来就不在岗（闸门若照跑会拿「本来就没人上班」的单元
        //      把「取消标休」误拦死）；
        //   ② 草稿/待审核行：onDutyAt 只认 已发布/已变更，删一条没生效的班不减在岗，
        //      按「全部班次」匹配最低在岗时会把「删草稿」误拦死。
        if (StaffDutyStatusEnum.isWorking(row.getDutyStatus())
                && PublishStatusEnum.isEffective(row.getPublishStatus())) {
            staffPlanRuleService.reviewAfterChange(row.getOrgType(), row.getOrgId(),
                    row.getShiftId(), row.getStaffType(), row.getScheduleDate(), true);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void swap(StaffScheduleSwapDTO dto) {
        BizSchedule from = requireById(dto.getFromScheduleId());
        assertRowScope(from, dto.getScope(), "换班");
        assertDeptAccessible(from.getDeptId());
        String reason = dto.getReason().trim();
        // 员工→门诊联动：出诊相关行（被门诊号源引用）换班/代班会改挂号医生，归门诊侧管理
        assertNotClinicBound(from);

        if (dto.getToScheduleId() != null) {
            BizSchedule to = requireById(dto.getToScheduleId());
            assertRowScope(to, dto.getScope(), "换班");
            assertNotClinicBound(to);
            if (Objects.equals(from.getEmployeeId(), to.getEmployeeId())) {
                throw new BusinessException("换班双方不能是同一个人");
            }
            // 换班是两个「在岗的人」互换，休息/请假行没有班次与时间可对调
            if (!StaffDutyStatusEnum.isWorking(from.getDutyStatus())
                    || !StaffDutyStatusEnum.isWorking(to.getDutyStatus())) {
                throw new BusinessException("换班只能在两条上班排班之间进行；休息/请假请直接改那条记录的出勤状态");
            }
            // D-业务规则：互换换班要求同日或同班，是跨字段约束而非「字段填没填」，DTO 注解无法表达，保留
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
            scheduleChangeLogService.record(from.getId(), ScheduleChangeTypeEnum.SWAP, fromEmpId,
                    to.getEmployeeId(), fromShiftId, from.getShiftId(), null, reason);
            scheduleChangeLogService.record(to.getId(), ScheduleChangeTypeEnum.SWAP, to.getEmployeeId(),
                    fromEmpId, toShiftId, to.getShiftId(), null, reason);
            return;
        }

        // C-非 web 入参：swap 还被 DutyRosterServiceImpl 用内部构造的 StaffScheduleSwapDTO 直接调用（代班路径），Bean Validation 不覆盖，保留
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
        scheduleChangeLogService.record(from.getId(), ScheduleChangeTypeEnum.SUBSTITUTE, originEmpId,
                from.getEmployeeId(), shiftId, shiftId, null, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int copyRange(StaffScheduleCopyDTO dto) {
        OrgUnitTypeEnum unitType = OrgUnitTypeEnum.fromCode(dto.getOrgType());
        if (unitType == null) {
            throw new BusinessException("排班单元类型取值不合法");
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
        // B-条件必填：单元类型非「全院」时才必填 orgId（全院固定落 0），按请求内容分支，DTO 注解无法表达，保留
        if (unitType != OrgUnitTypeEnum.HOSPITAL && (orgId == null || orgId == ID_NONE)) {
            throw new BusinessException("请选择" + unitType.getLabel());
        }
        // 域校验：复制只许在本域单元内进行（急诊科室只能在急诊排班页复制）
        if (StaffScheduleScopeEnum.ER.getCode().equals(dto.getScope())
                && !erDeptIds().contains(orgId)) {
            throw new BusinessException("急诊排班页只能复制急诊科室的排班");
        }
        if (StaffScheduleScopeEnum.STAFF.getCode().equals(dto.getScope())
                && erDeptIds().contains(orgId)) {
            throw new BusinessException("急诊科室的排班复制请到「急诊排班」页操作");
        }
        List<BizSchedule> sources = list(new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getOrgType, dto.getOrgType())
                .eq(BizSchedule::getOrgId, orgId)
                .ge(BizSchedule::getScheduleDate, dto.getFromStartDate())
                .le(BizSchedule::getScheduleDate, dto.getFromEndDate())
                .orderByAsc(BizSchedule::getScheduleDate).orderByAsc(BizSchedule::getId));
        if (sources.isEmpty()) {
            throw new BusinessException("来源区间内没有排班可复制");
        }
        int copied = 0;
        for (BizSchedule source : sources) {
            // STAFF 域复制跳过医生/护理/护工行：员工排班页只排医技/药学/收费/行政等岗位，
            // 历史遗留或误登记的医生/护理行（如曾洁 10-27 休息）复制会原样搬到下周，
            // 再次挡住门诊/急诊排班（assertDayExclusive 人×日×单元互斥）。复制不拦员工维度，
            // 只能在这里按行过滤；急诊科室单元已在上方按域拦下。
            if (StaffScheduleScopeEnum.STAFF.getCode().equals(dto.getScope())
                    && (Objects.equals(source.getStaffType(), StaffTypeEnum.DOCTOR.getCode())
                        || Objects.equals(source.getStaffType(), StaffTypeEnum.NURSE.getCode())
                        || Objects.equals(source.getStaffType(), StaffTypeEnum.NURSING_ASSISTANT.getCode()))) {
                continue;
            }
            LocalDate targetDate = dto.getToStartDate().plusDays(
                    source.getScheduleDate().toEpochDay() - dto.getFromStartDate().toEpochDay());
            BizSchedule exist = getOne(new LambdaQueryWrapper<BizSchedule>()
                    .eq(BizSchedule::getEmployeeId, source.getEmployeeId())
                    .eq(BizSchedule::getScheduleDate, targetDate)
                    .eq(BizSchedule::getShiftId, source.getShiftId()), false);
            if (exist != null) {
                // 已经排过就跳过：整周复制是「补齐空缺」，不是把人家的班覆盖掉
                continue;
            }
            BizSchedule copy = new BizSchedule();
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
            // 复制出来的是新的一周，一律草稿：复制不能替审核人把没审过的排班直接发布
            copy.setPublishStatus(PublishStatusEnum.DRAFT.getCode());
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
    public List<StaffScheduleVO> onDutyAt(LocalDateTime at, Integer orgType, Long orgId, Integer staffType) {
        LocalDate today = at.toLocalDate();
        LocalTime now = at.toLocalTime();
        // 跨零点班归开始日：凌晨两点在岗的人是「昨天夜班」的那一行，只查今天会查不到责任人
        List<BizSchedule> rows = list(new LambdaQueryWrapper<BizSchedule>()
                .in(BizSchedule::getScheduleDate, Arrays.asList(today.minusDays(1), today))
                .eq(BizSchedule::getDutyStatus, StaffDutyStatusEnum.WORK.getCode())
                // 只有发布生效的排班才算「此刻在岗」：草稿/待审核/调班中不能对外宣示有人在班
                .in(BizSchedule::getPublishStatus,
                        PublishStatusEnum.PUBLISHED.getCode(), PublishStatusEnum.CHANGED.getCode())
                .eq(orgType != null, BizSchedule::getOrgType, orgType)
                .eq(orgId != null, BizSchedule::getOrgId, orgId)
                .eq(staffType != null, BizSchedule::getStaffType, staffType)
                .orderByAsc(BizSchedule::getStartTime).orderByAsc(BizSchedule::getId));
        Map<Long, BizShift> shifts = shiftService.mapByIds(shiftIdsOf(rows));
        // 计划命中后，再关联实际出勤的签到时间：行业实践「当班 = 计划 + 签到」。
        // 无打卡数据的环境（check_in 全空）依然返回计划在岗 —— 兼容，不让急诊接诊取不到人。
        List<BizSchedule> inDuty = new ArrayList<>();
        for (BizSchedule row : rows) {
            if (ShiftCoverUtil.covers(now, row.getStartTime(), row.getEndTime())) {
                inDuty.add(row);
            }
        }
        Map<Long, LocalDateTime> checkedIn = attendanceCheckInMap(inDuty);
        List<StaffScheduleVO> vos = new ArrayList<>();
        for (BizSchedule row : inDuty) {
            StaffScheduleVO vo = toOnDutyVO(row, shifts.get(row.getShiftId()));
            LocalDateTime ci = checkedIn.get(row.getId());
            vo.setCheckInTime(ci == null ? null : ci.format(ATTENDANCE_TIME_FMT));
            vo.setActualOnDuty(ci != null);
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 按排班行ID批量取签到时间（biz_staff_attendance.check_in，NULL=未签到）。
     * 同一条排班行的出勤记录只有一行（uk_attend 唯一），重复时取最早签到。
     */
    private Map<Long, LocalDateTime> attendanceCheckInMap(List<BizSchedule> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = rows.stream().map(BizSchedule::getId).collect(Collectors.toList());
        return staffAttendanceMapper.selectList(new LambdaQueryWrapper<BizStaffAttendance>()
                        .in(BizStaffAttendance::getStaffScheduleId, ids)
                        .select(BizStaffAttendance::getStaffScheduleId, BizStaffAttendance::getCheckIn))
                .stream()
                .filter(a -> a.getCheckIn() != null)
                .collect(Collectors.toMap(BizStaffAttendance::getStaffScheduleId, BizStaffAttendance::getCheckIn,
                        (a, b) -> a.isBefore(b) ? a : b));
    }

    /**
     * 急诊值班公开视图：急诊科室（dept_type 含 6，字典 his_dept_type）当天已发布（2/4）的
     * 医生出勤行，按班次开始时间排序。虚父级（被其他急诊科室指为父级）不单列。
     * 只读、不按数据范围收口 —— 值班表全院公开是核心制度要求；接口层 isAuthenticated 即可。
     */
    @Override
    public List<StaffScheduleVO> erDutyPublic(LocalDate date) {
        Set<Long> erDeptIds = erLeafDeptIds();
        if (erDeptIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<BizSchedule> rows = list(new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getScheduleDate, date)
                .in(BizSchedule::getDeptId, erDeptIds)
                .eq(BizSchedule::getStaffType, StaffTypeEnum.DOCTOR.getCode())
                .eq(BizSchedule::getDutyStatus, StaffDutyStatusEnum.WORK.getCode())
                .in(BizSchedule::getPublishStatus,
                        PublishStatusEnum.PUBLISHED.getCode(), PublishStatusEnum.CHANGED.getCode())
                .orderByAsc(BizSchedule::getStartTime).orderByAsc(BizSchedule::getId));
        Map<Long, BizShift> shifts = shiftService.mapByIds(shiftIdsOf(rows));
        List<StaffScheduleVO> vos = new ArrayList<>();
        for (BizSchedule row : rows) {
            vos.add(toOnDutyVO(row, shifts.get(row.getShiftId())));
        }
        return vos;
    }

    /**
     * 急诊固定医师占比（评审口径：固定急诊医师 ≥ 在岗急诊医师 75%，卫医政发〔2009〕50号）。
     * 按急诊叶子科室（dept_type 含 6，排除虚父级）分别统计当天已发布（2/4）在岗医生中
     * is_fixed=1 的占比；未维护 is_fixed（NULL）的员工不计入固定数 ——
     * 档案维护越全，口径越接近真实。
     */
    @Override
    public List<ErFixedRatioVO> erFixedRatio(LocalDate date) {
        LocalDate day = date == null ? LocalDate.now() : date;
        List<ErFixedRatioVO> result = new ArrayList<>();
        for (Long deptId : erLeafDeptIds()) {
            List<BizSchedule> rows = list(new LambdaQueryWrapper<BizSchedule>()
                    .eq(BizSchedule::getScheduleDate, day)
                    .eq(BizSchedule::getDeptId, deptId)
                    .eq(BizSchedule::getStaffType, StaffTypeEnum.DOCTOR.getCode())
                    .eq(BizSchedule::getDutyStatus, StaffDutyStatusEnum.WORK.getCode())
                    .in(BizSchedule::getPublishStatus,
                            PublishStatusEnum.PUBLISHED.getCode(), PublishStatusEnum.CHANGED.getCode()));
            Set<Long> empIds = rows.stream().map(BizSchedule::getEmployeeId)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Long, Integer> fixedFlags = empIds.isEmpty() ? Collections.emptyMap()
                    : systemFacadeService.listEmployeesByIds(empIds).stream()
                    .filter(e -> e.getIsFixed() != null)
                    .collect(Collectors.toMap(EmployeeBriefVO::getId, EmployeeBriefVO::getIsFixed, (a, b) -> a));
            long fixed = fixedFlags.values().stream().filter(v -> Objects.equals(v, 1)).count();
            ErFixedRatioVO vo = new ErFixedRatioVO();
            vo.setDeptId(deptId);
            vo.setDeptName(rows.isEmpty() ? null : rows.get(0).getDeptName());
            vo.setOnDutyDoctors(empIds.size());
            vo.setFixedDoctors((int) fixed);
            vo.setFixedRatio(empIds.isEmpty() ? null
                    : BigDecimal.valueOf(fixed).divide(BigDecimal.valueOf(empIds.size()), 4, RoundingMode.HALF_UP));
            result.add(vo);
        }
        return result;
    }

    /**
     * 与列表/详情共出 {@link StaffScheduleVO} 这一个类，但只填「此刻在岗」这一页真正渲染的列 ——
     * 共用出参类不等于共用出参宽度，把 32 列一股脑塞给一个只显示 10 列的标签列表，
     * 等于让分诊台每次刷新都多载两倍正文。
     */
    private StaffScheduleVO toOnDutyVO(BizSchedule row, BizShift shift) {
        StaffScheduleVO vo = new StaffScheduleVO();
        // 行主键必须带出：值班表/在岗名单的消费方（急诊排班页跳转、签到关联）要拿 id 定位到排班行
        vo.setId(row.getId());
        vo.setEmployeeId(row.getEmployeeId());
        vo.setEmployeeName(row.getEmployeeName());
        vo.setStaffType(row.getStaffType());
        vo.setStaffTypeName(StaffTypeEnum.getLabel(row.getStaffType()));
        vo.setOrgId(row.getOrgId());
        vo.setOrgName(row.getOrgName());
        vo.setDeptId(row.getDeptId());
        vo.setDeptName(row.getDeptName());
        vo.setShiftId(row.getShiftId());
        vo.setShiftName(shift == null ? null : shift.getShiftName());
        vo.setStartTime(row.getStartTime());
        vo.setEndTime(row.getEndTime());
        vo.setAttendMode(row.getAttendMode());
        vo.setAttendModeText(AttendModeEnum.getLabel(row.getAttendMode()));
        return vo;
    }

    @Override
    public List<BizSchedule> listDay(LocalDate date, Integer orgType, Long orgId, Integer staffType) {
        return list(scoped(new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getScheduleDate, date)
                .eq(orgType != null, BizSchedule::getOrgType, orgType)
                .eq(orgId != null, BizSchedule::getOrgId, orgId)
                .eq(staffType != null, BizSchedule::getStaffType, staffType)
                .orderByAsc(BizSchedule::getStartTime).orderByAsc(BizSchedule::getId)));
    }

    @Override
    public boolean releasesClinicSource(BizSchedule schedule) {
        if (schedule == null) {
            return false;
        }
        return StaffTypeEnum.hasSource(schedule.getStaffType())
                && StaffDutyStatusEnum.isWorking(schedule.getDutyStatus())
                && AttendModeEnum.releasesSource(schedule.getAttendMode())
                && Objects.equals(YesOrNoEnum.YES.getCode(), schedule.getClinicFlag());
    }

    // 写入口的内部收口：单元 / 人 / 班 三段快照全部服务端重查

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

    private BizSchedule requireById(Long id) {
        BizSchedule row = id == null ? null : getById(id);
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
    private void applyUnit(BizSchedule schedule, Integer orgType, Long orgId) {
        OrgUnitTypeEnum unitType = OrgUnitTypeEnum.fromCode(orgType);
        if (unitType == null) {
            throw new BusinessException("排班单元类型取值不合法");
        }
        schedule.setOrgType(unitType.getCode());
        if (unitType == OrgUnitTypeEnum.HOSPITAL) {
            schedule.setOrgId(ID_NONE);
            schedule.setOrgName(unitType.getLabel());
            schedule.setDeptId(ID_NONE);
            schedule.setDeptName(unitType.getLabel());
            return;
        }
        // D-业务规则：非空与取值合法性（0 是全院占位值）混写在一个条件里，且仅当单元类型非全院才必填，DTO 注解无法表达，保留
        if (orgId == null || orgId == ID_NONE) {
            throw new BusinessException("请选择" + unitType.getLabel());
        }
        if (unitType == OrgUnitTypeEnum.WARD) {
            WardBriefVO ward = systemFacadeService.findWard(orgId);
            if (ward == null || isDisabled(ward.getStatus())) {
                throw new BusinessException("所选" + unitType.getLabel() + "不存在或已停用");
            }
            DeptBriefVO dept = systemFacadeService.findDepartment(ward.getDeptId());
            if (dept == null || isDisabled(dept.getStatus())) {
                throw new BusinessException("病区所属科室不存在或已停用，请先修正主数据");
            }
            schedule.setOrgId(ward.getWardId());
            schedule.setOrgName(ward.getWardName());
            schedule.setDeptId(dept.getId());
            schedule.setDeptName(dept.getDeptName());
            return;
        }
        DeptBriefVO dept = systemFacadeService.findDepartment(orgId);
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
    private void applyEmployee(BizSchedule schedule, Long employeeId, Long employeePostId) {
        EmployeeBriefVO employee = systemFacadeService.findEmployee(employeeId);
        if (employee == null) {
            throw new BusinessException("所选排班对象不存在或已删除，请重新选择");
        }
        if (isDisabled(employee.getStatus())) {
            throw new BusinessException("「" + employee.getEmpName() + "」已停用，不能排班");
        }
        EmployeePostBriefVO post = resolvePost(employee, schedule.getDeptId(), employeePostId);
        schedule.setEmployeeId(employee.getId());
        schedule.setEmpCode(employee.getEmpCode());
        schedule.setEmployeeName(employee.getEmpName());
        schedule.setEmployeePostId(post == null ? null : post.getId());
        schedule.setStaffType(staffTypeOf(post, employee));
    }

    private EmployeePostBriefVO resolvePost(EmployeeBriefVO employee, Long deptId, Long employeePostId) {
        if (employeePostId != null) {
            EmployeePostBriefVO post = systemFacadeService.findEmployeePost(employeePostId);
            if (post == null || !Objects.equals(post.getEmployeeId(), employee.getId())) {
                throw new BusinessException("所选岗位不属于这个人，请重新选择");
            }
            return post;
        }
        List<EmployeePostBriefVO> posts = systemFacadeService.listEmployeePosts(employee.getId());
        if (posts.isEmpty()) {
            throw new BusinessException("「" + employee.getEmpName() + "」还没有分配岗位，"
                    + "请先在员工档案里配置岗位（岗位类别决定这个班能不能排给他）");
        }
        if (deptId != null && deptId != ID_NONE) {
            for (EmployeePostBriefVO post : posts) {
                if (Objects.equals(post.getDeptId(), deptId)) {
                    return post;
                }
            }
        }
        for (EmployeePostBriefVO post : posts) {
            if (Objects.equals(YesOrNoEnum.YES.getCode(), post.getIsPrimary())) {
                return post;
            }
        }
        return posts.get(0);
    }

    private Integer staffTypeOf(EmployeePostBriefVO post, EmployeeBriefVO employee) {
        if (post == null || post.getRoleId() == null) {
            throw new BusinessException("「" + employee.getEmpName() + "」的岗位没有挂角色，"
                    + "无法判定岗位类别，请先在岗位配置里补齐角色");
        }
        RoleBriefVO role = systemFacadeService.findRole(post.getRoleId());
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
    private void applyShift(BizSchedule schedule, Long shiftId, StaffDutyStatusEnum status) {
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
    private void applyShiftForSwap(BizSchedule schedule, Long shiftId) {
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
                    + StaffTypeEnum.getLabel(shift.getApplyStaffType()) + "用的班次，"
                    + employeeName + "当前的岗位类别是" + StaffTypeEnum.getLabel(staffType) + "，排不进这个班");
        }
        return shift;
    }

    /**
     * 出诊标记只在「岗位有号源属性 + 非听班」时才可能为是。
     * <br>听班不放号：挂号系统给一个在家待命的人放号，患者到了没人看，那是投诉不是数据问题。
     */
    private void applyClinicFlag(BizSchedule schedule) {
        boolean mayRelease = StaffTypeEnum.hasSource(schedule.getStaffType())
                && AttendModeEnum.releasesSource(schedule.getAttendMode());
        if (!mayRelease) {
            schedule.setClinicFlag(YesOrNoEnum.NO.getCode());
        }
    }

    private boolean isDuplicated(BizSchedule schedule, Long... exceptIds) {
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
        // ⚠️ 无班次行（休息/请假/培训/停班）shift_id 为 NULL：eq(col, null) 恒假，
        // 判重会静默失效 → 同人同日同单元能插出无数条休息行，必须走 IS NULL（与 sameUnitShiftRow 同坑）
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getEmployeeId, schedule.getEmployeeId())
                .eq(BizSchedule::getScheduleDate, schedule.getScheduleDate())
                .notIn(!skip.isEmpty(), BizSchedule::getId, skip);
        if (schedule.getShiftId() == null) {
            wrapper.isNull(BizSchedule::getShiftId);
        } else {
            wrapper.eq(BizSchedule::getShiftId, schedule.getShiftId());
        }
        return count(wrapper) > 0;
    }

    /**
     * 换班/代班落库前：承接的人不能已经排过这个班，那天也不能已经请了假
     */
    private void assertSwappable(BizSchedule schedule, Long... exceptIds) {
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
    private void assertDayExclusive(BizSchedule schedule) {
        if (countDayConflict(schedule) == 0) {
            return;
        }
        if (StaffDutyStatusEnum.isWorking(schedule.getDutyStatus())) {
            throw new BusinessException("「" + schedule.getEmployeeName() + "」在 " + schedule.getScheduleDate()
                    + " 已排休息/请假");
        }
        throw new BusinessException("「" + schedule.getEmployeeName() + "」在 " + schedule.getScheduleDate()
                + " 已经排了班，改成" + StaffDutyStatusEnum.getLabel(schedule.getDutyStatus())
        );
    }

    /**
     * 同一<b>排班单元</b>内与本次状态互斥的行数（上班 ↔ 非上班）。
     *
     * <p>互斥的边界是<b>单元</b>而不是「人 × 日」：一格只允许一个走向说的是这个人在这个单元这一天，
     * 不是这个人这一天。同一个护士同一天可以在病区格子上填「休」（这个病区今天没她的班）
     * 同时又在门诊科室上一班 —— 这两件事不矛盾，时间冲突由 {@link #assertNoOverlap} 按班次时段兜。
     * 按「人 × 日」判互斥会让任何一个跨单元支援都写不进去。
     */
    private long countDayConflict(BizSchedule schedule) {
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getEmployeeId, schedule.getEmployeeId())
                .eq(BizSchedule::getScheduleDate, schedule.getScheduleDate())
                .eq(BizSchedule::getOrgType, schedule.getOrgType())
                .eq(BizSchedule::getOrgId, schedule.getOrgId())
                .ne(schedule.getId() != null, BizSchedule::getId, schedule.getId());
        if (StaffDutyStatusEnum.isWorking(schedule.getDutyStatus())) {
            wrapper.ne(BizSchedule::getDutyStatus, StaffDutyStatusEnum.WORK.getCode());
        } else {
            wrapper.eq(BizSchedule::getDutyStatus, StaffDutyStatusEnum.WORK.getCode());
        }
        return count(wrapper);
    }

    /**
     * 同一个人时间重叠校验：把每条排班摊成「日期 + 起止时刻」的时间段（跨零点班的结束算到次日），
     * 再与相邻自然日的行比对。命中重叠直接拦，报错带上两边的人可读信息。
     */
    private void assertNoOverlap(BizSchedule schedule, Long... exceptIds) {
        List<Long> skip = new ArrayList<>(Arrays.asList(exceptIds));
        if (schedule.getId() != null) {
            skip.add(schedule.getId());
        }
        if (!StaffDutyStatusEnum.isWorking(schedule.getDutyStatus())) {
            return;
        }
        LocalDate date = schedule.getScheduleDate();
        List<BizSchedule> neighbours = list(new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getEmployeeId, schedule.getEmployeeId())
                .eq(BizSchedule::getDutyStatus, StaffDutyStatusEnum.WORK.getCode())
                .ge(BizSchedule::getScheduleDate, date.minusDays(1))
                .le(BizSchedule::getScheduleDate, date.plusDays(1))
                .notIn(!skip.isEmpty(), BizSchedule::getId, skip));
        TimeRange mine = intervalOf(schedule, date);
        if (mine == null) {
            return;
        }
        for (BizSchedule other : neighbours) {
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
                        + "与本次的「" + timeText(schedule) + "」时间重叠");
            }
        }
    }

    /**
     * 排班行摊成时间段：跨零点（结束不晚于开始）的结束点算到次日；时间脏了返回 null（不猜）
     */
    private TimeRange intervalOf(BizSchedule row, LocalDate date) {
        LocalTime start = ShiftCoverUtil.parseShiftTime(row.getStartTime());
        LocalTime end = ShiftCoverUtil.parseShiftTime(row.getEndTime());
        if (start == null || end == null || date == null) {
            return null;
        }
        LocalDateTime from = LocalDateTime.of(date, start);
        LocalDateTime to = end.isAfter(start) ? LocalDateTime.of(date, end) : LocalDateTime.of(date.plusDays(1), end);
        return new TimeRange(from, to);
    }

    private String timeText(BizSchedule row) {
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
        if (!deptScopeService.canAccessDept(deptId)) {
            throw new BusinessException("没有该科室的排班权限（不在当前岗位的数据范围内）");
        }
    }

    /**
     * 数据范围收口：受限岗位只能看自己科室的行；全院级行对所有人开放 ——
     * 「今天全院谁负责」不是敏感信息，收掉等于让人半夜找不到打电话的对象。
     */
    private LambdaQueryWrapper<BizSchedule> scoped(LambdaQueryWrapper<BizSchedule> wrapper) {
        if (!deptScopeService.isScoped()) {
            return wrapper;
        }
        Set<Long> allowed = deptScopeService.allowedDeptIds();
        if (allowed == null || allowed.isEmpty()) {
            wrapper.eq(BizSchedule::getOrgType, OrgUnitTypeEnum.HOSPITAL.getCode());
            return wrapper;
        }
        wrapper.and(w -> w.in(BizSchedule::getDeptId, allowed)
                .or().eq(BizSchedule::getOrgType, OrgUnitTypeEnum.HOSPITAL.getCode()));
        return wrapper;
    }

    private StaffScheduleVO toVO(BizSchedule row, BizShift shift) {
        StaffScheduleVO vo = new StaffScheduleVO();
        vo.setId(row.getId());
        vo.setScheduleDate(row.getScheduleDate());
        vo.setWeekDay(row.getWeekDay());
        vo.setWeekDayText(weekDayText(row.getWeekDay()));
        vo.setOrgType(row.getOrgType());
        vo.setOrgTypeText(OrgUnitTypeEnum.getLabel(row.getOrgType()));
        vo.setOrgId(row.getOrgId());
        vo.setOrgName(row.getOrgName());
        vo.setDeptId(row.getDeptId());
        vo.setDeptName(row.getDeptName());
        vo.setEmployeeId(row.getEmployeeId());
        vo.setEmpCode(row.getEmpCode());
        vo.setEmployeeName(row.getEmployeeName());
        vo.setEmployeePostId(row.getEmployeePostId());
        vo.setStaffType(row.getStaffType());
        vo.setStaffTypeName(StaffTypeEnum.getLabel(row.getStaffType()));
        vo.setShiftId(row.getShiftId());
        vo.setShiftName(shift == null ? null : shift.getShiftName());
        vo.setStartTime(row.getStartTime());
        vo.setEndTime(row.getEndTime());
        vo.setCrossDay(isCrossDay(row.getStartTime(), row.getEndTime()));
        vo.setDutyStatus(row.getDutyStatus());
        vo.setDutyStatusText(StaffDutyStatusEnum.getLabel(row.getDutyStatus()));
        vo.setAttendMode(row.getAttendMode());
        vo.setAttendModeText(AttendModeEnum.getLabel(row.getAttendMode()));
        vo.setClinicFlag(row.getClinicFlag());
        vo.setWorkMinutes(row.getWorkMinutes());
        vo.setWorkHours(row.getWorkMinutes() == null ? null
                : String.format("%.1f", row.getWorkMinutes() / 60.0));
        vo.setScheduleSource(row.getScheduleSource());
        vo.setScheduleSourceText(StaffScheduleSourceEnum.getLabel(row.getScheduleSource()));
        vo.setTemplateId(row.getTemplateId());
        vo.setPublishStatus(row.getPublishStatus());
        vo.setPublishStatusText(PublishStatusEnum.getLabel(row.getPublishStatus()));
        vo.setRemark(row.getRemark());
        return vo;
    }

    private String weekDayText(Integer weekDay) {
        return weekDay == null || weekDay < 1 || weekDay > DAYS_OF_WEEK ? null : WEEK_DAY_TEXTS[weekDay - 1];
    }

    private String shiftLabelOf(BizSchedule schedule) {
        if (schedule.getShiftId() == null || schedule.getShiftId() == ID_NONE) {
            return StaffDutyStatusEnum.getLabel(schedule.getDutyStatus());
        }
        String name = shiftNameOf(schedule.getShiftId());
        return name == null ? "未命名班次" : name;
    }

    private Set<Long> shiftIdsOf(List<BizSchedule> rows) {
        Set<Long> ids = new HashSet<>();
        for (BizSchedule row : rows) {
            if (row.getShiftId() != null && row.getShiftId() != ID_NONE) {
                ids.add(row.getShiftId());
            }
        }
        return ids;
    }

    @Override
    public StaffCheckVO check(StaffScheduleCheckDTO dto) {
        LocalDate start = dto.getStartDate();
        LocalDate end = dto.getEndDate();
        if (end.isBefore(start)) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        List<StaffCheckRow> rows = baseMapper.selectStaffCheckRows(
                dto.getOrgType(), dto.getOrgId(), dto.getStaffType(), start, end);
        // 规则只在「单单元」口径下才有配置可比（跨单元汇总没有标准可对）
        List<BizStaffPlanRule> rules = dto.getOrgType() != null && dto.getOrgId() != null
                ? staffPlanRuleService.listByUnit(dto.getOrgType(), dto.getOrgId(), dto.getStaffType())
                : List.of();
        List<StaffCheckVO.Warning> warnings = new ArrayList<>();
        shiftOnNonWorkWarnings(rows, warnings);
        personWarnings(rows, rules, warnings);
        staffingWarnings(rows, rules, start, end, warnings);
        StaffCheckVO vo = new StaffCheckVO();
        vo.setStartDate(start.toString());
        vo.setEndDate(end.toString());
        vo.setOrgType(dto.getOrgType());
        vo.setOrgId(dto.getOrgId());
        vo.setOrgName(rows.isEmpty() ? null : rows.get(0).getOrgName());
        vo.setStaffType(dto.getStaffType());
        vo.setAlertCount(countLevel(warnings, 1));
        vo.setHintCount(countLevel(warnings, 2));
        vo.setWarnings(warnings);
        return vo;
    }

    // ==================== 员工排班增强：区间规则校验 / 月度工时（全区岗位通用） ====================

    /**
     * 班次冲突：休息/请假/培训/停班仍挂班次，直接算告警。
     */
    private void shiftOnNonWorkWarnings(List<StaffCheckRow> rows, List<StaffCheckVO.Warning> warnings) {
        for (StaffCheckRow row : rows) {
            if (row.getShiftId() != null && !isWorkStatus(row.getDutyStatus())) {
                warnings.add(warn(1, "SHIFT_ON_NON_WORK", "班次冲突", row.getScheduleDate(),
                        row.getShiftId(), row.getShiftName(), row.getEmployeeId(), row.getEmployeeName(),
                        row.getStaffType(), null, null,
                        row.getEmployeeName() + " 当天为" + StaffDutyStatusEnum.getLabel(row.getDutyStatus())
                                + "，仍挂了班次「" + row.getShiftName() + "」"));
            }
        }
    }

    /**
     * 人维度：连续夜班 / 连续上班 / 单周工时（规则配置来自人力标准行，未配置的规则不判）。
     */
    private void personWarnings(List<StaffCheckRow> rows, List<BizStaffPlanRule> rules,
                                List<StaffCheckVO.Warning> warnings) {
        Map<Long, List<StaffCheckRow>> byPerson = rows.stream()
                .collect(Collectors.groupingBy(StaffCheckRow::getEmployeeId, LinkedHashMap::new, Collectors.toList()));
        for (Map.Entry<Long, List<StaffCheckRow>> entry : byPerson.entrySet()) {
            List<StaffCheckRow> personRows = entry.getValue();
            personRows.sort(Comparator.comparing(StaffCheckRow::getScheduleDate));
            BizStaffPlanRule rule = personRuleOf(rules, personRows.get(0).getStaffType());
            int nightStreak = 0;
            int workStreak = 0;
            for (StaffCheckRow row : personRows) {
                if (isWorkStatus(row.getDutyStatus()) && Integer.valueOf(1).equals(row.getIsNight())) {
                    nightStreak++;
                } else {
                    nightStreak = 0;
                }
                workStreak = isWorkStatus(row.getDutyStatus()) ? workStreak + 1 : 0;
                if (rule == null) {
                    continue;
                }
                if (nvl(rule.getMaxConsecutiveNightDays()) > 0 && nightStreak > rule.getMaxConsecutiveNightDays()) {
                    warnings.add(warn(1, "NIGHT_STREAK", "连续夜班超限", row.getScheduleDate(),
                            row.getShiftId(), row.getShiftName(), row.getEmployeeId(), row.getEmployeeName(),
                            row.getStaffType(), nightStreak, rule.getMaxConsecutiveNightDays(),
                            row.getEmployeeName() + " 已连续 " + nightStreak + " 天夜班（上限 "
                                    + rule.getMaxConsecutiveNightDays() + " 天）"));
                }
                if (nvl(rule.getMaxConsecutiveWorkDays()) > 0 && workStreak > rule.getMaxConsecutiveWorkDays()) {
                    warnings.add(warn(1, "WORK_STREAK", "连续上班超限", row.getScheduleDate(),
                            row.getShiftId(), row.getShiftName(), row.getEmployeeId(), row.getEmployeeName(),
                            row.getStaffType(), workStreak, rule.getMaxConsecutiveWorkDays(),
                            row.getEmployeeName() + " 已连续 " + workStreak + " 天上班（上限 "
                                    + rule.getMaxConsecutiveWorkDays() + " 天）"));
                }
            }
            if (rule != null && rule.getMaxWeekHours() != null && rule.getMaxWeekHours().signum() > 0) {
                Map<YearWeek, Integer> weekMinutes = new HashMap<>();
                Map<YearWeek, StaffCheckRow> firstOfWeek = new HashMap<>();
                for (StaffCheckRow row : personRows) {
                    if (!isWorkStatus(row.getDutyStatus())) {
                        continue;
                    }
                    YearWeek key = YearWeek.of(row.getScheduleDate());
                    weekMinutes.merge(key, nvl(row.getWorkMinutes()), Integer::sum);
                    firstOfWeek.putIfAbsent(key, row);
                }
                int maxMinutes = rule.getMaxWeekHours().movePointRight(2).intValue();
                for (Map.Entry<YearWeek, Integer> e : weekMinutes.entrySet()) {
                    if (e.getValue() > maxMinutes) {
                        StaffCheckRow row = firstOfWeek.get(e.getKey());
                        warnings.add(warn(1, "WEEK_HOURS", "单周工时超限", row.getScheduleDate(),
                                row.getShiftId(), row.getShiftName(), row.getEmployeeId(), row.getEmployeeName(),
                                row.getStaffType(), e.getValue() / 60, maxMinutes / 60,
                                row.getEmployeeName() + " 第" + e.getKey().week() + "周工时 "
                                        + String.format("%.1f", e.getValue() / 60.0) + " 小时（上限 "
                                        + rule.getMaxWeekHours() + " 小时）"));
                    }
                }
            }
        }
    }

    /**
     * 单元维度：人力缺口（低于最低在岗）与人力富余（高于最高在岗），按生效行（已发布/已变更）统计。
     */
    private void staffingWarnings(List<StaffCheckRow> rows, List<BizStaffPlanRule> rules,
                                  LocalDate start, LocalDate end, List<StaffCheckVO.Warning> warnings) {
        Map<String, Integer> countByKey = new HashMap<>();
        for (StaffCheckRow row : rows) {
            if (!isWorkStatus(row.getDutyStatus()) || !isPublished(row.getPublishStatus())) {
                continue;
            }
            String key = keyOf(row.getOrgType(), row.getOrgId(), row.getScheduleDate(),
                    row.getShiftId() == null ? 0L : row.getShiftId(), row.getStaffType());
            countByKey.merge(key, 1, Integer::sum);
        }
        for (BizStaffPlanRule rule : rules) {
            if (rule.getStatus() == null || rule.getStatus() != 1) {
                continue;
            }
            long ruleShift = rule.getShiftId() == null ? 0L : rule.getShiftId();
            String shiftLabel = ruleShift == 0L ? "全部班次" : shiftNameOf(rule.getShiftId());
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                String key = keyOf(rule.getOrgType(), rule.getOrgId(), d, ruleShift, rule.getStaffType());
                int actual = countByKey.getOrDefault(key, 0);
                if (nvl(rule.getMinStaff()) > 0 && actual < rule.getMinStaff()) {
                    warnings.add(warn(1, "STAFF_GAP", "人力缺口", d,
                            rule.getShiftId(), shiftLabel, null, null, rule.getStaffType(),
                            actual, rule.getMinStaff(),
                            unitLabel(rule) + " " + d + " " + StaffTypeEnum.getLabel(rule.getStaffType())
                                    + " 在岗 " + actual + " 人，低于最低 " + rule.getMinStaff() + " 人"));
                }
                if (nvl(rule.getMaxStaff()) > 0 && actual > rule.getMaxStaff()) {
                    warnings.add(warn(2, "STAFF_OVER", "人力富余", d,
                            rule.getShiftId(), shiftLabel, null, null, rule.getStaffType(),
                            actual, rule.getMaxStaff(),
                            unitLabel(rule) + " " + d + " " + StaffTypeEnum.getLabel(rule.getStaffType())
                                    + " 在岗 " + actual + " 人，超过最高 " + rule.getMaxStaff() + " 人"));
                }
            }
        }
    }

    /**
     * 取该岗位类别下用于人维度校验的规则行：优先「全部班次」（shift_id=0）行，否则任取一条。
     */
    private BizStaffPlanRule personRuleOf(List<BizStaffPlanRule> rules, Integer staffType) {
        BizStaffPlanRule fallback = null;
        for (BizStaffPlanRule r : rules) {
            if (r.getStatus() == null || r.getStatus() != 1) {
                continue;
            }
            if (staffType != null && !Integer.valueOf(staffType).equals(r.getStaffType())) {
                continue;
            }
            long shift = r.getShiftId() == null ? 0L : r.getShiftId();
            if (shift == 0L) {
                return r;
            }
            if (fallback == null) {
                fallback = r;
            }
        }
        return fallback;
    }

    private String keyOf(Integer orgType, Long orgId, LocalDate date, long shiftId, Integer staffType) {
        return orgType + ":" + orgId + ":" + date + ":" + shiftId + ":" + staffType;
    }

    private String unitLabel(BizStaffPlanRule rule) {
        if (rule.getOrgName() != null && !rule.getOrgName().isBlank()) {
            return rule.getOrgName();
        }
        return "单元(" + rule.getOrgType() + "-" + rule.getOrgId() + ")";
    }

    private StaffCheckVO.Warning warn(int level, String type, String typeText, LocalDate date,
                                      Long shiftId, String shiftName, Long employeeId, String employeeName,
                                      Integer staffType, Integer actual, Integer required, String message) {
        StaffCheckVO.Warning w = new StaffCheckVO.Warning();
        w.setLevel(level);
        w.setType(type);
        w.setTypeText(typeText);
        w.setScheduleDate(date == null ? null : date.toString());
        w.setShiftId(shiftId);
        w.setShiftName(shiftName);
        w.setEmployeeId(employeeId);
        w.setEmployeeName(employeeName);
        w.setStaffType(staffType);
        w.setStaffTypeName(StaffTypeEnum.getLabel(staffType));
        w.setActual(actual);
        w.setRequired(required);
        w.setMessage(message);
        return w;
    }

    private int countLevel(List<StaffCheckVO.Warning> warnings, int level) {
        int n = 0;
        for (StaffCheckVO.Warning w : warnings) {
            if (w.getLevel() != null && w.getLevel() == level) {
                n++;
            }
        }
        return n;
    }

    @Override
    public StaffWorkloadVO monthWorkload(StaffScheduleWorkloadDTO dto) {
        YearMonth ym = parseMonth(dto.getMonth());
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        int days = ym.lengthOfMonth();
        Map<Long, StaffWorkloadVO.Workload> byEmployee = new LinkedHashMap<>();
        for (StaffWorkloadVO.Workload w : baseMapper.selectStaffWorkload(
                dto.getOrgType(), dto.getOrgId(), dto.getStaffType(), start, end)) {
            byEmployee.put(w.getEmployeeId(), w);
        }
        // 在册补齐「整月未排」的人：科室/病区口径按单元在册人员补；全院口径人太多，按单元筛更合理，不做全局补齐
        if (dto.getOrgType() != null && dto.getOrgId() != null && dto.getOrgType() != OrgUnitTypeEnum.HOSPITAL.getCode()) {
            for (StaffWorkloadVO.Workload w : baseMapper.selectUnitStaff(
                    dto.getOrgType(), dto.getOrgId(), dto.getStaffType())) {
                byEmployee.putIfAbsent(w.getEmployeeId(), w);
            }
        }
        List<StaffWorkloadVO.Workload> rows = new ArrayList<>(byEmployee.values());
        int totalWorkDays = 0;
        int totalNightDays = 0;
        BigDecimal totalHours = BigDecimal.ZERO;
        for (StaffWorkloadVO.Workload w : rows) {
            zeroFill(w);
            w.setMissingDays(Math.max(0, days - nvl(w.getWrittenDays())));
            totalWorkDays += nvl(w.getWorkDays());
            totalNightDays += nvl(w.getNightDays());
            totalHours = totalHours.add(w.getWorkHours() == null ? BigDecimal.ZERO : w.getWorkHours());
        }
        rows.sort(Comparator.comparing(StaffWorkloadVO.Workload::getEmpCode,
                Comparator.nullsLast(Comparator.naturalOrder())));
        StaffWorkloadVO result = new StaffWorkloadVO();
        result.setMonth(ym.toString());
        result.setOrgType(dto.getOrgType());
        result.setOrgId(dto.getOrgId());
        result.setStaffType(dto.getStaffType());
        result.setStaffCount(rows.size());
        result.setTotalWorkDays(totalWorkDays);
        result.setTotalNightDays(totalNightDays);
        result.setTotalWorkHours(totalHours.setScale(1, RoundingMode.HALF_UP));
        result.setRows(rows);
        return result;
    }

    private void zeroFill(StaffWorkloadVO.Workload w) {
        w.setWorkDays(nvl(w.getWorkDays()));
        w.setRestDays(nvl(w.getRestDays()));
        w.setLeaveDays(nvl(w.getLeaveDays()));
        w.setTrainingDays(nvl(w.getTrainingDays()));
        w.setSuspendedDays(nvl(w.getSuspendedDays()));
        w.setNightDays(nvl(w.getNightDays()));
        w.setWorkMinutes(nvl(w.getWorkMinutes()));
        if (w.getWorkHours() == null) {
            w.setWorkHours(BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP));
        }
        if (w.getWrittenDays() == null) {
            w.setWrittenDays(0);
        }
        w.setStaffTypeName(StaffTypeEnum.getLabel(w.getStaffType()));
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (Exception e) {
            throw new BusinessException("月份格式应为 yyyy-MM，例如 2026-10");
        }
    }

    private boolean isWorkStatus(Integer dutyStatus) {
        return dutyStatus != null && dutyStatus == 1;
    }

    private boolean isPublished(Integer publishStatus) {
        return publishStatus != null && (publishStatus == 2 || publishStatus == 4);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        BizSchedule row = requireScheduleRow(id);
        // 员工→门诊联动：门诊绑定行的发布态由门诊审核发布驱动，员工排班侧不得提交
        assertNotClinicBound(row);
        if (!PublishStatusEnum.DRAFT.getCode().equals(row.getPublishStatus())) {
            throw new BusinessException("只有草稿状态的排班可以提交审核");
        }
        updatePublishStatus(id, PublishStatusEnum.PENDING.getCode());
        scheduleChangeLogService.record(id, ScheduleChangeTypeEnum.SUBMIT, null, null, null, null, null,
                "草稿 → 待审核");
    }

    // ===== 发布态流转（全院岗位排班：提交 → 审核发布 / 驳回 / 撤回 / 调班）=====
    // 审核动作必须命中科室主档指定的全院岗位排班审核人（系统管理员豁免；orgType=3 全院级仅管理员）。

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPublish(Long id) {
        BizSchedule row = requireScheduleRow(id);
        // 员工→门诊联动：门诊绑定行由门诊侧审核发布（syncCoreSchedulePublish），员工侧不得双写
        assertNotClinicBound(row);
        if (!PublishStatusEnum.PENDING.getCode().equals(row.getPublishStatus())) {
            throw new BusinessException("只有待审核状态的排班可以审核发布");
        }
        assertScheduleAuditor(row.getDeptId());
        updatePublishStatus(id, PublishStatusEnum.PUBLISHED.getCode());
        scheduleChangeLogService.record(id, ScheduleChangeTypeEnum.PUBLISH, null, null, null, null, null,
                "待审核 → 已发布");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(StaffScheduleRejectDTO dto) {
        BizSchedule row = requireScheduleRow(dto.getId());
        // 员工→门诊联动：门诊绑定行由门诊侧管理，员工侧不得驳回
        assertNotClinicBound(row);
        if (!PublishStatusEnum.PENDING.getCode().equals(row.getPublishStatus())) {
            throw new BusinessException("只有待审核状态的排班可以驳回");
        }
        assertScheduleAuditor(row.getDeptId());
        updatePublishStatus(dto.getId(), PublishStatusEnum.DRAFT.getCode());
        String reason = (dto.getReason() == null || dto.getReason().isBlank()) ? "" : "：" + dto.getReason();
        scheduleChangeLogService.record(dto.getId(), ScheduleChangeTypeEnum.REJECT, null, null, null, null, null,
                "待审核 → 草稿" + reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recall(Long id) {
        BizSchedule row = requireScheduleRow(id);
        // 员工→门诊联动：门诊绑定行的发布态由门诊侧驱动，员工排班侧不得撤回
        assertNotClinicBound(row);
        String from = PublishStatusEnum.getLabel(row.getPublishStatus());
        updatePublishStatus(id, PublishStatusEnum.DRAFT.getCode());
        scheduleChangeLogService.record(id, ScheduleChangeTypeEnum.RECALL, null, null, null, null, null,
                from + " → 草稿");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStart(Long id) {
        BizSchedule row = requireScheduleRow(id);
        // 员工→门诊联动：门诊绑定行归门诊侧管理，员工排班侧不得进入调班
        assertNotClinicBound(row);
        if (!PublishStatusEnum.isEffective(row.getPublishStatus())) {
            throw new BusinessException("只有已发布/已变更的排班可以进入调班");
        }
        updatePublishStatus(id, PublishStatusEnum.ADJUSTING.getCode());
        scheduleChangeLogService.record(id, ScheduleChangeTypeEnum.ADJUST, null, null, null, null, null,
                PublishStatusEnum.getLabel(row.getPublishStatus()) + " → 调班中");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitWeek(StaffScheduleWeekActionDTO dto) {
        assertUnitScope(dto.getOrgType(), dto.getOrgId(), dto.getScope(), "整周提交审核");
        LocalDate start = mondayOf(dto.getWeekStart());
        LocalDate end = start.plusDays(6);
        int moved = baseMapper.updateWeekPublishStatusAll(dto.getOrgType(), dto.getOrgId(), start, end,
                PublishStatusEnum.DRAFT.getCode(), PublishStatusEnum.PENDING.getCode());
        if (moved == 0) {
            throw new BusinessException("本周没有待提交的草稿排班（已发布/待审核的不会重复提交）");
        }
        scheduleChangeLogService.record(null, ScheduleChangeTypeEnum.SUBMIT_WEEK, null, null, null, null, moved,
                weekRangeText(start, end));
        return moved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditWeek(StaffScheduleWeekActionDTO dto) {
        assertUnitScope(dto.getOrgType(), dto.getOrgId(), dto.getScope(), "整周审核发布");
        Long deptId = resolveDeptId(dto.getOrgType(), dto.getOrgId());
        assertScheduleAuditor(deptId);
        LocalDate start = mondayOf(dto.getWeekStart());
        LocalDate end = start.plusDays(6);
        int moved = baseMapper.updateWeekPublishStatusAll(dto.getOrgType(), dto.getOrgId(), start, end,
                PublishStatusEnum.PENDING.getCode(), PublishStatusEnum.PUBLISHED.getCode());
        if (moved == 0) {
            throw new BusinessException("本周没有待审核的排班（草稿需先提交，已发布的不会重复发布）");
        }
        scheduleChangeLogService.record(null, ScheduleChangeTypeEnum.PUBLISH_WEEK, null, null, null, null, moved,
                weekRangeText(start, end));
        return moved;
    }

    @Override
    public List<StaffScheduleVO.UnitOption> unitSelectList(Integer orgType, Integer scope) {
        List<StaffScheduleVO.UnitOption> options = new ArrayList<>();
        boolean all = orgType == null;
        // 域收口：急诊排班页(scope=ER)只出急诊叶子科室；员工排班页(scope=STAFF)排除急诊科室。
        // 叶子口径（去虚父级）与值班公开表一致：排班排到虚父级上没有意义。
        Set<Long> erInclude = null;
        Set<Long> erExclude = null;
        if (StaffScheduleScopeEnum.ER.getCode().equals(scope)) {
            erInclude = erLeafDeptIds();
        } else if (StaffScheduleScopeEnum.STAFF.getCode().equals(scope)
                || StaffScheduleScopeEnum.OPD.getCode().equals(scope)) {
            erExclude = erDeptIds();
        }
        if (all || OrgUnitTypeEnum.HOSPITAL.getCode() == orgType) {
            options.add(new StaffScheduleVO.UnitOption(0L, "全院", null));
        }
        if (all || OrgUnitTypeEnum.DEPT.getCode() == orgType) {
            for (DeptBriefVO dept : systemFacadeService.listEnabledDepartments()) {
                if (erExclude != null && erExclude.contains(dept.getId())) {
                    continue;
                }
                if (erInclude != null && !erInclude.contains(dept.getId())) {
                    continue;
                }
                options.add(new StaffScheduleVO.UnitOption(dept.getId(), dept.getDeptName(), null));
            }
        }
        if (all || OrgUnitTypeEnum.WARD.getCode() == orgType) {
            List<WardBriefVO> wards = systemFacadeService.listEnabledWards();
            Map<Long, String> deptNames = deptNameMapOf(wards);
            for (WardBriefVO ward : wards) {
                options.add(new StaffScheduleVO.UnitOption(ward.getWardId(), ward.getWardName(),
                        deptNames.get(ward.getDeptId())));
            }
        }
        return options;
    }

    /**
     * 病区所属科室名（一次批量查，避免 N+1）
     */
    private Map<Long, String> deptNameMapOf(List<WardBriefVO> wards) {
        Set<Long> deptIds = new HashSet<>();
        for (WardBriefVO ward : wards) {
            if (ward.getDeptId() != null) {
                deptIds.add(ward.getDeptId());
            }
        }
        if (deptIds.isEmpty()) {
            return Map.of();
        }
        return systemFacadeService.listDepartmentsByIds(deptIds).stream()
                .collect(Collectors.toMap(DeptBriefVO::getId, DeptBriefVO::getDeptName, (a, b) -> a));
    }

    /**
     * 全院岗位排班审核人校验：系统管理员(10012)全局豁免；
     * 其余当前登录人必须是该科室在科室主档指定的全院岗位排班审核人(schedule_auditor_id)。
     * orgType=3（全院级）没有归属科室（deptId=null），仅系统管理员可审核发布。
     */
    private void assertScheduleAuditor(Long deptId) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("未登录或登录已过期，请重新登录");
        }
        if (ROLE_ADMIN.equals(user.getCurrentRole())) {
            return;
        }
        DeptBriefVO dept = systemFacadeService.findDepartment(deptId);
        if (dept == null || dept.getScheduleAuditorId() == null
                || !dept.getScheduleAuditorId().equals(user.getEmployeeId())) {
            throw new BusinessException("您不是本科室指定的全院岗位排班审核人，无权审核发布");
        }
    }

    /**
     * 取一行排班并校验数据范围（跨科室操作一律挡）
     */
    private BizSchedule requireScheduleRow(Long id) {
        if (id == null) {
            throw new BusinessException("缺少排班行ID");
        }
        BizSchedule row = getById(id);
        if (row == null) {
            throw new BusinessException("排班行不存在或已删除");
        }
        deptScopeService.assertDeptAccessible(row.getDeptId());
        return row;
    }

    private void updatePublishStatus(Long id, Integer publishStatus) {
        BizSchedule update = new BizSchedule();
        update.setId(id);
        update.setPublishStatus(publishStatus);
        updateById(update);
    }

    /**
     * 把任意一天归到所在周的周一
     */
    private LocalDate mondayOf(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }

    /**
     * 单元类型 + 单元ID → 归属科室ID（orgType=3 全院级无归属科室，返回 null）
     */
    private Long resolveDeptId(Integer orgType, Long orgId) {
        if (orgType == null) {
            return null;
        }
        if (OrgUnitTypeEnum.HOSPITAL.getCode() == orgType) {
            return null;
        }
        if (OrgUnitTypeEnum.DEPT.getCode() == orgType) {
            return orgId;
        }
        if (OrgUnitTypeEnum.WARD.getCode() == orgType) {
            if (orgId == null) {
                return null;
            }
            WardBriefVO ward = systemFacadeService.findWard(orgId);
            return ward == null ? null : ward.getDeptId();
        }
        return null;
    }

    private String weekRangeText(LocalDate start, LocalDate end) {
        return start + " ～ " + end;
    }

    @Override
    public PageResult<StaffScheduleVO> pendingApprovalPage(StaffSchedulePendingQueryDTO dto) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("未登录或登录已过期，请重新登录");
        }
        boolean admin = ROLE_ADMIN.equals(user.getCurrentRole());
        Set<Long> deptIds = null;
        if (!admin) {
            List<DeptBriefVO> depts = systemFacadeService.listDepartmentsByScheduleAuditor(user.getEmployeeId());
            deptIds = depts.stream().map(DeptBriefVO::getId).collect(Collectors.toSet());
            if (deptIds.isEmpty()) {
                return PageResult.of(0, dto.getPageNum(), dto.getPageSize(), 0, List.of());
            }
        }
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<BizSchedule>()
                .eq(BizSchedule::getPublishStatus, PublishStatusEnum.PENDING.getCode())
                // 员工排班审批中心只审「纯员工排班」：排除门诊派生行(clinic_flag=1，走门诊审批)
                // 与护理病区行(org_type=2，走护理审批)；org_type=1 科室 + org_type=3 全院保留
                .and(w -> w.isNull(BizSchedule::getClinicFlag).or().eq(BizSchedule::getClinicFlag, 0))
                .ne(BizSchedule::getOrgType, 2)
                .eq(dto.getOrgType() != null, BizSchedule::getOrgType, dto.getOrgType())
                .eq(dto.getOrgId() != null, BizSchedule::getOrgId, dto.getOrgId());
        if (!admin) {
            // 普通审核人只看本科室主档指定的那几个科室/病区（dept_id 命中），全院级行(dept_id=null)排除
            wrapper.in(BizSchedule::getDeptId, deptIds);
        }
        wrapper.orderByAsc(BizSchedule::getOrgType).orderByAsc(BizSchedule::getScheduleDate)
                .orderByAsc(BizSchedule::getStartTime).orderByAsc(BizSchedule::getId);
        Page<BizSchedule> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        Map<Long, BizShift> shifts = shiftService.mapByIds(shiftIdsOf(page.getRecords()));
        List<StaffScheduleVO> vos = new ArrayList<>(page.getRecords().size());
        for (BizSchedule row : page.getRecords()) {
            vos.add(toVO(row, shifts.get(row.getShiftId())));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    /**
     * 一条排班摊开后的时间段
     */
    private record TimeRange(LocalDateTime from, LocalDateTime to) {
    }

    /**
     * ISO 周键（周工时按周聚合用）
     */
    private record YearWeek(int year, int week) {
        static YearWeek of(LocalDate date) {
            return new YearWeek(date.getYear(), date.get(WeekFields.ISO.weekOfWeekBasedYear()));
        }
    }
}
