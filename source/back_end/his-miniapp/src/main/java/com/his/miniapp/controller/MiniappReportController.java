package com.his.miniapp.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.vo.BizReportVO;
import com.his.miniapp.dto.ReportQueryPageDTO;
import com.his.miniapp.service.MiniappReportService;
import com.his.miniapp.vo.ReportPdfVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 患者端检查/检验报告。就诊人边界与发布状态两道闸在服务层，见 {@code MiniappReportService}。
 */
@Tag(name = "患者端-报告")
@RestController
@RequestMapping("/miniapp/report")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniappReportController {

    private final MiniappReportService miniappReportService;

    @Operation(summary = "我的报告（分页；只返回已发布）")
    @PostMapping("/list")
    public Result<PageResult<BizReportVO>> list(@RequestBody @Valid ReportQueryPageDTO dto) {
        return Result.success(miniappReportService.myReportPage(dto));
    }

    @Operation(summary = "报告详情（已发布才可见）")
    @GetMapping("/getById")
    public Result<BizReportVO> getById(@RequestParam Long reportId) {
        return Result.success(miniappReportService.myReportDetail(reportId));
    }

    @Operation(summary = "报告原文 PDF（打印桩：可打开的占位文档，真对接换 PDF 服务）")
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam Long reportId) {
        ReportPdfVO file = miniappReportService.reportPdf(reportId);
        if (file.getDenyStatus() != null) {
            return ResponseEntity.status(file.getDenyStatus()).build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + file.getFileName())
                .contentType(MediaType.APPLICATION_PDF)
                .body(file.getContent());
    }
}
