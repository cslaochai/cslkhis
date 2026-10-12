package com.his.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.ai.dto.KnowledgeDocQueryPageDTO;
import com.his.ai.dto.KnowledgeIngestDTO;
import com.his.ai.entity.SysKnowledgeDoc;
import com.his.ai.vo.KnowledgeDocListVO;
import com.his.ai.vo.KnowledgeDocVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库存储服务：文档维护 + 切块建索引（Milvus 持久化）+ 懒加载就绪 + 内置语料种子。
 * 切块事实源为 Milvus 向量库；文档全文留档于 sys_knowledge_doc.content（重建/留档用）。
 */
public interface KnowledgeStoreService {

    /**
     * 懒加载：确保向量索引就绪（启动不访问向量库，首次使用时按需构建一次；
     * Milvus 已有数据则跳过，空库则从 doc 全文重新切分灌入）。
     */
    void ensureIndexLoaded();

    /**
     * 手工录入（DTO → 实体组装在 service，Controller 不碰 entity）。
     */
    Long ingestFromDto(KnowledgeIngestDTO dto);

    /**
     * 上传知识文件（pdf / docx / txt / md）：自动解析纯文本 → 切块 → 灌入 Milvus。
     * 返回不含原文的列表 VO，避免大文本回传。
     */
    KnowledgeDocListVO upload(MultipartFile file, String title, String category,
                              String version, String sourceName);

    /**
     * 录入/更新文档：保存全文 → 切块 → 灌入 Milvus（幂等，同文档重录覆盖旧向量）。
     */
    Long ingest(SysKnowledgeDoc doc);

    /**
     * 录入/更新文档（带入库进度回调）：切块与向量入库期间以 0-100 回调进度，用于异步导入任务展示。
     */
    Long ingest(SysKnowledgeDoc doc, java.util.function.IntConsumer progressCallback);

    /**
     * 分页列表。
     */
    IPage<KnowledgeDocListVO> listPage(KnowledgeDocQueryPageDTO dto);

    /**
     * 详情（含原文）。
     */
    KnowledgeDocVO getById(Long id);

    /**
     * 删除文档（同步删除 Milvus 切块向量）。
     */
    void deleteById(Long id);

    /**
     * 从 doc 全文重新切分并全量重建 Milvus 向量索引。
     */
    void rebuild();

    /**
     * 若知识库为空且开启自动种子，灌入内置示例语料。返回灌入篇数。
     */
    int seedIfEmpty();
}
