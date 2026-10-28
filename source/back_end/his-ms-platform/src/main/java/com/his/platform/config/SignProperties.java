package com.his.platform.config;

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
     * 证书签发模式：internal-院内自签（默认）；external-外部 CA（走 {@code ExternalCaChannelService}）。
     * external 模式下适配器未返回证书即中断签发，绝不静默回退自签。
     */
    private String caMode = "internal";

    /**
     * 外部 CA 对接地址（csssl 根路径，如 http://localhost:8092/ssl）
     */
    private String caBaseUrl;

    /**
     * 外部 CA 服务账号（csssl 管理后台账号）
     */
    private String caUsername;

    /**
     * 外部 CA 服务账号口令（明文；生产经环境变量注入）
     */
    private String caPassword;

    /**
     * 外部 CA 请求超时（毫秒）
     */
    private int caTimeoutMs = 5000;

    /**
     * 新签发证书的密钥算法：RSA2048（默认，兼容既有链路）或 SM2（国密，对齐 WS/T 847 医学电子签名要求）。
     * 影响密钥生成、签名算法（SHA256withRSA / SM3withSM2）与指纹摘要；历史证书按各自行内算法继续工作。
     */
    private String keyAlgorithm = "RSA2048";

    /**
     * 可信时间戳通道模式：internal-本地内置 TSA（院内自签信任根）；external-外部 TSA（csssl 院内 TSA，
     * 复用 ca-base-url 地址，独立 SM2 时间戳密钥签发令牌）。external 模式下 csssl 不可用则时间来源降级本机时钟。
     */
    private String tsaMode = "internal";

    /**
     * 外部 CA（csssl）接口路径（his.sign.ca-paths.*），相对 ca-base-url
     */
    private CaPaths caPaths = new CaPaths();

    /**
     * csssl 各能力的接口路径：与 CA 侧 REST 路由契约一一对应
     */
    @Data
    public static class CaPaths {

        /**
         * 登录口令加密用的 SM2 公钥获取路径
         */
        private String loginPublicKey = "/auth/publicKey";

        /**
         * 服务账号登录路径
         */
        private String login = "/auth/login";

        /**
         * 公钥签发申请路径
         */
        private String certIssue = "/admin/certs/public-key";

        /**
         * 证书链下载路径，{certId} 由签发回执里的证书主键替换
         */
        private String certDownload = "/admin/certs/{certId}/cert.pem";

        /**
         * 可信时间戳盖章路径
         */
        private String tsaStamp = "/tsa/stamp";

        /**
         * 可信时间戳验签公钥获取路径
         */
        private String tsaPublicKey = "/tsa/publicKey";
    }

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
