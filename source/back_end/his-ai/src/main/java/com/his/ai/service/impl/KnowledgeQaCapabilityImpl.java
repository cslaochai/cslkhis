package com.his.ai.service.impl;

import com.his.ai.config.AiProperties;
import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.KnowledgeAskDTO;
import com.his.ai.dto.KnowledgeQaLlmOutputDTO;
import com.his.ai.rag.embedding.EmbeddingProviderSelector;
import com.his.ai.rag.store.RetrievedChunk;
import com.his.ai.rag.store.VectorStore;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.KnowledgeQaCapability;
import com.his.ai.service.KnowledgeStoreService;
import com.his.ai.vo.KnowledgeAskVO;
import com.his.ai.vo.KnowledgeQaPromptVariablesVO;
import com.his.ai.vo.KnowledgeSourceVO;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 知识库问答能力实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeQaCapabilityImpl implements KnowledgeQaCapability {

    private static final String TEMPLATE = "knowledge-qa";
    private static final String BIZ_TYPE = "knowledge";
    private static final int SNIPPET_MAX = 160;
    private static final int OUTPUT_TOKEN_LIMIT = 1024;

    private final EmbeddingProviderSelector embeddingSelector;
    private final VectorStore vectorStore;
    private final AiExecutionService aiExecutionService;
    private final AiProperties aiProperties;
    private final KnowledgeStoreService knowledgeStoreService;

    @Override
    public KnowledgeAskVO ask(KnowledgeAskDTO dto) {
        long start = System.currentTimeMillis();
        // 懒加载：索引未构建时先全量载入一次，保证首次问答可检索
        knowledgeStoreService.ensureIndexLoaded();
        KnowledgeAskVO vo = new KnowledgeAskVO();
        vo.setQuestion(dto.getQuestion());

        float[] queryVector = embeddingSelector.select()
                .embed(dto.getQuestion());
        List<RetrievedChunk> hits = vectorStore.search(queryVector, aiProperties.getRag().getTopK());

        if (hits.isEmpty()) {
            vo.setDegraded(true);
            vo.setDegradeReason("未检索到相关知识片段，请先录入知识文档或执行 rebuild");
            vo.setAnswer("知识库暂时没有收录相关内容，无法回答该问题。");
            vo.setSources(List.of());
            vo.setLatencyMs(System.currentTimeMillis() - start);
            return vo;
        }

        StringBuilder context = new StringBuilder();
        List<KnowledgeSourceVO> sources = new ArrayList<>();
        int i = 1;
        for (RetrievedChunk h : hits) {
            String sourceLabel = TextUtil.hasText(h.docSourceName()) ? h.docSourceName() : h.category();
            context.append("【材料").append(i).append("】来源：").append(h.docTitle())
                    .append("（").append(sourceLabel);
            if (TextUtil.hasText(h.docVersion())) {
                context.append("，版本 ").append(h.docVersion());
            }
            context.append("）\n").append(h.content()).append("\n\n");
            KnowledgeSourceVO s = new KnowledgeSourceVO();
            s.setDocId(h.docId());
            s.setDocTitle(h.docTitle());
            s.setCategory(h.category());
            s.setDocVersion(h.docVersion());
            s.setDocSourceName(h.docSourceName());
            s.setSnippet(TextUtil.ellipsis(h.content(), SNIPPET_MAX));
            sources.add(s);
            i++;
        }
        vo.setSources(sources);

        KnowledgeQaPromptVariablesVO variables = new KnowledgeQaPromptVariablesVO();
        variables.setQuestion(dto.getQuestion());
        variables.setContext(context.toString());

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.KNOWLEDGE_QA)
                .templateName(TEMPLATE)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .inputDigest(dto.getQuestion())
                .maxTokens(OUTPUT_TOKEN_LIMIT)
                .build();

        Optional<KnowledgeQaLlmOutputDTO> out =
                aiExecutionService.call(call, KnowledgeQaLlmOutputDTO.class);

        if (out.isPresent() && TextUtil.hasText(out.get().getAnswer())) {
            vo.setAnswer(out.get().getAnswer());
            vo.setDegraded(false);
        } else {
            // 降级：直接返回检索到的原文片段（确定性，不依赖模型）
            vo.setDegraded(true);
            vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.KNOWLEDGE_QA));
            StringBuilder fallback = new StringBuilder("（以下为知识库检索到的相关原文，模型未参与）\n\n");
            for (RetrievedChunk h : hits) {
                String sourceLabel = TextUtil.hasText(h.docSourceName()) ? h.docSourceName() : h.category();
                fallback.append("· ").append(h.docTitle());
                if (TextUtil.hasText(sourceLabel) || TextUtil.hasText(h.docVersion())) {
                    fallback.append("（");
                    if (TextUtil.hasText(sourceLabel)) {
                        fallback.append(sourceLabel);
                    }
                    if (TextUtil.hasText(h.docVersion())) {
                        fallback.append("，版本 ").append(h.docVersion());
                    }
                    fallback.append("）");
                }
                fallback.append("：").append(h.content()).append("\n\n");
            }
            vo.setAnswer(fallback.toString().trim());
        }
        vo.setLatencyMs(System.currentTimeMillis() - start);
        return vo;
    }
}
