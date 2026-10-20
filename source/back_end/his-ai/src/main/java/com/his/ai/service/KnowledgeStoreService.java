package com.his.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.ai.dto.KnowledgeDocPageQueryDTO;
import com.his.ai.dto.KnowledgeIngestDTO;
import com.his.ai.entity.SysKnowledgeDoc;
import com.his.ai.vo.KnowledgeChunkPreviewVO;
import com.his.ai.vo.KnowledgeDocHistoryVO;
import com.his.ai.vo.KnowledgeDocListVO;
import com.his.ai.vo.KnowledgeDocVO;
import com.his.ai.vo.KnowledgeRebuildTaskVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库存储服务：文档维护（保存/编辑/停用/删除）+ 单文档重建索引 + 切块预览 + 历史版本留痕。
 * <p>「保存」与「建索引」解耦：录入/编辑只落库（更新时清掉旧向量避免检索命中旧内容，chunk_count 置 0），
 * 索引由列表页「重建索引」逐个触发并带进度；全文留档于 sys_knowledge_doc.content（重建/留档用）。
 */
public interface KnowledgeStoreService {

    /**
     * 懒加载：确保向量索引就绪（启动不访问向量库，首次使用时按需构建一次；
     * Milvus 已有数据则跳过，空库则从 doc 全文重新切分灌入）。
     */
    void ensureIndexLoaded();

    /**
     * 手工录入/编辑（DTO → 实体组装在 service，Controller 不碰 entity）。
     * 只保存不建索引；更新时清旧向量；每次操作写一条历史快照（新建/编辑）。
     */
    Long ingestFromDto(KnowledgeIngestDTO dto);

    /**
     * 上传知识文件（pdf / docx / txt / md）：自动解析纯文本 → 只保存（不建索引，由「重建索引」触发）。
     * 返回不含原文的列表 VO，避免大文本回传。
     */
    KnowledgeDocListVO upload(MultipartFile file, String title, String category,
                              String version, String sourceName);

    /**
     * 保存文档（只落库不建索引）：新增直接 insert；修改按新字段 update 并清掉旧向量、chunk_count 置 0。
     */
    Long ingest(SysKnowledgeDoc doc);

    /**
     * 保存文档（带进度回调，供异步导入任务展示；保存为本地落库，回调立即 100）。
     */
    Long ingest(SysKnowledgeDoc doc, java.util.function.IntConsumer progressCallback);

    /**
     * 分页列表。
     */
    IPage<KnowledgeDocListVO> listPage(KnowledgeDocPageQueryDTO dto);

    /**
     * 详情（含原文）。
     */
    KnowledgeDocVO getById(Long id);

    /**
     * 删除文档（逻辑删 + 同步删除 Milvus 切块向量；历史版本保留）。
     */
    void deleteById(Long id);

    /**
     * 从 doc 全文重新切分并全量重建 Milvus 向量索引（懒加载自举 / 灾难恢复用，前端不暴露入口）。
     */
    void rebuild();

    /**
     * 若知识库为空且开启自动种子，灌入内置示例语料（保存并同步建索引）。返回灌入篇数。
     */
    int seedIfEmpty();

    /**
     * 提交单文档重建索引任务，返回任务 ID（异步执行，进度经 {@link #getRebuildProgress} 轮询）。
     */
    Long submitRebuild(Long docId);

    /**
     * 查询重建索引任务进度（status 1-进行中 2-完成 3-失败；progress 0-100，chunk 级精确进度）。
     */
    KnowledgeRebuildTaskVO getRebuildProgress(Long taskId);

    /**
     * 切块预览（只按当前切块参数计算，不落库不灌向量）：返回块数与每块开头预览。
     */
    KnowledgeChunkPreviewVO previewChunks(String content);

    /**
     * 停用/启用文档（status 0-正常 1-停用；停用不影响已建索引，仅检索侧按状态过滤）。
     */
    void updateStatus(Long id, Integer status);

    /**
     * 文档历史版本列表（按操作时间倒序）。
     */
    List<KnowledgeDocHistoryVO> listHistory(Long docId);

    /**
     * 回退到指定历史版本：快照写回当前文档并清旧向量（chunk_count 置 0，需重建索引），
     * 回退本身再落一条历史（change_type=3）。返回文档 ID。
     */
    Long revertHistory(Long historyId);

    /**
     * 写一条文档历史快照（留痕）。操作人可由调用方传入（异步线程 SecurityContext 丢失时）。
     */
    void recordHistory(SysKnowledgeDoc doc, int changeType, String operatorName, Long operatorId);
}
