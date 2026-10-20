package com.his.ai.rag.embedding;

/**
 * 文本向量化接口。
 */
public interface EmbeddingProvider {

    /**
     * 实现名（用于配置选择，如 local-tf / remote）。
     */
    String name();

    /**
     * 将文本转为定长稠密向量（已 L2 归一）。空文本返回零向量。
     */
    float[] embed(String text);
}
