package com.his.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.schedule.dto.*;
import com.his.schedule.entity.BizDutyPost;
import com.his.schedule.entity.BizDutyRoster;
import com.his.schedule.entity.BizSchedule;
import com.his.schedule.entity.BizShift;
import com.his.schedule.mapper.BizDutyPostMapper;
import com.his.schedule.mapper.BizDutyRosterMapper;
import com.his.schedule.mapper.BizShiftMapper;
import com.his.schedule.service.DutyRosterService;
import com.his.schedule.service.ShiftService;
import com.his.schedule.service.StaffScheduleService;
import com.his.schedule.vo.DutyCheckVO;
import com.his.schedule.vo.DutyOfficerVO;
import com.his.schedule.vo.DutyRosterVO;
import com.his.schedule.vo.DutyWorkloadVO;
import com.his.spi.service.SystemFacadeService;
import com.his.spi.vo.DeptBriefVO;
import com.his.spi.vo.EmployeeBriefVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    private final SystemFacadeService systemFacadeService;

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
                // 对外展示只认生效态：草稿/待审核/调班中的值守不进今日值班表
                .in(BizDutyRoster::getPublishStatus,
                        PublishStatusEnum.PUBLISHED.getCode(), PublishStatusEnum.CHANGED.getCode())
                .orderByAsc(BizDutyRoster::getShiftType)
                .orderByAsc(BizDutyRoster::getRoleType)
                .orderByAsc(BizDutyRoster::getId));
        if (Boolean.TRUE.equals(adminOnly)) {
            List<Long> adminIds = adminPostIds();
            rows = rows.stream().filter(r -> r.getPostId() == null || adminIds.contains(r.getPostId())).toList();
        }
        return toRosterVOs(rows);
    }

    @Override
    public List<DutyRosterVO> listDeptDayOnDuty(Long deptId, LocalDate date) {
        if (deptId == null || date == null) {
            return List.of();
        }
        // 先取本科室启用的临床值班点位（orgType=1 科室、orgId=deptId、责任范围=6 临床），再按点位收白班排班
        List<Long> postIds = bizDutyPostMapper.selectList(new LambdaQueryWrapper<BizDutyPost>()
                        .select(BizDutyPost::getId)
                        .eq(BizDutyPost::getStatus, 1)
                        .eq(BizDutyPost::getOrgType, 1)
                        .eq(BizDutyPost::getOrgId, deptId)
                        .eq(BizDutyPost::getDutyScope, 6))
                .stream().map(BizDutyPost::getId).toList();
        if (postIds.isEmpty()) {
            return List.of();
        }
        List<BizDutyRoster> rows = bizDutyRosterMapper.selectList(new LambdaQueryWrapper<BizDutyRoster>()
                .eq(BizDutyRoster::getDutyDate, date)
                .eq(BizDutyRoster::getShiftType, DutyShiftTypeEnum.DAY.getCode())
                .eq(BizDutyRoster::getStatus, 1)
                // 只认生效且已发布/已变更：草稿/待审核/调班中的值守不对外
                .in(BizDutyRoster::getPublishStatus,
                        PublishStatusEnum.PUBLISHED.getCode(), PublishStatusEnum.CHANGED.getCode())
                .in(BizDutyRoster::getPostId, postIds)
                .orderByAsc(BizDutyRoster::getRoleType)
                .orderByAsc(BizDutyRoster::getId));
        return toRosterVOs(rows);
    }

    // 排班维护
    public PageResult<DutyRosterVO> listPage(DutyRosterPageQueryDTO q) {
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
    public DutyRosterVO upsert(DutyRosterUpsertDTO dto) {
        DutyShiftTypeEnum shiftType = DutyShiftTypeEnum.fromCode(dto.getShiftType());
        DutyRoleTypeEnum roleType = DutyRoleTypeEnum.fromCode(dto.getRoleType());
        EmployeeBriefVO emp = requireOnDutyEmployee(dto.getEmployeeId());
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
        // 发布态归属：新增落草稿（要提交+审核发布才生效）；改单时原本生效的直接转已变更
        Integer oldPublish = insert ? null : row.getPublishStatus();
        Integer newPublish = nextPublishStatus(insert, oldPublish);
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
        row.setPublishStatus(newPublish);
        row.setRemark(dto.getRemark());
        row.setStaffScheduleId(enabled && shift != null
                ? dutyAttendance(dto.getDutyDate(), emp, shift, post, dto.getRemark(), newPublish).getId()
                : null);

        saveRow(row, insert);
        if (boundFactId != null && !Objects.equals(boundFactId, row.getStaffScheduleId())) {
            releaseFactIfUnused(boundFactId);
        }
        log.info("值班排班{} date={} post={} shift={} role={} emp={}", insert ? "登记" : "修改",
                row.getDutyDate(), post == null ? "-" : post.getPostName(),
                row.getShiftType(), row.getRoleType(), row.getEmployeeName());
        return toRosterVO(row, post);
    }

    /**
     * 临时换班：写 substitute_*，不动原值班人。
     */
    @Transactional(rollbackFor = Exception.class)
    public void substitute(DutySubstituteDTO dto) {
        BizDutyRoster row = requireRow(dto.getId());
        EmployeeBriefVO emp = requireOnDutyEmployee(dto.getSubstituteEmpId());
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
        EmployeeBriefVO origin = systemFacadeService.findEmployee(row.getEmployeeId());
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

    // ===== G-15 发布态流转（提交→审核→发布 / 调班 / 撤回）=====

    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        BizDutyRoster row = requireRow(id);
        if (!PublishStatusEnum.DRAFT.getCode().equals(row.getPublishStatus())) {
            throw new BusinessException("只有草稿状态的值守排班可以提交审核");
        }
        updatePublishStatus(id, PublishStatusEnum.PENDING.getCode(), row);
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditPublish(Long id) {
        BizDutyRoster row = requireRow(id);
        if (!PublishStatusEnum.PENDING.getCode().equals(row.getPublishStatus())) {
            throw new BusinessException("只有待审核状态的值守排班可以审核发布");
        }
        updatePublishStatus(id, PublishStatusEnum.PUBLISHED.getCode(), row);
    }

    @Transactional(rollbackFor = Exception.class)
    public void recall(Long id) {
        // 任意态可撤回回草稿：撤回后「此刻谁负责」解析不到它（展示只认 publish_status IN (2,4)）
        updatePublishStatus(id, PublishStatusEnum.DRAFT.getCode(), requireRow(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void adjustStart(Long id) {
        BizDutyRoster row = requireRow(id);
        if (!PublishStatusEnum.isEffective(row.getPublishStatus())) {
            throw new BusinessException("只有已发布/已变更的值守排班可以进入调班");
        }
        updatePublishStatus(id, PublishStatusEnum.ADJUSTING.getCode(), row);
    }

    /**
     * 发布态迁移：排班行与它绑定的在岗事实一起动 ——
     * 只改一半会出现「值守已发布、这个人却不计在岗」（或反之）。
     */
    private void updatePublishStatus(Long id, Integer publishStatus, BizDutyRoster row) {
        bizDutyRosterMapper.update(null, new LambdaUpdateWrapper<BizDutyRoster>()
                .eq(BizDutyRoster::getId, id)
                .set(BizDutyRoster::getPublishStatus, publishStatus));
        if (row.getStaffScheduleId() != null) {
            BizSchedule fact = new BizSchedule();
            fact.setId(row.getStaffScheduleId());
            fact.setPublishStatus(publishStatus);
            staffScheduleService.updateById(fact);
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
                .set(BizDutyRoster::getPublishStatus, row.getPublishStatus())
                .set(BizDutyRoster::getRemark, row.getRemark()));
    }

    /**
     * 登记这条值班的在岗事实 —— 值班不是"写在册子上"，它就是这个人的一段真实出勤。
     */
    private BizSchedule dutyAttendance(LocalDate date, EmployeeBriefVO emp, BizShift shift,
                                       BizDutyPost post, String remark, Integer publishStatus) {
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
        // 在岗事实跟随值守行的发布态：未发布的值守不该算进在岗
        core.setPublishStatus(publishStatus);
        return staffScheduleService.ensureAttendance(core, StaffScheduleSourceEnum.MANUAL);
    }

    /**
     * 保存/改单时的发布态归属：null=新增落草稿；原本生效的（含调班中）转已变更；草稿/待审核保持原态。
     */
    private Integer nextPublishStatus(boolean insert, Integer oldPublish) {
        if (insert) {
            return PublishStatusEnum.DRAFT.getCode();
        }
        if (oldPublish == null) {
            // 存量行（加列前就有）视为已发布：别把历史值守一夜之间打成草稿
            return PublishStatusEnum.PUBLISHED.getCode();
        }
        if (PublishStatusEnum.ADJUSTING.getCode().equals(oldPublish) || PublishStatusEnum.isEffective(oldPublish)) {
            return PublishStatusEnum.CHANGED.getCode();
        }
        return oldPublish;
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
    private void assertPostStaffType(BizDutyPost post, EmployeeBriefVO emp) {
        Integer required = post.getRequiredStaffType();
        if (required == null || required == 0) {
            return;
        }
        if (!Objects.equals(required, emp.getEmpType())) {
            throw new BusinessException("点位「" + post.getPostName() + "」要求"
                    + StaffTypeEnum.getLabel(required) + "岗，而「" + emp.getEmpName() + "」是"
                    + StaffTypeEnum.getLabel(emp.getEmpType()) + "岗，排上去这个位没人能顶");
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
                // 只有已发布/已变更的值守才算数：草稿/待审核/调班中不得对外宣示「此刻谁负责」
                .in(BizDutyRoster::getPublishStatus,
                        PublishStatusEnum.PUBLISHED.getCode(), PublishStatusEnum.CHANGED.getCode())
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
        vo.setRoleTypeText(DutyRoleTypeEnum.getLabel(r.getRoleType()));
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
        vo.setDutyScopeText(post == null ? null : DutyScopeEnum.getLabel(post.getDutyScope()));
        vo.setDutyLevel(post == null ? null : post.getDutyLevel());
        vo.setDutyLevelText(post == null ? null : DutyLevelEnum.getLabel(post.getDutyLevel()));
        vo.setAttendMode(post == null ? null : post.getAttendMode());
        vo.setAttendModeText(post == null ? null : AttendModeEnum.getLabel(post.getAttendMode()));
        vo.setOrgType(post == null ? null : post.getOrgType());
        vo.setOrgId(post == null ? null : post.getOrgId());
        vo.setShiftType(r.getShiftType());
        vo.setShiftTypeText(shiftText(r.getShiftType()));
        vo.setRoleType(r.getRoleType());
        vo.setRoleTypeText(DutyRoleTypeEnum.getLabel(r.getRoleType()));
        vo.setEmployeeId(r.getEmployeeId());
        vo.setEmployeeName(r.getEmployeeName());
        vo.setDeptId(r.getDeptId());
        vo.setDeptName(r.getDeptName());
        vo.setPhone(r.getPhone());
        vo.setStartTime(r.getStartTime());
        vo.setEndTime(r.getEndTime());
        vo.setStatus(r.getStatus());
        vo.setPublishStatus(r.getPublishStatus());
        vo.setPublishStatusText(PublishStatusEnum.getLabel(r.getPublishStatus()));
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
        EmployeeBriefVO emp = systemFacadeService.findEmployee(empId);
        return emp == null ? null : emp.getPhone();
    }

    private EmployeeBriefVO requireOnDutyEmployee(Long empId) {
        EmployeeBriefVO emp = systemFacadeService.findEmployee(empId);
        if (emp == null) {
            throw new BusinessException("值班人不存在");
        }
        if (!Objects.equals(EnableStatusEnum.ENABLED.getCode(), emp.getStatus())) {
            throw new BusinessException("值班人「" + emp.getEmpName() + "」不在职（离职/停职员工不能排总值班）");
        }
        return emp;
    }

    private String resolveDeptName(EmployeeBriefVO emp) {
        if (TextUtil.hasText(emp.getDeptName())) {
            return emp.getDeptName();
        }
        if (emp.getDeptId() == null) {
            return null;
        }
        DeptBriefVO dept = systemFacadeService.findDepartment(emp.getDeptId());
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

    // ==================== 值班增强：月度工时 / 区间规则校验 ====================

    @Override
    public DutyWorkloadVO monthWorkload(DutyWorkloadDTO dto) {
        YearMonth ym = parseMonth(dto.getMonth());
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        List<DutyWorkloadVO.Row> rows = baseMapper.selectDutyWorkload(dto.getPostId(), start, end);
        String postName = null;
        if (dto.getPostId() != null) {
            BizDutyPost post = bizDutyPostMapper.selectById(dto.getPostId());
            postName = post == null ? null : post.getPostName();
        }
        int totalDuties = 0;
        BigDecimal totalHours = BigDecimal.ZERO;
        for (DutyWorkloadVO.Row r : rows) {
            totalDuties += nvl(r.getDayCount()) + nvl(r.getNightCount());
            totalHours = totalHours.add(r.getTotalHours() == null ? BigDecimal.ZERO : r.getTotalHours());
        }
        DutyWorkloadVO result = new DutyWorkloadVO();
        result.setMonth(ym.toString());
        result.setPostId(dto.getPostId());
        result.setPostName(postName);
        result.setDutyCount(rows.size());
        result.setTotalDuties(totalDuties);
        result.setTotalHours(totalHours.setScale(1, RoundingMode.HALF_UP));
        result.setRows(rows);
        return result;
    }

    @Override
    public DutyCheckVO check(DutyCheckDTO dto) {
        LocalDate start = dto.getStartDate();
        LocalDate end = dto.getEndDate();
        if (end.isBefore(start)) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        List<DutyCheckRow> rows = baseMapper.selectDutyRows(dto.getPostId(), start, end);
        List<DutyCheckVO.Warning> warnings = new ArrayList<>();
        // 连续值班：同一实际值班人相邻自然日都有班（含白班接夜班）
        Map<Long, List<DutyCheckRow>> byPerson = rows.stream()
                .collect(Collectors.groupingBy(DutyCheckRow::getEmployeeId, LinkedHashMap::new, Collectors.toList()));
        for (Map.Entry<Long, List<DutyCheckRow>> entry : byPerson.entrySet()) {
            List<DutyCheckRow> personRows = entry.getValue();
            TreeSet<LocalDate> days = personRows.stream()
                    .map(DutyCheckRow::getDutyDate).collect(Collectors.toCollection(TreeSet::new));
            LocalDate prev = null;
            for (LocalDate d : days) {
                if (prev != null && d.equals(prev.plusDays(1))) {
                    DutyCheckRow row = personRows.stream().filter(r -> d.equals(r.getDutyDate()))
                            .findFirst().orElse(personRows.get(0));
                    warnings.add(dutyWarn(1, "DUTY_STREAK", "连续值班", row.getDutyDate(), row.getShiftType(),
                            row.getPostId(), row.getPostName(), row.getEmployeeId(), row.getEmployeeName(),
                            null, null,
                            row.getEmployeeName() + " 于 " + prev + " 与 " + d + " 连续两天值班"));
                }
                prev = d;
            }
            // 值班频次：默认月上限 10 天，按区间天数折算（区间越长越接近全月口径）
            long daysInRange = ChronoUnit.DAYS.between(start, end) + 1;
            int maxDuty = Math.max(3, (int) Math.round(10.0 * daysInRange / 30.0));
            if (days.size() > maxDuty) {
                DutyCheckRow row = personRows.get(0);
                warnings.add(dutyWarn(1, "DUTY_FREQ", "值班频次超限", null, null,
                        row.getPostId(), row.getPostName(), row.getEmployeeId(), row.getEmployeeName(),
                        days.size(), maxDuty,
                        row.getEmployeeName() + " 区间内值班 " + days.size() + " 天（折算月上限 " + maxDuty + " 天）"));
            }
        }
        // 主班漏排：点位×日×班次 主班空缺但有副班 → 提示（副班顶值符合业务，不阻断）
        Set<String> shiftKeys = new HashSet<>();
        Set<String> primaryKeys = new HashSet<>();
        for (DutyCheckRow row : rows) {
            String key = keyOf(row.getPostId(), row.getDutyDate(), row.getShiftType());
            shiftKeys.add(key);
            if (Objects.equals(DutyRoleTypeEnum.PRIMARY.getCode(), row.getRoleType())) {
                primaryKeys.add(key);
            }
        }
        for (DutyCheckRow row : rows) {
            String key = keyOf(row.getPostId(), row.getDutyDate(), row.getShiftType());
            if (!primaryKeys.contains(key) && Objects.equals(DutyRoleTypeEnum.SECONDARY.getCode(), row.getRoleType())) {
                warnings.add(dutyWarn(2, "MAIN_GAP", "主班漏排", row.getDutyDate(), row.getShiftType(),
                        row.getPostId(), row.getPostName(), row.getEmployeeId(), row.getEmployeeName(),
                        null, null,
                        row.getPostName() + " " + row.getDutyDate() + " " + shiftText(row.getShiftType())
                                + " 主班空缺，由 " + row.getEmployeeName() + " 副班顶值"));
            }
        }
        DutyCheckVO vo = new DutyCheckVO();
        vo.setStartDate(start.toString());
        vo.setEndDate(end.toString());
        vo.setPostId(dto.getPostId());
        int alertCount = 0;
        int hintCount = 0;
        for (DutyCheckVO.Warning w : warnings) {
            if (w.getLevel() != null && w.getLevel() == 1) {
                alertCount++;
            } else if (w.getLevel() != null && w.getLevel() == 2) {
                hintCount++;
            }
        }
        vo.setAlertCount(alertCount);
        vo.setHintCount(hintCount);
        vo.setWarnings(warnings);
        return vo;
    }

    private DutyCheckVO.Warning dutyWarn(int level, String type, String typeText, LocalDate date,
                                         Integer shiftType, Long postId, String postName,
                                         Long employeeId, String employeeName,
                                         Integer actual, Integer required, String message) {
        DutyCheckVO.Warning w = new DutyCheckVO.Warning();
        w.setLevel(level);
        w.setType(type);
        w.setTypeText(typeText);
        w.setDutyDate(date == null ? null : date.toString());
        w.setShiftType(shiftType);
        w.setShiftTypeText(shiftType == null ? null : shiftText(shiftType));
        w.setPostId(postId);
        w.setPostName(postName);
        w.setEmployeeId(employeeId);
        w.setEmployeeName(employeeName);
        w.setActual(actual);
        w.setRequired(required);
        w.setMessage(message);
        return w;
    }

    private String keyOf(Long postId, LocalDate date, Integer shiftType) {
        return postId + ":" + date + ":" + shiftType;
    }

    private YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (Exception e) {
            throw new BusinessException("月份格式应为 yyyy-MM，例如 2026-10");
        }
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }
}
