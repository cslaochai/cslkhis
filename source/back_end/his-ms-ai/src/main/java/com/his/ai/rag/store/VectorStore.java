package com.his.ai.rag.store;

import java.util.List;

/**
 * 向量库接口。
 */
public interface VectorStore {

    /**
     * 写入 / 更新一条切块向量。
     */
    void upsert(long chunkId, long docId, String content, String docTitle,
                String category, String docVersion, String docSourceName, float[] vector);

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

    /**
     * 确保索引就绪（幂等）：内存实现为空操作；Milvus 实现确保集合存在（不存在则创建）。
     */
    default void ensureReady() {
    }
}
