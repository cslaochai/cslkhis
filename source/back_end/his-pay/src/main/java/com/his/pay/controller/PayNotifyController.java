package com.his.pay.controller;

import com.his.common.base.Result;
import com.his.pay.service.PayNotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 支付渠道回调接口（微信/支付宝服务器推送）
 */
@Tag(name = "支付回调")
@RestController
@RequestMapping("/pay/notify")
@RequiredArgsConstructor
public class PayNotifyController {

    private final PayNotifyService payNotifyService;

    @Operation(summary = "微信支付回调（微信服务器推送支付结果）")
    @PostMapping("/wx")
    public String wxNotify(@RequestBody String body) {
        payNotifyService.handleWxNotify(body);
        return "success";
    }

    @Operation(summary = "支付宝支付回调（支付宝服务器推送支付结果）")
    @PostMapping("/alipay")
    public String alipayNotify(@RequestBody String body) {
        payNotifyService.handleAlipayNotify(body);
        return "success";
    }
}
