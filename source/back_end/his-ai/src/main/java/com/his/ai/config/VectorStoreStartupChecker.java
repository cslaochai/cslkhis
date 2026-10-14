package com.his.ai.config;

import com.his.ai.rag.store.VectorStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动阶段向量库探活。
 *
 * <p>向量库只服务 AI 知识库，它连不上时挂号/收费等主业务照常，因此这里**只告警不打断启动**
 * （早期实现是在 bean 构造里直连 Milvus，一台没起来的 Milvus 会让整个应用起不来）。
 * 真正的报错发生在 AI 功能访问向量库的那一刻，由 {@link VectorStore} 实现抛业务异常。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorStoreStartupChecker implements ApplicationRunner {

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
