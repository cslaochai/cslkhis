package com.his.emr.controller;

import com.his.common.base.Result;
import com.his.emr.dto.BizLaboratoryTemplateUpsertDTO;
import com.his.emr.service.InspectionTemplService;
import com.his.emr.vo.BizLaboratoryTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 医生工作站 - 检验申请模板控制器
 */
@Tag(name = "医生工作站-检验申请模板")
@RestController
@RequestMapping("/doctor/laboratory/template")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:doctorWorkstation:list')")
public class DoctorLabTplController {

    private final InspectionTemplService inspectionTemplService;

    @Operation(summary = "查询检验申请模板列表")
    @GetMapping("/list")
    public Result<List<BizLaboratoryTemplateVO>> getLaboratoryTemplates() {
        return Result.success(inspectionTemplService.getLaboratoryTemplateList());
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "新增检验申请模板")
    @PostMapping("/templateUpsert")
    public Result<Void> addLaboratoryTemplate(@RequestBody BizLaboratoryTemplateUpsertDTO upsertDTO) {
        boolean success = inspectionTemplService.addLaboratoryTemplate(upsertDTO);
        return success ? Result.success("新增成功", null) : Result.error("新增失败");
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "根据ID删除检验申请模板")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteLaboratoryTemplate(@RequestParam Long id) {
        boolean success = inspectionTemplService.deleteLaboratoryTemplate(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }
}
