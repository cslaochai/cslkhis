package com.his.inpatient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.inpatient.dto.*;
import com.his.inpatient.service.InpatientOrderService;
import com.his.inpatient.vo.InpatientOrderExecVO;
import com.his.inpatient.vo.InpatientOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 住院医嘱（P1：医嘱 → 校对 → 执行 → 计费）
 */
@Tag(name = "住院医嘱")
@RestController
@RequestMapping("/patient/inpatient/order")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ipd:order:list', 'ipd:nurse:list')")
public class InpatientOrderController {

    private final InpatientOrderService inpatientOrderService;

    /**
     * 医嘱分页查询：可按住院次、患者、类别、状态与关键字过滤，带待校对过滤位。
     * 住院医生站「住院医嘱」页与护士站医嘱页签共用同一张表格。
     */
    @Operation(summary = "医嘱分页（医生站 / 护士站共用；pendingVerifyOnly=1 只看待校对）")
    @GetMapping("/listPage")
    public Result<IPage<InpatientOrderVO>> listPage(@Valid InpatientOrderPageQueryDTO query) {
        return Result.success(inpatientOrderService.listPage(query));
    }

    /**
     * 开立或修改医嘱，一次提交的明细整体落成一个组套。
     * 用于住院医生站医嘱开立弹窗保存；套用组套或模板后仍是走这条链路落医嘱。
     */
    @PreAuthorize("hasAuthority('ipd:order:add')")
    @Operation(summary = "开立/修改医嘱（一次提交 = 一个组套），返回组套号")
    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid InpatientOrderUpsertDTO dto) {
        return Result.success("医嘱已保存", inpatientOrderService.save(dto));
    }

    /**
     * 护士批量校对医嘱，校对通过后才生成执行计划。
     * 用于护士站医嘱工作区「待校对」页签勾选后提交。
     */
    @PreAuthorize("hasAuthority('ipd:order:edit')")
    @Operation(summary = "护士医嘱校对（批量，未校对不可执行）")
    @PostMapping("/verify")
    public Result<Integer> verify(@Valid @RequestBody InpatientOrderVerifyDTO dto) {
        return Result.success("医嘱校对成功", inpatientOrderService.verify(dto));
    }

    /**
     * 停止医嘱：同一组套整组停，长期医嘱只能停不能作废。
     * 用于住院医生站「医嘱列表」页签的停止操作。
     */
    @PreAuthorize("hasAuthority('ipd:order:edit')")
    @Operation(summary = "停止医嘱（同组套整组停；长期医嘱只能停不能作废）")
    @PostMapping("/stop")
    public Result<Integer> stop(@Valid @RequestBody InpatientOrderStopDTO dto) {
        return Result.success("医嘱已停止", inpatientOrderService.stop(dto));
    }

    /**
     * 作废医嘱：只允许作废待校对的医嘱，属组套的整组作废。
     * 用于住院医生站「医嘱列表」页签的作废操作。
     */
    @PreAuthorize("hasAuthority('ipd:order:delete')")
    @Operation(summary = "作废医嘱（仅「待校对」；属组套的整组作废）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid InpatientOrderCancelDTO dto) {
        inpatientOrderService.cancel(dto);
        return Result.success("医嘱已作废", null);
    }

    /**
     * 待执行队列分页：加急优先、同级按计划时间升序。
     * 用于护士站医嘱工作区「待执行」页签，读取时同时为当天应执行的长期医嘱补执行计划（幂等）。
     */
    @Operation(summary = "护士待执行队列（加急优先、按计划时间升序；会补当天长期医嘱计划）")
    @GetMapping("/execPendingList")
    public Result<IPage<InpatientOrderExecVO>> execPendingList(@Valid OrderExecPageQueryDTO query) {
        return Result.success(inpatientOrderService.execPendingList(query));
    }

    /**
     * 批量登记医嘱执行结果：已执行的同时计费，已跳过的必须留原因。
     * 用于护士站「待执行」页签勾选后提交执行。
     */
    @PreAuthorize("hasAuthority('ipd:order:edit')")
    @Operation(summary = "医嘱执行（批量：2-已执行并计费 / 3-已跳过并留原因）")
    @PostMapping("/exec/complete")
    public Result<Integer> execComplete(@Valid @RequestBody OrderExecCompleteDTO execCompleteDTO) {
        return Result.success("医嘱执行已记录", inpatientOrderService.execComplete(execCompleteDTO));
    }

    /**
     * 执行记录分页：含已执行与已跳过，留痕不删除。
     * 用于护士站医嘱工作区「执行记录」页签回顾与追溯。
     */
    @Operation(summary = "执行记录查询（含已执行 / 已跳过，留痕不删除）")
    @GetMapping("/execList")
    public Result<IPage<InpatientOrderExecVO>> execList(@Valid OrderExecPageQueryDTO query) {
        return Result.success(inpatientOrderService.execList(query));
    }

    /**
     * 待校对医嘱计数。
     * 用于护士站医嘱工作区「待校对」页签的角标，按住院次过滤时只数该患者。
     */
    @Operation(summary = "待校对医嘱数（护士站卡片）")
    @GetMapping("/countPendingVerify")
    public Result<Long> countPendingVerify(@RequestParam(required = false) Long admissionId) {
        return Result.success(inpatientOrderService.countPendingVerify(admissionId));
    }

    /**
     * 待执行医嘱计数。
     * 用于护士站医嘱工作区「待执行」页签的角标，按住院次过滤时只数该患者。
     */
    @Operation(summary = "待执行医嘱数（护士站卡片）")
    @GetMapping("/countPendingExec")
    public Result<Long> countPendingExec(@RequestParam(required = false) Long admissionId) {
        return Result.success(inpatientOrderService.countPendingExec(admissionId));
    }
}
