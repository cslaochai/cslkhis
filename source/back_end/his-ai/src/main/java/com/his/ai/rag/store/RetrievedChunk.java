package com.his.ai.rag.store;

/**
 * 检索命中结果。
 *
 * @param chunkId  切块ID
 * @param docId    所属文档ID
 * @param content  切块原文
 * @param docTitle 文档标题
 * @param category 分类
 * @param score    余弦相似度（已 L2 归一）
 */
public record RetrievedChunk(long chunkId, long docId, String content,
                             String docTitle, String category, double score) {
}
