package com.his.charge.controller;




import com.his.charge.dto.RefundFlowQueryPageDTO;
import com.his.charge.service.RefundFlowService;
import com.his.charge.vo.BizRefundFlowDetailVO;
import com.his.charge.vo.BizRefundFlowVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 退费流水台账（M7）。
 *
 * <p>只读：台账由冲正入口（{@code ChargeService.refund / refundByApply}）在同事务里写入，
 * 这里不提供任何编辑口子 —— 能改台账就等于能对不上渠道。
 */
@Tag(name = "退费流水")
@RestController
@RequestMapping("/charge/refundFlow")
@RequiredArgsConstructor
public class RefundFlowController {

    private final RefundFlowService refundFlowService;

    @Operation(summary = "分页查询退费流水")
    @PreAuthorize("hasAuthority('finance:refundFlow:list')")
    @PostMapping("/listPage")
    public Result<PageResult<BizRefundFlowVO>> listPage(@Valid @RequestBody RefundFlowQueryPageDTO query) {
        PageResult<BizRefundFlowVO> result = refundFlowService.selectFlowPage(query);
        return Result.success(PageResult.of(result.getTotal(), result.getPageNum(), result.getPageSize(),
                result.getPages(), result.getRecords()));
    }

    @Operation(summary = "退费流水详情（含逐条退费明细）")
    @PreAuthorize("hasAuthority('finance:refundFlow:list')")
    @GetMapping("/getDetailById")
    public Result<BizRefundFlowDetailVO> getDetailById(@RequestParam Long id) {
        return Result.success(refundFlowService.getFlowDetail(id));
    }
}
