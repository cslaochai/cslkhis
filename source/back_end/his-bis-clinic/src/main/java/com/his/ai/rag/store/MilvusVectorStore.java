package com.his.ai.rag.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.his.ai.config.AiProperties;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.*;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Milvus 向量库实现
 */
@Slf4j
public class MilvusVectorStore implements VectorStore {

    /**
     * 报错消息里原始异常文本的上限（gRPC Status 带一堆堆栈行，够定位即可，别灌进响应体）
     */
    private static final int CAUSE_MAX_LENGTH = 300;

    private final AiProperties.Milvus cfg;
    /**
     * 懒建的客户端；创建失败保持 null，下次操作重试
     */
    private volatile MilvusClientV2 client;
    /**
     * 集合是否已确保存在（幂等标记，避免每次操作都探活）
     */
    private final AtomicBoolean collectionReady = new AtomicBoolean(false);

    public MilvusVectorStore(AiProperties.Milvus cfg) {
        this.cfg = cfg;
    }

    private MilvusClientV2 client() {
        MilvusClientV2 current = client;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (client == null) {
                try {
                    client = new MilvusClientV2(ConnectConfig.builder()
                            .uri(cfg.getUri())
                            .token(cfg.getToken())
                            .build());
                } catch (Exception ex) {
                    // 不缓存失败的客户端：Milvus 起来后下一次操作能连上
                    throw unavailable("建连", ex);
                }
            }
            return client;
        }
    }

    /**
     * 把 SDK/gRPC 的技术异常收成一句人话，让 AI 功能的调用方拿到「连不上」而不是「系统内部错误」
     */
    private BusinessException unavailable(String action, Exception ex) {
        String message = "向量库（Milvus）不可用，" + action + "失败：" + TextUtil.cut(ex.getMessage(), CAUSE_MAX_LENGTH);
        return new BusinessException(message);
    }

    /**
     * 有返回值的 Milvus 调用出口：连接层与 SDK 抛出的异常统一转成业务错误
     */
    private <T> T call(String action, Supplier<T> body) {
        try {
            return body.get();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw unavailable(action, ex);
        }
    }

    /**
     * SDK 的 createCollection/loadCollection/delete 是 void，走这条同样的异常口径
     */
    private void run(String action, Runnable body) {
        call(action, () -> {
            body.run();
            return null;
        });
    }

    private void ensureCollection() {
        if (collectionReady.get()) {
            return;
        }
        synchronized (this) {
            if (collectionReady.get()) {
                return;
            }
            MilvusClientV2 milvusClient = client();
            boolean exists = call("探测集合", () -> milvusClient.hasCollection(HasCollectionReq.builder()
                    .collectionName(cfg.getCollectionName())
                    .build()));
            if (!exists) {
                createCollection(milvusClient);
            } else {
                loadCollection(milvusClient);
            }
            collectionReady.set(true);
        }
    }

    private void createCollection(MilvusClientV2 milvusClient) {
        CreateCollectionReq.CollectionSchema schema = milvusClient.createSchema();
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

        run("创建集合", () -> milvusClient.createCollection(CreateCollectionReq.builder()
                .collectionName(cfg.getCollectionName())
                .collectionSchema(schema)
                .indexParams(List.of(index))
                .build()));
        loadCollection(milvusClient);
        log.info("[RAG] Milvus 集合 {} 已创建（向量维度 {}）", cfg.getCollectionName(), cfg.getVectorDim());
    }

    private void loadCollection(MilvusClientV2 milvusClient) {
        run("加载集合", () -> milvusClient.loadCollection(LoadCollectionReq.builder()
                .collectionName(cfg.getCollectionName())
                .build()));
    }

    @Override
    public void ensureReady() {
        ensureCollection();
    }

    @Override
    public boolean hasData() {
        try {
            QueryResp resp = client().query(QueryReq.builder()
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
            // 探测只用于决定「要不要从全文重建」，失败按无数据处理，不做报错的源头
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
        run("写入切块向量", () -> client().upsert(UpsertReq.builder()
                .collectionName(cfg.getCollectionName())
                .data(List.of(row))
                .build()));
    }

    @Override
    public void removeByDocId(long docId) {
        ensureCollection();
        run("删除切块", () -> client().delete(DeleteReq.builder()
                .collectionName(cfg.getCollectionName())
                .filter("doc_id == " + docId)
                .build()));
    }

    @Override
    public void clear() {
        // Milvus 无清空语义：删集合重建（rebuild 前先清后灌）
        try {
            client().dropCollection(DropCollectionReq.builder()
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
        SearchResp resp = call("检索", () -> client().search(SearchReq.builder()
                .collectionName(cfg.getCollectionName())
                .annsField("embedding")
                .data(List.of(new FloatVec(queryVector)))
                .topK(topK)
                .outputFields(List.of("chunk_id", "doc_id", "doc_title", "category",
                        "doc_version", "doc_source_name", "content"))
                // 强一致：ingest/rebuild 完成后立即问答也要能读到刚写入的数据
                .consistencyLevel(ConsistencyLevel.STRONG)
                .build()));
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
