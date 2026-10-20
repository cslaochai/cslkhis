package com.his.ai.service;

import com.his.ai.dto.KnowledgeAskDTO;
import com.his.ai.vo.KnowledgeAskVO;

/**
 * 知识库问答能力（RAG）。
 */
public interface KnowledgeQaCapability {

    KnowledgeAskVO ask(KnowledgeAskDTO dto);
}
