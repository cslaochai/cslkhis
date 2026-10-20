package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.PublicHealthAuditDTO;
import com.his.emr.dto.PublicHealthPageQueryDTO;
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

    /**
     * 上报台账分页：按上报类型、上报状态、患者与关键词收口。
     * 用于公卫上报页的查询卡与上报记录表格。
     */
    @Operation(summary = "分页查询上报记录")
    @PostMapping("/listPage")
    public Result<PageResult<BizPublicHealthReportVO>> listPage(@Valid @RequestBody PublicHealthPageQueryDTO queryDTO) {
        return Result.success(publicHealthReportService.selectReportPage(
                queryDTO.getPatientId(),
                queryDTO.getReportType(),
                queryDTO.getReportStatus(),
                queryDTO.getPatientName(),
                queryDTO.getKeyword(),
                queryDTO.getPageNum(),
                queryDTO.getPageSize()));
    }

    /**
     * 单条上报详情：按上报 id 取完整上报内容与审核结论。
     * 供上报审核环节回看待审上报时取数。
     */
    @Operation(summary = "获取上报详情")
    @GetMapping("/getById")
    public Result<BizPublicHealthReportVO> getById(@RequestParam Long id) {
        return Result.success(publicHealthReportService.getReportDetail(id));
    }

    /**
     * 提交上报：写入即置为已上报，上报人取当前登录态、不采信前端传值。
     * 用于公卫上报页的新增上报弹框保存。
     */
    @PreAuthorize("hasAuthority('emr:publicHealth:add')")
    @Operation(summary = "提交上报（提交即「已上报」，服务端回填上报人）")
    @PostMapping("/submitReport")
    public Result<BizPublicHealthReportVO> submitReport(@Valid @RequestBody PublicHealthSubmitDTO submitDTO) {
        return Result.success("提交成功", publicHealthReportService.submitReport(submitDTO));
    }

    /**
     * 审核上报：只对待审核的上报生效，通过与驳回都留审核人与审核意见。
     * 用于公卫上报页台账行的审核动作。
     */
    @PreAuthorize("hasAuthority('emr:publicHealth:edit')")
    @Operation(summary = "审核上报（仅「待审核」可审；通过置 2，驳回置 3，审核人服务端兜底）")
    @PostMapping("/auditReport")
    public Result<Void> auditReport(@Valid @RequestBody PublicHealthAuditDTO auditDTO) {
        boolean success = publicHealthReportService.auditReport(
                auditDTO.getId(), auditDTO.getApproved(), auditDTO.getAuditBy(), auditDTO.getRemark());
        return success ? Result.success("审核成功", null) : Result.error("审核失败");
    }
}
