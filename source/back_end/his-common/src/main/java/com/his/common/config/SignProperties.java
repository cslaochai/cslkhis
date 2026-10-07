package com.his.common.config;

import com.his.common.util.TextUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 签名能力配置（{@code his.sign.*}）。
 *
 * <p><b>口令的来源</b>：优先取配置项（约定写成 {@code ${HIS_SIGN_SECRET:...}}，
 * 于是环境变量能覆盖）。类里额外提供 {@link #effectiveSecret()} 做一次
 * "环境变量直读"兜底 —— 因为运维习惯直接 export 环境变量而不改 yml，
 * 少了这层兜底就会出现"我明明设了变量却还是报没配口令"，排查成本极高。
 *
 * <p><b>配置项 vs 系统参数表的分工</b>：口令与算法这类"改了要重启、且绝不能进库"的走本类；
 * 有效期天数、是否自动发证这类运维随时要调的走系统参数（表里已有
 * {@code sign.cert.valid_days} / {@code sign.cert.auto_issue} / {@code sign.time_source}）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.sign")
public class SignProperties {

    /**
     * 主口令。**生产必须用环境变量 {@code HIS_SIGN_SECRET} 覆盖**。
     * 留默认值的理由：本仓库连数据库口令都是明文配置，这里再要求"必须配环境变量"只会导致
     * 本地跑不起来；但类注释与页面都要说清"这个默认值不是安全配置"。
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
