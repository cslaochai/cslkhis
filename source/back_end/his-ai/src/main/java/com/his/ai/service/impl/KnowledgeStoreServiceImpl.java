package com.his.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.ai.config.AiProperties;
import com.his.ai.dto.KnowledgeDocQueryPageDTO;
import com.his.ai.entity.SysKnowledgeChunk;
import com.his.ai.entity.SysKnowledgeDoc;
import com.his.ai.mapper.SysKnowledgeChunkMapper;
import com.his.ai.mapper.SysKnowledgeDocMapper;
import com.his.ai.rag.embedding.EmbeddingProviderSelector;
import com.his.ai.rag.split.TextSplitter;
import com.his.ai.rag.store.InMemoryVectorStore;
import com.his.ai.service.KnowledgeStoreService;
import com.his.ai.vo.KnowledgeDocListVO;
import com.his.ai.vo.KnowledgeDocVO;
import com.his.common.util.TextUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 知识库存储服务实现。
 *
 * <p><b>启动钩子：</b> {@link #init()} 在 bean 初始化时先 {@link #rebuild()} 载入已有切块，
 * 再 {@link #seedIfEmpty()} 灌内置语料（库空且开启自动种子时）。
 * 整段 try-catch 包裹——若表尚未创建（首次部署忘了跑 SQL），只告警不阻断启动，符合「降级不阻断」。
 *
 * <p><b>向量不落库：</b>索引只在内存，重启自动从 chunk 表重建，零外部依赖。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeStoreServiceImpl implements KnowledgeStoreService {

    private final SysKnowledgeDocMapper sysKnowledgeDocMapper;
    private final SysKnowledgeChunkMapper sysKnowledgeChunkMapper;
    private final EmbeddingProviderSelector embeddingSelector;
    private final TextSplitter textSplitter;
    private final InMemoryVectorStore inMemoryVectorStore;
    private final AiProperties aiProperties;
    private final ResourceLoader resourceLoader;

    @PostConstruct
    public void init() {
        try {
            rebuild();
            seedIfEmpty();
        } catch (Exception ex) {
            log.warn("[RAG] 启动索引构建/种子失败（可能表尚未创建），已跳过：{}", ex.getMessage());
        }
    }

    @Override
    public Long ingest(SysKnowledgeDoc doc) {
        if (doc.getSourceType() == null) {
            doc.setSourceType(2);
        }
        if (doc.getStatus() == null) {
            doc.setStatus(0);
        }
        doc.setChunkCount(0);
        sysKnowledgeDocMapper.insert(doc);

        // 幂等：清掉该文档旧切块（软删）与内存索引，再重新建
        sysKnowledgeChunkMapper.delete(new QueryWrapper<SysKnowledgeChunk>().eq("doc_id", doc.getId()));
        inMemoryVectorStore.removeByDocId(doc.getId());

        int chunkSize = aiProperties.getRag().getChunkSize();
        int overlap = aiProperties.getRag().getChunkOverlap();
        List<String> pieces = textSplitter.split(doc.getContent(), chunkSize, overlap);

        int idx = 0;
        for (String piece : pieces) {
            SysKnowledgeChunk chunk = new SysKnowledgeChunk();
            chunk.setDocId(doc.getId());
            chunk.setDocTitle(doc.getTitle());
            chunk.setCategory(doc.getCategory());
            chunk.setChunkIndex(idx);
            chunk.setContent(piece);
            sysKnowledgeChunkMapper.insert(chunk);
            inMemoryVectorStore.upsert(chunk.getId(), doc.getId(), piece, doc.getTitle(),
                    doc.getCategory(),
                    embeddingSelector.select().embed(piece));
            idx++;
        }
        doc.setChunkCount(pieces.size());
        sysKnowledgeDocMapper.updateById(doc);
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
        KnowledgeDocVO v = new KnowledgeDocVO();
        v.setId(d.getId());
        v.setTitle(d.getTitle());
        v.setCategory(d.getCategory());
        v.setSourceType(d.getSourceType());
        v.setContent(d.getContent());
        v.setChunkCount(d.getChunkCount());
        v.setStatus(d.getStatus());
        v.setCreateTime(d.getCreateTime());
        return v;
    }

    @Override
    public void deleteById(Long id) {
        sysKnowledgeDocMapper.deleteById(id); // 逻辑删
        sysKnowledgeChunkMapper.delete(new QueryWrapper<SysKnowledgeChunk>().eq("doc_id", id)); // 逻辑删
        inMemoryVectorStore.removeByDocId(id);
    }

    @Override
    public void rebuild() {
        inMemoryVectorStore.clear();
        List<SysKnowledgeChunk> all = sysKnowledgeChunkMapper.selectList(null); // del_flag=0 自动生效
        int count = 0;
        for (SysKnowledgeChunk c : all) {
            if (c.getContent() == null) {
                continue;
            }
            inMemoryVectorStore.upsert(c.getId(), c.getDocId(), c.getContent(), c.getDocTitle(),
                    c.getCategory(),
                    embeddingSelector.select().embed(c.getContent()));
            count++;
        }
        log.info("[RAG] 索引重建完成，载入切块 {} 条", count);
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
