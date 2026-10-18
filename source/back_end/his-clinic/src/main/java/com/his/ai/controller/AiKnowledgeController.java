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

    /**
     * 就院内制度、就诊须知等科普类提问做检索增强回答，模型不可用时降级返回命中的原文片段。
     * 用于「AI 管理台」知识库问答签页的提问框；只做科普不做医疗判定，降级原因必须展示。
     */
    @PostMapping("/ask")
    @Operation(summary = "知识库问答（RAG 检索增强生成；模型不可用降级返回检索原文）")
    public Result<KnowledgeAskVO> ask(@Valid @RequestBody KnowledgeAskDTO askDTO) {
        return Result.success(knowledgeQaCapability.ask(askDTO));
    }

    /**
     * 保存或更新一篇知识文档的正文与元数据，只落库不建索引，修改时清掉旧向量。
     * 用于「知识库管理」的录入弹框手工录入与编辑弹框保存，新内容要到列表点「重建索引」才可被检索命中。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/ingest")
    @Operation(summary = "录入/更新知识文档（只保存不建索引，索引由「重建索引」触发）")
    public Result<Long> ingest(@Valid @RequestBody KnowledgeIngestDTO ingestDTO) {
        return Result.success(knowledgeStoreService.ingestFromDto(ingestDTO));
    }

    /**
     * 上传单个知识文件自动解析出正文后保存，元数据全部必填且不建索引。
     * 用于知识文档按份直传解析的维护流程；页面当前批量导入走导入任务通道。
     */
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

    /**
     * 一次接收多个知识文件并逐个建导入任务，异步解析正文后等人工确认再入库。
     * 用于「知识库管理」录入弹框的文件批量导入首步。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/import/submit")
    @Operation(summary = "批量上传知识文件（异步解析，返回导入任务列表；解析完成后预览确认再入库）")
    public Result<List<KnowledgeImportTaskVO>> submitImport(@RequestParam("files") MultipartFile[] files) {
        List<MultipartFile> fileList = files == null ? List.of() : Arrays.asList(files);
        return Result.success(knowledgeImportService.submitBatch(fileList));
    }

    /**
     * 按任务编号批量取导入任务的解析与入库进度。
     * 用于批量导入提交后在弹框内轮询任务状态，直到出现待确认或失败。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/import/list")
    @Operation(summary = "查询导入任务状态（轮询进度；解析中/入库中/待确认/完成/失败）")
    public Result<List<KnowledgeImportTaskVO>> listImports(@Valid @RequestBody KnowledgeImportQueryDTO dto) {
        return Result.success(knowledgeImportService.listByIds(dto.getIds()));
    }

    /**
     * 把解析完成的任务连同补充的标题、分类、版本、来源确认入库并留历史版本。
     * 用于批量导入弹框预览解析文本后的确认动作；入库不建索引，仍需到列表重建。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/import/commit")
    @Operation(summary = "确认入库（预览解析文本后补充元数据，异步切块 + embedding + 灌入 Milvus）")
    public Result<KnowledgeImportTaskVO> commitImport(@Valid @RequestBody KnowledgeImportCommitDTO dto) {
        return Result.success(knowledgeImportService.commit(
                dto.getId(), dto.getTitle(), dto.getCategory(), dto.getVersion(), dto.getSourceName()));
    }

    /**
     * 放弃一个导入任务，只有还没入库的解析中与待确认两种状态可取消。
     * 用于批量导入弹框里剔除解析结果不满意的那个文件。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/import/cancel")
    @Operation(summary = "取消导入任务（仅解析中 / 待确认可取消）")
    public Result<Void> cancelImport(@Valid @RequestBody KnowledgeIdDTO dto) {
        knowledgeImportService.cancel(dto.getId());
        return Result.success("已取消", null);
    }

    /**
     * 知识文档分页列表，只回标题分类来源与索引状态等列，不含正文原文。
     * 用于「知识库管理」签页的查询卡与表格；评测用例弹框选「期望命中文档」时同样取这份列表。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/listPage")
    @Operation(summary = "知识文档分页列表")
    public Result<IPage<KnowledgeDocListVO>> listPage(@Valid @RequestBody KnowledgeDocPageQueryDTO dto) {
        return Result.success(knowledgeStoreService.listPage(dto));
    }

    /**
     * 取单篇知识文档详情，含可用于编辑的正文原文。
     * 用于「知识库管理」编辑弹框按文档主键回显表单。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/getById")
    @Operation(summary = "知识文档详情（含原文）")
    public Result<KnowledgeDocVO> getById(@Valid @RequestBody KnowledgeIdDTO dto) {
        return Result.success(knowledgeStoreService.getById(dto.getId()));
    }

    /**
     * 删除一篇知识文档并连带清掉它的切块与向量索引，历史版本快照保留。
     * 用于「知识库管理」列表操作列的删除。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/deleteById")
    @Operation(summary = "删除知识文档（同步删除切块与索引）")
    public Result<Void> deleteById(@Valid @RequestBody KnowledgeIdDTO dto) {
        knowledgeStoreService.deleteById(dto.getId());
        return Result.success("已删除", null);
    }

    /**
     * 为一篇文档提交重建索引任务，异步执行并返回任务编号。
     * 用于「知识库管理」操作列的「重建索引」，也用于编辑弹框保存后立即重建。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/rebuildById")
    @Operation(summary = "提交单文档重建索引任务（异步执行，返回任务 ID 供轮询进度）")
    public Result<Long> rebuildById(@Valid @RequestBody KnowledgeIdDTO dto) {
        return Result.success(knowledgeStoreService.submitRebuild(dto.getId()));
    }

    /**
     * 查询一次单文档重建索引任务的进度与结果状态。
     * 用于重建发起后轮询，把操作列的按钮渲染成「重建中 百分比」直到完成或失败。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/rebuildProgress")
    @Operation(summary = "查询单文档重建索引任务进度（status 1-进行中 2-完成 3-失败）")
    public Result<KnowledgeRebuildTaskVO> rebuildProgress(@Valid @RequestBody KnowledgeTaskIdDTO dto) {
        return Result.success(knowledgeStoreService.getRebuildProgress(dto.getTaskId()));
    }

    /**
     * 按当前切块参数试算一段正文会被切成哪几块，只返回块数与每块开头，不落库也不灌向量。
     * 用于编辑弹框的「预览切块」，改内容后先确认切块效果再保存。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/previewChunks")
    @Operation(summary = "切块预览（按当前切块参数计算，不落库不灌向量）")
    public Result<KnowledgeChunkPreviewVO> previewChunks(@Valid @RequestBody KnowledgePreviewDTO dto) {
        return Result.success(knowledgeStoreService.previewChunks(dto.getContent()));
    }

    /**
     * 停用或启用一篇知识文档，停用后退出检索范围但内容保留。
     * 用于「知识库管理」操作列的停用 / 启用切换。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/updateStatus")
    @Operation(summary = "停用/启用知识文档（status 0-正常 1-停用）")
    public Result<Void> updateStatus(@Valid @RequestBody KnowledgeStatusDTO dto) {
        knowledgeStoreService.updateStatus(dto.getId(), dto.getStatus());
        return Result.success("已更新", null);
    }

    /**
     * 取一篇文档的历史版本快照列表，新建、编辑、回退每次保存都留一条。
     * 用于编辑弹框的版本区查看与当前内容做行级对比。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/history/list")
    @Operation(summary = "知识文档历史版本列表（按操作时间倒序）")
    public Result<List<KnowledgeDocHistoryVO>> listHistory(@Valid @RequestBody KnowledgeIdDTO dto) {
        return Result.success(knowledgeStoreService.listHistory(dto.getId()));
    }

    /**
     * 把某条历史快照写回当前文档并清掉旧向量，返回文档主键。
     * 用于编辑弹框的版本回退操作；回退后正文与索引不一致，必须再重建索引才可被检索。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/history/revert")
    @Operation(summary = "回退到指定历史版本（快照写回当前文档并清旧向量，需重建索引）")
    public Result<Long> revertHistory(@Valid @RequestBody KnowledgeIdDTO dto) {
        return Result.success(knowledgeStoreService.revertHistory(dto.getId()));
    }

    /**
     * 问答观测日志分页：每次提问的问题、回答、命中文档与耗时，可按是否降级和用户反馈收窄。
     * 用于「AI 管理台」「问答观测」签页的查询卡与表格。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/queryLog/listPage")
    @Operation(summary = "问答观测日志分页（按提问时间倒序；degraded 0-正常 1-降级，feedback 0-未反馈 1-有用 2-无用）")
    public Result<IPage<KnowledgeQueryLogVO>> queryLogListPage(@Valid @RequestBody KnowledgeQueryLogPageQueryDTO dto) {
        return Result.success(knowledgeQueryLogService.listPage(dto));
    }

    /**
     * 给一条问答观测记录打有用或无用的反馈，同一记录重复提交覆盖上一次。
     * 用于「问答观测」列表的反馈列，供后续判断语料与检索质量。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/queryLog/feedback")
    @Operation(summary = "问答观测反馈（1-有用 2-无用，同一记录重复提交覆盖）")
    public Result<Void> queryLogFeedback(@Valid @RequestBody KnowledgeFeedbackDTO dto) {
        knowledgeQueryLogService.feedback(dto.getId(), dto.getFeedback());
        return Result.success("已反馈", null);
    }

    /**
     * 评测用例分页：一条标准问题加它期望命中的知识文档。
     * 用于「AI 管理台」「评测中心」签页的用例列表。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/evalCase/listPage")
    @Operation(summary = "评测用例分页")
    public Result<IPage<KnowledgeEvalCaseVO>> evalCaseListPage(@Valid @RequestBody KnowledgeEvalCasePageQueryDTO dto) {
        return Result.success(knowledgeEvalService.listCasePage(dto));
    }

    /**
     * 新增或修改一条评测用例，期望命中文档连同标题一起留存便于列表展示。
     * 用于「评测中心」的新增 / 编辑用例弹框保存。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/evalCase/upsert")
    @Operation(summary = "新增/修改评测用例（id 为空新增，非空修改）")
    public Result<Long> evalCaseUpsert(@Valid @RequestBody KnowledgeEvalCaseUpsertDTO dto) {
        return Result.success(knowledgeEvalService.upsertCase(dto));
    }

    /**
     * 删除一条评测用例，留档不物理删。
     * 用于「评测中心」用例列表的操作列。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/evalCase/deleteById")
    @Operation(summary = "删除评测用例（软删）")
    public Result<Void> evalCaseDelete(@Valid @RequestBody KnowledgeIdDTO dto) {
        knowledgeEvalService.deleteCase(dto.getId());
        return Result.success("已删除", null);
    }

    /**
     * 拿全部评测用例跑一遍检索，算出召回率与 MRR 并落一次运行汇总加逐用例明细。
     * 用于「评测中心」的「运行评测」按钮，同步执行，耗时随用例数增长。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/eval/run")
    @Operation(summary = "运行离线评测（对全部用例检索，算 recall@k / MRR，落运行汇总与明细）")
    public Result<KnowledgeEvalRunVO> evalRun() {
        return Result.success(knowledgeEvalService.runEval());
    }

    /**
     * 评测运行历史分页，按运行时间倒序带回召回率、MRR 与当时的配置快照。
     * 用于「评测中心」的运行记录列表做改动前后的回归对比。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/eval/runListPage")
    @Operation(summary = "评测运行历史分页（按运行时间倒序，用于回归对比）")
    public Result<IPage<KnowledgeEvalRunVO>> evalRunListPage(@Valid @RequestBody KnowledgeEvalCasePageQueryDTO dto) {
        return Result.success(knowledgeEvalService.listRunPage(dto));
    }

    /**
     * 取一次评测运行的逐用例明细：是否命中、命中位次与期望命中的文档。
     * 用于「评测中心」点开某次运行定位是哪条用例把召回率拉低的。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/eval/runDetail")
    @Operation(summary = "评测运行明细（每用例一条：是否命中/位次/实际命中）")
    public Result<List<KnowledgeEvalRunDetailVO>> evalRunDetail(@Valid @RequestBody KnowledgeIdDTO dto) {
        return Result.success(knowledgeEvalService.listRunDetail(dto.getId()));
    }

    /**
     * 把所有文档全文重新切分并整库重建向量索引。
     * 用于向量库与文档内容对不上的灾难恢复流程；日常维护只按单篇文档重建。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/rebuild")
    @Operation(summary = "重建向量索引（从 doc 全文重新切分灌入 Milvus）")
    public Result<Void> rebuild() {
        knowledgeStoreService.rebuild();
        return Result.success("索引已重建", null);
    }

    /**
     * 灌入内置示例语料，库里有文档时直接跳过并返回本次灌入条数。
     * 用于「知识库管理」签页的「灌入示例」按钮，给新环境铺一层可问答的底料。
     */
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @PostMapping("/seed")
    @Operation(summary = "灌入内置示例语料（若库为空）")
    public Result<Integer> seed() {
        return Result.success(knowledgeStoreService.seedIfEmpty());
    }
}
