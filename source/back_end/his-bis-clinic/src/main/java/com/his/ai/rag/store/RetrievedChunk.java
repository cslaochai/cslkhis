package com.his.ai.rag.store;

/**
 * 检索命中结果（含文档元数据：标题/分类/版本/来源，供问答展示）。
 */
public record RetrievedChunk(long chunkId, long docId, String content,
                             String docTitle, String category, String docVersion,
                             String docSourceName, double score) {
}
