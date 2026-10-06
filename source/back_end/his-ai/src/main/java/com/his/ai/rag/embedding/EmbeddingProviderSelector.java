package com.his.ai.rag.embedding;

import com.his.ai.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选择具体 embedding 实现。
 *
 * <p>所有 {@link EmbeddingProvider} 实现注册进同一份 registry，按「是否配置了远程 embedding 地址」路由：
 * {@code ai.rag.embed-base-url} 有值 → 用 {@code remote}（OpenAI 兼容语义向量）；
 * 未配 → 回落 {@code local-tf}（零依赖、必可用的本地哈希向量）。
 * 未知实现名回落 local-tf。
 */
@Slf4j
@Component
public class EmbeddingProviderSelector {

    private final Map<String, EmbeddingProvider> registry = new HashMap<>();
    private final AiProperties aiProperties;

    public EmbeddingProviderSelector(List<EmbeddingProvider> providers, AiProperties aiProperties) {
        for (EmbeddingProvider p : providers) {
            registry.put(p.name(), p);
        }
        this.aiProperties = aiProperties;
    }

    /**
     * 选择实现：配了 embed-base-url 用 remote，否则 local-tf。
     */
    public EmbeddingProvider select() {
        String name = StringUtils.hasText(aiProperties.getRag().getEmbedBaseUrl()) ? "remote" : "local-tf";
        EmbeddingProvider p = registry.get(name);
        if (p == null) {
            p = registry.get("local-tf");
        }
        return p;
    }
}
