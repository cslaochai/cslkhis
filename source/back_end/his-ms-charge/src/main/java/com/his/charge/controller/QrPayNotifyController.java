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

    /**
     * 受理支付宝侧的支付结果通知并据此勾账：通知只加速入账，收款事实仍以渠道查单为准。
     * 用于患者扫码付款后渠道服务端的异步回调，落支付层的收款侧；无登录态，应答体是渠道协议要求的裸文本。
     */
    @Operation(summary = "支付宝回调·收费台出码")
    @PostMapping("/alipay")
    public String alipayNotify(@RequestParam Map<String, String> params) {
        return qrPayService.confirmByAlipayNotify(params);
    }

    /**
     * 受理微信侧的支付结果通知并据此勾账，与支付宝回调和页面轮询共用同一套入账口径。
     * 用于微信渠道回调到账环节，收到的报文按统一响应之外的裸文本应答渠道。
     */
    @Operation(summary = "微信回调·收费台出码")
    @PostMapping("/wx")
    public String wxNotify(@RequestBody String body) {
        return qrPayService.confirmByWxNotify(body);
    }
}
