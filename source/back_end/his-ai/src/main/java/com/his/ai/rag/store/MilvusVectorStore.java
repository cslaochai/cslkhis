package com.his.ai.rag.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.his.ai.config.AiProperties;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Milvus 向量库实现（vector-store=milvus 时装配）。
 * 切块的文本 + 向量 + 元数据整体持久化到 Milvus，重启不丢，无需从 doc 全文重建。
 *
 * <p>集合结构（his_knowledge_chunk）：
 * 主键 chunk_id(Int64)，标量 doc_id / doc_title / category / doc_version / doc_source_name / content，
 * 向量 embedding(FloatVector, dim 与 embedding 输出一致，默认 1024)。
 *
 * <p>连接为懒连接：bean 创建不访问 Milvus，首次操作时建集合；Milvus 不可达时操作抛异常，
 * 由上层 ensureIndexLoaded 的兜底逻辑降级为空索引。
 */
@Slf4j
public class MilvusVectorStore implements VectorStore {

    private final AiProperties.Milvus cfg;
    private final MilvusClientV2 client;
    /** 集合是否已确保存在（幂等标记，避免每次操作都探活） */
    private final AtomicBoolean collectionReady = new AtomicBoolean(false);

    public MilvusVectorStore(AiProperties.Milvus cfg) {
        this.cfg = cfg;
        this.client = new MilvusClientV2(ConnectConfig.builder()
                .uri(cfg.getUri())
                .token(cfg.getToken())
                .build());
    }

    private void ensureCollection() {
        if (collectionReady.get()) {
            return;
        }
        synchronized (this) {
            if (collectionReady.get()) {
                return;
            }
            boolean exists = client.hasCollection(HasCollectionReq.builder()
                    .collectionName(cfg.getCollectionName())
                    .build());
            if (!exists) {
                createCollection();
            } else {
                loadCollection();
            }
            collectionReady.set(true);
        }
    }

    private void createCollection() {
        CreateCollectionReq.CollectionSchema schema = client.createSchema();
        schema.addField(AddFieldReq.builder().fieldName("chunk_id").dataType(DataType.Int64)
                .isPrimaryKey(true).autoID(false).build());
        schema.addField(AddFieldReq.builder().fieldName("doc_id").dataType(DataType.Int64).build());
        schema.addField(AddFieldReq.builder().fieldName("doc_title").dataType(DataType.VarChar).maxLength(200).build());
        schema.addField(AddFieldReq.builder().fieldName("category").dataType(DataType.VarChar).maxLength(50).build());
        schema.addField(AddFieldReq.builder().fieldName("doc_version").dataType(DataType.VarChar).maxLength(50).build());
        schema.addField(AddFieldReq.builder().fieldName("doc_source_name").dataType(DataType.VarChar).maxLength(100).build());
        schema.addField(AddFieldReq.builder().fieldName("content").dataType(DataType.VarChar).maxLength(65535).build());
        schema.addField(AddFieldReq.builder().fieldName("embedding").dataType(DataType.FloatVector)
                .dimension(cfg.getVectorDim()).build());

        IndexParam index = IndexParam.builder()
                .fieldName("embedding")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .metricType(IndexParam.MetricType.COSINE)
                .build();

        client.createCollection(CreateCollectionReq.builder()
                .collectionName(cfg.getCollectionName())
                .collectionSchema(schema)
                .indexParams(List.of(index))
                .build());
        loadCollection();
        log.info("[RAG] Milvus 集合 {} 已创建（向量维度 {}）", cfg.getCollectionName(), cfg.getVectorDim());
    }

    private void loadCollection() {
        client.loadCollection(LoadCollectionReq.builder()
                .collectionName(cfg.getCollectionName())
                .build());
    }

    @Override
    public void ensureReady() {
        ensureCollection();
    }

    @Override
    public boolean hasData() {
        try {
            QueryResp resp = client.query(QueryReq.builder()
                    .collectionName(cfg.getCollectionName())
                    .filter("chunk_id > 0")
                    .outputFields(List.of("count(*)"))
                    .build());
            List<QueryResp.QueryResult> rows = resp.getQueryResults();
            if (rows == null || rows.isEmpty()) {
                return false;
            }
            Object count = rows.get(0).getEntity().get("count(*)");
            return count != null && ((Number) count).longValue() > 0;
        } catch (Exception ex) {
            // 集合不存在/连接异常：视为无数据，由上层决定是否重建
            log.warn("[RAG] Milvus hasData 探测失败：{}", ex.getMessage());
            return false;
        }
    }

    @Override
    public void upsert(long chunkId, long docId, String content, String docTitle,
                       String category, String docVersion, String docSourceName, float[] vector) {
        ensureCollection();
        JsonObject row = new JsonObject();
        row.addProperty("chunk_id", chunkId);
        row.addProperty("doc_id", docId);
        row.addProperty("doc_title", docTitle == null ? "" : docTitle);
        row.addProperty("category", category == null ? "" : category);
        row.addProperty("doc_version", docVersion == null ? "" : docVersion);
        row.addProperty("doc_source_name", docSourceName == null ? "" : docSourceName);
        row.addProperty("content", content == null ? "" : content);
        JsonArray vec = new JsonArray();
        for (float f : vector) {
            vec.add(f);
        }
        row.add("embedding", vec);
        client.upsert(UpsertReq.builder()
                .collectionName(cfg.getCollectionName())
                .data(List.of(row))
                .build());
    }

    @Override
    public void removeByDocId(long docId) {
        ensureCollection();
        client.delete(DeleteReq.builder()
                .collectionName(cfg.getCollectionName())
                .filter("doc_id == " + docId)
                .build());
    }

    @Override
    public void clear() {
        // Milvus 无清空语义：删集合重建（rebuild 前先清后灌）
        try {
            client.dropCollection(DropCollectionReq.builder()
                    .collectionName(cfg.getCollectionName())
                    .build());
        } catch (Exception ex) {
            log.warn("[RAG] Milvus dropCollection 失败（可能不存在）：{}", ex.getMessage());
        }
        collectionReady.set(false);
        ensureCollection();
    }

    @Override
    public List<RetrievedChunk> search(float[] queryVector, int topK) {
        if (queryVector == null || queryVector.length == 0) {
            return List.of();
        }
        ensureCollection();
        SearchResp resp = client.search(SearchReq.builder()
                .collectionName(cfg.getCollectionName())
                .annsField("embedding")
                .data(List.of(new FloatVec(queryVector)))
                .topK(topK)
                .outputFields(List.of("chunk_id", "doc_id", "doc_title", "category",
                        "doc_version", "doc_source_name", "content"))
                // 强一致：ingest/rebuild 完成后立即问答也要能读到刚写入的数据
                .consistencyLevel(ConsistencyLevel.STRONG)
                .build());
        List<RetrievedChunk> out = new ArrayList<>();
        for (List<SearchResp.SearchResult> group : resp.getSearchResults()) {
            for (SearchResp.SearchResult r : group) {
                Float score = r.getScore();
                if (score == null || score <= 0) {
                    continue;
                }
                Map<String, Object> e = r.getEntity();
                out.add(new RetrievedChunk(
                        num(e.get("chunk_id")),
                        num(e.get("doc_id")),
                        str(e.get("content")),
                        str(e.get("doc_title")),
                        str(e.get("category")),
                        str(e.get("doc_version")),
                        str(e.get("doc_source_name")),
                        score));
            }
        }
        // Milvus 已按相似度降序返回，此处兜底排序保证顺序稳定
        out.sort((a, b) -> Double.compare(b.score(), a.score()));
        return out;
    }

    private static long num(Object v) {
        return v instanceof Number n ? n.longValue() : 0L;
    }

    private static String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
