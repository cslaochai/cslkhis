package com.his.pharmacy.controller;

import com.his.common.base.Result;
import com.his.pharmacy.dto.BizDrugPackageUpsertDTO;
import com.his.pharmacy.service.DrugPackageService;
import com.his.pharmacy.vo.BizDrugPackageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医生工作站 - 药品/耗材套餐（URL 契约沿用 /doctor/template/package*）。
 * 套餐按医生归属、属医生个人参照数据：门诊/急诊两个医生站都可维护与套用
 * （招标条款「急诊医生工作站：维护医嘱套和医嘱模板快捷入口」），故各口放行急诊医生编辑权。
 */
@Tag(name = "医生个人模板-药品套餐")
@RestController
@RequestMapping("/doctor/template")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'opd:emergencyDoctor:edit')")
public class DrugPackageController {

    private final DrugPackageService drugPackageService;

    /**
     * 取当前登录医生的药品/耗材套餐清单。用于「药品/耗材套餐」弹框列表。
     */
    @Operation(summary = "查询药品套餐列表")
    @GetMapping("/packageList")
    public Result<List<BizDrugPackageVO>> listDrugPackages() {
        return Result.success(drugPackageService.listDrugPackages());
    }

    /**
     * 按套餐主键取套餐全套明细。用于医生工作站套用套餐时一次性回填处方与检查检验。
     */
    @Operation(summary = "根据ID查询药品套餐明细")
    @GetMapping("/getPackageById")
    public Result<BizDrugPackageVO> getDrugPackageDetail(@RequestParam Long id) {
        return Result.success(drugPackageService.getDrugPackageDetail(id));
    }

    /**
     * 把当前就诊已开的处方等项目保存为一份套餐。用于「保存为套餐」弹框的提交。
     */
    @PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:add', 'opd:emergencyDoctor:edit')")
    @Operation(summary = "保存药品套餐")
    @PostMapping("/savePackage")
    public Result<Void> saveDrugPackage(@Valid @RequestBody BizDrugPackageUpsertDTO upsertDTO) {
        boolean success = drugPackageService.saveDrugPackage(upsertDTO);
        return success ? Result.success("保存成功", null) : Result.error("保存失败");
    }

    /**
     * 按套餐主键删除一份套餐。用于「药品/耗材套餐」弹框列表行的删除。
     */
    @PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:delete', 'opd:emergencyDoctor:edit')")
    @Operation(summary = "根据ID删除药品套餐")
    @DeleteMapping("/deletePackageById")
    public Result<Void> deleteDrugPackage(@RequestParam Long id) {
        boolean success = drugPackageService.deleteDrugPackage(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }
}
