package com.his.ai.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.ai.dto.KnowledgeAskDTO;
import com.his.ai.dto.KnowledgeDocQueryPageDTO;
import com.his.ai.dto.KnowledgeIdDTO;
import com.his.ai.dto.KnowledgeIngestDTO;
import com.his.ai.entity.SysKnowledgeDoc;
import com.his.ai.service.KnowledgeQaCapability;
import com.his.ai.service.KnowledgeStoreService;
import com.his.ai.vo.KnowledgeAskVO;
import com.his.ai.vo.KnowledgeDocListVO;
import com.his.ai.vo.KnowledgeDocVO;
import com.his.common.base.Result;
import com.his.common.util.TextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库问答（RAG）接口。
 */
@Tag(name = "AI 能力-知识库问答(RAG)")
@RestController
@RequestMapping("/ai/knowledge")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AiKnowledgeController {

    private final KnowledgeQaCapability knowledgeQaCapability;
    private final KnowledgeStoreService knowledgeStoreService;

    @PostMapping("/ask")
    @Operation(summary = "知识库问答（RAG 检索增强生成；模型不可用降级返回检索原文）")
    public Result<KnowledgeAskVO> ask(@Valid @RequestBody KnowledgeAskDTO askDTO) {
        return Result.success(knowledgeQaCapability.ask(askDTO));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/ingest")
    @Operation(summary = "录入/更新知识文档（手工或文件导入；自动切块建索引）")
    public Result<Long> ingest(@Valid @RequestBody KnowledgeIngestDTO ingestDTO) {
        SysKnowledgeDoc doc = new SysKnowledgeDoc();
        doc.setTitle(ingestDTO.getTitle());
        doc.setCategory(TextUtil.hasText(ingestDTO.getCategory()) ? ingestDTO.getCategory() : ingestDTO.getTitle());
        doc.setContent(ingestDTO.getContent());
        doc.setSourceType(ingestDTO.getSourceType() == null ? 2 : ingestDTO.getSourceType());
        return Result.success(knowledgeStoreService.ingest(doc));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/listPage")
    @Operation(summary = "知识文档分页列表")
    public Result<IPage<KnowledgeDocListVO>> listPage(@Valid @RequestBody KnowledgeDocQueryPageDTO dto) {
        return Result.success(knowledgeStoreService.listPage(dto));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/getById")
    @Operation(summary = "知识文档详情（含原文）")
    public Result<KnowledgeDocVO> getById(@Valid @RequestBody KnowledgeIdDTO dto) {
        return Result.success(knowledgeStoreService.getById(dto.getId()));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/deleteById")
    @Operation(summary = "删除知识文档（同步删除切块与索引）")
    public Result<Void> deleteById(@Valid @RequestBody KnowledgeIdDTO dto) {
        knowledgeStoreService.deleteById(dto.getId());
        return Result.success("已删除", null);
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/rebuild")
    @Operation(summary = "重建向量索引（从 chunk 表全量重载）")
    public Result<Void> rebuild() {
        knowledgeStoreService.rebuild();
        return Result.success("索引已重建", null);
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/seed")
    @Operation(summary = "灌入内置示例语料（若库为空）")
    public Result<Integer> seed() {
        return Result.success(knowledgeStoreService.seedIfEmpty());
    }
}
