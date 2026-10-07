package com.his.ai.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * AI 配置的语义化读取入口。
 * <p>
 * 配置本体在 {@code application.yml} 的 {@code ai.*} 段，由 {@link AiProperties} 在启动时绑定一次。
 * 本类只做两件事：把原始配置翻译成业务语义（能力开没开、超时多少、用哪个模型），
 * 以及把「为什么用不了」翻译成人能看懂的原因。
 * <p>
 * <b>已经没有缓存了。</b> 之前的实现从系统参数里读、带 60 秒 TTL 缓存、并提供 {@code refresh()}
 * 支持在线热改；改成 yml 之后配置在启动时就固定了，改值必须重启 —— 所以缓存、
 * 刷新接口这些机制一并去掉，避免留下「以为改了就能生效」的假象。
 * <p>
 * <b>失败姿态不变：</b>配置缺失时一律判定为「不可用」，让能力降级而不是抛异常，
 * AI 挂掉绝不能影响业务主流程。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiConfigProvider {

    public static final String ENV_API_KEY = "HIS_AI_API_KEY";

    private final AiProperties aiProperties;

    private final Environment environment;

    /**
     * 获取当前生效配置（启动时绑定，运行期不变）
     */
    public AiProperties get() {
        return aiProperties;
    }

    /**
     * 只看配置里的能力开关，不考虑全局是否就绪。
     * <p>
     * 与 {@link #isCapabilityEnabled} 的区别必须分清：后者回答「这个能力现在能不能工作」
     * （总开关、地址、密钥、模型任一不满足就是 false），前者回答「配置里是否允许这个能力」。
     * <p>
     * <b>健康检查必须显示后者。</b> 这里踩过一次坑：healthCheck 一开始用 isCapabilityEnabled，
     * 于是在没配密钥的环境里 7 个能力全显示 false —— 运维看到的是「yml 里明明开了却不生效」，
     * 而真正原因（缺 HIS_AI_API_KEY）藏在同一份返回的 notReadyReason 里。
     * 「开关状态」和「可用状态」是两个问题，不能合成一个字段回答。
     */
    public boolean switchedOn(String capabilityKey) {
        Boolean feature = aiProperties.getFeatures().get(capabilityKey);
        return feature == null || feature;
    }

    /**
     * 判断某能力是否可用。总开关关闭、或该能力开关关闭，都返回 false。
     * 未单独配置的能力默认视为开启（总开关已控制）。
     */
    public boolean isCapabilityEnabled(String capabilityKey) {
        if (!aiProperties.isReady()) {
            return false;
        }
        Boolean feature = aiProperties.getFeatures().get(capabilityKey);
        return feature == null || feature;
    }

    /**
     * 说明当前为什么用不了模型。用于降级原因回传前端 ——
     * 「AI 不可用」这句话对医生没有价值，「服务地址未配置」才有价值。
     * <p>
     * 文案里必须带上配置项的真实位置，否则运维照着系统参数找一个已经不存在的键。
     */
    public String notReadyReason() {
        if (!aiProperties.isEnabled()) {
            return "AI 总开关已关闭（application.yml: ai.enabled）";
        }
        if (!StringUtils.hasText(aiProperties.getBaseUrl())) {
            return "模型服务地址未配置（application.yml: ai.base-url）";
        }
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            return "模型访问密钥未配置（环境变量 " + ENV_API_KEY + "）";
        }
        if (!StringUtils.hasText(aiProperties.getModel())) {
            return "模型名称未配置（application.yml: ai.model）";
        }
        return "";
    }

    /**
     * 说明某能力为什么被关闭。
     * <p>
     * <b>能力可用时必须返回空串</b>。这里踩过一个坑：最初实现无论能力是否开启
     * 都会返回「能力开关已关闭」，导致真实网络失败时前端看到的原因驴唇不对马嘴 ——
     * 排查方向被带偏。凡是这种「拿来做判断依据」的文案，返回值语义必须严格。
     */
    public String capabilityDisabledReason(String capabilityKey) {
        String notReady = notReadyReason();
        if (StringUtils.hasText(notReady)) {
            return notReady;
        }
        Boolean feature = aiProperties.getFeatures().get(capabilityKey);
        if (feature != null && !feature) {
            return "能力开关已关闭（application.yml: ai.features." + capabilityKey + "）";
        }
        return "";
    }

    /**
     * 密钥是否来自环境变量（用于健康检查回显来源，不涉及密钥内容）
     */
    public boolean apiKeyFromEnv() {
        return StringUtils.hasText(environment.getProperty(ENV_API_KEY));
    }

    /**
     * 取某能力的超时时间，未单独配置则回落全局超时
     */
    public int timeoutOf(String capabilityKey) {
        Integer override = aiProperties.getTimeouts().get(capabilityKey);
        return override != null && override > 0 ? override : aiProperties.getTimeoutMs();
    }

    /**
     * 取某能力实际生效的模型（G-16 多模型路由）：
     * {@code ai.models.<capabilityKey>} 覆盖命中 → 用之（该能力整体换模型，lite 分流同样被覆盖）；
     * 否则 lite=true 且配了轻量模型 → 轻量模型；再否则回落主模型。
     */
    public String modelOf(String capabilityKey, boolean lite) {
        String override = aiProperties.getModels().get(capabilityKey);
        if (StringUtils.hasText(override)) {
            return override;
        }
        if (lite && StringUtils.hasText(aiProperties.getModelLite())) {
            return aiProperties.getModelLite();
        }
        return aiProperties.getModel();
    }

    /**
     * ASR 模型名（语音转写与 chat 模型分属两个端点，不允许混用）
     */
    public String asrModel() {
        return aiProperties.getAsr().getModel();
    }

    /**
     * ASR 访问密钥：未单独配置时回落 chat 密钥（同一把 HIS_AI_API_KEY）
     */
    public String asrApiKey() {
        String key = aiProperties.getAsr().getApiKey();
        return StringUtils.hasText(key) ? key : aiProperties.getApiKey();
    }
}
