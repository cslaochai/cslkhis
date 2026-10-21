package com.his.app.controller;

import com.his.app.service.MiniPayService;
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
 * 支付渠道异步通知入口（微信/支付宝服务器推送）。
 *
 * <p>URL 挂在 `/pay/notify` 而不是 `/miniapp/pay/notify`：`alipay.notify-url` 与微信商户平台
 * 登记的回调地址是对外契约，已经报给渠道方，改路径等于让线上通知全部 404。
 * 落点选在 his-ms-app 是因为入账要同时触达支付单与收费、挂号两侧，只有患者端聚合层
 * 能单向看到它们（his-pay 反过来依赖不了 his-charge，那是 Maven 环）。
 *
 * <p>出参是渠道协议规定的裸文本，不是统一 Result 结构：支付宝只看 body 是否等于 `success`，
 * 包一层 JSON 会被判为处理失败并持续重试。
 */
@Tag(name = "支付回调")
@RestController
@RequestMapping("/pay/notify")
@RequiredArgsConstructor
public class MiniPayNotifyController {

    private final MiniPayService miniPayService;

    /**
     * 支付宝支付结果通知：校验表单参数后把对应支付单落账。
     * 由支付宝服务器在患者完成付款后异步推送，不是患者端页面的主动请求；验签或落账失败时返回非成功文本让渠道重试。
     */
    @Operation(summary = "支付宝支付回调（支付宝服务器推送支付结果）")
    @PostMapping("/alipay")
    public String alipayNotify(@RequestParam Map<String, String> params) {
        return miniPayService.confirmByAlipayNotify(params);
    }

    /**
     * 微信支付结果通知：解密报文取出商户单号后把对应支付单落账。
     * 由微信服务器在患者完成付款后异步推送，不是患者端页面的主动请求；处理不成功时返回失败让渠道按策略重发。
     */
    @Operation(summary = "微信支付回调（微信服务器推送支付结果）")
    @PostMapping("/wx")
    public String wxNotify(@RequestBody String body) {
        return miniPayService.confirmByWxNotify(body);
    }
}
