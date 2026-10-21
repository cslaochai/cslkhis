package com.his.charge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "wxpay")
public class WxPayProperties {

    /**
     * 微信公众号/小程序 AppID（wx 开头），非敏感，用于标识接入应用。对应 yml: wx.pay.app-id。
     */
    private String appId;

    /**
     * 微信支付商户号（mch_id，纯数字）。对应 yml: wx.pay.mch-id。
     */
    private String mchId;

    /**
     * APIv3 密钥（APIv3Key），用于回调解密与报文加解密，敏感。对应 yml: wx.pay.api-v3-key。
     */
    private String apiV3Key;

    /**
     * 商户 API 证书序列号（certSerialNo），微信回调验签使用。对应 yml: wx.pay.cert-serial-no。
     */
    private String certSerialNo;

    /**
     * 商户私钥证书本地路径（privateKeyPath），用于请求签名，敏感。对应 yml: wx.pay.private-key-path。
     */
    private String privateKeyPath;

    /**
     * 支付结果异步回调地址（notifyUrl）。微信支付平台在用户支付完成后会把结果推送到该地址，
     */
    private String notifyUrl;

    /**
     * 是否走模拟支付：true=后端直接判定支付成功（dev/演示），false=走真实微信支付 SDK。对应 yml: wx.pay.mock-enabled。
     */
    private boolean mockEnabled = true;
}
