package com.his.ai.rag.store;

import java.util.List;

/**
 * 向量库接口。
 *
 * <p>开发环境实现 {@link InMemoryVectorStore}（内存 + 启动时从 chunk 表重建）。
 * 向量形态是定长稠密 float[]，因此将来接 Milvus（存 {@code FloatVector}）只需新增一个实现，
 * {@link com.his.ai.rag.embedding.EmbeddingProvider} 与检索调用方完全不变。
 */
public interface VectorStore {

    /**
     * 写入 / 更新一条切块向量。
     */
    void upsert(long chunkId, long docId, String content, String docTitle,
                String category, float[] vector);

    /**
     * 删除某文档下的全部切块（文档重新切块或删除时调用）。
     */
    void removeByDocId(long docId);

    /**
     * 清空整个索引（rebuild 时先清后灌）。
     */
    void clear();

    /**
     * 按余弦相似度召回 top-k。
     */
    List<RetrievedChunk> search(float[] queryVector, int topK);
}
