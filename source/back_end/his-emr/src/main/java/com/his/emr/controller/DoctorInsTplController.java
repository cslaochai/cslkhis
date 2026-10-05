package com.his.emr.controller;

import com.his.common.base.Result;
import com.his.emr.dto.BizInspectionTemplateUpsertDTO;
import com.his.emr.service.InspectionTemplService;
import com.his.emr.vo.BizInspectionTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医生工作站 - 检查申请模板控制器
 */
@Tag(name = "医生工作站-检查申请模板")
@RestController
@RequestMapping("/doctor/inspection/template")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:doctorWorkstation:list')")
public class DoctorInsTplController {

    private final InspectionTemplService inspectionTemplService;

    @Operation(summary = "查询检查申请模板列表")
    @GetMapping("/list")
    public Result<List<BizInspectionTemplateVO>> getInspectionTemplates() {
        return Result.success(inspectionTemplService.getInspectionTemplateList());
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "新增检查申请模板")
    @PostMapping("/templateUpsert")
    public Result<Void> addInspectionTemplate(@Valid @RequestBody BizInspectionTemplateUpsertDTO upsertDTO) {
        boolean success = inspectionTemplService.addInspectionTemplate(upsertDTO);
        return success ? Result.success("新增成功", null) : Result.error("新增失败");
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "根据ID删除检查申请模板")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteInspectionTemplate(@RequestParam Long id) {
        boolean success = inspectionTemplService.deleteInspectionTemplate(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }
}
