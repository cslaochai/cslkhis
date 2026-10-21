package com.his.inpatient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.inpatient.dto.*;
import com.his.inpatient.service.BedCenterService;
import com.his.inpatient.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 床位服务中心（等床队列 / 全院床位调配）
 */
@Tag(name = "床位服务中心")
@RestController
@RequestMapping("/patient/bedCenter")
@RequiredArgsConstructor
public class BedCenterController {

    private final BedCenterService bedCenterService;

    /**
     * 等床队列分页，排序由服务端钉死为优先级降序、同级按登记先后。
     * 用于床位管理页「等床队列」页签的查询卡与表格。
     */
    @Operation(summary = "等床队列分页（危重优先，同级按登记先后）")
    @GetMapping("/queue/listPage")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<IPage<BedWaitVO>> queueListPage(@Valid BedWaitPageQueryDTO query) {
        return Result.success(bedCenterService.queuePage(query));
    }

    /**
     * 取一条排队记录的完整信息（含已安排床位快照与来源住院证）。
     * 用于等床队列页签的行详情弹框。
     */
    @Operation(summary = "等床队列详情")
    @GetMapping("/queue/getDetailById")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<BedWaitVO> queueDetail(@RequestParam Long waitId) {
        return Result.success(bedCenterService.queueDetail(waitId));
    }

    /**
     * 登记新的等床排队或修改已有排队的需求与优先级。
     * 用于等床队列页签「登记/修改排队」弹框保存。
     */
    @Operation(summary = "登记/修改床位排队")
    @PostMapping("/queue/upsert")
    @PreAuthorize("hasAuthority('ipd:bedCenter:add')")
    public Result<String> upsertWait(@RequestBody @Valid BedWaitUpsertDTO dto) {
        return Result.success("排队登记成功", String.valueOf(bedCenterService.upsertWait(dto)));
    }

    /**
     * 把排队患者落到指定床位并即刻锁定该床，支持跨科调配。
     * 用于等床队列页签「安排床位」选床后的提交。
     */
    @Operation(summary = "安排床位（含跨科调配，床位即刻锁定）")
    @PostMapping("/queue/assignBed")
    @PreAuthorize("hasAuthority('ipd:bedCenter:assign')")
    public Result<Void> assignBed(@RequestBody @Valid BedAssignUpsertDTO dto) {
        bedCenterService.assignBed(dto);
        return Result.success("床位已预留：该床已锁定并挂上患者，等待办理入院", null);
    }

    /**
     * 把已预留床位退回等床队列并释放锁定的床位。
     * 用于等床队列页签上「已安排床位但尚未收治」记录的退回动作。
     */
    @Operation(summary = "退回队列（释放已锁定的床位）")
    @PostMapping("/queue/releaseBed")
    @PreAuthorize("hasAuthority('ipd:bedCenter:release')")
    public Result<Void> releaseBed(@RequestBody @Valid BedWaitOperateDTO dto) {
        bedCenterService.releaseBed(dto);
        return Result.success("已退回队列：床位已释放", null);
    }

    /**
     * 撤销患者的等床排队，已预留床位的同步释放。
     * 用于等床队列页签的取消排队动作（患者暂不住院、转院等）。
     */
    @Operation(summary = "取消排队（已安排床位的一并释放）")
    @PostMapping("/queue/cancel")
    @PreAuthorize("hasAuthority('ipd:bedCenter:cancel')")
    public Result<Void> cancelWait(@RequestBody @Valid BedWaitOperateDTO dto) {
        bedCenterService.cancelWait(dto);
        return Result.success("排队已取消", null);
    }

    /**
     * 以已安排好的床位为凭办理入院登记，床位由锁定转为占用。
     * 用于床位管理页「办理入院」弹框提交。
     */
    @Operation(summary = "按已安排床位办理入院登记")
    @PostMapping("/queue/admit")
    @PreAuthorize("hasAuthority('ipd:bedCenter:admit')")
    public Result<String> admit(@RequestBody @Valid BedWaitAdmitDTO dto) {
        return Result.success("入院登记成功：床位已转为占用", bedCenterService.admit(dto));
    }

    /**
     * 汇总等待分布、平均等待时长与床位现状。
     * 用于床位管理页顶部的队列概览区。
     */
    @Operation(summary = "队列概览（等待分布/平均等待/床位现状）")
    @GetMapping("/queue/stats")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<BedWaitStatsVO> queueStats() {
        return Result.success(bedCenterService.queueStats());
    }

    /**
     * 取当前等待中的人数。
     * 用于床位管理页队列入口的等待人数角标提醒。
     */
    @Operation(summary = "等待中人数（角标）")
    @GetMapping("/queue/countWaiting")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<String> countWaiting() {
        return Result.success(String.valueOf(bedCenterService.countWaiting()));
    }

    /**
     * 为一条排队记录算出可用床位候选及其匹配档位，只给候选不代替现场决策。
     * 用于「安排床位」弹窗的智能匹配模式列出可选床。
     */
    @Operation(summary = "床位智能匹配（给候选不给最优解，决定由现场做）")
    @GetMapping("/pool/match")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<List<BedMatchVO>> matchBeds(@RequestParam Long waitId) {
        return Result.success(bedCenterService.matchBeds(waitId));
    }

    /**
     * 分页查全院床位及其占用者与预留去向。
     * 用于床位管理页「全院床位」页签的查询表格，也用于安排床位时按空闲过滤选床。
     */
    @Operation(summary = "全院床位池（含占用者与预留去向）")
    @GetMapping("/pool/listPage")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<BedPoolVO> bedPool(@Valid BedPoolPageQueryDTO query) {
        return Result.success(bedCenterService.bedPool(query));
    }

    /**
     * 床位调配图：与护士站 {@code /patient/inpatient/bedMap} 同一张图、两种视角 ——
     * 护士看"床上躺着谁、几级护理"，这里看"这张床能不能用、被谁预定了、能不能直接放人"。
     * 所以走的是床位中心的读权限，不复用护士站的 {@code ipd:nurse:list}。
     */
    @Operation(summary = "床位调配图（一床一卡，含预留去向与可用动作）")
    @GetMapping("/pool/bedMap")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<BedMapVO> bedMap(@Valid BedMapQueryDTO query) {
        return Result.success(bedCenterService.bedMap(query));
    }

    /**
     * 汇总全院床位数量、使用率并按科室排行。
     * 用于「全院床位」页签顶部的床位总数/空闲/占用/锁定/维修汇总卡。
     */
    @Operation(summary = "全院床位总览与科室排行")
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<BedOverviewVO> overview() {
        return Result.success(bedCenterService.overview());
    }

    /**
     * 等床超时催总值班的手工补跑（定时任务 {@code BedWaitDutyEscalateTrigger} 每小时一轮）。
     * 挂 {@code ipd:bedCenter:assign}：只有管床位调配的人该主动去"喊总值班"，
     * 只读的护士站不该有这个按钮。
     */
    @Operation(summary = "等床超时催总值班（手工补跑，返回本轮发出的待办条数）")
    @PostMapping("/escalateWaitToDuty")
    @PreAuthorize("hasAuthority('ipd:bedCenter:assign')")
    public Result<String> escalateWaitToDuty() {
        return Result.success("已向当日总值班催办", String.valueOf(bedCenterService.escalateWaitToDuty()));
    }
}
