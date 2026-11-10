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
         * 接入密钥（HMAC 验签 + 报文 AES-GCM 加密共用；只出现在配置里，绝不落业务库）。
         *
         * <p>配置了 {@link #apiPrivateKey} 后本项不再使用：非对称模式下私钥签名、
         * 报文走明文 JSON（机密性由 HTTPS 负责），CA 侧也不需要保存任何共享口令。</p>
         */
        private String appSecret;

        /**
         * 调用 CA 开放接口用的 SM2 私钥（64 位十六进制）。
         *
         * <p>由 HIS 本地生成（{@code CaRequestSigner#publicKeyOf} 导出公钥），
         * 只把公钥登记到 CA 的「系统接入」里；私钥永远只在本机配置/密钥库里，
         * CA 拿不到、也不需要它能伪造请求。配置了本项即切到非对称签名模式。</p>
         */
        private String apiPrivateKey;

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
         * CA 状态推送回调的限流阈值：同一来源 IP 每分钟最多接收多少次，超出直接 429。
         * HIS 只保留提交与吊销两个出站能力，状态流转一律由 CA 推送。
         */
        private int callbackRateLimitPerMinute;

        /**
         * CA 状态推送回调的验签密钥（HMAC-SHA256），与 CA 侧 {@code csssl.his-push.secret} 同值。
         *
         * <p>留空时回调端点<b>拒绝一切推送</b>：该端点匿名开放（CA 服务器不持有 HIS token），
         * 没有验签就等于任何人都能伪造「签发成功」报文，把任意一张证书塞进 HIS
         * 当成这位医生的证书——这是反向的公钥替换攻击，比伪造一次业务请求严重得多。
         */
        private String callbackPublicKey;

        /**
         * 是否启用 CA 推送回调验签（默认 true）。
         *
         * <p>这是<b>显式能力开关</b>：不靠「classpath 里有没有 csssl-sdk」隐式决定。
         * 置 false 时验签 Filter 不注册，并由 {@code CaPushDisabledFilter} 直接对回调端点回 503，
         * 保证不会出现「端点开门但无人验签」的窗口——停用是拒绝，不是放行。</p>
         *
         * <p>保持 true（默认）而 classpath 又缺 SDK 时，应用<b>启动即失败</b>（fail-fast），
         * 不允许带着未验签的匿名写入口上线。</p>
         */
        private boolean pushVerifyEnabled = true;

        /**
         * 预期的 CA 推送 keyId（公钥指纹前 16 位）。配置后会校验请求头 X-Ca-Key-Id 是否匹配，
         * 防止 CA 换了推送密钥后 HIS 还在用旧公钥验签。留空则不校验 keyId。
         */
        private String callbackKeyId;

        /**
         * 回调时间戳允许偏差（秒）：超出即判定为过期或重放请求
         */
        private int callbackClockSkewSeconds;

        /**
         * 是否信任代理传来的来源 IP 头（默认 false）。
         *
         * <p>默认 false 只取 {@code request.getRemoteAddr()}：本端点直连对外端口（容器映射 8009），
         * 客户端可以随意伪造 {@code X-Forwarded-For} / {@code X-Real-IP}，若按头取 IP，
         * 攻击者每次换一个伪造值就能让限流分钟桶永远填不满，限流等于没做。</p>
         *
         * <p>只有确实部署在受信反向代理之后（由代理强制覆盖该头）才置 true；
         * 开启后优先取 {@code X-Real-IP}，其次取 {@code X-Forwarded-For} 的<b>最后一段</b>
         * ——nginx 用 {@code $proxy_add_x_forwarded_for} 是把真实地址追加在末尾，
         * 第一段反而是客户端可以自己塞的。</p>
         */
        private boolean callbackTrustForwardedIp;

        /**
         * 对外接口路径（相对 base-url）。地址只写路径，参数一律封装在请求体内；
         * 默认值只写在 config/domain/his-platform.yml，代码不内联。
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
            if (!TextUtil.hasText(appSecret) && !TextUtil.hasText(apiPrivateKey)) {
                missing.add("his.sign.ca.app-secret 或 his.sign.ca.api-private-key（二者填其一）");
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
         * 提交申请
         */
        private String applySign;

        /**
         * 撤销申请（申请单号放在请求体内，URL 不暴露参数）
         */
        private String cancel;

        /**
         * 吊销已签发证书（证书序列号放在请求体内）
         */
        private String revoke;
    }
}
