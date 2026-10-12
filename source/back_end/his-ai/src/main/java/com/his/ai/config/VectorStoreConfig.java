package com.his.ai.config;

import com.his.ai.rag.store.InMemoryVectorStore;
import com.his.ai.rag.store.MilvusVectorStore;
import com.his.ai.rag.store.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量库装配：his.ai.rag.vector-store 决定实现。
 * <ul>
 *   <li>{@code milvus}：Milvus 持久化向量库（重启不丢，推荐）</li>
 *   <li>{@code memory}（默认）：进程内存，重启丢失，靠 doc 全文懒加载重建</li>
 * </ul>
 */
@Configuration
public class VectorStoreConfig {

    @Bean
    @ConditionalOnProperty(prefix = "his.ai.rag", name = "vector-store", havingValue = "milvus")
    public VectorStore milvusVectorStore(AiProperties properties) {
        return new MilvusVectorStore(properties.getRag().getMilvus());
    }

    @Bean
    @ConditionalOnProperty(prefix = "his.ai.rag", name = "vector-store", havingValue = "memory", matchIfMissing = true)
    public VectorStore memoryVectorStore() {
        return new InMemoryVectorStore();
    }
}
