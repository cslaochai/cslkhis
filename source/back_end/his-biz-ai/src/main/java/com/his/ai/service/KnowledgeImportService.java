package com.his.ai.service;

import com.his.ai.vo.KnowledgeImportTaskVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库文件导入任务：批量上传 → 异步解析（可查进度）→ 预览确认 → 异步切块入库（带进度）。
 * 任务为进程内存态（一次性的导入过程，重启失效后前端重传即可）。
 */
public interface KnowledgeImportService {

    /**
     * 批量提交文件，创建导入任务并异步开始解析。返回任务列表（初始均为解析中）。
     */
    List<KnowledgeImportTaskVO> submitBatch(List<MultipartFile> files);

    /**
     * 批量查询任务状态（前端轮询用）。不存在的任务直接跳过。
     */
    List<KnowledgeImportTaskVO> listByIds(List<Long> ids);

    /**
     * 确认入库：更新元数据后异步切块 + embedding + 灌 Milvus（带进度）。仅待确认状态可提交。
     */
    KnowledgeImportTaskVO commit(Long id, String title, String category, String version, String sourceName);

    /**
     * 取消任务（仅解析中 / 待确认可取消；入库中与已完成不可取消）。
     */
    void cancel(Long id);
}
