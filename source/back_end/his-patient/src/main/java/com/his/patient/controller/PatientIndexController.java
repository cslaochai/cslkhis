package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.PatientIndexQueryDTO;
import com.his.patient.dto.PatientMergeDTO;
import com.his.patient.dto.PatientMergeRevertDTO;
import com.his.patient.service.PatientIndexService;
import com.his.patient.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者主索引控制器（P5.1 EMPI）
 */
@Tag(name = "患者主索引(EMPI)")
@RestController
@RequestMapping("/patient/patientIndex")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:empi:list')")
public class PatientIndexController {

    private final PatientIndexService patientIndexService;

    @Operation(summary = "患者主索引分页（含档案完整度与业务数据量）")
    @PostMapping("/listPage")
    public Result<PageResult<PatientIndexVO>> listPage(@Valid @RequestBody PatientIndexQueryDTO queryDTO) {
        return Result.success(patientIndexService.selectIndexPage(queryDTO));
    }

    @Operation(summary = "疑似重复档案检测（分级；返回的是'值得看一眼'而非'应该合并'）")
    @PostMapping("/duplicateList")
    public Result<List<PatientDuplicateGroupVO>> duplicateList(@Valid @RequestBody PatientIndexQueryDTO queryDTO) {
        return Result.success(patientIndexService.detectDuplicates(queryDTO));
    }

    @Operation(summary = "患者主索引详情（含同主档下的其他档案）")
    @GetMapping("/getDetailById")
    public Result<PatientIndexVO> getDetailById(@RequestParam Long patientId) {
        return Result.success(patientIndexService.getIndexDetail(patientId));
    }

    @PreAuthorize("hasAuthority('patient:empi:edit')")
    @Operation(summary = "合并档案（服端重新判定匹配级别；返回合并审计）")
    @PostMapping("/merge")
    public Result<PatientMergeLogVO> merge(@RequestBody @Valid PatientMergeDTO dto) {
        return Result.success(patientIndexService.merge(dto));
    }

    @PreAuthorize("hasAuthority('patient:empi:delete')")
    @Operation(summary = "撤销合并（按审计快照还原）")
    @PostMapping("/revert")
    public Result<PatientMergeLogVO> revert(@RequestBody @Valid PatientMergeRevertDTO dto) {
        return Result.success(patientIndexService.revert(dto));
    }

    @PreAuthorize("hasAuthority('patient:empi:edit')")
    @Operation(summary = "合并历史分页")
    @PostMapping("/mergeLogListPage")
    public Result<PageResult<PatientMergeLogVO>> mergeLogListPage(@Valid @RequestBody PatientIndexQueryDTO queryDTO) {
        return Result.success(patientIndexService.selectMergeLogPage(queryDTO));
    }

    @Operation(summary = "EMPI 概览指标（唯一性/完整性）")
    @GetMapping("/stats")
    public Result<PatientIndexStatVO> stats() {
        return Result.success(patientIndexService.stats());
    }

    @Operation(summary = "字典：匹配级别 / 档案关键字段清单")
    @GetMapping("/dict")
    public Result<PatientIndexDictVO> dict() {
        return Result.success(patientIndexService.dict());
    }
}
