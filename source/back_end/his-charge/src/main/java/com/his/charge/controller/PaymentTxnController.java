package com.his.charge.controller;


import com.his.charge.dto.PaymentTxnPageQueryDTO;
import com.his.charge.service.PaymentService;
import com.his.charge.vo.BizPaymentTxnVO;
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
 * 支付流水控制器：收/退资金流水的只读查询入口，落收费四层里的支付层（一笔钱一行、收退同表带符号）。
 */
@Tag(name = "支付流水")
@RestController
@RequestMapping("/charge/paymentTxn")
@RequiredArgsConstructor
public class PaymentTxnController {

    private final PaymentService paymentService;

    /**
     * 资金流水分页：按资金方向、渠道、患者等条件捞每一笔真金白银的进出。
     * 用于「支付流水」台账查询卡，财务核对点钞数与渠道到账时的原始依据；本页无任何写口子。
     */
    @Operation(summary = "分页查询支付流水")
    @PreAuthorize("hasAuthority('finance:payTxn:list')")
    @PostMapping("/listPage")
    public Result<PageResult<BizPaymentTxnVO>> listPage(@Valid @RequestBody PaymentTxnPageQueryDTO query) {
        return Result.success(paymentService.selectPage(query));
    }

    /**
     * 一张账单名下的全部收款与退款流水，按发生顺序逐笔给出。
     * 供按账单勾对「这笔钱分别走哪个渠道收进来、又退回多少」的核对环节，账单整体是否付清由这些流水比出来。
     */
    @Operation(summary = "某张账单的全部收/退流水")
    @PreAuthorize("hasAuthority('finance:payTxn:list')")
    @GetMapping("/listByBill")
    public Result<List<BizPaymentTxnVO>> listByBill(@RequestParam Long billId) {
        return Result.success(paymentService.listByBillVO(billId));
    }
}
