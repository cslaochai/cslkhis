package com.his.validator;

import com.his.ai.rag.store.VectorStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动阶段向量库探活。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorStoreValidator implements ApplicationRunner {

    private final VectorStore vectorStore;

    @Override
    public void run(ApplicationArguments args) {
        try {
            vectorStore.ensureReady();
            log.info("[RAG] 向量库 {} 就绪", vectorStore.getClass().getSimpleName());
        } catch (Exception ex) {
            log.warn("[RAG] 向量库 {} 启动探活失败，AI 知识库功能不可用（不影响其他业务启动）：{}",
                    vectorStore.getClass().getSimpleName(), ex.getMessage());
        }
    }
}
