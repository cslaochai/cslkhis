package com.his.ai.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.ai.dto.KnowledgeAskDTO;
import com.his.ai.dto.KnowledgeDocQueryPageDTO;
import com.his.ai.dto.KnowledgeIdDTO;
import com.his.ai.dto.KnowledgeImportCommitDTO;
import com.his.ai.dto.KnowledgeImportQueryDTO;
import com.his.ai.dto.KnowledgeIngestDTO;
import com.his.ai.service.KnowledgeImportService;
import com.his.ai.service.KnowledgeQaCapability;
import com.his.ai.service.KnowledgeStoreService;
import com.his.ai.vo.KnowledgeAskVO;
import com.his.ai.vo.KnowledgeDocListVO;
import com.his.ai.vo.KnowledgeDocVO;
import com.his.ai.vo.KnowledgeImportTaskVO;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

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
    private final KnowledgeImportService knowledgeImportService;

    @PostMapping("/ask")
    @Operation(summary = "知识库问答（RAG 检索增强生成；模型不可用降级返回检索原文）")
    public Result<KnowledgeAskVO> ask(@Valid @RequestBody KnowledgeAskDTO askDTO) {
        return Result.success(knowledgeQaCapability.ask(askDTO));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/ingest")
    @Operation(summary = "录入/更新知识文档（手工或文件导入；自动切块建索引）")
    public Result<Long> ingest(@Valid @RequestBody KnowledgeIngestDTO ingestDTO) {
        return Result.success(knowledgeStoreService.ingestFromDto(ingestDTO));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/upload")
    @Operation(summary = "上传知识文件（自动解析 + 切块 + 建索引；元数据全部必填；支持 pdf / docx / txt / md，≤20MB）")
    public Result<KnowledgeDocListVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("version") String version,
            @RequestParam("sourceName") String sourceName) {
        return Result.success(knowledgeStoreService.upload(file, title, category, version, sourceName));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/import/submit")
    @Operation(summary = "批量上传知识文件（异步解析，返回导入任务列表；解析完成后预览确认再入库）")
    public Result<List<KnowledgeImportTaskVO>> submitImport(@RequestParam("files") MultipartFile[] files) {
        List<MultipartFile> fileList = files == null ? List.of() : Arrays.asList(files);
        return Result.success(knowledgeImportService.submitBatch(fileList));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/import/list")
    @Operation(summary = "查询导入任务状态（轮询进度；解析中/入库中/待确认/完成/失败）")
    public Result<List<KnowledgeImportTaskVO>> listImports(@Valid @RequestBody KnowledgeImportQueryDTO dto) {
        return Result.success(knowledgeImportService.listByIds(dto.getIds()));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/import/commit")
    @Operation(summary = "确认入库（预览解析文本后补充元数据，异步切块 + embedding + 灌入 Milvus）")
    public Result<KnowledgeImportTaskVO> commitImport(@Valid @RequestBody KnowledgeImportCommitDTO dto) {
        return Result.success(knowledgeImportService.commit(
                dto.getId(), dto.getTitle(), dto.getCategory(), dto.getVersion(), dto.getSourceName()));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/import/cancel")
    @Operation(summary = "取消导入任务（仅解析中 / 待确认可取消）")
    public Result<Void> cancelImport(@Valid @RequestBody KnowledgeIdDTO dto) {
        knowledgeImportService.cancel(dto.getId());
        return Result.success("已取消", null);
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
    @Operation(summary = "重建向量索引（从 doc 全文重新切分灌入 Milvus）")
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
