package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.ArchiveQueryPageDTO;
import com.his.emr.service.MedicalRecordArchiveService;
import com.his.emr.vo.BizMedicalRecordArchiveVO;
import com.his.emr.vo.MedicalRecordArchiveCountVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 病历归档控制器
 */
@Tag(name = "病历归档")
@RestController
@RequestMapping("/charge/archive")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('emr:archiveBorrow:list', 'emr:archive:list')")
public class MedicalRecordArchiveController {

    private final MedicalRecordArchiveService medicalRecordArchiveService;

    @Operation(summary = "分页查询归档记录")
    @PostMapping("/listPage")
    public Result<PageResult<BizMedicalRecordArchiveVO>> listPage(@Valid @RequestBody ArchiveQueryPageDTO queryDTO) {
        return Result.success(medicalRecordArchiveService.selectArchivePage(queryDTO.getPatientId(), queryDTO.getArchiveStatus(),
                queryDTO.getKeyword(), queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    @Operation(summary = "获取归档详情")
    @GetMapping("/getById")
    public Result<BizMedicalRecordArchiveVO> getById(@RequestParam Long id) {
        return Result.success(medicalRecordArchiveService.getArchiveDetail(id));
    }

    @PreAuthorize("hasAuthority('emr:archive:edit')")
    @Operation(summary = "归档病历")
    @PostMapping("/archive")
    public Result<Void> archive(@RequestParam Long id) {
        boolean success = medicalRecordArchiveService.archive(id);
        return success ? Result.success("归档成功", null) : Result.error("归档失败");
    }

    @PreAuthorize("hasAuthority('emr:archive:edit')")
    @Operation(summary = "封存病历")
    @PostMapping("/seal")
    public Result<Void> seal(@RequestParam Long id) {
        boolean success = medicalRecordArchiveService.seal(id);
        return success ? Result.success("封存成功", null) : Result.error("封存失败");
    }

    @Operation(summary = "归档三态计数（待归档 / 已归档 / 已封存）")
    @GetMapping("/statusCount")
    public Result<MedicalRecordArchiveCountVO> statusCount() {
        return Result.success(medicalRecordArchiveService.statusCount());
    }

    @PreAuthorize("hasAuthority('emr:archive:edit')")
    @Operation(summary = "手动补跑归档超期提醒")
    @PostMapping("/notifyOverdue")
    public Result<Integer> notifyOverdue() {
        int sent = medicalRecordArchiveService.notifyOverdueArchives();
        return Result.success("已发送 " + sent + " 条归档超期提醒", sent);
    }
}
