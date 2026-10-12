package com.his.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.ai.config.AiProperties;
import com.his.ai.dto.KnowledgeDocQueryPageDTO;
import com.his.ai.dto.KnowledgeIngestDTO;
import com.his.ai.entity.SysKnowledgeDoc;
import com.his.ai.mapper.SysKnowledgeDocMapper;
import com.his.ai.rag.embedding.EmbeddingProviderSelector;
import com.his.ai.rag.parse.DocumentParser;
import com.his.ai.rag.split.TextSplitter;
import com.his.ai.rag.store.VectorStore;
import com.his.ai.service.KnowledgeStoreService;
import com.his.ai.vo.KnowledgeDocListVO;
import com.his.ai.vo.KnowledgeDocVO;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 知识库存储服务实现。
 * 切块事实源为 Milvus 向量库（文本 + 向量 + 元数据整体持久化，重启不丢）；
 * 文档全文留档在 sys_knowledge_doc.content（文本事实源，用于重建与留档）。
 * 索引构建采用懒加载：启动不访问向量库，首次 ask / ingest / deleteById 时按需就绪一次；
 * Milvus 已有数据则跳过重建，空库则从 doc 全文重新切分灌入；也可通过 /ai/knowledge/rebuild 手动重建。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeStoreServiceImpl implements KnowledgeStoreService {

    private final SysKnowledgeDocMapper sysKnowledgeDocMapper;
    private final EmbeddingProviderSelector embeddingSelector;
    private final TextSplitter textSplitter;
    private final VectorStore vectorStore;
    private final AiProperties aiProperties;
    private final ResourceLoader resourceLoader;
    private final DocumentParser documentParser;

    /** 向量索引是否已就绪（懒加载标记，Milvus 有数据或重建成功后置位） */
    private final AtomicBoolean indexLoaded = new AtomicBoolean(false);

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
        SysKnowledgeDoc doc = new SysKnowledgeDoc();
        doc.setTitle(dto.getTitle().trim());
        doc.setCategory(dto.getCategory().trim());
        doc.setVersion(dto.getVersion().trim());
        doc.setSourceName(dto.getSourceName().trim());
        doc.setContent(dto.getContent());
        doc.setSourceType(dto.getSourceType() == null ? 2 : dto.getSourceType());
        return ingest(doc);
    }

    @Override
    public Long ingest(SysKnowledgeDoc doc) {
        return ingest(doc, null);
    }

    @Override
    public Long ingest(SysKnowledgeDoc doc, java.util.function.IntConsumer progressCallback) {
        ensureIndexLoaded();
        if (doc.getSourceType() == null) {
            doc.setSourceType(2);
        }
        if (doc.getStatus() == null) {
            doc.setStatus(0);
        }
        doc.setChunkCount(0);
        sysKnowledgeDocMapper.insert(doc);

        // 幂等：清掉该文档旧向量，再重新切块入库（切块事实源为 Milvus，文本留档在 doc.content）
        vectorStore.removeByDocId(doc.getId());

        int chunkSize = aiProperties.getRag().getChunkSize();
        int overlap = aiProperties.getRag().getChunkOverlap();
        List<String> pieces = textSplitter.split(doc.getContent(), chunkSize, overlap);

        int idx = 0;
        int total = pieces.size();
        for (String piece : pieces) {
            vectorStore.upsert(composeChunkId(doc.getId(), idx), doc.getId(), piece, doc.getTitle(),
                    doc.getCategory(), doc.getVersion(), doc.getSourceName(),
                    embeddingSelector.select().embed(piece));
            idx++;
            if (progressCallback != null && total > 0) {
                progressCallback.accept(Math.min(99, idx * 100 / total));
            }
        }
        doc.setChunkCount(pieces.size());
        sysKnowledgeDocMapper.updateById(doc);
        if (progressCallback != null) {
            progressCallback.accept(100);
        }
        return doc.getId();
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
        doc.setSourceType(3); // 文件导入
        ingest(doc);
        return toListVo(doc);
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
        // 切块事实源为 Milvus；空库重建从 doc 全文重新切分（与 ingest 同口径，chunkId 规则一致可幂等覆盖）
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

    /**
     * 切块向量主键：docId * 10000 + idx。
     * 同一文档内稳定且唯一（单文档切块数远小于 10000），rebuild 与 ingest 幂等覆盖。
     */
    private static long composeChunkId(long docId, int idx) {
        return docId * 10000L + idx;
    }

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
                    doc.setSourceType(1);
                    doc.setSourceName("内置示例");
                    doc.setContent(text);
                    ingest(doc);
                    seeded++;
                }
            }
        } catch (IOException ex) {
            log.warn("[RAG] 读取内置语料失败：{}", ex.getMessage());
        }
        log.info("[RAG] 已自动灌入内置示例语料 {} 篇", seeded);
        return seeded;
    }
}
