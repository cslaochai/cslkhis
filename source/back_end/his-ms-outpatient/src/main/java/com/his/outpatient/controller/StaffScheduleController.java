package com.his.outpatient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.outpatient.dto.StaffScheduleChangePageQueryDTO;
import com.his.outpatient.dto.StaffScheduleCheckDTO;
import com.his.outpatient.dto.StaffScheduleCopyDTO;
import com.his.outpatient.dto.StaffSchedulePageQueryDTO;
import com.his.outpatient.dto.StaffScheduleRejectDTO;
import com.his.outpatient.dto.StaffSchedulePendingQueryDTO;
import com.his.outpatient.dto.StaffScheduleSwapDTO;
import com.his.outpatient.dto.StaffScheduleUpsertDTO;
import com.his.outpatient.dto.StaffScheduleWeekActionDTO;
import com.his.outpatient.dto.StaffScheduleWorkloadDTO;
import com.his.outpatient.service.ScheduleChangeLogService;
import com.his.outpatient.service.StaffScheduleService;
import com.his.outpatient.vo.ScheduleChangeLogVO;
import com.his.outpatient.vo.StaffCheckVO;
import com.his.outpatient.vo.StaffScheduleVO;
import com.his.outpatient.vo.ErFixedRatioVO;
import com.his.outpatient.vo.StaffWorkloadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 全院岗位排班（谁 · 哪天 · 在哪个单元 · 什么班 · 出不出勤）。
 */
@Tag(name = "全院岗位排班")
@RestController
@RequestMapping("/system/staffSchedule")
@RequiredArgsConstructor
public class StaffScheduleController {

    private final StaffScheduleService staffScheduleService;
    private final ScheduleChangeLogService scheduleChangeLogService;

    /**
     * 按日期区间、单元、岗位类别、出勤状态与姓名/工号关键词分页查排班行。
     * 用于「全院岗位排班」页查询卡下方的排班表格。
     */
    @Operation(summary = "分页查询排班（日期区间 + 单元 + 岗位类别 + 出勤状态 + 姓名/工号关键词）")
    @PreAuthorize("hasAuthority('staff:schedule:list')")
    @PostMapping("/listPage")
    public Result<PageResult<StaffScheduleVO>> listPage(@Valid @RequestBody StaffSchedulePageQueryDTO queryDTO) {
        return Result.success(staffScheduleService.pageVO(queryDTO));
    }

    /**
     * 登记或修改一条排班，上下班时间与工时由所选班次带出。
     * 用于该页「登记排班」弹框保存；超出单元人力上限时照样保存，只把提示语带回给操作者。
     */
    @Operation(summary = "新增/修改排班（时间与工时由班次带出；超出人力上限只提示不拦）")
    @PreAuthorize("hasAuthority('staff:schedule:add')")
    @PostMapping("/staffScheduleUpsert")
    public Result<Void> staffScheduleUpsert(@Valid @RequestBody StaffScheduleUpsertDTO upsertDTO) {
        String warning = staffScheduleService.upsert(upsertDTO);
        return warning == null ? Result.success("排班成功", null) : Result.success(warning, null);
    }

    /**
     * 按主键删除一条排班。
     * 用于该页表格行的删除动作；删完低于该单元最低在岗人数会当场拦下并回滚。
     */
    @Operation(summary = "删除排班（物理删；删后低于最低在岗会当场拦下并回滚）")
    @PreAuthorize("hasAuthority('staff:schedule:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        staffScheduleService.deleteById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 换班（两条同天排班互换）或代班（单向换人承接），双方都要重过时间与岗位校验。
     * 用于该页「换班」弹框；原排班人的变动会留痕，供留痕抽屉回看。
     */
    @Operation(summary = "换班（两条同天排班互换）/ 代班（单向换人承接），双方都要重过时间与岗位校验")
    @PreAuthorize("hasAuthority('staff:schedule:edit')")
    @PostMapping("/swap")
    public Result<Void> swap(@Valid @RequestBody StaffScheduleSwapDTO dto) {
        staffScheduleService.swap(dto);
        return Result.success("换班成功，原排班人已留痕", null);
    }

    /**
     * 按星期把已排好的整周模式复制到目标周，返回补上的行数，已排过的人跳过。
     * 用于该页「整周复制」弹框，避免新的一周逐条重登同样的班。
     */
    @Operation(summary = "按星期整周复制排班（返回补上的行数，已排过的人自动跳过）")
    @PreAuthorize("hasAuthority('staff:schedule:add')")
    @PostMapping("/copyRange")
    public Result<Integer> copyRange(@Valid @RequestBody StaffScheduleCopyDTO dto) {
        int copied = staffScheduleService.copyRange(dto);
        return Result.success("已复制 " + copied + " 条排班", copied);
    }

    /**
     * 取指定时刻在岗的人员名单，跨零点班归到开始日，所以按昨天加今天两天解析。
     * 用于该页顶部「此刻在岗」卡片——回答「现在打电话给谁」；任何登录岗位都要看，不挂排班管理权限。
     */
    @Operation(summary = "此刻在岗名单（跨零点班归开始日，按昨天+今天两天解析）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/onDuty")
    public Result<List<StaffScheduleVO>> onDuty(@RequestParam(required = false) Integer orgType,
                                                @RequestParam(required = false) Long orgId,
                                                @RequestParam(required = false) Integer staffType) {
        return Result.success(staffScheduleService.onDutyAt(LocalDateTime.now(), orgType, orgId, staffType));
    }

    /**
     * 急诊值班公开视图：按日期返回急诊内科/外科/儿科三科已发布的医生值班表。
     * 核心制度要求值班表全院公开 —— 任何登录职工可查，不挂排班管理权限。
     */
    @Operation(summary = "急诊值班公开视图（急诊三科当天已发布医生值班表，全院可查）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/erDutyPublic")
    public Result<List<StaffScheduleVO>> erDutyPublic(@RequestParam(required = false) LocalDate date) {
        return Result.success(staffScheduleService.erDutyPublic(date == null ? LocalDate.now() : date));
    }

    /**
     * 急诊固定医师占比（评审口径：固定急诊医师 ≥ 在岗 75%，卫医政发〔2009〕50号）。
     * 按急诊内科/外科/儿科返回：当日发布在岗医师数、其中固定医师数、占比。
     */
    @Operation(summary = "急诊固定医师占比（评审口径：固定≥在岗75%）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/erFixedRatio")
    public Result<List<ErFixedRatioVO>> erFixedRatio(@RequestParam(required = false) LocalDate date) {
        return Result.success(staffScheduleService.erFixedRatio(date == null ? LocalDate.now() : date));
    }

    /**
     * 取一条排班的历次变更留痕（换班/代班/停班/加减号），最新在前。
     * 用于该页行内「留痕」抽屉，追查这条班是谁在什么时候改成的。
     */
    @Operation(summary = "某条排班的变更留痕（换班/代班/停班/加减号，最新在前）")
    @PreAuthorize("hasAuthority('staff:schedule:list')")
    @GetMapping("/changeLogList")
    public Result<List<ScheduleChangeLogVO>> changeLogList(@RequestParam Long staffScheduleId) {
        return Result.success(scheduleChangeLogService.listBySchedule(staffScheduleId));
    }

    /**
     * 变更记录全局分页（台账）：跨排班单元回看所有留痕，按发生时间/动作/值班人关键词筛。
     * 用于该页「变更记录」页签。
     */
    @Operation(summary = "变更记录全局分页（台账，跨单元回看）")
    @PreAuthorize("hasAuthority('staff:schedule:list')")
    @PostMapping("/changeLogPage")
    public Result<PageResult<ScheduleChangeLogVO>> changeLogPage(@Valid @RequestBody StaffScheduleChangePageQueryDTO dto) {
        return Result.success(scheduleChangeLogService.pageQuery(dto));
    }

    /**
     * 区间规则校验（全区岗位通用）：连续夜班/连续上班/单周工时/班次冲突/人力缺口与富余。
     * 用于该页「规则校验」页签，只列告警不阻断保存。
     */
    @Operation(summary = "区间规则校验（连续夜班/连续上班/单周工时/班次冲突/人力配置）")
    @PreAuthorize("hasAuthority('staff:schedule:list')")
    @PostMapping("/check")
    public Result<StaffCheckVO> check(@Valid @RequestBody StaffScheduleCheckDTO dto) {
        return Result.success(staffScheduleService.check(dto));
    }

    /**
     * 月度工时统计（全区岗位通用）：一人一行，含整段未排班的人（科室/病区口径补齐）。
     * 用于该页「月度工时」页签。
     */
    @Operation(summary = "月度工时统计（含整段未排班的人）")
    @PreAuthorize("hasAuthority('staff:schedule:list')")
    @PostMapping("/monthWorkload")
    public Result<StaffWorkloadVO> monthWorkload(@Valid @RequestBody StaffScheduleWorkloadDTO dto) {
        return Result.success(staffScheduleService.monthWorkload(dto));
    }

    /**
     * 提交审核：草稿 → 待审核。任何有排班录入权限的人都能提交。
     */
    @Operation(summary = "提交审核（草稿 → 待审核）")
    @PreAuthorize("hasAuthority('staff:schedule:add')")
    @PostMapping("/submit")
    public Result<Void> submit(@RequestParam Long id) {
        staffScheduleService.submit(id);
        return Result.success("已提交审核", null);
    }

    /**
     * 审核发布：待审核 → 已发布。当前登录人须为本科室指定的全院岗位排班审核人（系统管理员豁免）。
     */
    @Operation(summary = "审核发布（待审核 → 已发布，须命中科室指定审核人）")
    @PreAuthorize("hasAuthority('staff:schedule:audit')")
    @PostMapping("/auditPublish")
    public Result<Void> auditPublish(@RequestParam Long id) {
        staffScheduleService.auditPublish(id);
        return Result.success("已审核发布", null);
    }

    /**
     * 驳回：待审核 → 草稿，并写入驳回原因留痕。
     */
    @Operation(summary = "驳回（待审核 → 草稿，附原因）")
    @PreAuthorize("hasAuthority('staff:schedule:audit')")
    @PostMapping("/reject")
    public Result<Void> reject(@Valid @RequestBody StaffScheduleRejectDTO dto) {
        staffScheduleService.reject(dto);
        return Result.success("已驳回", null);
    }

    /**
     * 撤回：任意态 → 草稿（撤回后不计在岗）。
     */
    @Operation(summary = "撤回（任意态 → 草稿）")
    @PreAuthorize("hasAuthority('staff:schedule:edit')")
    @PostMapping("/recall")
    public Result<Void> recall(@RequestParam Long id) {
        staffScheduleService.recall(id);
        return Result.success("已撤回", null);
    }

    /**
     * 进入调班：已发布/已变更 → 调班中。
     */
    @Operation(summary = "进入调班（已发布/已变更 → 调班中）")
    @PreAuthorize("hasAuthority('staff:schedule:edit')")
    @PostMapping("/adjustStart")
    public Result<Void> adjustStart(@RequestParam Long id) {
        staffScheduleService.adjustStart(id);
        return Result.success("已进入调班", null);
    }

    /**
     * 整周提交：本单元整周（周一～周日）草稿 → 待审核。
     */
    @Operation(summary = "整周提交（本单元整周草稿 → 待审核）")
    @PreAuthorize("hasAuthority('staff:schedule:add')")
    @PostMapping("/submitWeek")
    public Result<Integer> submitWeek(@Valid @RequestBody StaffScheduleWeekActionDTO dto) {
        int moved = staffScheduleService.submitWeek(dto);
        return Result.success("已提交 " + moved + " 条排班", moved);
    }

    /**
     * 整周审核发布：本单元整周（周一～周日）待审核 → 已发布，须命中科室指定审核人。
     */
    @Operation(summary = "整周审核发布（本单元整周待审核 → 已发布，须命中科室指定审核人）")
    @PreAuthorize("hasAuthority('staff:schedule:audit')")
    @PostMapping("/auditWeek")
    public Result<Integer> auditWeek(@Valid @RequestBody StaffScheduleWeekActionDTO dto) {
        int moved = staffScheduleService.auditWeek(dto);
        return Result.success("已审核发布 " + moved + " 条排班", moved);
    }

    /**
     * 待我审批（聚合）：当前登录人作为指定审核人的待审核员工排班（纯科室/全院，
     * 不含门诊派生行与护理病区行——前者走门诊审批、后者走护理审批）。
     * 系统管理员看全院的待审；普通审核人只看本科室主档指定的那几个科室。
     */
    @Operation(summary = "待我审批聚合（返回当前登录人作为指定审核人的待审核排班）")
    @PreAuthorize("hasAuthority('staff:schedule:audit')")
    @PostMapping("/pendingApproval")
    public Result<PageResult<StaffScheduleVO>> pendingApproval(@Valid @RequestBody StaffSchedulePendingQueryDTO dto) {
        return Result.success(staffScheduleService.pendingApprovalPage(dto));
    }

    /**
     * 排班单元候选（科室/病区/全院）：页面按单元过滤与整周提交审核时选单元。
     */
    @Operation(summary = "排班单元候选（科室/病区/全院）")
    @PreAuthorize("hasAuthority('staff:schedule:list')")
    @GetMapping("/unitSelectList")
    public Result<List<StaffScheduleVO.UnitOption>> unitSelectList(@RequestParam(required = false) Integer orgType) {
        return Result.success(staffScheduleService.unitSelectList(orgType));
    }
}
