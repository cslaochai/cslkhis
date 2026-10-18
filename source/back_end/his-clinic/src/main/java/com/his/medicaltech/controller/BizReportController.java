package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.ReportPublishDTO;
import com.his.medicaltech.dto.ReportRecordQueryDTO;
import com.his.medicaltech.service.MedicalTechService;
import com.his.medicaltech.vo.BizReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 医技报告控制器：检查与检验等报告的分页检索、详情读取与发布的接口入口。
 */
@Tag(name = "医技管理")
@RestController
@RequestMapping("/medicaltech/report")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('emr:infectiousReport:list', 'finance:insurance:list')")
public class BizReportController {

    private final MedicalTechService medicalTechService;

    /**
     * 报告分页：按患者、报告类型与报告状态筛出报告行。
     * 供按患者汇总历次报告的环节取数，如患者侧报告列表只查已发布状态。
     */
    @Operation(summary = "分页查询报告列表")
    @PostMapping("/list")
    public Result<PageResult<BizReportVO>> reportList(@Valid @RequestBody ReportRecordQueryDTO queryDTO) {
        return Result.success(medicalTechService.selectReportPageVO(
                queryDTO.getPatientId(), queryDTO.getReportType(), queryDTO.getReportStatus(),
                queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    /**
     * 按报告取详情，连同影像帧一起返回。
     * 供报告查看与打印环节取数。
     *
     * @return 报告不存在时返回空数据而非报错
     */
    @Operation(summary = "根据ID获取报告详情")
    @GetMapping("/getById")
    public Result<BizReportVO> getReportDetail(@RequestParam Long reportId) {
        return Result.success(medicalTechService.getReportDetailVO(reportId));
    }

    /**
     * 发布报告：置为已发布并记发布人与发布时间。
     * 供报告审核通过后的对外发布动作，发布后患者侧才检索得到。
     */
    @PreAuthorize("hasAuthority('medtech:laboratoryWorkstation:edit')")
    @Operation(summary = "发布报告")
    @PostMapping("/publish")
    public Result<Void> publishReport(@Valid @RequestBody ReportPublishDTO publishDTO) {
        boolean success = medicalTechService.publishReport(publishDTO.getReportId(), publishDTO.getPublishBy());
        return success ? Result.success("发布成功", null) : Result.error("发布失败");
    }
}
