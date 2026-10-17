package com.his.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.ai.config.AiProperties;
import com.his.ai.dto.KnowledgeDocQueryPageDTO;
import com.his.ai.dto.KnowledgeIngestDTO;
import com.his.ai.entity.SysKnowledgeDoc;
import com.his.ai.entity.SysKnowledgeDocHistory;
import com.his.ai.mapper.SysKnowledgeDocHistoryMapper;
import com.his.ai.mapper.SysKnowledgeDocMapper;
import com.his.ai.rag.embedding.EmbeddingProviderSelector;
import com.his.ai.rag.parse.DocumentParser;
import com.his.ai.rag.split.TextSplitter;
import com.his.ai.rag.store.VectorStore;
import com.his.ai.service.KnowledgeStoreService;
import com.his.ai.vo.*;
import com.his.common.enums.KnowledgeDocChangeTypeEnum;
import com.his.common.enums.KnowledgeDocSourceTypeEnum;
import com.his.common.enums.KnowledgeIndexRebuildStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 知识库存储服务实现。
 * <p>「保存」与「建索引」解耦：ingest 只落库（新增 insert；修改 update 并清旧向量、chunk_count 置 0），
 * 索引由 submitRebuild 的单文档重建任务（异步 + 进度轮询）负责；重建任务与导入任务同为进程内存态。
 * 切块事实源为 Milvus 向量库（文本 + 向量 + 元数据整体持久化，重启不丢）；
 * 文档全文留档在 sys_knowledge_doc.content（文本事实源，用于重建与留档）。
 * 索引构建采用懒加载：启动不访问向量库，首次 ask / rebuild / deleteById 时按需就绪一次；
 * Milvus 已有数据则跳过重建，空库则从 doc 全文重新切分灌入。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeStoreServiceImpl implements KnowledgeStoreService {

    private final SysKnowledgeDocMapper sysKnowledgeDocMapper;
    private final SysKnowledgeDocHistoryMapper sysKnowledgeDocHistoryMapper;
    private final EmbeddingProviderSelector embeddingSelector;
    private final TextSplitter textSplitter;
    private final VectorStore vectorStore;
    private final AiProperties aiProperties;
    private final ResourceLoader resourceLoader;
    private final DocumentParser documentParser;

    /**
     * 向量索引是否已就绪（懒加载标记，Milvus 有数据或重建成功后置位）
     */
    private final AtomicBoolean indexLoaded = new AtomicBoolean(false);

    /**
     * 单文档重建任务（内存态；应用重启后任务失效，前端轮询不到时提示重试即可）
     */
    private final Map<Long, RebuildTask> rebuildTasks = new ConcurrentHashMap<>();
    private final AtomicLong rebuildIdGen = new AtomicLong(5000);
    private final ExecutorService rebuildExecutor =
            Executors.newSingleThreadExecutor(namedThreadFactory("knowledge-rebuild"));

    private static ThreadFactory namedThreadFactory(String prefix) {
        AtomicInteger seq = new AtomicInteger(1);
        return r -> {
            Thread t = new Thread(r, prefix + "-" + seq.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
    }

    /**
     * 切块向量主键：docId * 10000 + idx。
     * 同一文档内稳定且唯一（单文档切块数远小于 10000），rebuild 与 ingest 幂等覆盖。
     */
    private static long composeChunkId(long docId, int idx) {
        return docId * 10000L + idx;
    }

    @PreDestroy
    public void shutdown() {
        rebuildExecutor.shutdownNow();
    }

    @Override
    public void ensureIndexLoaded() {
        if (indexLoaded.get()) {
            return;
        }
        synchronized (this) {
            if (indexLoaded.get()) {
                return;
            }
            try {
                vectorStore.ensureReady();
                // Milvus 持久化且已有数据：无需重建
                if (vectorStore.hasData()) {
                    indexLoaded.set(true);
                    return;
                }
                rebuild();
                indexLoaded.set(true);
            } catch (Exception ex) {
                log.warn("[RAG] 懒加载索引构建失败（可能表尚未创建/向量库不可达），本次按空索引处理：{}", ex.getMessage());
            }
        }
    }

    @Override
    public Long ingestFromDto(KnowledgeIngestDTO dto) {
        SysKnowledgeDoc doc;
        int changeType;
        // 条件必填分支（第 10 节①类：DTO 注解无法表达）：id 为空走新增，id 非空走修改，且文档必须存在
        if (dto.getId() != null) {
            doc = sysKnowledgeDocMapper.selectById(dto.getId());
            if (doc == null) {
                throw new BusinessException("知识文档不存在");
            }
            changeType = KnowledgeDocChangeTypeEnum.EDIT.getCode();
        } else {
            doc = new SysKnowledgeDoc();
            changeType = KnowledgeDocChangeTypeEnum.CREATE.getCode();
        }
        doc.setTitle(dto.getTitle().trim());
        doc.setCategory(dto.getCategory().trim());
        doc.setVersion(dto.getVersion().trim());
        doc.setSourceName(dto.getSourceName().trim());
        doc.setContent(dto.getContent());
        if (dto.getSourceType() != null) {
            doc.setSourceType(dto.getSourceType());
        }
        Long id = ingest(doc);
        // 留痕：新建/编辑各落一条快照，操作人取当前登录员工
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        recordHistory(doc, changeType, user.getRealName(), user.getEmployeeId());
        return id;
    }

    @Override
    public KnowledgeDocListVO upload(MultipartFile file, String title, String category,
                                     String version, String sourceName) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        String originalName = file.getOriginalFilename();
        if (!TextUtil.hasText(originalName)) {
            throw new BusinessException("文件名无效");
        }
        String content;
        try {
            content = documentParser.parse(originalName, file.getBytes());
        } catch (IOException ex) {
            throw new BusinessException("文件读取失败：" + ex.getMessage());
        }
        SysKnowledgeDoc doc = new SysKnowledgeDoc();
        doc.setTitle(title.trim());
        doc.setCategory(category.trim());
        doc.setVersion(version.trim());
        doc.setSourceName(sourceName.trim());
        doc.setContent(content);
        doc.setSourceType(KnowledgeDocSourceTypeEnum.FILE_IMPORT.getCode());
        ingest(doc); // 只保存不建索引，由「重建索引」触发
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        recordHistory(doc, KnowledgeDocChangeTypeEnum.CREATE.getCode(), user.getRealName(), user.getEmployeeId());
        return toListVo(doc);
    }

    @Override
    public Long ingest(SysKnowledgeDoc doc) {
        return ingest(doc, null);
    }

    @Override
    public Long ingest(SysKnowledgeDoc doc, java.util.function.IntConsumer progressCallback) {
        if (doc.getSourceType() == null) {
            doc.setSourceType(KnowledgeDocSourceTypeEnum.MANUAL_ENTRY.getCode());
        }
        if (doc.getStatus() == null) {
            doc.setStatus(0);
        }
        // 只保存不建索引：新增直接 insert；修改按新字段 update 并清掉旧向量（避免检索命中旧内容），chunk_count 置 0
        doc.setChunkCount(0);
        if (doc.getId() == null) {
            sysKnowledgeDocMapper.insert(doc);
        } else {
            sysKnowledgeDocMapper.updateById(doc);
            vectorStore.removeByDocId(doc.getId());
        }
        if (progressCallback != null) {
            progressCallback.accept(100);
        }
        return doc.getId();
    }

    @Override
    public IPage<KnowledgeDocListVO> listPage(KnowledgeDocQueryPageDTO dto) {
        Page<SysKnowledgeDoc> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        QueryWrapper<SysKnowledgeDoc> w = new QueryWrapper<>();
        if (TextUtil.hasText(dto.getTitle())) {
            w.like("title", dto.getTitle());
        }
        if (TextUtil.hasText(dto.getCategory())) {
            w.eq("category", dto.getCategory());
        }
        w.eq("del_flag", 0).orderByDesc("create_time");
        return sysKnowledgeDocMapper.selectPage(page, w).convert(this::toListVo);
    }

    private KnowledgeDocListVO toListVo(SysKnowledgeDoc d) {
        KnowledgeDocListVO v = new KnowledgeDocListVO();
        v.setId(d.getId());
        v.setTitle(d.getTitle());
        v.setCategory(d.getCategory());
        v.setSourceType(d.getSourceType());
        v.setVersion(d.getVersion());
        v.setSourceName(d.getSourceName());
        v.setChunkCount(d.getChunkCount());
        v.setStatus(d.getStatus());
        v.setCreateTime(d.getCreateTime());
        return v;
    }

    @Override
    public KnowledgeDocVO getById(Long id) {
        SysKnowledgeDoc d = sysKnowledgeDocMapper.selectById(id);
        if (d == null) {
            return null;
        }
        return toVo(d);
    }

    private KnowledgeDocVO toVo(SysKnowledgeDoc d) {
        KnowledgeDocVO v = new KnowledgeDocVO();
        v.setId(d.getId());
        v.setTitle(d.getTitle());
        v.setCategory(d.getCategory());
        v.setSourceType(d.getSourceType());
        v.setVersion(d.getVersion());
        v.setSourceName(d.getSourceName());
        v.setContent(d.getContent());
        v.setChunkCount(d.getChunkCount());
        v.setStatus(d.getStatus());
        v.setCreateTime(d.getCreateTime());
        return v;
    }

    @Override
    public void deleteById(Long id) {
        ensureIndexLoaded();
        sysKnowledgeDocMapper.deleteById(id); // 逻辑删
        vectorStore.removeByDocId(id);
    }

    @Override
    public void rebuild() {
        vectorStore.clear();
        // 切块事实源为 Milvus；空库重建从 doc 全文重新切分（与 rebuildDocIndex 同口径，chunkId 规则一致可幂等覆盖）
        List<SysKnowledgeDoc> docs = sysKnowledgeDocMapper.selectList(null); // del_flag=0 自动生效
        int chunkSize = aiProperties.getRag().getChunkSize();
        int overlap = aiProperties.getRag().getChunkOverlap();
        int count = 0;
        for (SysKnowledgeDoc doc : docs) {
            if (!TextUtil.hasText(doc.getContent())) {
                continue;
            }
            List<String> pieces = textSplitter.split(doc.getContent(), chunkSize, overlap);
            int idx = 0;
            for (String piece : pieces) {
                vectorStore.upsert(composeChunkId(doc.getId(), idx), doc.getId(), piece, doc.getTitle(),
                        doc.getCategory(), doc.getVersion(), doc.getSourceName(),
                        embeddingSelector.select().embed(piece));
                idx++;
            }
            count += pieces.size();
        }
        indexLoaded.set(true);
        log.info("[RAG] 索引重建完成，载入切块 {} 条", count);
    }

    // ---------------- 单文档重建索引（异步任务 + 进度） ----------------

    @Override
    public int seedIfEmpty() {
        if (!aiProperties.getRag().isAutoSeed()) {
            return 0;
        }
        Long cnt = sysKnowledgeDocMapper.selectCount(new QueryWrapper<SysKnowledgeDoc>().eq("del_flag", 0));
        if (cnt != null && cnt > 0) {
            return 0;
        }
        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver(resourceLoader);
        int seeded = 0;
        try {
            Resource[] resources = resolver.getResources("classpath*:rag-corpus/*.md");
            for (Resource r : resources) {
                try (InputStream in = r.getInputStream()) {
                    String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    String filename = r.getFilename();
                    String title = filename == null ? "内置语料" : filename.replaceAll("\\.md$", "");
                    SysKnowledgeDoc doc = new SysKnowledgeDoc();
                    doc.setTitle(title);
                    doc.setCategory(title);
                    doc.setSourceType(KnowledgeDocSourceTypeEnum.BUILTIN_SAMPLE.getCode());
                    doc.setSourceName("内置示例");
                    doc.setContent(text);
                    ingest(doc); // 保存
                    rebuildDocIndex(doc); // 示例语料直接建索引，保证灌入即可检索
                    seeded++;
                }
            }
        } catch (IOException ex) {
            log.warn("[RAG] 读取内置语料失败：{}", ex.getMessage());
        }
        log.info("[RAG] 已自动灌入内置示例语料 {} 篇", seeded);
        return seeded;
    }

    @Override
    public Long submitRebuild(Long docId) {
        SysKnowledgeDoc doc = sysKnowledgeDocMapper.selectById(docId);
        if (doc == null) {
            throw new BusinessException("知识文档不存在");
        }
        RebuildTask t = new RebuildTask();
        t.id = rebuildIdGen.incrementAndGet();
        t.docId = docId;
        t.status = KnowledgeIndexRebuildStatusEnum.RUNNING.getCode();
        t.progress = 0;
        rebuildTasks.put(t.id, t);
        rebuildExecutor.execute(() -> doRebuild(t));
        return t.id;
    }

    @Override
    public KnowledgeRebuildTaskVO getRebuildProgress(Long taskId) {
        RebuildTask t = rebuildTasks.get(taskId);
        if (t == null) {
            return null;
        }
        KnowledgeRebuildTaskVO v = new KnowledgeRebuildTaskVO();
        v.setTaskId(t.id);
        v.setDocId(t.docId);
        v.setStatus(t.status);
        v.setProgress(t.progress);
        v.setChunkCount(t.chunkCount);
        v.setErrorMsg(t.errorMsg);
        return v;
    }

    private void doRebuild(RebuildTask t) {
        try {
            // 懒加载在后台完成（空库时内部已全量重建），单文档重建随后幂等覆盖
            ensureIndexLoaded();
            SysKnowledgeDoc doc = sysKnowledgeDocMapper.selectById(t.docId);
            if (doc == null) {
                throw new BusinessException("知识文档不存在");
            }
            if (!TextUtil.hasText(doc.getContent())) {
                throw new BusinessException("文档无正文，无法重建索引");
            }
            int chunkSize = aiProperties.getRag().getChunkSize();
            int overlap = aiProperties.getRag().getChunkOverlap();
            vectorStore.removeByDocId(doc.getId());
            List<String> pieces = textSplitter.split(doc.getContent(), chunkSize, overlap);
            int idx = 0;
            int total = pieces.size();
            for (String piece : pieces) {
                vectorStore.upsert(composeChunkId(doc.getId(), idx), doc.getId(), piece, doc.getTitle(),
                        doc.getCategory(), doc.getVersion(), doc.getSourceName(),
                        embeddingSelector.select().embed(piece));
                idx++;
                t.progress = Math.min(99, idx * 100 / Math.max(1, total));
            }
            doc.setChunkCount(pieces.size());
            sysKnowledgeDocMapper.updateById(doc);
            t.chunkCount = pieces.size();
            t.progress = 100;
            t.status = KnowledgeIndexRebuildStatusEnum.DONE.getCode();
            log.info("[RAG] 文档 {}（{}）重建索引完成，切块 {} 条", doc.getId(), doc.getTitle(), pieces.size());
        } catch (Exception ex) {
            t.status = KnowledgeIndexRebuildStatusEnum.FAILED.getCode();
            t.errorMsg = ex.getMessage();
            log.error("[RAG] 文档 {} 重建索引失败：{}", t.docId, ex.getMessage());
        }
    }

    /**
     * 单文档重建（同步，供种子语料直接建索引复用；切块与 doRebuild 同口径）
     */
    private void rebuildDocIndex(SysKnowledgeDoc doc) {
        if (!TextUtil.hasText(doc.getContent())) {
            return;
        }
        int chunkSize = aiProperties.getRag().getChunkSize();
        int overlap = aiProperties.getRag().getChunkOverlap();
        vectorStore.removeByDocId(doc.getId());
        List<String> pieces = textSplitter.split(doc.getContent(), chunkSize, overlap);
        int idx = 0;
        for (String piece : pieces) {
            vectorStore.upsert(composeChunkId(doc.getId(), idx), doc.getId(), piece, doc.getTitle(),
                    doc.getCategory(), doc.getVersion(), doc.getSourceName(),
                    embeddingSelector.select().embed(piece));
            idx++;
        }
        doc.setChunkCount(pieces.size());
        sysKnowledgeDocMapper.updateById(doc);
    }

    // ---------------- 切块预览 / 停用启用 / 历史版本 ----------------

    @Override
    public KnowledgeChunkPreviewVO previewChunks(String content) {
        int chunkSize = aiProperties.getRag().getChunkSize();
        int overlap = aiProperties.getRag().getChunkOverlap();
        List<String> pieces = textSplitter.split(content == null ? "" : content, chunkSize, overlap);
        KnowledgeChunkPreviewVO v = new KnowledgeChunkPreviewVO();
        v.setChunkCount(pieces.size());
        v.setTotalChars(content == null ? 0 : content.length());
        v.setChunkSize(chunkSize);
        v.setChunkOverlap(overlap);
        // 只回传每块开头预览，避免大文本回传
        v.setChunks(pieces.stream().map(p -> p.length() > 200 ? p.substring(0, 200) : p).toList());
        return v;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        // 码值白名单已由 DTO @AllowedValues 收口（web 入口 400）
        SysKnowledgeDoc doc = sysKnowledgeDocMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("知识文档不存在");
        }
        SysKnowledgeDoc upd = new SysKnowledgeDoc();
        upd.setId(id);
        upd.setStatus(status);
        sysKnowledgeDocMapper.updateById(upd);
    }

    @Override
    public List<KnowledgeDocHistoryVO> listHistory(Long docId) {
        return sysKnowledgeDocHistoryMapper.selectList(
                        new QueryWrapper<SysKnowledgeDocHistory>()
                                .eq("doc_id", docId)
                                .orderByDesc("create_time", "id"))
                .stream().map(this::toHistoryVo).toList();
    }

    @Override
    public Long revertHistory(Long historyId) {
        SysKnowledgeDocHistory h = sysKnowledgeDocHistoryMapper.selectById(historyId);
        if (h == null) {
            throw new BusinessException("历史版本不存在");
        }
        SysKnowledgeDoc doc = sysKnowledgeDocMapper.selectById(h.getDocId());
        if (doc == null) {
            throw new BusinessException("知识文档不存在");
        }
        // 快照写回当前文档（标题/分类/版本/来源/内容随快照；来源类型保留当前值）
        doc.setTitle(h.getTitle());
        doc.setCategory(h.getCategory());
        doc.setVersion(h.getVersion());
        doc.setSourceName(h.getSourceName());
        doc.setContent(h.getContent());
        ingest(doc); // update + 清旧向量 + chunk_count 置 0
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        recordHistory(doc, KnowledgeDocChangeTypeEnum.REVERT.getCode(), user.getRealName(), user.getEmployeeId());
        return doc.getId();
    }

    @Override
    public void recordHistory(SysKnowledgeDoc doc, int changeType, String operatorName, Long operatorId) {
        if (doc == null || doc.getId() == null) {
            return;
        }
        SysKnowledgeDocHistory h = new SysKnowledgeDocHistory();
        h.setDocId(doc.getId());
        h.setTitle(doc.getTitle());
        h.setCategory(doc.getCategory());
        h.setSourceType(doc.getSourceType());
        h.setVersion(doc.getVersion());
        h.setSourceName(doc.getSourceName());
        h.setContent(doc.getContent());
        h.setChangeType(changeType);
        h.setOperatorName(operatorName);
        h.setOperatorId(operatorId);
        sysKnowledgeDocHistoryMapper.insert(h);
    }

    private KnowledgeDocHistoryVO toHistoryVo(SysKnowledgeDocHistory h) {
        KnowledgeDocHistoryVO v = new KnowledgeDocHistoryVO();
        v.setId(h.getId());
        v.setDocId(h.getDocId());
        v.setTitle(h.getTitle());
        v.setCategory(h.getCategory());
        v.setSourceType(h.getSourceType());
        v.setVersion(h.getVersion());
        v.setSourceName(h.getSourceName());
        v.setContent(h.getContent());
        v.setChangeType(h.getChangeType());
        v.setOperatorName(h.getOperatorName());
        v.setCreateTime(h.getCreateTime());
        return v;
    }

    /**
     * 重建任务（内存态）
     */
    private static class RebuildTask {
        private long id;
        private long docId;
        private volatile int status;
        private volatile int progress;
        private volatile Integer chunkCount;
        private volatile String errorMsg;
    }
}
