package com.his.charge.controller;

import com.his.charge.dto.QrPayBillDTO;
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

    /**
     * 按账单生成一张聚合收款码：支付单金额只能由账单当前尚需缴算出，前端传不进金额。
     * 一张码微信/支付宝/云闪付都能扫（渠道在支付时由医院聚合网关识别确定），所以入参不需要选渠道。
     */
    @Operation(summary = "按账单出聚合收款码（金额取账单实时应缴，一码多平台）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/create")
    public Result<QrPayCreateVO> create(@Valid @RequestBody QrPayCreateDTO dto) {
        return Result.success(qrPayService.create(dto));
    }

    /**
     * 查一次支付单的收款结果：渠道查单才是收款事实，本地未入账时在此兜底补记流水。
     * 用于码展示期间的页面轮询，公网回调断了也靠它把收到的钱落到账上。
     */
    @Operation(summary = "轮询收款结果（渠道已实收而本地未入账时兜底入账）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/queryResult")
    public Result<QrPayResultVO> queryResult(@Valid @RequestBody QrPayQueryDTO dto) {
        return Result.success(qrPayService.queryResult(dto.getPayNo()));
    }

    /**
     * 关闭支付单：本地置为已关闭并尽力关掉渠道侧交易，作废这张码。
     * 用于收银员放弃本次扫码收款或在现金与码之间切档——旧码不关，患者扫完就成了同一笔账收两次。
     */
    @Operation(summary = "关闭支付单（收银员放弃本次扫码收款）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/close")
    public Result<Void> close(@Valid @RequestBody QrPayQueryDTO dto) {
        qrPayService.close(dto.getPayNo());
        return Result.success();
    }

    /**
     * 关掉这张账单上所有待支付码：收银员切到现金或关掉收款弹层时调。
     * 聚合码一张账单只有一张，但作废动作仍按账单收口（历史多渠道单也一并关掉）。
     */
    @Operation(summary = "按账单关闭待支付码")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/closeByBill")
    public Result<Void> closeByBill(@Valid @RequestBody QrPayBillDTO dto) {
        qrPayService.closePendingByBill(dto.getBillId());
        return Result.success();
    }

    /**
     * 把「患者已扫聚合码照单付款」登记进模拟渠道，随后的查单就按实收入账；真实网关模式一律拒绝。
     * 供未接通真实聚合网关时的联调验收环节触发一次完整收款链路，不由收费页面调用。
     * channel 指定模拟落在哪个资金渠道（不传默认微信）。
     */
    @Operation(summary = "模拟收款（仅聚合模拟模式生效，真模式拒绝）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/mockPaid")
    public Result<Void> mockPaid(@Valid @RequestBody QrPayQueryDTO dto) {
        qrPayService.mockMarkPaid(dto.getPayNo(), dto.getChannel());
        return Result.success();
    }
}
