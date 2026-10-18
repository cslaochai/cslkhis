package com.his.ai.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.ai.dto.KnowledgeAskDTO;
import com.his.ai.dto.KnowledgeDocPageQueryDTO;
import com.his.ai.dto.KnowledgeEvalCasePageQueryDTO;
import com.his.ai.dto.KnowledgeEvalCaseUpsertDTO;
import com.his.ai.dto.KnowledgeFeedbackDTO;
import com.his.ai.dto.KnowledgeIdDTO;
import com.his.ai.dto.KnowledgeImportCommitDTO;
import com.his.ai.dto.KnowledgeImportQueryDTO;
import com.his.ai.dto.KnowledgeIngestDTO;
import com.his.ai.dto.KnowledgePreviewDTO;
import com.his.ai.dto.KnowledgeQueryLogPageQueryDTO;
import com.his.ai.dto.KnowledgeStatusDTO;
import com.his.ai.dto.KnowledgeTaskIdDTO;
import com.his.ai.service.KnowledgeEvalService;
import com.his.ai.service.KnowledgeImportService;
import com.his.ai.service.KnowledgeQaCapability;
import com.his.ai.service.KnowledgeQueryLogService;
import com.his.ai.service.KnowledgeStoreService;
import com.his.ai.vo.KnowledgeAskVO;
import com.his.ai.vo.KnowledgeChunkPreviewVO;
import com.his.ai.vo.KnowledgeDocHistoryVO;
import com.his.ai.vo.KnowledgeDocListVO;
import com.his.ai.vo.KnowledgeDocVO;
import com.his.ai.vo.KnowledgeEvalCaseVO;
import com.his.ai.vo.KnowledgeEvalRunDetailVO;
import com.his.ai.vo.KnowledgeEvalRunVO;
import com.his.ai.vo.KnowledgeImportTaskVO;
import com.his.ai.vo.KnowledgeQueryLogVO;
import com.his.ai.vo.KnowledgeRebuildTaskVO;
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
    private final KnowledgeQueryLogService knowledgeQueryLogService;
    private final KnowledgeEvalService knowledgeEvalService;

    @PostMapping("/ask")
    @Operation(summary = "知识库问答（RAG 检索增强生成；模型不可用降级返回检索原文）")
    public Result<KnowledgeAskVO> ask(@Valid @RequestBody KnowledgeAskDTO askDTO) {
        return Result.success(knowledgeQaCapability.ask(askDTO));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/ingest")
    @Operation(summary = "录入/更新知识文档（只保存不建索引，索引由「重建索引」触发）")
    public Result<Long> ingest(@Valid @RequestBody KnowledgeIngestDTO ingestDTO) {
        return Result.success(knowledgeStoreService.ingestFromDto(ingestDTO));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/upload")
    @Operation(summary = "上传知识文件（自动解析；只保存不建索引，索引由「重建索引」触发；元数据全部必填；支持 pdf / docx / txt / md，≤20MB）")
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
    public Result<IPage<KnowledgeDocListVO>> listPage(@Valid @RequestBody KnowledgeDocPageQueryDTO dto) {
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
    @PostMapping("/rebuildById")
    @Operation(summary = "提交单文档重建索引任务（异步执行，返回任务 ID 供轮询进度）")
    public Result<Long> rebuildById(@Valid @RequestBody KnowledgeIdDTO dto) {
        return Result.success(knowledgeStoreService.submitRebuild(dto.getId()));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/rebuildProgress")
    @Operation(summary = "查询单文档重建索引任务进度（status 1-进行中 2-完成 3-失败）")
    public Result<KnowledgeRebuildTaskVO> rebuildProgress(@Valid @RequestBody KnowledgeTaskIdDTO dto) {
        return Result.success(knowledgeStoreService.getRebuildProgress(dto.getTaskId()));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/previewChunks")
    @Operation(summary = "切块预览（按当前切块参数计算，不落库不灌向量）")
    public Result<KnowledgeChunkPreviewVO> previewChunks(@Valid @RequestBody KnowledgePreviewDTO dto) {
        return Result.success(knowledgeStoreService.previewChunks(dto.getContent()));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/updateStatus")
    @Operation(summary = "停用/启用知识文档（status 0-正常 1-停用）")
    public Result<Void> updateStatus(@Valid @RequestBody KnowledgeStatusDTO dto) {
        knowledgeStoreService.updateStatus(dto.getId(), dto.getStatus());
        return Result.success("已更新", null);
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/history/list")
    @Operation(summary = "知识文档历史版本列表（按操作时间倒序）")
    public Result<List<KnowledgeDocHistoryVO>> listHistory(@Valid @RequestBody KnowledgeIdDTO dto) {
        return Result.success(knowledgeStoreService.listHistory(dto.getId()));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/history/revert")
    @Operation(summary = "回退到指定历史版本（快照写回当前文档并清旧向量，需重建索引）")
    public Result<Long> revertHistory(@Valid @RequestBody KnowledgeIdDTO dto) {
        return Result.success(knowledgeStoreService.revertHistory(dto.getId()));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/queryLog/listPage")
    @Operation(summary = "问答观测日志分页（按提问时间倒序；degraded 0-正常 1-降级，feedback 0-未反馈 1-有用 2-无用）")
    public Result<IPage<KnowledgeQueryLogVO>> queryLogListPage(@Valid @RequestBody KnowledgeQueryLogPageQueryDTO dto) {
        return Result.success(knowledgeQueryLogService.listPage(dto));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/queryLog/feedback")
    @Operation(summary = "问答观测反馈（1-有用 2-无用，同一记录重复提交覆盖）")
    public Result<Void> queryLogFeedback(@Valid @RequestBody KnowledgeFeedbackDTO dto) {
        knowledgeQueryLogService.feedback(dto.getId(), dto.getFeedback());
        return Result.success("已反馈", null);
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/evalCase/listPage")
    @Operation(summary = "评测用例分页")
    public Result<IPage<KnowledgeEvalCaseVO>> evalCaseListPage(@Valid @RequestBody KnowledgeEvalCasePageQueryDTO dto) {
        return Result.success(knowledgeEvalService.listCasePage(dto));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/evalCase/upsert")
    @Operation(summary = "新增/修改评测用例（id 为空新增，非空修改）")
    public Result<Long> evalCaseUpsert(@Valid @RequestBody KnowledgeEvalCaseUpsertDTO dto) {
        return Result.success(knowledgeEvalService.upsertCase(dto));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/evalCase/deleteById")
    @Operation(summary = "删除评测用例（软删）")
    public Result<Void> evalCaseDelete(@Valid @RequestBody KnowledgeIdDTO dto) {
        knowledgeEvalService.deleteCase(dto.getId());
        return Result.success("已删除", null);
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/eval/run")
    @Operation(summary = "运行离线评测（对全部用例检索，算 recall@k / MRR，落运行汇总与明细）")
    public Result<KnowledgeEvalRunVO> evalRun() {
        return Result.success(knowledgeEvalService.runEval());
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/eval/runListPage")
    @Operation(summary = "评测运行历史分页（按运行时间倒序，用于回归对比）")
    public Result<IPage<KnowledgeEvalRunVO>> evalRunListPage(@Valid @RequestBody KnowledgeEvalCasePageQueryDTO dto) {
        return Result.success(knowledgeEvalService.listRunPage(dto));
    }

    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/eval/runDetail")
    @Operation(summary = "评测运行明细（每用例一条：是否命中/位次/实际命中）")
    public Result<List<KnowledgeEvalRunDetailVO>> evalRunDetail(@Valid @RequestBody KnowledgeIdDTO dto) {
        return Result.success(knowledgeEvalService.listRunDetail(dto.getId()));
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
