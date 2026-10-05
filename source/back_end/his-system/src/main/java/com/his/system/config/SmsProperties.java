package com.his.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信验证码配置（yml 的 {@code sms.code.*} 段）。
 *
 * <p>与 {@code wechat.miniapp.*} 同一口径：部署期配置随代码走，网关密钥只从环境变量取。
 * 本仓库没有接真实短信网关，{@code mock=true} 时验证码写日志并随响应回显，供联调使用；
 * 关掉 mock 而未配网关 = 明确发送失败，不会假装"已发送"。
 */
@Data
@Component
@ConfigurationProperties(prefix = "sms.code")
public class SmsProperties {

    /**
     * 测试模式：不走网关，验证码回显给调用方。
     *
     * <p>默认 <b>false</b>（失败关闭）：漏配时注册功能直接不可用，
     * 而不是把验证码匿名回显给任何人 —— 那等于没有校验。dev 配置里显式打开。
     */
    private boolean mock = false;

    /**
     * 验证码有效期（秒）
     */
    private int ttlSeconds = 300;

    /**
     * 同一手机号+场景的重发间隔（秒）
     */
    private int resendSeconds = 60;

    /**
     * 验证码位数
     */
    private int length = 6;
}
