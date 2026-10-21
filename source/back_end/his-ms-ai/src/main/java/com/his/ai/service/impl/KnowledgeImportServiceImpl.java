package com.his.ai.service.impl;

import com.his.ai.entity.SysKnowledgeDoc;
import com.his.ai.rag.parse.DocumentParser;
import com.his.ai.service.KnowledgeImportService;
import com.his.ai.service.KnowledgeStoreService;
import com.his.ai.vo.KnowledgeImportTaskVO;
import com.his.common.enums.KnowledgeDocChangeTypeEnum;
import com.his.common.enums.KnowledgeDocSourceTypeEnum;
import com.his.common.enums.KnowledgeImportTaskStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.entity.CurrentUser;
import com.his.common.utils.UserUtils;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 知识库文件导入任务实现。
 * <p>任务状态机：1-解析中 → 2-待确认 →（commit）→ 3-入库中 → 4-完成；异常 → 5-失败；
 * 仅 1/2 可取消 → 6-已取消。
 * <p>解析（本地 CPU）异步执行；确认入库只保存全文不建索引（切块由列表页「重建索引」逐个触发），
 * 任务表为进程内存态——导入是一次性过程，应用重启后任务失效，前端轮询不到时提示重传即可。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeImportServiceImpl implements KnowledgeImportService {

    /** 解析预览截断长度（预览区只展示前段，全文保存在任务内存供确认入库） */
    private static final int PREVIEW_LENGTH = 3000;

    private final DocumentParser documentParser;
    private final KnowledgeStoreService knowledgeStoreService;

    private final Map<Long, ImportTask> tasks = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1000);

    private final ExecutorService parseExecutor =
            Executors.newFixedThreadPool(2, namedThreadFactory("knowledge-parse"));
    private final ExecutorService commitExecutor =
            Executors.newFixedThreadPool(2, namedThreadFactory("knowledge-commit"));

    @PreDestroy
    public void shutdown() {
        parseExecutor.shutdownNow();
        commitExecutor.shutdownNow();
    }

    private static ThreadFactory namedThreadFactory(String prefix) {
        AtomicInteger seq = new AtomicInteger(1);
        return r -> {
            Thread t = new Thread(r, prefix + "-" + seq.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
    }

    @Override
    public List<KnowledgeImportTaskVO> submitBatch(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        List<KnowledgeImportTaskVO> out = new ArrayList<>(files.size());
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            ImportTask task = new ImportTask();
            task.id = idGen.incrementAndGet();
            task.fileName = file.getOriginalFilename();
            task.status = KnowledgeImportTaskStatusEnum.PARSING.getCode();
            tasks.put(task.id, task);
            parseExecutor.execute(() -> doParse(task, file));
            out.add(toVo(task));
        }
        if (out.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        return out;
    }

    private void doParse(ImportTask task, MultipartFile file) {
        try {
            String text = documentParser.parse(file.getOriginalFilename(), file.getBytes());
            if (task.status == KnowledgeImportTaskStatusEnum.CANCELLED.getCode()) {
                return;
            }
            task.fullText = text;
            String name = file.getOriginalFilename();
            int dot = name == null ? -1 : name.lastIndexOf('.');
            task.title = (name != null && dot > 0) ? name.substring(0, dot) : name;
            task.category = task.title;
            task.progress = 100;
            task.status = KnowledgeImportTaskStatusEnum.PENDING_CONFIRM.getCode();
        } catch (Exception ex) {
            task.status = KnowledgeImportTaskStatusEnum.FAILED.getCode();
            task.errorMsg = ex.getMessage();
            log.warn("[RAG] 导入任务 {} 解析失败：{}", task.id, ex.getMessage());
        }
    }

    @Override
    public List<KnowledgeImportTaskVO> listByIds(List<Long> ids) {
        List<KnowledgeImportTaskVO> out = new ArrayList<>();
        for (Long id : ids) {
            ImportTask task = tasks.get(id);
            if (task != null) {
                out.add(toVo(task));
            }
        }
        return out;
    }

    @Override
    public KnowledgeImportTaskVO commit(Long id, String title, String category,
                                        String version, String sourceName) {
        ImportTask task = tasks.get(id);
        if (task == null) {
            throw new BusinessException("导入任务不存在或已过期，请重新上传");
        }
        synchronized (task) {
            if (task.status != KnowledgeImportTaskStatusEnum.PENDING_CONFIRM.getCode()) {
                throw new BusinessException("任务当前状态不可确认入库（仅解析完成后可提交）");
            }
            // 元数据由 DTO 校验必填（@NotBlank），此处仅 trim 与回填
            task.title = title.trim();
            task.category = category.trim();
            task.version = version.trim();
            task.sourceName = sourceName.trim();
            task.status = KnowledgeImportTaskStatusEnum.COMMITTING.getCode();
            task.progress = 0;
            // 操作人随任务带走（commit 在 web 线程，SecurityContext 可用；doCommit 在异步线程拿不到）
            CurrentUser user = UserUtils.getCurrentUser();
            if (user == null) {
                throw new BusinessException("当前用户信息不存在");
            }
            task.operatorName = user.getRealName();
            task.operatorId = user.getEmployeeId();
        }
        commitExecutor.execute(() -> doCommit(task));
        return toVo(task);
    }

    private void doCommit(ImportTask task) {
        try {
            SysKnowledgeDoc doc = new SysKnowledgeDoc();
            doc.setTitle(task.title);
            doc.setCategory(task.category);
            doc.setVersion(task.version);
            doc.setSourceName(task.sourceName);
            doc.setContent(task.fullText);
            doc.setSourceType(KnowledgeDocSourceTypeEnum.FILE_IMPORT.getCode());
            // 只保存不建索引（切块由列表页「重建索引」逐个触发），留一条新建历史
            knowledgeStoreService.ingest(doc, p -> task.progress = p);
            knowledgeStoreService.recordHistory(doc, KnowledgeDocChangeTypeEnum.CREATE.getCode(),
                    task.operatorName, task.operatorId);
            task.status = KnowledgeImportTaskStatusEnum.DONE.getCode();
            log.info("[RAG] 导入任务 {}（{}）已保存（未建索引，待手动重建）", task.id, task.title);
        } catch (Exception ex) {
            task.status = KnowledgeImportTaskStatusEnum.FAILED.getCode();
            task.errorMsg = ex.getMessage();
            log.error("[RAG] 导入任务 {} 入库失败：{}", task.id, ex.getMessage());
        }
    }

    @Override
    public void cancel(Long id) {
        ImportTask task = tasks.get(id);
        if (task == null) {
            return;
        }
        synchronized (task) {
            if (task.status == KnowledgeImportTaskStatusEnum.PARSING.getCode()
                    || task.status == KnowledgeImportTaskStatusEnum.PENDING_CONFIRM.getCode()) {
                task.status = KnowledgeImportTaskStatusEnum.CANCELLED.getCode();
            }
        }
    }

    private KnowledgeImportTaskVO toVo(ImportTask task) {
        if (task == null) {
            return null;
        }
        KnowledgeImportTaskVO v = new KnowledgeImportTaskVO();
        v.setId(task.id);
        v.setFileName(task.fileName);
        v.setStatus(task.status);
        v.setProgress(task.status == KnowledgeImportTaskStatusEnum.PENDING_CONFIRM.getCode() ? 100 : task.progress);
        v.setTitle(task.title);
        v.setCategory(task.category);
        v.setVersion(task.version);
        v.setSourceName(task.sourceName);
        if (task.fullText != null) {
            v.setContentLength(task.fullText.length());
            v.setContentPreview(task.fullText.length() > PREVIEW_LENGTH
                    ? task.fullText.substring(0, PREVIEW_LENGTH) : task.fullText);
        }
        v.setErrorMsg(task.errorMsg);
        return v;
    }

    // ---- 任务内部模型 ----

    /** 导入任务（内存态；解析全文保存在此，供预览与确认入库） */
    private static class ImportTask {
        private long id;
        private String fileName;
        private volatile int status;
        private volatile Integer progress;
        private volatile String title;
        private volatile String category;
        private volatile String version;
        private volatile String sourceName;
        private volatile String fullText;
        private volatile String errorMsg;
        /** 操作人（commit 时从 SecurityContext 捕获，供留痕；异步线程里拿不到） */
        private volatile String operatorName;
        private volatile Long operatorId;
    }
}
