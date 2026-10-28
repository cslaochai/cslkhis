package com.his.clinical.controller;

import com.his.common.base.Result;
import com.his.clinical.dto.BizLaboratoryTemplateUpsertDTO;
import com.his.clinical.service.InspectionTemplService;
import com.his.clinical.vo.BizLaboratoryTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 检验申请模板控制器：医生个人检验申请模板的取列表、存模板与删除入口。
 */
@Tag(name = "医生工作站-检验申请模板")
@RestController
@RequestMapping("/doctor/laboratory/template")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'opd:emergencyDoctor:list')")
public class DoctorLabTplController {

    private final InspectionTemplService inspectionTemplService;

    /**
     * 取当前登录医生的检验申请模板清单。
     * 用于医生工作站「模板 / 套餐」下拉里的「检验申请模板」弹框列表。
     */
    @Operation(summary = "查询检验申请模板列表")
    @GetMapping("/list")
    public Result<List<BizLaboratoryTemplateVO>> getLaboratoryTemplates() {
        return Result.success(inspectionTemplService.getLaboratoryTemplateList());
    }

    /**
     * 把一个常用检验项目存为个人模板。
     * 用于「检验申请模板」弹框的「新增模板」保存。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "新增检验申请模板")
    @PostMapping("/templateUpsert")
    public Result<Void> addLaboratoryTemplate(@Valid @RequestBody BizLaboratoryTemplateUpsertDTO upsertDTO) {
        boolean success = inspectionTemplService.addLaboratoryTemplate(upsertDTO);
        return success ? Result.success("新增成功", null) : Result.error("新增失败");
    }

    /**
     * 按模板主键删除一名医生的检验申请模板。
     * 用于「检验申请模板」弹框列表行的删除按钮。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "根据ID删除检验申请模板")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteLaboratoryTemplate(@RequestParam Long id) {
        boolean success = inspectionTemplService.deleteLaboratoryTemplate(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }
}
