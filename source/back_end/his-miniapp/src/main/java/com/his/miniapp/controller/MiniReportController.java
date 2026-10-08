package com.his.miniapp.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.vo.BizReportVO;
import com.his.miniapp.dto.ReportQueryPageDTO;
import com.his.miniapp.service.MiniReportService;
import com.his.miniapp.vo.MiniReportPdfVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 患者端检查/检验报告
 */
@Tag(name = "患者端-报告")
@RestController
@RequestMapping("/miniapp/report")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniReportController {

    private final MiniReportService miniReportService;

    @Operation(summary = "我的报告（分页；只返回已发布）")
    @PostMapping("/list")
    public Result<PageResult<BizReportVO>> list(@RequestBody @Valid ReportQueryPageDTO dto) {
        return Result.success(miniReportService.myReportPage(dto));
    }

    @Operation(summary = "报告详情（已发布才可见）")
    @GetMapping("/getById")
    public Result<BizReportVO> getById(@RequestParam Long reportId) {
        return Result.success(miniReportService.myReportDetail(reportId));
    }

    @Operation(summary = "报告原文 PDF（打印：可打开的占位文档，真对接换 PDF 服务）")
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam Long reportId) {
        MiniReportPdfVO file = miniReportService.reportPdf(reportId);
        if (file.getDenyStatus() != null) {
            return ResponseEntity.status(file.getDenyStatus()).build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + file.getFileName())
                .contentType(MediaType.APPLICATION_PDF)
                .body(file.getContent());
    }
}
