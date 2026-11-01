package com.his.platform.config;

import com.his.common.util.TextUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 签名能力配置（his.sign.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.sign")
public class SignProperties {

    /**
     * 签名主口令（派生私钥存储密钥的输入）。随环境变：dev 在 application-dev.yml 内联真值，
     * 生产由环境变量 HIS_SIGN_SECRET 注入；未配置时证书签发入口直接拒绝。
     */
    private String masterSecret;

    /**
     * PBKDF2 迭代次数（新证书用；老证书用自己行里存的值）
     */
    private int iterations;

    /**
     * 默认证书有效期（天），可被系统参数的 sign.cert.valid_days 覆盖
     */
    private int defaultValidDays;

    /**
     * 缺证书时是否自动签发（可被系统参数的 sign.cert.auto_issue 覆盖）
     */
    private boolean autoIssueCert;

    /**
     * 默认时间来源（可被系统参数的 sign.time_source 覆盖），1-本机时钟
     */
    private int timeSource;

    /**
     * 证书主体 O（机构名）：写入签名证书申请单 DN，必须与 CA 侧租户登记的「证书主体O」一致。
     */
    private String organization;

    /**
     * 新签发证书的密钥算法：RSA2048 或 SM2（国密，对齐 WS/T 847 医学电子签名要求）。
     * 影响密钥生成、签名算法（SHA256withRSA / SM3withSM2）与指纹摘要；历史证书按各自行内算法继续工作。
     * 走在线申请时必须与 CA 模板 algorithm 一致（如 DOCTOR_SIGN 为 SM2）。
     */
    private String keyAlgorithm;

    /**
     * CA 在线证书申请（csssl 对外 API）接入配置。
     *
     * <p>租户在 CA 侧创建后由 CA 颁发 App ID 与接入密钥，HIS 把它们写进环境配置
     * （application-dev.yml 内联 / 生产环境变量注入），每次请求带 App ID 签名，
     * CA 按 App ID 反查租户——请求里不出现 tenantKey。</p>
     */
    private Ca ca = new Ca();

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

    @Data
    public static class Ca {

        /**
         * 是否启用在线申请：false 时退回「生成密钥对 → 线下送 CA → 回填证书」人工流程
         */
        private boolean enabled;

        /**
         * CA 服务地址（含 context-path，如 http://{host}:{port}/ssl）
         */
        private String baseUrl;

        /**
         * 租户 App ID（CA 侧建租户时颁发）
         */
        private String appId;

        /**
         * 接入密钥（HMAC 验签 + 报文 AES-GCM 加密共用；只出现在配置里，绝不落业务库）
         */
        private String appSecret;

        /**
         * CA 证书模板编码（模板的 algorithm 必须与本机 keyAlgorithm 一致）
         */
        private String templateCode;

        /**
         * 申请人类别：INSTITUTION-单位 / PERSON-个人
         */
        private String applicantType;

        /**
         * 建立连接超时（毫秒）
         */
        private int connectTimeoutMs;

        /**
         * 单次请求超时（毫秒）
         */
        private int requestTimeoutMs;

        /**
         * 下载证书时是否带链（叶子 + SUB_CA + 根）
         */
        private boolean chain;

        /**
         * 对外接口路径（相对 base-url，{applyNo} 为路径占位符）。
         * 地址只写路径，查询参数由客户端拼装；默认值只写在 config/domain/his-platform.yml，代码不内联。
         */
        private Paths paths = new Paths();

        /**
         * 列出当前缺失的接入凭证配置项；三项（base-url / app-id / app-secret）都配齐时返回 null。
         * 用于报错 / 状态提示时明确告知「具体缺哪一项」，不让运维去猜。
         */
        public String missingCredentialParams() {
            List<String> missing = new ArrayList<>();
            if (!TextUtil.hasText(baseUrl)) {
                missing.add("his.sign.ca.base-url");
            }
            if (!TextUtil.hasText(appId)) {
                missing.add("his.sign.ca.app-id");
            }
            if (!TextUtil.hasText(appSecret)) {
                missing.add("his.sign.ca.app-secret");
            }
            return missing.isEmpty() ? null : String.join("、", missing);
        }
    }

    /**
     * CA 对外接口路径（均为相对 base-url 的路径模板，查询参数不入配置）。
     */
    @Data
    public static class Paths {

        /**
         * 模板列表
         */
        private String templates;

        /**
         * 提交申请
         */
        private String applications;

        /**
         * 按申请单号查询
         */
        private String applicationDetail;

        /**
         * 撤销申请
         */
        private String cancel;

        /**
         * 下载已签发证书
         */
        private String certificate;
    }
}
