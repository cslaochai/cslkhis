package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.PublicHealthAuditDTO;
import com.his.emr.dto.PublicHealthQueryPageDTO;
import com.his.emr.dto.PublicHealthSubmitDTO;
import com.his.emr.service.PublicHealthReportService;
import com.his.emr.vo.BizPublicHealthReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 公卫上报控制器（菜单 618 /public-health）。
 */
@Tag(name = "公卫上报")
@RestController
@RequestMapping("/charge/publicHealth")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('emr:publicHealth:list')")
public class PublicHealthReportController {

    private final PublicHealthReportService publicHealthReportService;

    @Operation(summary = "分页查询上报记录")
    @PostMapping("/listPage")
    public Result<PageResult<BizPublicHealthReportVO>> listPage(@Valid @RequestBody PublicHealthQueryPageDTO queryDTO) {
        return Result.success(publicHealthReportService.selectReportPage(
                queryDTO.getPatientId(),
                queryDTO.getReportType(),
                queryDTO.getReportStatus(),
                queryDTO.getPatientName(),
                queryDTO.getKeyword(),
                queryDTO.getPageNum(),
                queryDTO.getPageSize()));
    }

    @Operation(summary = "获取上报详情")
    @GetMapping("/getById")
    public Result<BizPublicHealthReportVO> getById(@RequestParam Long id) {
        return Result.success(publicHealthReportService.getReportDetail(id));
    }

    @PreAuthorize("hasAuthority('emr:publicHealth:add')")
    @Operation(summary = "提交上报（提交即「已上报」，服务端回填上报人）")
    @PostMapping("/submitReport")
    public Result<BizPublicHealthReportVO> submitReport(@Valid @RequestBody PublicHealthSubmitDTO submitDTO) {
        return Result.success("提交成功", publicHealthReportService.submitReport(submitDTO));
    }

    @PreAuthorize("hasAuthority('emr:publicHealth:edit')")
    @Operation(summary = "审核上报（仅「待审核」可审；通过置 2，驳回置 3，审核人服务端兜底）")
    @PostMapping("/auditReport")
    public Result<Void> auditReport(@Valid @RequestBody PublicHealthAuditDTO auditDTO) {
        boolean success = publicHealthReportService.auditReport(
                auditDTO.getId(), auditDTO.getApproved(), auditDTO.getAuditBy(), auditDTO.getRemark());
        return success ? Result.success("审核成功", null) : Result.error("审核失败");
    }
}
