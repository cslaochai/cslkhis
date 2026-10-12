package com.his.charge.controller;

import com.his.charge.dto.QrPayCreateDTO;
import com.his.charge.dto.QrPayQueryDTO;
import com.his.charge.service.QrPayService;
import com.his.charge.vo.QrPayCreateVO;
import com.his.charge.vo.QrPayResultVO;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收费台扫码收款（医院出码、患者用手机扫）。
 *
 * <p>权限沿用收费台收款本尊的码（与 {@code /charge/settlementBill/pay} 同一档），
 * 出码只是换一种收钱方式，不该另开一档权限。
 */
@RestController
@RequestMapping("/charge/qrPay")
@RequiredArgsConstructor
public class QrPayController {

    private final QrPayService qrPayService;

    @Operation(summary = "按账单出收款码（金额取账单实时应缴）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/create")
    public Result<QrPayCreateVO> create(@Valid @RequestBody QrPayCreateDTO dto) {
        return Result.success(qrPayService.create(dto));
    }

    @Operation(summary = "轮询收款结果（渠道已实收而本地未入账时兜底入账）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/queryResult")
    public Result<QrPayResultVO> queryResult(@Valid @RequestBody QrPayQueryDTO dto) {
        return Result.success(qrPayService.queryResult(dto.getPayNo()));
    }

    @Operation(summary = "关闭支付单（收银员放弃本次扫码收款）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/close")
    public Result<Void> close(@Valid @RequestBody QrPayQueryDTO dto) {
        qrPayService.close(dto.getPayNo());
        return Result.success();
    }

    @Operation(summary = "模拟收款（仅渠道模拟模式生效，真模式拒绝）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/mockPaid")
    public Result<Void> mockPaid(@Valid @RequestBody QrPayQueryDTO dto) {
        qrPayService.mockMarkPaid(dto.getPayNo());
        return Result.success();
    }
}
