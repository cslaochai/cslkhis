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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 取当前登录医生的常用诊断清单。
     * 用于医生工作站病历区的诊断快捷选择，以及「常用诊断维护」弹框列表。
     */
    @Operation(summary = "查询常用诊断模板列表")
    @GetMapping("/diagList")
    public Result<List<BizDiagTemplateVO>> listDiagTemplates() {
        return Result.success(otherTemplateService.listDiagTemplates());
    }

    /**
     * 整表覆盖保存当前医生的常用诊断清单。
     * 用于「常用诊断维护」弹框增删后的整体回存，以及病历诊断区「加入常用诊断」。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "保存常用诊断模板（全量覆盖）")
    @PostMapping("/saveDiag")
    public Result<Void> saveDiagTemplates(@Valid @RequestBody DiagTemplateUpsertDTO saveDTO) {
        boolean success = otherTemplateService.saveDiagTemplates(saveDTO);
        return success ? Result.success("保存成功", null) : Result.error("保存失败");
    }

    /**
     * 按主键从常用诊断清单中摘掉一条诊断。
     * 用于「常用诊断维护」弹框列表行的删除。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "根据ID删除常用诊断模板")
    @DeleteMapping("/deleteDiagById")
    public Result<Void> deleteDiagTemplate(@RequestParam Long id) {
        boolean success = otherTemplateService.deleteDiagTemplate(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    // 处方模板

    /**
     * 取当前登录医生的处方模板清单。
     * 用于「处方模板管理」弹框列表。
     */
    @Operation(summary = "查询处方模板列表")
    @GetMapping("/rxList")
    public Result<List<BizRxTemplateVO>> listRxTemplates() {
        return Result.success(otherTemplateService.listRxTemplates());
    }

    /**
     * 按模板主键取处方模板的药品明细。
     * 用于医生工作站套用处方模板时回填当前处方。
     */
    @Operation(summary = "根据ID查询处方模板明细")
    @GetMapping("/getRxById")
    public Result<BizRxTemplateVO> getRxTemplateDetail(@RequestParam Long id) {
        return Result.success(otherTemplateService.getRxTemplateDetail(id));
    }

    /**
     * 把处方区当前药品明细存成一张个人处方模板。
     * 用于「处方模板管理」弹框的保存入口。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "保存处方模板")
    @PostMapping("/saveRx")
    public Result<Void> saveRxTemplate(@Valid @RequestBody BizRxTemplateUpsertDTO upsertDTO) {
        boolean success = otherTemplateService.saveRxTemplate(upsertDTO);
        return success ? Result.success("保存成功", null) : Result.error("保存失败");
    }

    /**
     * 按模板主键删除一张处方模板。
     * 用于「处方模板管理」弹框列表行的删除。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "根据ID删除处方模板")
    @DeleteMapping("/deleteRxById")
    public Result<Void> deleteRxTemplate(@RequestParam Long id) {
        boolean success = otherTemplateService.deleteRxTemplate(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    // 药品套餐

    /**
     * 取当前登录医生的药品/耗材套餐清单。
     * 用于「药品/耗材套餐」弹框列表。
     */
    @Operation(summary = "查询药品套餐列表")
    @GetMapping("/packageList")
    public Result<List<BizDrugPackageVO>> listDrugPackages() {
        return Result.success(otherTemplateService.listDrugPackages());
    }

    /**
     * 按套餐主键取套餐全套明细。
     * 用于医生工作站套用套餐时一次性回填处方与检查检验。
     */
    @Operation(summary = "根据ID查询药品套餐明细")
    @GetMapping("/getPackageById")
    public Result<BizDrugPackageVO> getDrugPackageDetail(@RequestParam Long id) {
        return Result.success(otherTemplateService.getDrugPackageDetail(id));
    }

    /**
     * 把当前就诊已开的处方等项目保存为一份套餐。
     * 用于「保存为套餐」弹框的提交。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "保存药品套餐")
    @PostMapping("/savePackage")
    public Result<Void> saveDrugPackage(@Valid @RequestBody BizDrugPackageUpsertDTO upsertDTO) {
        boolean success = otherTemplateService.saveDrugPackage(upsertDTO);
        return success ? Result.success("保存成功", null) : Result.error("保存失败");
    }

    /**
     * 按套餐主键删除一份套餐。
     * 用于「药品/耗材套餐」弹框列表行的删除。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "根据ID删除药品套餐")
    @DeleteMapping("/deletePackageById")
    public Result<Void> deleteDrugPackage(@RequestParam Long id) {
        boolean success = otherTemplateService.deleteDrugPackage(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }
}
