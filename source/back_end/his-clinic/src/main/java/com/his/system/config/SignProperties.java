package com.his.system.config;

import com.his.common.util.TextUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 签名能力配置（his.sign.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.sign")
public class SignProperties {

    /**
     * 签名主口令（派生私钥存储密钥的输入）。随环境变：dev 在 application-dev.yml 内联真值，
     * 生产由环境变量 HIS_SIGN_SECRET 注入；未配置时证书签发与可信时间入口直接拒绝。
     */
    private String masterSecret;

    /**
     * PBKDF2 迭代次数（新证书用；老证书用自己行里存的值）
     */
    private int iterations = 120000;

    /**
     * 默认证书有效期（天），可被系统参数的 sign.cert.valid_days 覆盖
     */
    private int defaultValidDays = 365;

    /**
     * 缺证书时是否自动签发（可被系统参数的 sign.cert.auto_issue 覆盖）
     */
    private boolean autoIssueCert = true;

    /**
     * 默认时间来源（可被系统参数的 sign.time_source 覆盖），1-本机时钟
     */
    private int timeSource = 1;

    /**
     * 证书签发模式（M8 留口子）：internal-院内自签（默认，G6/G6b 形态）；
     * external-外部 CA（走 {@code ExternalCaChannelService}，当前为控制台打印形态，
     * 未接入真 CA 前签发会在提交 CSR 后中断 —— 绝不静默回退自签）。
     */
    private String caMode = "internal";

    /**
     * 概要：拿最终生效的口令（配置项为空时读环境变量）
     */
    public String effectiveSecret() {
        if (TextUtil.hasText(masterSecret)) {
            return masterSecret;
        }
        String env = System.getenv("HIS_SIGN_SECRET");
        return TextUtil.hasText(env) ? env : null;
    }
}
