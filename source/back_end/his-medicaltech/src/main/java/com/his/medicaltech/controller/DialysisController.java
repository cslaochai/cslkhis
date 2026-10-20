package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.DialysisDTO;
import com.his.medicaltech.service.DialysisService;
import com.his.medicaltech.vo.DialysisVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 血液净化（透析）中心：档案→处方→机位排班→上机/下机→不良反应→工作量统计。
 */
@Tag(name = "血液净化（透析）")
@RestController
@RequestMapping("/medicaltech/dialysis")
@RequiredArgsConstructor
public class DialysisController {

    private final DialysisService dialysisService;

    // 透析档案

    @PreAuthorize("hasAuthority('medtech:dialysis:list')")
    @Operation(summary = "透析档案分页（电话出参脱敏）")
    @PostMapping("/archive/listPage")
    public Result<PageResult<DialysisVO.ArchiveVO>> archiveListPage(@Valid @RequestBody DialysisDTO.ArchiveQuery dto) {
        return Result.success(dialysisService.archiveListPage(dto));
    }

    /**
     * 单条透析档案：联系方式取明文。
     * 用于血液净化中心「透析档案」页签的编辑弹框回显——回显成星号会被整对象保存洗成真号。
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:list')")
    @Operation(summary = "档案详情（编辑回显，电话明文）")
    @GetMapping("/archive/getById")
    public Result<DialysisVO.ArchiveVO> archiveGetById(@RequestParam Long id) {
        return Result.success(dialysisService.archiveGetById(id));
    }

    /**
     * 新建或修改透析档案：一人一档，患者姓名等快照由服务端从患者主档重查。
     * 用于血液净化中心「透析档案」页签的新建与编辑弹框。
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:add')")
    @Operation(summary = "档案新增/修改（一人一档，患者快照服务端重查）")
    @PostMapping("/archive/upsert")
    public Result<DialysisVO.ArchiveVO> archiveUpsert(@Valid @RequestBody DialysisDTO.ArchiveUpsert dto) {
        return Result.success("保存成功", dialysisService.archiveUpsert(dto));
    }

    /**
     * 变更透析档案状态（在透/暂停/退出）：暂停与退出必须写明原因。
     * 用于「透析档案」页签行内状态操作，只有「在透」的档案能被排班。
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:add')")
    @Operation(summary = "档案状态变更（在透/暂停/退出，暂停与退出原因必填）")
    @PostMapping("/archive/changeStatus")
    public Result<DialysisVO.ArchiveVO> archiveChangeStatus(@Valid @RequestBody DialysisDTO.ArchiveStatus dto) {
        return Result.success("状态已更新", dialysisService.archiveChangeStatus(dto));
    }

    // 透析处方

    @PreAuthorize("hasAuthority('medtech:dialysis:list')")
    @Operation(summary = "某档案的处方台账（新→旧）")
    @GetMapping("/prescription/list")
    public Result<List<DialysisVO.PrescriptionVO>> prescriptionList(@RequestParam Long archiveId) {
        return Result.success(dialysisService.prescriptionList(archiveId));
    }

    /**
     * 开立或修改透析处方（模式、频率、干体重等）：新开处方自动停用该档案原来的有效处方。
     * 用于「透析档案」页签处方弹框；已退出透析的档案不能再开。
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:add')")
    @Operation(summary = "处方新增/修改（新开自动停用旧的有效处方）")
    @PostMapping("/prescription/upsert")
    public Result<DialysisVO.PrescriptionVO> prescriptionUpsert(@Valid @RequestBody DialysisDTO.PrescriptionUpsert dto) {
        return Result.success("处方已保存", dialysisService.prescriptionUpsert(dto));
    }

    /**
     * 停用一条透析处方：处方退出有效状态，后续排班不再套用。
     * 用于处方弹框台账行的停用操作，已停用的不能重复停。
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:add')")
    @Operation(summary = "处方停用")
    @PostMapping("/prescription/stop")
    public Result<DialysisVO.PrescriptionVO> prescriptionStop(@Valid @RequestBody DialysisDTO.PrescriptionStop dto) {
        return Result.success("处方已停用", dialysisService.prescriptionStop(dto));
    }

    // 机位

    @PreAuthorize("hasAuthority('medtech:dialysis:list')")
    @Operation(summary = "机位台账分页")
    @PostMapping("/machine/listPage")
    public Result<PageResult<DialysisVO.MachineVO>> machineListPage(@Valid @RequestBody DialysisDTO.MachineQuery dto) {
        return Result.success(dialysisService.machineListPage(dto));
    }

    /**
     * 可用机位下拉：只返当前能排的机位，不带故障与停用位。
     * 用于血液净化中心排班弹框与日看板的机位候选，档案列表与看板共用一份。
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:list')")
    @Operation(summary = "可用机位下拉（排班用）")
    @GetMapping("/machine/selectList")
    public Result<List<DialysisVO.MachineVO>> machineSelectList() {
        return Result.success(dialysisService.machineSelectList());
    }

    /**
     * 新增或修改透析机位：机位号不得与已有机位重复。
     * 用于「血液净化机位管理」页的新增与编辑弹框。
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:add')")
    @Operation(summary = "机位新增/修改（机位号唯一）")
    @PostMapping("/machine/upsert")
    public Result<DialysisVO.MachineVO> machineUpsert(@Valid @RequestBody DialysisDTO.MachineUpsert dto) {
        return Result.success("保存成功", dialysisService.machineUpsert(dto));
    }

    // 透析单：排班与执行

    @PreAuthorize("hasAuthority('medtech:dialysis:list')")
    @Operation(summary = "透析单分页台账")
    @PostMapping("/session/listPage")
    public Result<PageResult<DialysisVO.SessionVO>> sessionListPage(@Valid @RequestBody DialysisDTO.SessionQuery dto) {
        return Result.success(dialysisService.sessionListPage(dto));
    }

    /**
     * 单张透析治疗单详情：排班信息与透前透后登记项一次带回。
     * 用于「透析单台账」页签行点击的只读详情弹框。
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:list')")
    @Operation(summary = "透析单详情")
    @GetMapping("/session/getById")
    public Result<DialysisVO.SessionVO> sessionGetById(@RequestParam Long id) {
        return Result.success(dialysisService.sessionGetById(id));
    }

    /**
     * 某天的机位排班看板：三个时段 × 全部机位，空格与已占格一次给出。
     * 用于血液净化中心「机位日看板」页签，点空格子直接排班。
     *
     * @param date 看板日期，留空按当天出
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:list')")
    @Operation(summary = "日看板（三时段 × 全机位，date 缺省为今天）")
    @GetMapping("/session/board")
    public Result<DialysisVO.BoardVO> board(@RequestParam(required = false)
                                            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dialysisService.board(date));
    }

    /**
     * 给在透患者排一张透析治疗单：占定机位与时段，并把当时有效的处方快照进单。
     * 用于「机位日看板」点空格子发起排班；档案已暂停或退出的排不进去。
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:edit')")
    @Operation(summary = "排班（占机位，处方快照）")
    @PostMapping("/session/schedule")
    public Result<DialysisVO.SessionVO> schedule(@Valid @RequestBody DialysisDTO.Schedule dto) {
        return Result.success("已排班", dialysisService.schedule(dto));
    }

    /**
     * 治疗单改期或改机位：只动还没上机的单，机位冲突由服务端拦。
     * 用于「透析单台账」已排班行的改期动作。
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:edit')")
    @Operation(summary = "改期/改机位（仅已排班）")
    @PostMapping("/session/reschedule")
    public Result<DialysisVO.SessionVO> reschedule(@Valid @RequestBody DialysisDTO.Reschedule dto) {
        return Result.success("已改期", dialysisService.reschedule(dto));
    }

    /**
     * 患者上机开始透析：登记透前体重与血管通路评估，状态推进到透析中。
     * 用于日看板或台账行的「上机」弹框，只有已排班的单能上机。
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:edit')")
    @Operation(summary = "上机（透前体重 + 通路评估必填）")
    @PostMapping("/session/start")
    public Result<DialysisVO.SessionVO> startSession(@Valid @RequestBody DialysisDTO.SessionStart dto) {
        return Result.success("已上机", dialysisService.startSession(dto));
    }

    /**
     * 患者下机结束透析：录入透后体重，超滤量与实际透析时长按透前透后现算回写。
     * 用于日看板或台账行的「下机」弹框，只有透析中的单能下机。
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:edit')")
    @Operation(summary = "下机（超滤量与实际时长服务端回算）")
    @PostMapping("/session/finish")
    public Result<DialysisVO.SessionVO> finishSession(@Valid @RequestBody DialysisDTO.SessionFinish dto) {
        return Result.success("已完成", dialysisService.finishSession(dto));
    }

    /**
     * 给治疗单补记不良反应（类型、程度、处置）：透析中与已完成都能记。
     * 用于上机后的不良反应登记弹框，是工作量统计里不良反应分布的来源。
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:edit')")
    @Operation(summary = "登记不良反应（透析中或已完成均可）")
    @PostMapping("/session/adverse")
    public Result<DialysisVO.SessionVO> recordAdverse(@Valid @RequestBody DialysisDTO.AdverseUpsert dto) {
        return Result.success("不良反应已登记", dialysisService.recordAdverse(dto));
    }

    /**
     * 取消透析治疗单：取消原因必填并释放已占的机位；已上机的只能走下机。
     * 用于日看板或台账行排错后的撤销。
     */
    @PreAuthorize("hasAuthority('medtech:dialysis:edit')")
    @Operation(summary = "取消治疗单（原因必填，释放机位）")
    @PostMapping("/session/cancel")
    public Result<DialysisVO.SessionVO> cancelSession(@Valid @RequestBody DialysisDTO.SessionCancel dto) {
        return Result.success("已取消", dialysisService.cancelSession(dto));
    }

    // 统计

    @PreAuthorize("hasAuthority('medtech:dialysis:list')")
    @Operation(summary = "工作量统计（例次/超滤/不良反应分布/机位负荷）")
    @PostMapping("/stats")
    public Result<DialysisVO.StatsVO> stats(@Valid @RequestBody DialysisDTO.StatsQuery dto) {
        return Result.success(dialysisService.stats(dto));
    }
}
