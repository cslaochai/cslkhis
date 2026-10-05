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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库问答（RAG）接口。
 *
 * <p><b>落地位置：</b> his-ai 暴露 {@code /ai/knowledge/*}，不改动业务模块（避免循环依赖）。
 * 前端若要做患者端智能咨询，调 {@code /ai/knowledge/ask} 即可。
 *
 * <p><b>权限：</b> ask 任何登录用户可用（患者/医护都可能需要）；维护类接口（录入/列表/删除/重建/种子）
 * 限定 {@code ai:knowledge:manage}（sys_menu 按钮码，见 sql/225），与 AI 管理台页面的维护签页同源。
 */
@Tag(name = "AI 能力-知识库问答(RAG)")
@RestController
@RequestMapping("/ai/knowledge")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AiKnowledgeController {

    private final KnowledgeQaCapability qaCapability;
    private final KnowledgeStoreService storeService;

    @PostMapping("/ask")
    @Operation(summary = "知识库问答（RAG 检索增强生成；模型不可用降级返回检索原文）")
    public Result<KnowledgeAskVO> ask(@Valid @RequestBody KnowledgeAskDTO dto) {
        return Result.success(qaCapability.ask(dto));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/ingest")
    @Operation(summary = "录入/更新知识文档（手工或文件导入；自动切块建索引）")
    public Result<Long> ingest(@Valid @RequestBody KnowledgeIngestDTO dto) {
        SysKnowledgeDoc doc = new SysKnowledgeDoc();
        doc.setTitle(dto.getTitle());
        doc.setCategory(StringUtils.hasText(dto.getCategory()) ? dto.getCategory() : dto.getTitle());
        doc.setContent(dto.getContent());
        doc.setSourceType(dto.getSourceType() == null ? 2 : dto.getSourceType());
        return Result.success(storeService.ingest(doc));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/listPage")
    @Operation(summary = "知识文档分页列表")
    public Result<IPage<KnowledgeDocListVO>> listPage(@Valid @RequestBody KnowledgeDocQueryPageDTO dto) {
        return Result.success(storeService.listPage(dto));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/getById")
    @Operation(summary = "知识文档详情（含原文）")
    public Result<KnowledgeDocVO> getById(@Valid @RequestBody KnowledgeIdDTO dto) {
        return Result.success(storeService.getById(dto.getId()));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/deleteById")
    @Operation(summary = "删除知识文档（同步删除切块与索引）")
    public Result<Void> deleteById(@Valid @RequestBody KnowledgeIdDTO dto) {
        storeService.deleteById(dto.getId());
        return Result.success("已删除", null);
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/rebuild")
    @Operation(summary = "重建向量索引（从 chunk 表全量重载）")
    public Result<Void> rebuild() {
        storeService.rebuild();
        return Result.success("索引已重建", null);
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/seed")
    @Operation(summary = "灌入内置示例语料（若库为空）")
    public Result<Integer> seed() {
        return Result.success(storeService.seedIfEmpty());
    }
}
