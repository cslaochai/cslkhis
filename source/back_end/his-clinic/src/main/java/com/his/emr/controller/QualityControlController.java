package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.*;
import com.his.emr.service.QualityControlService;
import com.his.emr.vo.QcIssueVO;
import com.his.emr.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 病案质控
 */
@Tag(name = "病案质控")
@RestController
@RequestMapping("/emr/qualityControl")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('qc:recordQc:list')")
public class QualityControlController {

    private final QualityControlService qualityControlService;

    /**
     * 质控单分页：按病历来源、质控类型、状态、结论与关键词收口。
     * 用于病案质控工作台「质控单」页签的查询卡与表格。
     */
    @Operation(summary = "质控单分页（可按来源/类型/状态/结果/关键词过滤）")
    @PostMapping("/listPage")
    public Result<PageResult<BizQualityControlVO>> listPage(@Valid @RequestBody QcPageQueryDTO queryDTO) {
        return Result.success(qualityControlService.selectQcPage(queryDTO));
    }

    /**
     * 质控单详情：一份病历的得分、结论与其下全部问题明细。
     * 用于「质控单」页签行点开的质控详情，逐条整改在这一屏处理。
     */
    @Operation(summary = "质控单详情（含问题明细）")
    @GetMapping("/getById")
    public Result<BizQualityControlVO> getById(@RequestParam Long id) {
        return Result.success(qualityControlService.getQcDetail(id));
    }

    /**
     * 质控概览：总数、通过率、平均分、甲级率与各维度问题分布的计数。
     * 用于病案质控工作台顶部的概览卡片区。
     */
    @Operation(summary = "质控概览（总数/通过率/平均分/甲级率/维度分布）")
    @GetMapping("/getOverview")
    public Result<QcOverviewVO> getOverview() {
        return Result.success(qualityControlService.getOverview());
    }

    /**
     * 规则清单与命中统计：一条规则一行带命中次数，从未命中的规则也列出并标记为空。
     * 用于「规则清单」页签，质控专员据此判断哪些规则需要修或该下线。
     */
    @Operation(summary = "规则清单与命中统计（含从未命中的规则，empty 标记）")
    @GetMapping("/listRuleMetric")
    public Result<List<QcRuleMetricVO>> listRuleMetric(@RequestParam(required = false) Integer dimension) {
        return Result.success(qualityControlService.listRuleMetric(dimension));
    }

    /**
     * 按质控单取问题明细：逐条缺陷及其定位字段与整改建议。
     * 供质控详情里展开单条质控单缺陷、逐条走整改环节的取数。
     */
    @Operation(summary = "质控单的问题明细")
    @GetMapping("/listIssueByQc")
    public Result<List<QcIssueVO>> listIssueByQc(@RequestParam Long qcId) {
        return Result.success(qualityControlService.listIssueByQc(qcId));
    }

    /**
     * 待质控病历候选分页：先定门诊还是住院来源，再筛出还没出质控单的病历。
     * 用于「待质控病历」页签，单份与批量质控都从这里发起。
     */
    @Operation(summary = "待质控病历候选分页（先选来源：OUTPATIENT / INPATIENT）")
    @GetMapping("/listCandidatePage")
    public Result<PageResult<QcCandidateVO>> listCandidatePage(@Valid QcCandidatePageQueryDTO queryDTO) {
        return Result.success(qualityControlService.listCandidatePage(queryDTO));
    }

    /**
     * 质控维度字典：完整性、规范性、逻辑性等维度的编码与名称。
     * 供按维度筛质控单、看规则清单的下拉取值口径。
     */
    @Operation(summary = "维度字典")
    @GetMapping("/dimensionDict")
    public Result<List<QcDimensionSelectListVO>> dimensionDict() {
        return Result.success(qualityControlService.dimensionDict());
    }

    /**
     * 质控类型字典：综合质控、三个单维度专项质控与 AI 内涵质控的编码与名称。
     * 用于病案质控工作台的类型筛选与执行质控时的类型选择。
     */
    @Operation(summary = "质控类型字典（0-综合 1~3-三维度 4-AI内涵质控）")
    @GetMapping("/qcTypeDict")
    public Result<List<QcTypeSelectListVO>> qcTypeDict() {
        return Result.success(qualityControlService.qcTypeDict());
    }

    /**
     * 执行单份病历质控：跑规则集打分，生成一张质控单与其下问题明细。
     * 用于「待质控病历」页签选中一份病历后的立即质控。
     */
    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "执行质控（单份病历）")
    @PostMapping("/executeQc")
    public Result<BizQualityControlVO> executeQc(@Valid @RequestBody QcExecuteDTO actionDTO) {
        return Result.success(qualityControlService.executeQc(actionDTO));
    }

    /**
     * 批量执行质控：一次请求对同一来源的一批病历出质控单，单次份数有上限。
     * 用于病案室月末集中质控时勾选一批病历整体跑分。
     */
    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "执行质控（批量，病案室月末质控）")
    @PostMapping("/executeQcBatch")
    public Result<List<BizQualityControlVO>> executeQcBatch(@Valid @RequestBody QcBatchExecuteDTO actionDTO) {
        return Result.success(qualityControlService.executeQcBatch(
                actionDTO.getRecordSource(), actionDTO.getRecordIds(), actionDTO.getQcType()));
    }

    /**
     * 处理质控问题：把单条缺陷置为整改完成或忽略，可附整改说明。
     * 用于质控详情里问题明细行的处理动作，处理后概览与台账同步刷新。
     */
    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "处理质控问题（整改完成 / 忽略）")
    @PostMapping("/handleQc")
    public Result<Void> handleQc(@Valid @RequestBody QcHandleDTO actionDTO) {
        boolean success = qualityControlService.handleQc(actionDTO.getId(), actionDTO.getIgnore(), actionDTO.getRemark());
        return success ? Result.success("处理成功", null) : Result.error("处理失败");
    }
}
