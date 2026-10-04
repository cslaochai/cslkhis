package com.his.ai.service.impl;

import com.his.ai.config.AiProperties;
import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.KnowledgeAskDTO;
import com.his.ai.dto.KnowledgeQaLlmOutputDTO;
import com.his.ai.rag.embedding.EmbeddingProviderSelector;
import com.his.ai.rag.store.InMemoryVectorStore;
import com.his.ai.rag.store.RetrievedChunk;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.KnowledgeQaCapability;
import com.his.ai.vo.KnowledgeAskVO;
import com.his.ai.vo.KnowledgeSourceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 知识库问答能力实现。
 *
 * <p>设计要点（贴合本项目「代码算事实、模型做解释」）：
 * <ul>
 *   <li>召回是确定性的（本地 TF / 远程语义向量余弦），模型只负责把召回片段串成通顺回答；</li>
 *   <li>模型不在候选集之外编造——本能力连候选集都没有，所以硬约束是「只能引用参考材料」；</li>
 *   <li>模型不可用时绝不空手而归：降级为直接返回检索到的原文片段，业务照常可用。</li>
 * </ul>
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
    private final InMemoryVectorStore vectorStore;
    private final AiExecutionService aiExecutionService;
    private final AiProperties aiProperties;

    @Override
    public KnowledgeAskVO ask(KnowledgeAskDTO dto) {
        long start = System.currentTimeMillis();
        KnowledgeAskVO vo = new KnowledgeAskVO();
        vo.setQuestion(dto.getQuestion());

        float[] queryVector = embeddingSelector.select(aiProperties.getRag().getEmbeddingProvider())
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
            context.append("【材料").append(i).append("】来源：").append(h.docTitle())
                    .append("（").append(h.category()).append("）\n").append(h.content()).append("\n\n");
            KnowledgeSourceVO s = new KnowledgeSourceVO();
            s.setDocId(h.docId());
            s.setDocTitle(h.docTitle());
            s.setCategory(h.category());
            s.setSnippet(truncate(h.content(), SNIPPET_MAX));
            sources.add(s);
            i++;
        }
        vo.setSources(sources);

        Map<String, Object> variables = new HashMap<>();
        variables.put("question", dto.getQuestion());
        variables.put("context", context.toString());

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

        if (out.isPresent() && StringUtils.hasText(out.get().getAnswer())) {
            vo.setAnswer(out.get().getAnswer());
            vo.setDegraded(false);
        } else {
            // 降级：直接返回检索到的原文片段（确定性，不依赖模型）
            vo.setDegraded(true);
            vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.KNOWLEDGE_QA));
            StringBuilder fallback = new StringBuilder("（以下为知识库检索到的相关原文，模型未参与）\n\n");
            for (RetrievedChunk h : hits) {
                fallback.append("· ").append(h.docTitle()).append("：").append(h.content()).append("\n\n");
            }
            vo.setAnswer(fallback.toString().trim());
        }
        vo.setLatencyMs(System.currentTimeMillis() - start);
        return vo;
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        String t = text.trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }
}
