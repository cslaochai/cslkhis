package com.his.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.ai.dto.KnowledgeDocQueryPageDTO;
import com.his.ai.entity.SysKnowledgeDoc;
import com.his.ai.vo.KnowledgeDocListVO;
import com.his.ai.vo.KnowledgeDocVO;

/**
 * 知识库存储服务：文档维护 + 切块建索引 + 启动时重建 + 内置语料种子。
 */
public interface KnowledgeStoreService {

    /**
     * 录入/更新文档：保存全文 → 切块 → 落 chunk 表 → 更新内存向量索引（幂等）。
     */
    Long ingest(SysKnowledgeDoc doc);

    /**
     * 分页列表。
     */
    IPage<KnowledgeDocListVO> listPage(KnowledgeDocQueryPageDTO dto);

    /**
     * 详情（含原文）。
     */
    KnowledgeDocVO getById(Long id);

    /**
     * 删除文档（同步删除切块与内存索引）。
     */
    void deleteById(Long id);

    /**
     * 从 chunk 表全量重建内存向量索引。
     */
    void rebuild();

    /**
     * 若知识库为空且开启自动种子，灌入内置示例语料。返回灌入篇数。
     */
    int seedIfEmpty();
}
