package com.his.charge.controller;


import com.his.charge.dto.RefundFlowPageQueryDTO;
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
 * 退费流水控制器：从资金流水的退款方向投影出来的退费台账，只读、无写入口。
 */
@Tag(name = "退费流水")
@RestController
@RequestMapping("/charge/refundFlow")
@RequiredArgsConstructor
public class RefundFlowController {

    private final RefundFlowService refundFlowService;

    /**
     * 退费流水分页：一次退款一行，给出退款单号、原单据、金额与到账渠道。
     * 用于「退费流水」台账查询卡，财务核对退出去的钱分别落在哪张单据、走哪个渠道。
     */
    @Operation(summary = "分页查询退费流水")
    @PreAuthorize("hasAuthority('finance:refundFlow:list')")
    @PostMapping("/listPage")
    public Result<PageResult<BizRefundFlowVO>> listPage(@Valid @RequestBody RefundFlowPageQueryDTO query) {
        PageResult<BizRefundFlowVO> result = refundFlowService.selectFlowPage(query);
        return Result.success(PageResult.of(result.getTotal(), result.getPageNum(), result.getPageSize(),
                result.getPages(), result.getRecords()));
    }

    /**
     * 一笔退费的逐条退款明细：这次退款摊到了哪几张账单行上。
     * 用于「退费流水」台账行点开的只读详情弹框，排查退款金额与原收款对不上时进这里。
     */
    @Operation(summary = "退费流水详情（含逐条退费明细）")
    @PreAuthorize("hasAuthority('finance:refundFlow:list')")
    @GetMapping("/getDetailById")
    public Result<BizRefundFlowDetailVO> getDetailById(@RequestParam Long id) {
        return Result.success(refundFlowService.getFlowDetail(id));
    }
}
