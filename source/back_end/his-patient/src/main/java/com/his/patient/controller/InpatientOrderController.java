package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.*;
import com.his.patient.service.InpatientOrderService;
import com.his.patient.vo.InpatientOrderExecVO;
import com.his.patient.vo.InpatientOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 住院医嘱（P1：医嘱 → 校对 → 执行 → 计费）
 *
 * <p>约定：查询一律 GET，写操作一律 POST，路径驼峰。
 *
 * <p><b>注意 {@code /execPendingList} 是 GET 但会写库</b>：它会为"该有今天这次执行"的长期医嘱
 * 补当天计划行（幂等）。这是刻意的设计——见 {@code InpatientOrderServiceImpl#backfillTodayPlans}，
 * 目的是不引入定时任务，避免"服务停机那天全院长期医嘱计划集体缺失"这种静默故障。
 */
@Tag(name = "住院医嘱")
@RestController
@RequestMapping("/patient/inpatient/order")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ipd:order:list', 'ipd:nurse:list')")
public class InpatientOrderController {

    private final InpatientOrderService orderService;

    @Operation(summary = "医嘱分页（医生站 / 护士站共用；pendingVerifyOnly=1 只看待校对）")
    @GetMapping("/listPage")
    public Result<IPage<InpatientOrderVO>> listPage(@Valid InpatientOrderQueryPageDTO query) {
        return Result.success(orderService.listPage(query));
    }

    @PreAuthorize("hasAuthority('ipd:order:add')")
    @Operation(summary = "开立/修改医嘱（一次提交 = 一个组套），返回组套号")
    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid InpatientOrderUpsertDTO dto) {
        return Result.success("医嘱已保存", orderService.save(dto));
    }

    @PreAuthorize("hasAuthority('ipd:order:edit')")
    @Operation(summary = "护士医嘱校对（批量，未校对不可执行）")
    @PostMapping("/verify")
    public Result<Integer> verify(@Valid @RequestBody InpatientOrderVerifyDTO dto) {
        return Result.success("医嘱校对成功", orderService.verify(dto));
    }

    @PreAuthorize("hasAuthority('ipd:order:edit')")
    @Operation(summary = "停止医嘱（同组套整组停；长期医嘱只能停不能作废）")
    @PostMapping("/stop")
    public Result<Integer> stop(@Valid @RequestBody InpatientOrderStopDTO dto) {
        return Result.success("医嘱已停止", orderService.stop(dto));
    }

    @PreAuthorize("hasAuthority('ipd:order:delete')")
    @Operation(summary = "作废医嘱（仅「待校对」；属组套的整组作废）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid InpatientOrderCancelDTO dto) {
        orderService.cancel(dto);
        return Result.success("医嘱已作废", null);
    }

    @Operation(summary = "护士待执行队列（加急优先、按计划时间升序；会补当天长期医嘱计划）")
    @GetMapping("/execPendingList")
    public Result<IPage<InpatientOrderExecVO>> execPendingList(@Valid OrderExecQueryPageDTO query) {
        return Result.success(orderService.execPendingList(query));
    }

    @PreAuthorize("hasAuthority('ipd:order:edit')")
    @Operation(summary = "医嘱执行（批量：2-已执行并计费 / 3-已跳过并留原因）")
    @PostMapping("/exec/complete")
    public Result<Integer> execComplete(@Valid @RequestBody OrderExecCompleteDTO dto) {
        return Result.success("医嘱执行已记录", orderService.execComplete(dto));
    }

    @Operation(summary = "执行记录查询（含已执行 / 已跳过，留痕不删除）")
    @GetMapping("/execList")
    public Result<IPage<InpatientOrderExecVO>> execList(@Valid OrderExecQueryPageDTO query) {
        return Result.success(orderService.execList(query));
    }

    @Operation(summary = "待校对医嘱数（护士站卡片）")
    @GetMapping("/countPendingVerify")
    public Result<Long> countPendingVerify(@RequestParam(required = false) Long admissionId) {
        return Result.success(orderService.countPendingVerify(admissionId));
    }

    @Operation(summary = "待执行医嘱数（护士站卡片）")
    @GetMapping("/countPendingExec")
    public Result<Long> countPendingExec(@RequestParam(required = false) Long admissionId) {
        return Result.success(orderService.countPendingExec(admissionId));
    }
}
