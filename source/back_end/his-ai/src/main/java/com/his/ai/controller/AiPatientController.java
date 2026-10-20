package com.his.ai.controller;

import com.his.ai.dto.*;
import com.his.ai.service.*;
import com.his.ai.vo.*;
import com.his.common.base.Result;
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
 * AI 能力 - 患者端
 */
@Tag(name = "AI能力-患者端")
@RestController
@RequestMapping("/ai/patient")
@RequiredArgsConstructor
public class AiPatientController {

    private final PatientReportExplainCapability reportExplainCapability;

    private final PatientImagingExplainCapability imagingExplainCapability;

    private final PatientFeeExplainCapability feeExplainCapability;

    private final PatientMedicationGuideCapability medicationGuideCapability;

    private final PatientTriageNormalizeCapability triageNormalizeCapability;

    private final PrevisitSummaryCapability previsitSummaryCapability;

    @Operation(summary = "报告解读（患者版大白话）")
    @PostMapping("/reportExplain")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<PatientReportExplainVO> reportExplain(@Valid @RequestBody PatientReportExplainDTO dto) {
        return Result.success(reportExplainCapability.execute(dto));
    }

    @Operation(summary = "影像报告解读（患者版大白话，只解读不做诊断）")
    @PostMapping("/imagingExplain")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<PatientImagingExplainVO> imagingExplain(@Valid @RequestBody PatientImagingExplainDTO dto) {
        return Result.success(imagingExplainCapability.execute(dto));
    }

    @Operation(summary = "费用解释（这笔钱怎么算的）")
    @PostMapping("/feeExplain")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<PatientFeeExplainVO> feeExplain(@Valid @RequestBody PatientFeeExplainDTO dto) {
        return Result.success(feeExplainCapability.execute(dto));
    }

    @Operation(summary = "用药说明（这盒药怎么吃）")
    @PostMapping("/medicationGuide")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<PatientMedicationGuideVO> medicationGuide(@Valid @RequestBody PatientMedicationGuideDTO dto) {
        return Result.success(medicationGuideCapability.execute(dto));
    }

    @Operation(summary = "导诊口语归一（把口语整理成症状词）")
    @PostMapping("/triageNormalize")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<PatientTriageNormalizeVO> triageNormalize(@Valid @RequestBody PatientTriageNormalizeDTO dto) {
        return Result.success(triageNormalizeCapability.execute(dto));
    }

    @Operation(summary = "预问诊病史摘要（提交问卷后凝练，写回医生站报告卡）")
    @PostMapping("/previsitSummary")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<PrevisitSummaryVO> previsitSummary(@Valid @RequestBody PrevisitSummaryDTO dto) {
        return Result.success(previsitSummaryCapability.execute(dto));
    }
}
