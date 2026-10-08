package com.his.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信验证码配置（yml 的 sms.code.* 段）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.sms.code")
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
