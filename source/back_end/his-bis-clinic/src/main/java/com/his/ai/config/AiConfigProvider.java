package com.his.ai.config;

import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * AI 配置的语义化读取入口。
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
     */
    public String notReadyReason() {
        if (!aiProperties.isEnabled()) {
            return "AI 总开关已关闭（application.yml: ai.enabled）";
        }
        if (!TextUtil.hasText(aiProperties.getBaseUrl())) {
            return "模型服务地址未配置（application.yml: ai.base-url）";
        }
        if (!TextUtil.hasText(aiProperties.getApiKey())) {
            return "模型访问密钥未配置（环境变量 " + ENV_API_KEY + "）";
        }
        if (!TextUtil.hasText(aiProperties.getModel())) {
            return "模型名称未配置（application.yml: ai.model）";
        }
        return "";
    }

    /**
     * 说明某能力为什么被关闭。
     */
    public String capabilityDisabledReason(String capabilityKey) {
        String notReady = notReadyReason();
        if (TextUtil.hasText(notReady)) {
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
        return TextUtil.hasText(environment.getProperty(ENV_API_KEY));
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
        if (TextUtil.hasText(override)) {
            return override;
        }
        if (lite && TextUtil.hasText(aiProperties.getModelLite())) {
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
        return TextUtil.hasText(key) ? key : aiProperties.getApiKey();
    }
}
