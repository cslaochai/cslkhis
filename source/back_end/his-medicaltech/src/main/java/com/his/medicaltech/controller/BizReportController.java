package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.ReportPublishDTO;
import com.his.medicaltech.dto.ReportRecordQueryDTO;
import com.his.medicaltech.service.MedicalTechService;
import com.his.medicaltech.vo.BizReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 医技管理 - 报告控制器
 */
@Tag(name = "医技管理")
@RestController
@RequestMapping("/medicaltech/report")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('emr:infectiousReport:list', 'finance:insurance:list')")
public class BizReportController {

    private final MedicalTechService medicalTechService;

    @Operation(summary = "分页查询报告列表")
    @PostMapping("/list")
    public Result<PageResult<BizReportVO>> reportList(@RequestBody ReportRecordQueryDTO queryDTO) {
        return Result.success(medicalTechService.selectReportPageVO(
                queryDTO.getPatientId(), queryDTO.getReportType(), queryDTO.getReportStatus(),
                queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    @Operation(summary = "根据ID获取报告详情")
    @GetMapping("/getById")
    public Result<BizReportVO> getReportDetail(@RequestParam Long reportId) {
        return Result.success(medicalTechService.getReportDetailVO(reportId));
    }

    @PreAuthorize("hasAuthority('medtech:laboratoryWorkstation:edit')")
    @Operation(summary = "发布报告")
    @PostMapping("/publish")
    public Result<Void> publishReport(@RequestBody ReportPublishDTO publishDTO) {
        boolean success = medicalTechService.publishReport(publishDTO.getReportId(), publishDTO.getPublishBy());
        return success ? Result.success("发布成功", null) : Result.error("发布失败");
    }
}
