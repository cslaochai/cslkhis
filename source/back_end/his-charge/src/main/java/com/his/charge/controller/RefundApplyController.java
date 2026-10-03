package com.his.charge.controller;

import com.his.charge.dto.*;
import com.his.charge.service.RefundApplyService;
import com.his.charge.vo.BizRefundApplyVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 退费申请控制器
 */
@Tag(name = "退费申请")
@RestController
@RequestMapping("/charge/refund")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('finance:refund:list')")
public class RefundApplyController {

    private final RefundApplyService refundApplyService;

    @Operation(summary = "分页查询退费申请")
    @PostMapping("/listPage")
    public Result<PageResult<BizRefundApplyVO>> listPage(@RequestBody RefundQueryPageDTO queryDTO) {
        PageResult<BizRefundApplyVO> result = refundApplyService.selectRefundApplyPage(
                queryDTO.getPatientId(),
                queryDTO.getApplyStatus(),
                queryDTO.getKeyword(),
                queryDTO.getPageNum(),
                queryDTO.getPageSize());
        return Result.success(PageResult.of(result.getTotal(), result.getPageNum(), result.getPageSize(),
                result.getPages(), result.getRecords()));
    }

    @Operation(summary = "获取退费申请详情")
    @GetMapping("/getById")
    public Result<BizRefundApplyVO> getById(@RequestParam Long id) {
        return Result.success(refundApplyService.getRefundApplyDetail(id));
    }

    @PreAuthorize("hasAuthority('finance:refund:add')")
    @Operation(summary = "提交退费申请")
    @PostMapping("/applyRefund")
    public Result<BizRefundApplyVO> applyRefund(@Valid @RequestBody RefundApplySubmitDTO submitDTO) {
        return Result.success("提交成功", refundApplyService.submitRefundApply(submitDTO));
    }

    /**
     * 审核走 {@code :edit} 而不是 {@code :add}：{@code :add} 现在是「发起退费申请」的码，
     * 挂号窗口（前台导诊，sql/122）只拿 {@code :add} —— 若审核也挂 {@code :add}，
     * 窗口就能自己发起、自己审核，「一人申请、另一人审核」的口径当场失效。
     * 原先持有 2142 的角色（管理员/收费员/医保结算员）同时持有 2143，所以这次调整对它们行为中性。
     */
    @PreAuthorize("hasAuthority('finance:refund:edit')")
    @Operation(summary = "审核退费申请")
    @PostMapping("/auditApply")
    public Result<Void> auditApply(@Valid @RequestBody RefundApplyAuditDTO auditDTO) {
        boolean success = refundApplyService.auditRefundApply(
                auditDTO.getId(), auditDTO.getApproved(),
                auditDTO.getAuditorId(), auditDTO.getAuditorName(), auditDTO.getRemark());
        return success ? Result.success("审核成功", null) : Result.error("审核失败");
    }

    /**
     * 作废与审核/执行同属"处理别人发起的申请"，因此共用 {@code :edit} 码：
     * 只有 {@code :add} 的挂号窗口能发起、不能把自己发起的申请作废掉（要作废得找收费处）。
     */
    @PreAuthorize("hasAuthority('finance:refund:edit')")
    @Operation(summary = "作废退费申请")
    @PostMapping("/discardApply")
    public Result<Void> discardApply(@Valid @RequestBody RefundDiscardDTO discardDTO) {
        boolean success = refundApplyService.discardRefundApply(discardDTO.getId(), discardDTO.getReason());
        return success ? Result.success("已作废", null) : Result.error("作废失败");
    }

    @PreAuthorize("hasAuthority('finance:refund:edit')")
    @Operation(summary = "执行退费")
    @PostMapping("/executeRefund")
    public Result<Void> executeRefund(@Valid @RequestBody RefundExecuteDTO executeDTO) {
        boolean success = refundApplyService.executeRefund(executeDTO.getId(), executeDTO.getRefundBy());
        return success ? Result.success("退费成功", null) : Result.error("退费失败");
    }
}
