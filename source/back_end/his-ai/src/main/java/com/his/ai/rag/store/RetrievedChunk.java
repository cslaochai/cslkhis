package com.his.ai.rag.store;

/**
 * 检索命中结果。
 */
public record RetrievedChunk(long chunkId, long docId, String content,
                             String docTitle, String category, double score) {
}
