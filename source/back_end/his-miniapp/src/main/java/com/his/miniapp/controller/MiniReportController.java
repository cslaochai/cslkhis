package com.his.miniapp.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.vo.BizReportVO;
import com.his.miniapp.dto.ReportPageQueryDTO;
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

    /**
     * 我的报告分页：按就诊人与报告类别取已发布的检查、检验结果，未发布的一律不出现。
     * 用于小程序「报告查询」页分别拉取检验与检查两列清单。
     */
    @Operation(summary = "我的报告（分页；只返回已发布）")
    @PostMapping("/list")
    public Result<PageResult<BizReportVO>> list(@RequestBody @Valid ReportPageQueryDTO dto) {
        return Result.success(miniReportService.myReportPage(dto));
    }

    /**
     * 报告详情：按报告 ID 取单份结果的项目明细与结论，未发布或不属于本人时拒读。
     * 用于小程序「报告详情」页的结果正文展示。
     */
    @Operation(summary = "报告详情（已发布才可见）")
    @GetMapping("/getById")
    public Result<BizReportVO> getById(@RequestParam Long reportId) {
        return Result.success(miniReportService.myReportDetail(reportId));
    }

    /**
     * 报告原文：按报告 ID 输出可下载查看的 PDF 文件流，无权或无文件时回对应状态码。
     * 用于小程序「报告详情」页的查看原件与打印动作。
     */
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
