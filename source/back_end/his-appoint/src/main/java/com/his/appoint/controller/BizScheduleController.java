package com.his.appoint.controller;


import com.his.appoint.dto.*;
import com.his.appoint.service.BizScheduleService;
import com.his.appoint.service.BizScheduleSlotService;
import com.his.appoint.vo.*;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "排班管理")
@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:appointments:list', 'opd:emergency:list', 'org:schedule:list')")
public class BizScheduleController {

    private final BizScheduleService bizScheduleService;
    private final BizScheduleSlotService bizScheduleSlotService;

    @PreAuthorize("hasAuthority('org:schedule:add')")
    @Operation(summary = "新增或修改排班（新增/修改合一）")
    @PostMapping("/scheduleUpsert")
    public Result<Void> scheduleUpsert(@Valid @RequestBody ScheduleUpsertDTO upsertDTO) {
        bizScheduleService.scheduleUpsert(upsertDTO);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('org:schedule:delete')")
    @Operation(summary = "删除排班")
    @DeleteMapping("/deleteById")
    public Result<Void> removeSchedule(@RequestParam Long scheduleId) {
        boolean success = bizScheduleService.deleteSchedule(scheduleId);
        return success ? Result.success() : Result.error("删除失败");
    }

    @Operation(summary = "查询排班列表")
    @PostMapping("/list")
    public Result<List<ScheduleDetailVO>> scheduleList(@Valid @RequestBody ScheduleQueryDTO queryDTO) {
        return Result.success(bizScheduleService.listDetail(queryDTO));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询可挂号源（下拉取数，只要求登录）")
    @PostMapping("/selectList")
    public Result<List<ScheduleSelectListVO>> scheduleSelectList(@Valid @RequestBody ScheduleSelectQueryDTO scheduleQueryDTO) {
        return Result.success(bizScheduleService.selectListVO(scheduleQueryDTO));
    }

    @Operation(summary = "查询今日本科室排班")
    @GetMapping("/today")
    public Result<List<ScheduleDetailVO>> todaySchedule() {
        return Result.success(bizScheduleService.todayScheduleOfCurrentDept());
    }

    @PreAuthorize("hasAuthority('org:schedule:edit')")
    @Operation(summary = "更新就诊状态")
    @PostMapping("/updateConsultStatus")
    public Result<Void> updateConsultStatus(@Valid @RequestBody ScheduleConsultStatusUpsertDTO dto) {
        boolean success = bizScheduleService.updateConsultStatus(dto.getScheduleId(), dto.getConsultStatus());
        return success ? Result.success() : Result.error("更新失败");
    }

    @PreAuthorize("hasAuthority('org:schedule:edit')")
    @Operation(summary = "停诊/启用（只更新状态列）")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@Valid @RequestBody ScheduleStatusUpsertDTO dto) {
        boolean success = bizScheduleService.updateStatus(dto.getScheduleId(), dto.getStatus());
        return success ? Result.success() : Result.error("更新失败");
    }

    @Operation(summary = "停诊影响名单（该班次在挂患者）")
    @GetMapping("/stopImpact")
    public Result<List<StopImpactItemVO>> stopImpact(@RequestParam Long scheduleId) {
        return Result.success(bizScheduleService.stopImpact(scheduleId));
    }

    @PreAuthorize("hasAuthority('org:schedule:delete')")
    @Operation(summary = "停诊批量退号")
    @PostMapping("/batchCancel")
    public Result<String> batchCancel(@Valid @RequestBody ScheduleBatchCancelDTO dto) {
        return Result.success(bizScheduleService.batchCancel(dto.getRegistIds(), dto.getReason()), null);
    }

    /**
     * 今日在岗 —— 排班的下游出口。
     *
     * <p>只要求登录：这是院内公共信息（「今天这个科室谁在」谁都需要知道），
     * 跟总值班 {@code /system/dutyRoster/current} 一个口径，排班维护才要 org:schedule:list。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "今日在岗（跨岗位；可按指定时刻判定，支持跨零点夜班）")
    @PostMapping("/onDuty")
    public Result<List<OnDutyStaffVO>> onDuty(@Valid @RequestBody OnDutyQueryDTO queryDTO) {
        return Result.success(bizScheduleService.onDuty(queryDTO));
    }

    @Operation(summary = "查询排班时间片段（半小时一档，挂号选段用）")
    @GetMapping("/slotList")
    public Result<List<ScheduleSlotVO>> slotList(@RequestParam Long scheduleId) {
        return Result.success(bizScheduleSlotService.listVOByScheduleId(scheduleId));
    }

    @Operation(summary = "批量查询排班时间片段（日视图看板「医生 × 半小时段」一次拉全）")
    @PostMapping("/slotListBatch")
    public Result<List<ScheduleSlotVO>> slotListBatch(@Valid @RequestBody ScheduleSlotBatchQueryDTO dto) {
        return Result.success(bizScheduleSlotService.listVOByScheduleIds(dto.getScheduleIds()));
    }

    @PreAuthorize("hasAuthority('org:schedule:add')")
    @Operation(summary = "加号（号源总数/剩余同步增加并留痕）")
    @PostMapping("/addSource")
    public Result<Void> addSource(@Valid @RequestBody ScheduleAddSourceUpsertDTO dto) {
        boolean success = bizScheduleService.addSource(dto.getScheduleId(), dto.getAddNum(), dto.getReason());
        return success ? Result.success() : Result.error("加号失败");
    }

    @PreAuthorize("hasAuthority('org:schedule:add')")
    @Operation(summary = "段级号源编辑（每段号源/预约池/停用状态，Σ段写回主表并留痕）")
    @PostMapping("/slotUpsert")
    public Result<Void> slotUpsert(@Valid @RequestBody ScheduleSlotUpsertDTO dto) {
        bizScheduleSlotService.updateSlotSources(dto);
        return Result.success();
    }

}
