package com.his.ai.rag.embedding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 按 {@code ai.rag.embedding-provider} 选择具体 embedding 实现。
 *
 * <p>所有 {@link EmbeddingProvider} 实现注册进同一份 registry，根据 name 路由。
 * 未知 / 空 name 回落 {@code local-tf}（零依赖、必可用）。
 */
@Slf4j
@Component
public class EmbeddingProviderSelector {

    private final Map<String, EmbeddingProvider> registry = new HashMap<>();

    public EmbeddingProviderSelector(List<EmbeddingProvider> providers) {
        for (EmbeddingProvider p : providers) {
            registry.put(p.name(), p);
        }
    }

    /**
     * 选择实现；name 为空或未知时回落 local-tf。
     */
    public EmbeddingProvider select(String name) {
        EmbeddingProvider p = registry.get(name);
        if (p == null) {
            p = registry.get("local-tf");
        }
        return p;
    }
}
