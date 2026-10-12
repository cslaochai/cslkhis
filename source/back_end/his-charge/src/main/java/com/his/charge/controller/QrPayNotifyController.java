package com.his.charge.controller;

import com.his.charge.service.QrPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 收费台出码的渠道异步通知入口
 */
@Tag(name = "支付回调（收费台出码）")
@RestController
@RequestMapping("/pay/notify/charge")
@RequiredArgsConstructor
public class QrPayNotifyController {

    private final QrPayService qrPayService;

    @Operation(summary = "支付宝回调·收费台出码")
    @PostMapping("/alipay")
    public String alipayNotify(@RequestParam Map<String, String> params) {
        return qrPayService.confirmByAlipayNotify(params);
    }

    @Operation(summary = "微信回调·收费台出码")
    @PostMapping("/wx")
    public String wxNotify(@RequestBody String body) {
        return qrPayService.confirmByWxNotify(body);
    }
}
