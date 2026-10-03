package com.his.ai.config;

import com.his.ai.constant.AiCapabilityKeys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 运行时配置，由 {@code application.yml} 的 {@code ai.*} 段绑定（见 §AI 能力配置注释）。
 * <p>
 * <b>为什么从系统参数迁到 yml：</b> 这些是「部署期配置」而非「运营配置」——
 * 服务地址、模型名、超时、熔断阈值、密钥来源在不同环境本就不同，应当作为配置即代码
 * 跟代码一起评审、随发布走；而且密钥不能落库（DB 被导出即泄露）。
 * 代价是改值需重启，不再支持在线热改。
 * <p>
 * <b>设计约束：</b>任何业务代码都不要直接读这里的字段做判断分支，
 * 统一走 {@link AiConfigProvider} 的语义化方法（isCapabilityEnabled / timeoutOf / modelOf）。
 * 直接读字段会让「开关关掉了但某处没判断」这种漏网之鱼无从排查。
 * <p>
 * 这是启动时绑定一次的单例，运行期不会变；想改配置请改 yml 并重启。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * 总开关。false 时所有 AI 能力直接降级，业务走原有逻辑。
     */
    private boolean enabled = false;

    /**
     * 接口协议标识，当前仅支持 OpenAI 兼容协议
     */
    private String provider = "openai-compatible";

    /**
     * 服务地址，例如 https://api.deepseek.com 或内网 http://10.0.0.5:8000
     */
    private String baseUrl = "";

    /**
     * 访问密钥。yml 里写成 {@code ${HIS_AI_API_KEY:}}，即只从环境变量取，不落库、不进仓库。
     */
    private String apiKey = "";

    /**
     * 主模型
     */
    private String model = "";

    /**
     * 轻量模型（简单分类任务分流，降低 token 成本）
     */
    private String modelLite = "";

    /**
     * 全局超时（毫秒）
     */
    private int timeoutMs = 8000;

    /**
     * 失败重试次数
     */
    private int maxRetries = 1;

    /**
     * 熔断时长（秒）
     */
    private int circuitBreakerSeconds = 300;

    /**
     * 连续失败多少次触发熔断
     */
    private int circuitFailureThreshold = 5;

    /**
     * RAG 粗召回条数
     */
    private int retrieveTopN = 50;

    /**
     * 能力开关，来自 {@code ai.features.*}，key 为 {@link AiCapabilityKeys} 的能力标识
     */
    private Map<String, Boolean> features = new HashMap<>();

    /**
     * 单能力超时覆盖，来自 {@code ai.timeouts.*}（毫秒），未配的回落 {@link #timeoutMs}
     */
    private Map<String, Integer> timeouts = new HashMap<>();

    /**
     * 是否具备真正发起调用的条件（开关打开且地址、密钥、模型都已配置）
     */
    public boolean isReady() {
        return enabled
                && StringUtils.hasText(baseUrl)
                && StringUtils.hasText(apiKey)
                && StringUtils.hasText(model);
    }
}
