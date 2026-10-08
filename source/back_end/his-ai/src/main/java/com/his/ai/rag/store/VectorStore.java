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
