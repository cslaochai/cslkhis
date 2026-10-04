package com.his.ai.rag.embedding;

/**
 * 文本向量化接口。
 *
 * <p>统一返回<b>定长稠密向量</b>（已 L2 归一），这样无论本地还是远程实现、无论内存还是 Milvus
 * 存储，向量形态一致，切换只改实现、不动调用方。
 *
 * <p>开发环境默认实现 {@link LocalTfEmbeddingProvider}（hashing trick 落到固定维度，零外部依赖）。
 * 若院内部署了 Ollama / TEI / vLLM 等 embedding 服务，新增 {@link RemoteEmbeddingProvider}
 * 并在 {@code application.yml: ai.rag.embedding-provider} 切到 {@code remote} 即可用语义向量。
 *
 * <p>语义对齐（同义词、药品别名）不靠向量——那走结构化知识表；向量只负责「字面/语义相近的片段聚到一起」。
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
