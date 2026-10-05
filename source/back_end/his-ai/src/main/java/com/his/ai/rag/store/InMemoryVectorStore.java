package com.his.ai.rag.store;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存向量库（开发环境方案）。
 *
 * <p>向量不落库：服务启动时由 {@code KnowledgeStoreService.rebuild()} 从
 * {@code sys_knowledge_chunk} 全量读出、调用 {@link com.his.ai.rag.embedding.EmbeddingProvider}
 * 算向量后灌入本库。重启自动重建，零外部依赖。
 *
 * <p>向量为定长稠密 float[]（已 L2 归一），检索直接算点积（= 余弦）。语料在开发/演示量级
 * （千块以内）时线性遍历足够；将来量级上来再换 Milvus。
 */
@Component
public class InMemoryVectorStore implements VectorStore {

    private final Map<Long, Entry> store = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> docIndex = new ConcurrentHashMap<>();

    /**
     * 余弦相似度（两向量均已 L2 归一，点积即余弦）。
     */
    private static double cosine(float[] a, float[] b) {
        if (a.length != b.length) {
            return 0;
        }
        double dot = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
        }
        return dot;
    }

    @Override
    public void upsert(long chunkId, long docId, String content, String docTitle,
                       String category, float[] vector) {
        store.put(chunkId, new Entry(chunkId, docId, content, docTitle, category, vector));
        docIndex.computeIfAbsent(docId, k -> ConcurrentHashMap.newKeySet()).add(chunkId);
    }

    @Override
    public void removeByDocId(long docId) {
        Set<Long> ids = docIndex.remove(docId);
        if (ids != null) {
            ids.forEach(store::remove);
        }
    }

    @Override
    public void clear() {
        store.clear();
        docIndex.clear();
    }

    @Override
    public List<RetrievedChunk> search(float[] queryVector, int topK) {
        if (queryVector == null || queryVector.length == 0) {
            return List.of();
        }
        List<RetrievedChunk> scored = new ArrayList<>();
        for (Entry e : store.values()) {
            double score = cosine(queryVector, e.vector());
            if (score > 0) {
                scored.add(new RetrievedChunk(e.chunkId(), e.docId(), e.content(),
                        e.docTitle(), e.category(), score));
            }
        }
        scored.sort((a, b) -> Double.compare(b.score(), a.score()));
        return scored.size() > topK ? scored.subList(0, topK) : scored;
    }

    private record Entry(long chunkId, long docId, String content, String docTitle,
                         String category, float[] vector) {
    }
}
