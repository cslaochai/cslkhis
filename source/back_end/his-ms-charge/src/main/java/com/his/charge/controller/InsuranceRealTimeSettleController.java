package com.his.charge.controller;

import com.his.charge.service.InsuranceChannelService;
import com.his.charge.service.InsuranceRealTimeSettleService;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门诊医保实时结算：收费窗口收款时调 2201 预结算 / 2301 正式结算（扣个账）。
 *
 * <p>与 {@link InsuranceSettlementController} 的区别：那个是收完钱之后给医保局报盘的清单管理；
 * 这里是收款那一刻跟医保平台实时交互的入口。
 */
@Tag(name = "门诊医保实时结算")
@RestController
@RequestMapping("/charge/insurance")
@RequiredArgsConstructor
public class InsuranceRealTimeSettleController {

    private final InsuranceRealTimeSettleService insuranceRealTimeSettleService;

    /**
     * 2201 预结算：按账单费用明细推医保平台，返回统筹/个账/自付 splits，不落库。
     * 用于收款弹层「医保个账」档点一下就调，给收费员和患者核对数。
     */
    @Operation(summary = "医保预结算（2201，不落库）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/preSettle")
    public Result<InsuranceChannelService.SettleSplit> preSettle(@RequestParam Long billId) {
        return Result.success(insuranceRealTimeSettleService.preSettle(billId));
    }

    /**
     * 2301 正式结算：医保平台当场扣个账、出 YBZ 单号。
     * 成功后 HIS 同事务落个账流水 + 回写账单 splits，剩余 cashPay 再由前端选现金/扫码收。
     */
    @Operation(summary = "医保正式结算（2301，扣个账并回写账单）")
    @PreAuthorize("hasAuthority('finance:bill:edit')")
    @PostMapping("/formalSettle")
    public Result<InsuranceChannelService.SettleSplit> formalSettle(@RequestParam Long billId) {
        return Result.success(insuranceRealTimeSettleService.formalSettle(billId));
    }
}
