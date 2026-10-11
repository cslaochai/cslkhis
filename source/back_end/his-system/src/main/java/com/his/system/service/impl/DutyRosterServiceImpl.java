package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.dto.*;
import com.his.system.entity.*;
import com.his.system.mapper.*;
import com.his.system.service.DutyRosterService;
import com.his.system.service.ShiftService;
import com.his.system.service.StaffScheduleService;
import com.his.system.vo.DutyOfficerVO;
import com.his.system.vo.DutyRosterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 全院总值班排班服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DutyRosterServiceImpl extends ServiceImpl<BizDutyRosterMapper, BizDutyRoster> implements DutyRosterService {

    /**
     * 白班起点 / 夜班起点（解析「此刻属于哪一段」用，与班次字典的值守册同口径）
     */
    private static final int DAY_START_HOUR = 8;
    private static final int NIGHT_START_HOUR = 18;

    /**
     * 列表默认窗口：昨天（凌晨解析用）起，往后 30 天
     */
    private static final int DEFAULT_BACK_DAYS = 1;
    private static final int DEFAULT_FORWARD_DAYS = 30;

    private final ShiftService shiftService;

    private final StaffScheduleService staffScheduleService;

    private final BizDutyRosterMapper bizDutyRosterMapper;

    private final BizDutyPostMapper bizDutyPostMapper;

    private final BizShiftMapper bizShiftMapper;

    private final SysEmployeeMapper sysEmployeeMapper;

    private final SysDepartmentMapper sysDepartmentMapper;

    /**
     * 当前时刻的总值班（跨自然日的夜班按开始日解析）
     */
    public DutyOfficerVO current() {
        return currentAt(LocalDateTime.now());
    }

    /**
     * 指定时刻的总值班（定时任务的补跑 / 验证脚本要能按点验算，所以显式开放）
     */
    public DutyOfficerVO currentAt(LocalDateTime at) {
        LocalDate date;
        int shift;
        int hour = at.getHour();
        if (hour < DAY_START_HOUR) {
            date = at.toLocalDate().minusDays(1);
            shift = DutyShiftTypeEnum.NIGHT.getCode();
        } else if (hour < NIGHT_START_HOUR) {
            date = at.toLocalDate();
            shift = DutyShiftTypeEnum.DAY.getCode();
        } else {
            date = at.toLocalDate();
            shift = DutyShiftTypeEnum.NIGHT.getCode();
        }

        return officerOf(date, shift);
    }

    /**
     * 指定「日期 + 班次」的总值班（不走"当前时刻"推断）。
     * 值班日志交班时用它推算接班人：白班交给同日夜班，夜班交给次日白班。
     */
    public DutyOfficerVO officerOf(LocalDate date, int shift) {
        BizDutyRoster main = pick(date, shift, DutyRoleTypeEnum.PRIMARY.getCode());
        BizDutyRoster row = main != null ? main : pick(date, shift, DutyRoleTypeEnum.SECONDARY.getCode());
        if (row == null) {
            return empty(String.format("%s %s 未排总值班（主班副班都没有），全院应急协调当前无人接手，请先在「总值班排班」登记",
                    date, shiftText(shift)));
        }
        return toOfficer(row, main == null);
    }

    /**
     * 下一班的总值班：白班 → 同日夜班，夜班 → 次日白班。
     * 交班时接班人的默认值由这里给 —— 交班最怕的是"没想好交给谁"，
     * 让他必须显式挑一个人，结果通常是随手选个不相干的人，或者干脆不交。
     */
    public DutyOfficerVO nextOfficer(LocalDate date, int shift) {
        return shift == DutyShiftTypeEnum.DAY.getCode()
                ? officerOf(date, DutyShiftTypeEnum.NIGHT.getCode())
                : officerOf(date.plusDays(1), DutyShiftTypeEnum.DAY.getCode());
    }

    /**
     * 今天（自然日）的全部排班：白班主/副 + 夜班主/副。
     * 界面上「今日排班」表用这个；「此刻谁负责」用 {@link #current()} —— 两件事不要混：
     * 凌晨看今日排班表，看到的是今天白天的人，而现在值班的是昨天夜班的人。
     */
    public List<DutyRosterVO> todayList() {
        return listOf(LocalDate.now(), true);
    }

    /**
     * 今日总值班表：只列行政那几条。
     * 24 个临床科室的一线/二线/三线混进这张表，它就回答不了「此刻全院谁负责」了 ——
     * 要看科室医师值班请走 {@code listPage} 带 {@code dutyScope=6}。
     */
    private List<DutyRosterVO> listOf(LocalDate date, Boolean adminOnly) {
        List<BizDutyRoster> rows = bizDutyRosterMapper.selectList(new LambdaQueryWrapper<BizDutyRoster>()
                .eq(BizDutyRoster::getDutyDate, date)
                .orderByAsc(BizDutyRoster::getShiftType)
                .orderByAsc(BizDutyRoster::getRoleType)
                .orderByAsc(BizDutyRoster::getId));
        if (Boolean.TRUE.equals(adminOnly)) {
            List<Long> adminIds = adminPostIds();
            rows = rows.stream().filter(r -> r.getPostId() == null || adminIds.contains(r.getPostId())).toList();
        }
        return toRosterVOs(rows);
    }

    // 排班维护
    public PageResult<DutyRosterVO> listPage(DutyRosterQueryPageDTO q) {
        LocalDate begin = q.getBeginDate() != null ? q.getBeginDate() : LocalDate.now().minusDays(DEFAULT_BACK_DAYS);
        LocalDate end = q.getEndDate() != null ? q.getEndDate() : LocalDate.now().plusDays(DEFAULT_FORWARD_DAYS);
        LambdaQueryWrapper<BizDutyRoster> w = new LambdaQueryWrapper<>();
        w.ge(BizDutyRoster::getDutyDate, begin)
                .le(BizDutyRoster::getDutyDate, end)
                .eq(q.getShiftType() != null, BizDutyRoster::getShiftType, q.getShiftType())
                .eq(q.getRoleType() != null, BizDutyRoster::getRoleType, q.getRoleType())
                .eq(q.getEmployeeId() != null, BizDutyRoster::getEmployeeId, q.getEmployeeId());
        // 按点位 / 责任范围筛：临床医师值班与全院总值班同居一表，不看点位就分不开
        if (q.getPostId() != null) {
            w.eq(BizDutyRoster::getPostId, q.getPostId());
        } else if (q.getDutyScope() != null) {
            List<Long> ids = postIdsOfScope(q.getDutyScope());
            w.in(!ids.isEmpty(), BizDutyRoster::getPostId, ids.isEmpty() ? List.of(0L) : ids);
        } else if (Boolean.TRUE.equals(q.getAdminOnly())) {
            List<Long> adminIds = adminPostIds();
            w.and(x -> {
                x.isNull(BizDutyRoster::getPostId);
                if (!adminIds.isEmpty()) {
                    x.or().in(BizDutyRoster::getPostId, adminIds);
                }
            });
        }
        // 二级键收口：同一天同班次同角色的顺序必须稳定（MP 分页只按这些列排会有重复/漏行）
        w.orderByAsc(BizDutyRoster::getDutyDate)
                .orderByAsc(BizDutyRoster::getShiftType)
                .orderByAsc(BizDutyRoster::getRoleType)
                .orderByAsc(BizDutyRoster::getId);
        IPage<BizDutyRoster> page = bizDutyRosterMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        List<DutyRosterVO> list = toRosterVOs(page.getRecords());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), list);
    }

    /**
     * 某个责任范围下的全部点位ID（临床=6 一次能捞出 24 个科室位）
     */
    private List<Long> postIdsOfScope(Integer dutyScope) {
        List<BizDutyPost> posts = bizDutyPostMapper.selectList(new LambdaQueryWrapper<BizDutyPost>()
                .eq(BizDutyPost::getDutyScope, dutyScope)
                .eq(BizDutyPost::getStatus, EnableStatusEnum.ENABLED.getCode()));
        List<Long> ids = new ArrayList<>();
        for (BizDutyPost p : posts) {
            if (!Objects.equals(1, p.getDelFlag())) {
                ids.add(p.getId());
            }
        }
        return ids;
    }

    private List<DutyRosterVO> toRosterVOs(List<BizDutyRoster> rows) {
        // 点位一次捞齐再渲染：一行一个点位会变成 N+1（列表 10 行就是 10 次往返）
        List<Long> postIds = rows.stream().map(BizDutyRoster::getPostId)
                .filter(Objects::nonNull).distinct().toList();
        Map<Long, BizDutyPost> posts = new HashMap<>();
        if (!postIds.isEmpty()) {
            for (BizDutyPost p : bizDutyPostMapper.selectBatchIds(postIds)) {
                posts.put(p.getId(), p);
            }
        }
        List<DutyRosterVO> vos = new ArrayList<>();
        for (BizDutyRoster r : rows) {
            vos.add(toRosterVO(r, r.getPostId() == null ? null : posts.get(r.getPostId())));
        }
        return vos;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long upsert(DutyRosterUpsertDTO dto) {
        DutyShiftTypeEnum shiftType = DutyShiftTypeEnum.fromCode(dto.getShiftType());
        DutyRoleTypeEnum roleType = DutyRoleTypeEnum.fromCode(dto.getRoleType());
        SysEmployee emp = requireOnDutyEmployee(dto.getEmployeeId());
        BizDutyPost post = resolvePost(dto, roleType);
        if (post != null) {
            assertPostStaffType(post, emp);
        }
        BizShift shift = post != null ? bizShiftMapper.selectById(post.getShiftId()) : dutyShiftOf(shiftType);
        if (post != null && shift == null) {
            throw new BusinessException("点位「" + post.getPostName() + "」绑定的班次不存在，请先到点位维护里重新绑定班次");
        }
        // 点位是权威，白/夜按班次自身的 cross_day 认，不认入参（入参可能与点位班次打架）
        int shiftCode = shift != null && Objects.equals(YesOrNoEnum.YES.getCode(), shift.getCrossDay())
                ? DutyShiftTypeEnum.NIGHT.getCode() : DutyShiftTypeEnum.DAY.getCode();
        int roleCode = post != null && post.getRoleType() != null ? post.getRoleType() : roleType.getCode();

        BizDutyRoster row = pickAny(dto.getDutyDate(), post, shiftCode, roleCode);
        boolean insert = row == null;
        Long boundFactId = insert ? null : row.getStaffScheduleId();
        if (insert) {
            row = new BizDutyRoster();
            row.setDutyDate(dto.getDutyDate());
        }
        boolean enabled = dto.getStatus() == null || !Objects.equals(EnableStatusEnum.DISABLED.getCode(), dto.getStatus());
        row.setShiftType(shiftCode);
        row.setRoleType(roleCode);
        row.setEmployeeId(emp.getId());
        row.setEmployeeName(emp.getEmpName());
        row.setDeptId(emp.getDeptId());
        row.setDeptName(resolveDeptName(emp));
        row.setPhone(TextUtil.hasText(dto.getPhone()) ? dto.getPhone().trim()
                : (post != null && TextUtil.hasText(post.getPhone()) ? post.getPhone() : emp.getPhone()));
        row.setShiftId(shift == null ? null : shift.getId());
        row.setPostId(post == null ? null : post.getId());
        row.setStartTime(shift != null ? shift.getStartTime() : TextUtil.trimToNull(dto.getStartTime()));
        row.setEndTime(shift != null ? shift.getEndTime() : TextUtil.trimToNull(dto.getEndTime()));
        row.setStatus(enabled ? EnableStatusEnum.ENABLED.getCode() : EnableStatusEnum.DISABLED.getCode());
        row.setRemark(dto.getRemark());
        row.setStaffScheduleId(enabled && shift != null
                ? dutyAttendance(dto.getDutyDate(), emp, shift, post, dto.getRemark()).getId()
                : null);

        saveRow(row, insert);
        if (boundFactId != null && !Objects.equals(boundFactId, row.getStaffScheduleId())) {
            releaseFactIfUnused(boundFactId);
        }
        log.info("值班排班{} date={} post={} shift={} role={} emp={}", insert ? "登记" : "修改",
                row.getDutyDate(), post == null ? "-" : post.getPostName(),
                row.getShiftType(), row.getRoleType(), row.getEmployeeName());
        return row.getId();
    }

    /**
     * 临时换班：写 substitute_*，不动原值班人。
     */
    @Transactional(rollbackFor = Exception.class)
    public void substitute(DutySubstituteDTO dto) {
        BizDutyRoster row = requireRow(dto.getId());
        SysEmployee emp = requireOnDutyEmployee(dto.getSubstituteEmpId());
        // 在岗事实改到承接的人身上：他真的上了这个班，今日在岗名单与工时就得算他的。
        // 排班行上的 employee_id 原样不动 —— 那是「原本排的是谁」的追责凭据。
        transferAttendance(row, emp.getId(), dto.getSubstituteReason().trim());
        row.setSubstituteEmpId(emp.getId());
        row.setSubstituteEmpName(emp.getEmpName());
        row.setSubstituteTime(LocalDateTime.now());
        row.setSubstituteReason(dto.getSubstituteReason().trim());
        // 换班后联系电话跟着人走：留了新号码就用新的，否则回落新员工档案手机
        row.setPhone(TextUtil.hasText(dto.getPhone()) ? dto.getPhone().trim() : emp.getPhone());
        bizDutyRosterMapper.updateById(row);
        log.info("总值班换班 date={} shift={} role={} 原={} 现={} 原因={}",
                row.getDutyDate(), row.getShiftType(), row.getRoleType(),
                row.getEmployeeName(), emp.getEmpName(), row.getSubstituteReason());
    }

    /**
     * 取消换班：恢复成原排班人（换班错了要能撤回，但撤回本身也留痕）
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelSubstitute(Long id) {
        BizDutyRoster row = requireRow(id);
        if (row.getSubstituteEmpId() == null) {
            throw new BusinessException("该排班未换班，无需撤回");
        }
        transferAttendance(row, row.getEmployeeId(), "取消换班，交回原值班人");
        SysEmployee origin = sysEmployeeMapper.selectById(row.getEmployeeId());
        bizDutyRosterMapper.update(null, new LambdaUpdateWrapper<BizDutyRoster>()
                .eq(BizDutyRoster::getId, row.getId())
                .set(BizDutyRoster::getSubstituteEmpId, null)
                .set(BizDutyRoster::getSubstituteEmpName, null)
                .set(BizDutyRoster::getSubstituteTime, null)
                .set(BizDutyRoster::getSubstituteReason, null)
                .set(BizDutyRoster::getPhone, origin == null ? null : origin.getPhone()));
    }

    /**
     * 删除排班（物理删：唯一键不含 del_flag，软删后同日同班次同角色再也排不上）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        BizDutyRoster row = bizDutyRosterMapper.selectById(id);
        if (row == null) {
            throw new BusinessException("排班记录不存在");
        }
        Long factId = row.getStaffScheduleId();
        bizDutyRosterMapper.purgeById(id);
        if (factId != null) {
            releaseFactIfUnused(factId);
        }
    }

    // 内部

    private BizDutyRoster requireRow(Long id) {
        BizDutyRoster row = id == null ? null : bizDutyRosterMapper.selectById(id);
        if (row == null) {
            throw new BusinessException("排班记录不存在");
        }
        return row;
    }

    /**
     * 排班行的落库。更新必须逐列显式 set：换人/停用时 shift_id、post_id、staff_schedule_id
     */
    private void saveRow(BizDutyRoster row, boolean insert) {
        if (insert) {
            bizDutyRosterMapper.insert(row);
            return;
        }
        bizDutyRosterMapper.update(null, new LambdaUpdateWrapper<BizDutyRoster>()
                .eq(BizDutyRoster::getId, row.getId())
                .set(BizDutyRoster::getEmployeeId, row.getEmployeeId())
                .set(BizDutyRoster::getEmployeeName, row.getEmployeeName())
                .set(BizDutyRoster::getDeptId, row.getDeptId())
                .set(BizDutyRoster::getDeptName, row.getDeptName())
                .set(BizDutyRoster::getPhone, row.getPhone())
                .set(BizDutyRoster::getShiftId, row.getShiftId())
                .set(BizDutyRoster::getPostId, row.getPostId())
                .set(BizDutyRoster::getStaffScheduleId, row.getStaffScheduleId())
                .set(BizDutyRoster::getStartTime, row.getStartTime())
                .set(BizDutyRoster::getEndTime, row.getEndTime())
                .set(BizDutyRoster::getStatus, row.getStatus())
                .set(BizDutyRoster::getRemark, row.getRemark()));
    }

    /**
     * 登记这条值班的在岗事实 —— 值班不是"写在册子上"，它就是这个人的一段真实出勤。
     */
    private BizSchedule dutyAttendance(LocalDate date, SysEmployee emp, BizShift shift,
                                            BizDutyPost post, String remark) {
        StaffScheduleUpsertDTO core = new StaffScheduleUpsertDTO();
        core.setScheduleDate(date);
        boolean hospitalWide = post == null || post.getOrgId() == null
                || Objects.equals(DutyScopeEnum.ADMIN.getCode(), post.getDutyScope())
                || Objects.equals(OrgUnitTypeEnum.HOSPITAL.getCode(), post.getOrgType());
        if (hospitalWide) {
            core.setOrgType(OrgUnitTypeEnum.HOSPITAL.getCode());
        } else {
            core.setOrgType(post.getOrgType());
            core.setOrgId(post.getOrgId());
        }
        core.setEmployeeId(emp.getId());
        core.setShiftId(shift.getId());
        core.setDutyStatus(StaffDutyStatusEnum.WORK.getCode());
        core.setAttendMode(post != null && post.getAttendMode() != null
                ? post.getAttendMode() : AttendModeEnum.IN_HOSPITAL.getCode());
        core.setClinicFlag(YesOrNoEnum.NO.getCode());
        core.setRemark(remark);
        return staffScheduleService.ensureAttendance(core, StaffScheduleSourceEnum.MANUAL);
    }

    /**
     * 把这条位的在岗事实交给承接的人（人没变就不动，避免每次都记一条换班留痕）
     */
    private void transferAttendance(BizDutyRoster row, Long toEmployeeId, String reason) {
        if (row.getStaffScheduleId() == null || toEmployeeId == null) {
            log.warn("总值班排班 {} 没有绑定在岗事实，换班只记在排班行上", row.getId());
            return;
        }
        BizSchedule bound = staffScheduleService.getById(row.getStaffScheduleId());
        if (bound != null && Objects.equals(bound.getEmployeeId(), toEmployeeId)) {
            return;
        }
        StaffScheduleSwapDTO swap = new StaffScheduleSwapDTO();
        swap.setFromScheduleId(row.getStaffScheduleId());
        swap.setSubstituteEmployeeId(toEmployeeId);
        swap.setReason(reason);
        staffScheduleService.swap(swap);
    }

    /**
     * 这条在岗事实不再被任何值守位引用时，把事实收掉（同一个人主班副班共用一条时不能误删）
     */
    private void releaseFactIfUnused(Long staffScheduleId) {
        Long holding = bizDutyRosterMapper.selectCount(new LambdaQueryWrapper<BizDutyRoster>()
                .eq(BizDutyRoster::getStaffScheduleId, staffScheduleId));
        if (holding == null || holding == 0L) {
            staffScheduleService.deleteById(staffScheduleId);
        }
    }

    /**
     * 值守册里按「跨不跨零点」认白/夜：夜段就是 18:00~次日 08:00 那条，白段是同日起止的那条。
     */
    private BizShift dutyShiftOf(DutyShiftTypeEnum shiftType) {
        boolean night = shiftType == DutyShiftTypeEnum.NIGHT;
        List<BizShift> shifts = shiftService.listShifts(null, EnableStatusEnum.ENABLED.getCode(),
                ShiftUseScopeEnum.DUTY.getCode());
        for (BizShift shift : shifts) {
            boolean crossDay = Objects.equals(YesOrNoEnum.YES.getCode(), shift.getCrossDay());
            if (crossDay == night) {
                return shift;
            }
        }
        return null;
    }

    /**
     * 点位解析：传了 {@code postId} 就以它为准（科室医师值班走这条）；
     */
    private BizDutyPost resolvePost(DutyRosterUpsertDTO dto, DutyRoleTypeEnum roleType) {
        if (dto.getPostId() != null) {
            BizDutyPost post = bizDutyPostMapper.selectById(dto.getPostId());
            if (post == null || Objects.equals(1, post.getDelFlag())) {
                throw new BusinessException("值班点位不存在或已删除");
            }
            if (!Objects.equals(EnableStatusEnum.ENABLED.getCode(), post.getStatus())) {
                throw new BusinessException("值班点位「" + post.getPostName() + "」已停用，不能排班");
            }
            return post;
        }
        BizShift shift = dutyShiftOf(DutyShiftTypeEnum.fromCode(dto.getShiftType()));
        if (shift == null) {
            return null;
        }
        List<BizDutyPost> posts = bizDutyPostMapper.selectList(new LambdaQueryWrapper<BizDutyPost>()
                .eq(BizDutyPost::getShiftId, shift.getId())
                .eq(BizDutyPost::getRoleType, roleType.getCode())
                .eq(BizDutyPost::getDutyScope, DutyScopeEnum.ADMIN.getCode())
                .eq(BizDutyPost::getStatus, EnableStatusEnum.ENABLED.getCode())
                .orderByAsc(BizDutyPost::getSortNo)
                .orderByAsc(BizDutyPost::getId)
                .last("LIMIT 1"));
        return posts.isEmpty() ? null : posts.get(0);
    }

    /**
     * 点位要求的岗位类别校验：临床医师值班点位 required_staff_type=1，
     */
    private void assertPostStaffType(BizDutyPost post, SysEmployee emp) {
        Integer required = post.getRequiredStaffType();
        if (required == null || required == 0) {
            return;
        }
        if (!Objects.equals(required, emp.getEmpType())) {
            throw new BusinessException("点位「" + post.getPostName() + "」要求"
                    + StaffTypeEnum.getText(required) + "岗，而「" + emp.getEmpName() + "」是"
                    + StaffTypeEnum.getText(emp.getEmpType()) + "岗，排上去这个位没人能顶");
        }
    }

    /**
     * 全部「全院行政」点位ID —— 「此刻谁负责」只在这些位上解析
     */
    private List<Long> adminPostIds() {
        List<BizDutyPost> posts = bizDutyPostMapper.selectList(new LambdaQueryWrapper<BizDutyPost>()
                .eq(BizDutyPost::getDutyScope, DutyScopeEnum.ADMIN.getCode())
                .eq(BizDutyPost::getStatus, EnableStatusEnum.ENABLED.getCode()));
        List<Long> ids = new ArrayList<>();
        for (BizDutyPost p : posts) {
            if (!Objects.equals(1, p.getDelFlag())) {
                ids.add(p.getId());
            }
        }
        return ids;
    }

    /**
     * 取一行有效总值班（status=1）；同一键理论上只有一行，仍按 id 升序取第一条，绝不 selectOne 撞多行。
     */
    private BizDutyRoster pick(LocalDate date, int shift, int role) {
        List<Long> adminIds = adminPostIds();
        LambdaQueryWrapper<BizDutyRoster> w = new LambdaQueryWrapper<>();
        w.eq(BizDutyRoster::getDutyDate, date)
                .eq(BizDutyRoster::getShiftType, shift)
                .eq(BizDutyRoster::getRoleType, role)
                .eq(BizDutyRoster::getStatus, 1)
                .and(x -> {
                    x.isNull(BizDutyRoster::getPostId);
                    if (!adminIds.isEmpty()) {
                        x.or().in(BizDutyRoster::getPostId, adminIds);
                    }
                })
                .orderByAsc(BizDutyRoster::getId)
                .last("LIMIT 1");
        List<BizDutyRoster> rows = bizDutyRosterMapper.selectList(w);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 忽略 status 查重（upsert 用：停用的行也要能被重新启用，而不是插第二行撞唯一键）。
     */
    private BizDutyRoster pickAny(LocalDate date, BizDutyPost post, int shift, int role) {
        LambdaQueryWrapper<BizDutyRoster> w = new LambdaQueryWrapper<>();
        w.eq(BizDutyRoster::getDutyDate, date);
        if (post != null) {
            w.eq(BizDutyRoster::getPostId, post.getId());
        } else {
            w.eq(BizDutyRoster::getShiftType, shift)
                    .eq(BizDutyRoster::getRoleType, role)
                    .isNull(BizDutyRoster::getPostId);
        }
        w.orderByAsc(BizDutyRoster::getId).last("LIMIT 1");
        List<BizDutyRoster> rows = bizDutyRosterMapper.selectList(w);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private DutyOfficerVO toOfficer(BizDutyRoster r, boolean viceTookOver) {
        boolean substituted = r.getSubstituteEmpId() != null;
        Long empId = substituted ? r.getSubstituteEmpId() : r.getEmployeeId();
        String name = substituted ? r.getSubstituteEmpName() : r.getEmployeeName();

        DutyOfficerVO vo = new DutyOfficerVO();
        vo.setFound(1);
        vo.setEmployeeId(empId);
        vo.setEmployeeName(name);
        vo.setPhone(resolvePhone(r, empId));
        vo.setDeptId(r.getDeptId());
        vo.setDeptName(r.getDeptName());
        vo.setDutyDate(r.getDutyDate());
        vo.setShiftType(r.getShiftType());
        vo.setShiftTypeText(shiftText(r.getShiftType()));
        vo.setRoleType(r.getRoleType());
        vo.setRoleTypeText(DutyRoleTypeEnum.getText(r.getRoleType()));
        vo.setStartTime(r.getStartTime());
        vo.setEndTime(r.getEndTime());
        vo.setSubstituted(substituted ? 1 : 0);
        vo.setOriginEmpName(substituted ? r.getEmployeeName() : null);
        if (viceTookOver) {
            vo.setEmptyReason(null);
        }
        return vo;
    }

    private DutyOfficerVO empty(String reason) {
        DutyOfficerVO vo = new DutyOfficerVO();
        vo.setFound(0);
        vo.setSubstituted(0);
        vo.setEmptyReason(reason);
        return vo;
    }

    private DutyRosterVO toRosterVO(BizDutyRoster r, BizDutyPost post) {
        boolean substituted = r.getSubstituteEmpId() != null;
        DutyRosterVO vo = new DutyRosterVO();
        vo.setId(r.getId());
        vo.setDutyDate(r.getDutyDate());
        vo.setPostId(r.getPostId());
        vo.setPostName(post == null ? null : post.getPostName());
        vo.setDutyScope(post == null ? null : post.getDutyScope());
        vo.setDutyScopeText(post == null ? null : DutyScopeEnum.getText(post.getDutyScope()));
        vo.setDutyLevel(post == null ? null : post.getDutyLevel());
        vo.setDutyLevelText(post == null ? null : DutyLevelEnum.getText(post.getDutyLevel()));
        vo.setAttendMode(post == null ? null : post.getAttendMode());
        vo.setAttendModeText(post == null ? null : AttendModeEnum.getText(post.getAttendMode()));
        vo.setOrgType(post == null ? null : post.getOrgType());
        vo.setOrgId(post == null ? null : post.getOrgId());
        vo.setShiftType(r.getShiftType());
        vo.setShiftTypeText(shiftText(r.getShiftType()));
        vo.setRoleType(r.getRoleType());
        vo.setRoleTypeText(DutyRoleTypeEnum.getText(r.getRoleType()));
        vo.setEmployeeId(r.getEmployeeId());
        vo.setEmployeeName(r.getEmployeeName());
        vo.setDeptId(r.getDeptId());
        vo.setDeptName(r.getDeptName());
        vo.setPhone(r.getPhone());
        vo.setStartTime(r.getStartTime());
        vo.setEndTime(r.getEndTime());
        vo.setStatus(r.getStatus());
        vo.setSubstituted(substituted ? 1 : 0);
        vo.setActualEmpId(substituted ? r.getSubstituteEmpId() : r.getEmployeeId());
        vo.setActualEmpName(substituted ? r.getSubstituteEmpName() : r.getEmployeeName());
        vo.setActualPhone(resolvePhone(r, substituted ? r.getSubstituteEmpId() : r.getEmployeeId()));
        vo.setSubstituteEmpName(r.getSubstituteEmpName());
        vo.setSubstituteTime(r.getSubstituteTime());
        vo.setSubstituteReason(r.getSubstituteReason());
        vo.setRemark(r.getRemark());
        return vo;
    }

    /**
     * 联系电话：行内电话（换班后就是换班人的）→ 员工档案手机。都为空返回 null，不编造号码
     */
    private String resolvePhone(BizDutyRoster r, Long empId) {
        if (TextUtil.hasText(r.getPhone())) {
            return r.getPhone();
        }
        if (empId == null) {
            return null;
        }
        SysEmployee emp = sysEmployeeMapper.selectById(empId);
        return emp == null ? null : emp.getPhone();
    }

    private SysEmployee requireOnDutyEmployee(Long empId) {
        SysEmployee emp = sysEmployeeMapper.selectById(empId);
        if (emp == null || (emp.getDelFlag() != null && emp.getDelFlag() == 1)) {
            throw new BusinessException("值班人不存在");
        }
        if (!Objects.equals(1, emp.getStatus())) {
            throw new BusinessException("值班人「" + emp.getEmpName() + "」不在职（离职/停职员工不能排总值班）");
        }
        return emp;
    }

    private String resolveDeptName(SysEmployee emp) {
        if (TextUtil.hasText(emp.getDeptName())) {
            return emp.getDeptName();
        }
        if (emp.getDeptId() == null) {
            return null;
        }
        SysDepartment dept = sysDepartmentMapper.selectById(emp.getDeptId());
        return dept == null ? null : dept.getDeptName();
    }

    private String shiftText(Integer shift) {
        return Objects.equals(DutyShiftTypeEnum.NIGHT.getCode(), shift)
                ? DutyShiftTypeEnum.NIGHT.getLabel() : DutyShiftTypeEnum.DAY.getLabel();
    }

    /**
     * 班内角色文案：角色码认不出来时按副班显示（与「主班优先、其余顶上」的解析口径一致）
     */
    private String roleText(Integer roleType) {
        DutyRoleTypeEnum role = DutyRoleTypeEnum.fromCode(roleType);
        return role == null ? DutyRoleTypeEnum.SECONDARY.getLabel() : role.getLabel();
    }
}
