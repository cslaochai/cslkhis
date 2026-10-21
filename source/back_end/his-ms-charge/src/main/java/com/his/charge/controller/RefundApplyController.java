package com.his.charge.controller;


import com.his.charge.dto.*;
import com.his.charge.service.RefundApplyService;
import com.his.charge.vo.BizRefundApplyVO;
import com.his.charge.vo.RefundableLineVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 退费申请控制器：退费单据的发起、审核、作废与执行入口。本层只是红冲前置审批单，不承载资金事实。
 */
@Tag(name = "退费申请")
@RestController
@RequestMapping("/charge/refund")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('finance:refund:list')")
public class RefundApplyController {

    private final RefundApplyService refundApplyService;

    /**
     * 退费申请分页：出参带退款方式与到账信息，一眼看出这笔钱退没退、怎么退的。
     * 用于「退费管理」台账查询卡，按患者、申请状态或单号关键字筛查待办与已办申请。
     */
    @Operation(summary = "分页查询退费申请")
    @PostMapping("/listPage")
    public Result<PageResult<BizRefundApplyVO>> listPage(@Valid @RequestBody RefundPageQueryDTO queryDTO) {
        PageResult<BizRefundApplyVO> result = refundApplyService.selectRefundApplyPage(
                queryDTO.getPatientId(),
                queryDTO.getApplyStatus(),
                queryDTO.getKeyword(),
                queryDTO.getPageNum(),
                queryDTO.getPageSize());
        return Result.success(PageResult.of(result.getTotal(), result.getPageNum(), result.getPageSize(),
                result.getPages(), result.getRecords()));
    }

    /**
     * 单条退费申请的取数：申请金额、原账单与执行后的退款去向一并回显。
     * 用于「退费管理」台账行点开的只读详情弹框。
     */
    @Operation(summary = "获取退费申请详情")
    @GetMapping("/getById")
    public Result<BizRefundApplyVO> getById(@RequestParam Long id) {
        return Result.success(refundApplyService.getRefundApplyDetail(id));
    }

    /**
     * 提交退费申请：登记一张待审核单据，不动账单也不动钱。
     * 用于「退费管理」页的新增申请，以及挂号页对已收费用发起的「申请退费」弹框；发起人与审核人必须是两个人。
     */
    @PreAuthorize("hasAuthority('finance:refund:add')")
    @Operation(summary = "提交退费申请")
    @PostMapping("/applyRefund")
    public Result<BizRefundApplyVO> applyRefund(@Valid @RequestBody RefundApplySubmitDTO submitDTO) {
        return Result.success("提交成功", refundApplyService.submitRefundApply(submitDTO));
    }

    /**
     * 退费候选行：某张账单按退费类型还能退的记账行（含剩余数量与来源单号）。
     * 用于「发起退费申请」弹框勾选要退的行——按处方号/申请单号圈定范围，改每行退费数量。
     */
    @PreAuthorize("hasAuthority('finance:refund:add')")
    @Operation(summary = "退费候选行（按账单 + 退费类型）")
    @GetMapping("/refundableLines")
    public Result<List<RefundableLineVO>> refundableLines(@RequestParam Long billId, @RequestParam Integer refundType) {
        return Result.success(refundApplyService.refundableLines(billId, refundType));
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

    /**
     * 执行退费：把一张审核通过的申请落成真实退款，记账行红冲与原路退钱在同一步里一次做完。
     * 用于「退费管理」台账上收费员对已通过申请点的执行动作，是审批链路通向资金层的唯一出口。
     */
    @PreAuthorize("hasAuthority('finance:refund:edit')")
    @Operation(summary = "执行退费")
    @PostMapping("/executeRefund")
    public Result<Void> executeRefund(@Valid @RequestBody RefundExecuteDTO executeDTO) {
        boolean success = refundApplyService.executeRefund(executeDTO.getId(), executeDTO.getRefundBy());
        return success ? Result.success("退费成功", null) : Result.error("退费失败");
    }
}
