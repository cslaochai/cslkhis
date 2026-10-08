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

    @Operation(summary = "质控单分页（可按来源/类型/状态/结果/关键词过滤）")
    @PostMapping("/listPage")
    public Result<PageResult<BizQualityControlVO>> listPage(@Valid @RequestBody QcQueryPageDTO queryDTO) {
        return Result.success(qualityControlService.selectQcPage(queryDTO));
    }

    @Operation(summary = "质控单详情（含问题明细）")
    @GetMapping("/getById")
    public Result<BizQualityControlVO> getById(@RequestParam Long id) {
        return Result.success(qualityControlService.getQcDetail(id));
    }

    @Operation(summary = "质控概览（总数/通过率/平均分/甲级率/维度分布）")
    @GetMapping("/getOverview")
    public Result<QcOverviewVO> getOverview() {
        return Result.success(qualityControlService.getOverview());
    }

    @Operation(summary = "规则清单与命中统计（含从未命中的规则，empty 标记）")
    @GetMapping("/listRuleMetric")
    public Result<List<QcRuleMetricVO>> listRuleMetric(@RequestParam(required = false) Integer dimension) {
        return Result.success(qualityControlService.listRuleMetric(dimension));
    }

    @Operation(summary = "质控单的问题明细")
    @GetMapping("/listIssueByQc")
    public Result<List<QcIssueVO>> listIssueByQc(@RequestParam Long qcId) {
        return Result.success(qualityControlService.listIssueByQc(qcId));
    }

    @Operation(summary = "待质控病历候选分页（先选来源：OUTPATIENT / INPATIENT）")
    @GetMapping("/listCandidatePage")
    public Result<PageResult<QcCandidateVO>> listCandidatePage(@Valid QcCandidateQueryPageDTO queryDTO) {
        return Result.success(qualityControlService.listCandidatePage(queryDTO));
    }

    @Operation(summary = "维度字典")
    @GetMapping("/dimensionDict")
    public Result<List<QcDimensionSelectListVO>> dimensionDict() {
        return Result.success(qualityControlService.dimensionDict());
    }

    @Operation(summary = "质控类型字典（0-综合 1~3-三维度 4-AI内涵质控）")
    @GetMapping("/qcTypeDict")
    public Result<List<QcTypeSelectListVO>> qcTypeDict() {
        return Result.success(qualityControlService.qcTypeDict());
    }

    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "执行质控（单份病历）")
    @PostMapping("/executeQc")
    public Result<BizQualityControlVO> executeQc(@Valid @RequestBody QcExecuteDTO actionDTO) {
        return Result.success(qualityControlService.executeQc(actionDTO));
    }

    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "执行质控（批量，病案室月末质控）")
    @PostMapping("/executeQcBatch")
    public Result<List<BizQualityControlVO>> executeQcBatch(@Valid @RequestBody QcBatchExecuteDTO actionDTO) {
        return Result.success(qualityControlService.executeQcBatch(
                actionDTO.getRecordSource(), actionDTO.getRecordIds(), actionDTO.getQcType()));
    }

    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "处理质控问题（整改完成 / 忽略）")
    @PostMapping("/handleQc")
    public Result<Void> handleQc(@Valid @RequestBody QcHandleDTO actionDTO) {
        boolean success = qualityControlService.handleQc(actionDTO.getId(), actionDTO.getIgnore(), actionDTO.getRemark());
        return success ? Result.success("处理成功", null) : Result.error("处理失败");
    }
}
