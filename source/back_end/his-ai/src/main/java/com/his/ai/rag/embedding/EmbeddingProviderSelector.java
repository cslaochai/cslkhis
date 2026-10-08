package com.his.ai.rag.embedding;

import com.his.ai.config.AiProperties;
import com.his.common.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选择具体 embedding 实现。
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
        String name = TextUtil.hasText(aiProperties.getRag().getEmbedBaseUrl()) ? "remote" : "local-tf";
        EmbeddingProvider p = registry.get(name);
        if (p == null) {
            p = registry.get("local-tf");
        }
        return p;
    }
}
