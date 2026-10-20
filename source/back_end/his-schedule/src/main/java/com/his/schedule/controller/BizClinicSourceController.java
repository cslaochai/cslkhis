package com.his.schedule.controller;


import com.his.schedule.dto.ScheduleSelectQueryDTO;
import com.his.schedule.dto.*;
import com.his.schedule.service.BizClinicSourceService;
import com.his.schedule.service.BizClinicSourceSlotService;
import com.his.schedule.service.ClinicSourceLogService;
import com.his.schedule.vo.*;
import com.his.spi.vo.StopImpactItemVO;
import com.his.common.base.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 门诊号源排班控制器：排班维护、号源时段、停诊加号与变更留痕的接口入口。 */
@Tag(name = "门诊号源排班")
@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class BizClinicSourceController {

    private final BizClinicSourceService bizScheduleService;

    private final BizClinicSourceSlotService bizScheduleSlotService;

    private final ClinicSourceLogService clinicSourceLogService;

    /**
     * 新增或修改一条门诊排班（医生×日期×班次）。
     * 用于门诊排班页「新增/修改排班」弹框保存。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:add')")
    @Operation(summary = "新增或修改排班")
    @PostMapping("/scheduleUpsert")
    public Result<Void> scheduleUpsert(@Valid @RequestBody ScheduleUpsertDTO upsertDTO) {
        bizScheduleService.scheduleUpsert(upsertDTO);
        return Result.success();
    }

    /**
     * 提交审核：草稿 → 待审核。
     * 排班人完成排班后提交，进入待审核队列等审核人发布。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:add')")
    @Operation(summary = "提交审核（草稿→待审核）")
    @PostMapping("/submit")
    public Result<Void> submit(@RequestParam Long scheduleId) {
        bizScheduleService.submit(scheduleId);
        return Result.success();
    }

    /**
     * 审核发布：待审核 → 已发布，并生成号源。
     * 审核人审核通过后此排班才正式生效、对外出号。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:audit')")
    @Operation(summary = "审核发布（待审核→已发布，并出号）")
    @PostMapping("/auditPublish")
    public Result<Void> auditPublish(@RequestParam Long scheduleId) {
        bizScheduleService.auditPublish(scheduleId);
        return Result.success();
    }

    /**
     * 撤回：任意态 → 草稿，并清理已生成的号源。
     * 审核前反悔、或发布后需大改时可用。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:edit')")
    @Operation(summary = "撤回（任意态→草稿，撤号）")
    @PostMapping("/recall")
    public Result<Void> recall(@RequestParam Long scheduleId) {
        bizScheduleService.recall(scheduleId);
        return Result.success();
    }

    /**
     * 进入调班：已发布/已变更 → 调班中（锁定编辑，号源暂冻结）。
     * 改完保存后自动转为「已变更」直接生效（不二次审核）。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:edit')")
    @Operation(summary = "进入调班（已发布/已变更→调班中，锁定）")
    @PostMapping("/adjustStart")
    public Result<Void> adjustStart(@RequestParam Long scheduleId) {
        bizScheduleService.adjustStart(scheduleId);
        return Result.success();
    }

    /**
     * 整周提交审核：把查询区间内本科室的草稿班一次提交为待审核，返回提交条数。
     * 已待审核/已发布/调班中行不动，逐条记留痕，单事务。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:add')")
    @Operation(summary = "整周提交审核（区间内草稿→待审核）")
    @PostMapping("/submitWeek")
    public Result<Integer> submitWeek(@Valid @RequestBody ScheduleQueryDTO queryDTO) {
        return Result.success(bizScheduleService.submitWeek(queryDTO));
    }

    /**
     * 整周审核发布：把查询区间内本科室的待审核班一次发布，逐条补号段并同步出勤事实，返回发布条数。
     * 草稿/调班中行不动，单事务。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:audit')")
    @Operation(summary = "整周审核发布（区间内待审核→已发布并出号）")
    @PostMapping("/auditWeek")
    public Result<Integer> auditWeek(@Valid @RequestBody ScheduleQueryDTO queryDTO) {
        return Result.success(bizScheduleService.auditWeek(queryDTO));
    }

    /**
     * 批量排班登记：一次请求内把「人×日×班次」展开逐条落库，失败整体回滚。
     * 用于门诊排班页批量新增排班弹框保存。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:add')")
    @Operation(summary = "批量新增排班（人×日 展开与落库在单事务内完成）")
    @PostMapping("/batchUpsert")
    public Result<BizClinicSourceBatchResultVO> scheduleBatchUpsert(@Valid @RequestBody BizClinicSourceBatchUpsertDTO batchDTO) {
        return Result.success(bizScheduleService.batchUpsert(batchDTO));
    }

    /**
     * 复刻排班：把源区间内的正常排班整体后移若干天生成新排班。
     * 用于门诊排班页「复刻排班」面板，目标区间已存在的班次跳过不覆盖。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:add')")
    @Operation(summary = "复刻排班（把源区间内正常排班整体后移若干天）")
    @PostMapping("/replicate")
    public Result<BizClinicSourceBatchResultVO> scheduleReplicate(@Valid @RequestBody BizClinicSourceReplicateDTO dto) {
        return Result.success(bizScheduleService.replicate(dto));
    }

    /**
     * 删除一条排班。
     * 用于门诊排班页排班行的删除操作。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:delete')")
    @Operation(summary = "删除排班")
    @DeleteMapping("/deleteById")
    public Result<Void> removeSchedule(@RequestParam Long scheduleId) {
        boolean success = bizScheduleService.deleteSchedule(scheduleId);
        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * 按科室、日期、医生等条件查询门诊排班明细。
     * 用于门诊排班页周视图取数与挂号管理页的日历、排班列表。
     */
    @PreAuthorize("hasAnyAuthority('opd:appointments:list', 'opd:emergency:list', 'outpatient:schedule:list')")
    @Operation(summary = "查询排班列表")
    @PostMapping("/list")
    public Result<List<ScheduleDetailVO>> scheduleList(@Valid @RequestBody ScheduleQueryDTO queryDTO) {
        return Result.success(bizScheduleService.listDetail(queryDTO));
    }

    /**
     * 可挂号源下拉：只出当日有余号的排班。
     * 用于挂号管理页新增/修改/改约弹框的班次的号源选择；院内公共参照数据，只要求登录。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询可挂号源（下拉取数，只要求登录）")
    @PostMapping("/selectList")
    public Result<List<ScheduleSelectListVO>> scheduleSelectList(@Valid @RequestBody ScheduleSelectQueryDTO scheduleQueryDTO) {
        return Result.success(bizScheduleService.selectListVO(scheduleQueryDTO));
    }

    /**
     * 查询当前登录用户所在科室今日的排班。
     * 供当日在岗总览环节取数，科室侧查看今天谁出诊时使用。
     */
    @PreAuthorize("hasAnyAuthority('opd:appointments:list', 'opd:emergency:list', 'outpatient:schedule:list')")
    @Operation(summary = "查询今日本科室排班")
    @GetMapping("/today")
    public Result<List<ScheduleDetailVO>> todaySchedule() {
        return Result.success(bizScheduleService.todayScheduleOfCurrentDept());
    }

    /**
     * 回写排班的就诊进度状态。
     * 供医生就诊流转环节回写状态（如标记患者已就诊）时调用。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:edit')")
    @Operation(summary = "更新就诊状态")
    @PostMapping("/updateConsultStatus")
    public Result<Void> updateConsultStatus(@Valid @RequestBody ScheduleConsultStatusUpsertDTO dto) {
        boolean success = bizScheduleService.updateConsultStatus(dto.getScheduleId(), dto.getConsultStatus());
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 将排班置为停诊或启用，只迁移状态、不动班次其余内容。
     * 用于门诊排班页排班行的停诊/启用开关。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:edit')")
    @Operation(summary = "停诊/启用（只更新状态列）")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@Valid @RequestBody ScheduleStatusUpsertDTO dto) {
        boolean success = bizScheduleService.updateStatus(dto.getScheduleId(), dto.getStatus());
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 查询该班次已挂号患者名单，作为停诊的影响范围。
     * 用于门诊排班页执行停诊前的影响确认展示。
     */
    @PreAuthorize("hasAnyAuthority('opd:appointments:list', 'opd:emergency:list', 'outpatient:schedule:list')")
    @Operation(summary = "停诊影响名单（该班次在挂患者）")
    @GetMapping("/stopImpact")
    public Result<List<StopImpactItemVO>> stopImpact(@RequestParam Long scheduleId) {
        return Result.success(bizScheduleService.stopImpact(scheduleId));
    }

    /**
     * 批量退掉停诊班次下的患者挂号。
     * 用于门诊排班页停诊动作中确认影响名单后的批量退号环节。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:delete')")
    @Operation(summary = "停诊批量退号")
    @PostMapping("/batchCancel")
    public Result<String> batchCancel(@Valid @RequestBody ScheduleBatchCancelDTO dto) {
        return Result.success(bizScheduleService.batchCancel(dto.getScheduleId(), dto.getRegistIds(), dto.getReason()), null);
    }

    /**
     * 今日在岗 —— 排班的下游出口。
     *
     * <p>只要求登录：这是院内公共信息（「今天这个科室谁在」谁都需要知道），
     * 跟总值班 {@code /system/dutyRoster/current} 一个口径，排班维护才要 outpatient:schedule:list。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "今日在岗（跨岗位；可按指定时刻判定，支持跨零点夜班）")
    @PostMapping("/onDuty")
    public Result<List<OnDutyStaffVO>> onDuty(@Valid @RequestBody OnDutyQueryDTO queryDTO) {
        return Result.success(bizScheduleService.onDuty(queryDTO));
    }

    /**
     * 查询单条排班的半小时档号源时段。
     * 用于挂号管理页新增挂号弹框的选段，以及排班编辑弹框的时段编辑取数。
     */
    @PreAuthorize("hasAnyAuthority('opd:appointments:list', 'opd:emergency:list', 'outpatient:schedule:list')")
    @Operation(summary = "查询排班时间片段（半小时一档，挂号选段用）")
    @GetMapping("/slotList")
    public Result<List<ScheduleSlotVO>> slotList(@RequestParam Long scheduleId) {
        return Result.success(bizScheduleSlotService.listVOByScheduleId(scheduleId));
    }

    /**
     * 一次批量拉取多条排班的号源时段，输出「医生×半小时段」矩阵行。
     * 用于挂号管理页日视图看板的整屏取数。
     */
    @PreAuthorize("hasAnyAuthority('opd:appointments:list', 'opd:emergency:list', 'outpatient:schedule:list')")
    @Operation(summary = "批量查询排班时间片段（日视图看板「医生 × 半小时段」一次拉全）")
    @PostMapping("/slotListBatch")
    public Result<List<ScheduleSlotVO>> slotListBatch(@Valid @RequestBody ScheduleSlotBatchQueryDTO dto) {
        return Result.success(bizScheduleSlotService.listVOByScheduleIds(dto.getScheduleIds()));
    }

    /**
     * 为班次加号：号源总数与剩余号同步增加并登记变更留痕。
     * 用于门诊排班页排班行的加号弹框保存。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:add')")
    @Operation(summary = "加号（号源总数/剩余同步增加并留痕）")
    @PostMapping("/addSource")
    public Result<Void> addSource(@Valid @RequestBody ScheduleAddSourceUpsertDTO dto) {
        boolean success = bizScheduleService.addSource(dto.getScheduleId(), dto.getAddNum(), dto.getReason());
        return success ? Result.success() : Result.error("加号失败");
    }

    /**
     * 保存班次段级号源配置（每段号源/预约池/停用），各段合计回写排班主行并登记留痕。
     * 用于门诊排班编辑弹框的号源时段编辑表保存。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:add')")
    @Operation(summary = "段级号源编辑（每段号源/预约池/停用状态，Σ段写回主表并留痕）")
    @PostMapping("/slotUpsert")
    public Result<Void> slotUpsert(@Valid @RequestBody ScheduleSlotUpsertDTO dto) {
        bizScheduleSlotService.updateSlotSources(dto);
        return Result.success();
    }

    /**
     * 门诊排班变更留痕分页：谁在何时对哪个班次做了什么、为什么。
     * 用于门诊排班页变更记录页签的审计查询。
     */
    @PreAuthorize("hasAuthority('outpatient:schedule:list')")
    @Operation(summary = "门诊排班变更留痕分页（审计：谁在何时对哪个班次做了什么、为什么）")
    @PostMapping("/logPage")
    public Result<IPage<ClinicSourceLogVO>> logPage(@Valid @RequestBody ClinicSourceLogQueryDTO queryDTO) {
        return Result.success(clinicSourceLogService.pageByQuery(queryDTO));
    }

}
