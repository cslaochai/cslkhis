package com.his.emr.controller;

import com.his.common.base.Result;
import com.his.emr.dto.BizDrugPackageUpsertDTO;
import com.his.emr.dto.BizRxTemplateUpsertDTO;
import com.his.emr.dto.DiagTemplateUpsertDTO;
import com.his.emr.service.OtherTemplateService;
import com.his.emr.vo.BizDiagTemplateVO;
import com.his.emr.vo.BizDrugPackageVO;
import com.his.emr.vo.BizRxTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 医生工作站 - 医生个人模板控制器（常用诊断 / 处方模板 / 药品套餐）
 */
@Tag(name = "医生个人模板")
@RestController
@RequestMapping("/doctor/template")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:doctorWorkstation:list')")
public class DoctorTemplateController {

    private final OtherTemplateService otherTemplateService;

    // 常用诊断

    @Operation(summary = "查询常用诊断模板列表")
    @GetMapping("/diagList")
    public Result<List<BizDiagTemplateVO>> listDiagTemplates() {
        return Result.success(otherTemplateService.listDiagTemplates());
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "保存常用诊断模板（全量覆盖）")
    @PostMapping("/saveDiag")
    public Result<Void> saveDiagTemplates(@RequestBody DiagTemplateUpsertDTO saveDTO) {
        boolean success = otherTemplateService.saveDiagTemplates(saveDTO);
        return success ? Result.success("保存成功", null) : Result.error("保存失败");
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "根据ID删除常用诊断模板")
    @DeleteMapping("/deleteDiagById")
    public Result<Void> deleteDiagTemplate(@RequestParam Long id) {
        boolean success = otherTemplateService.deleteDiagTemplate(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    // 处方模板

    @Operation(summary = "查询处方模板列表")
    @GetMapping("/rxList")
    public Result<List<BizRxTemplateVO>> listRxTemplates() {
        return Result.success(otherTemplateService.listRxTemplates());
    }

    @Operation(summary = "根据ID查询处方模板明细")
    @GetMapping("/getRxById")
    public Result<BizRxTemplateVO> getRxTemplateDetail(@RequestParam Long id) {
        return Result.success(otherTemplateService.getRxTemplateDetail(id));
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "保存处方模板")
    @PostMapping("/saveRx")
    public Result<Void> saveRxTemplate(@RequestBody BizRxTemplateUpsertDTO upsertDTO) {
        boolean success = otherTemplateService.saveRxTemplate(upsertDTO);
        return success ? Result.success("保存成功", null) : Result.error("保存失败");
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "根据ID删除处方模板")
    @DeleteMapping("/deleteRxById")
    public Result<Void> deleteRxTemplate(@RequestParam Long id) {
        boolean success = otherTemplateService.deleteRxTemplate(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    // 药品套餐

    @Operation(summary = "查询药品套餐列表")
    @GetMapping("/packageList")
    public Result<List<BizDrugPackageVO>> listDrugPackages() {
        return Result.success(otherTemplateService.listDrugPackages());
    }

    @Operation(summary = "根据ID查询药品套餐明细")
    @GetMapping("/getPackageById")
    public Result<BizDrugPackageVO> getDrugPackageDetail(@RequestParam Long id) {
        return Result.success(otherTemplateService.getDrugPackageDetail(id));
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "保存药品套餐")
    @PostMapping("/savePackage")
    public Result<Void> saveDrugPackage(@RequestBody BizDrugPackageUpsertDTO upsertDTO) {
        boolean success = otherTemplateService.saveDrugPackage(upsertDTO);
        return success ? Result.success("保存成功", null) : Result.error("保存失败");
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "根据ID删除药品套餐")
    @DeleteMapping("/deletePackageById")
    public Result<Void> deleteDrugPackage(@RequestParam Long id) {
        boolean success = otherTemplateService.deleteDrugPackage(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }
}
