package com.his.ai.service;

import com.his.ai.dto.KnowledgeAskDTO;
import com.his.ai.vo.KnowledgeAskVO;

/**
 * 知识库问答能力（RAG）。
 *
 * <p>纯检索增强生成：召回 top-k 片段 → 拼进提示词 → 模型基于真实文档作答。
 * <b>只做解释与科普，不调用任何医疗判定。</b>模型不可用时降级为「返回检索到的原文片段」。
 */
public interface KnowledgeQaCapability {

    KnowledgeAskVO ask(KnowledgeAskDTO dto);
}
