package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.*;
import com.his.system.service.MedicalItemService;
import com.his.system.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "检查检验项目管理")
@RestController
@RequestMapping("/system/medical-item")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'medtech:laboratoryWorkstation:list', 'medtech:inspectionItems:list', 'medtech:laboratoryItems:list')")
public class MedicalItemController {

    private final MedicalItemService medicalItemService;

    // 检查项目

    @Operation(summary = "查询检查项目列表")
    @PostMapping("/inspectionListPage")
    public Result<PageResult<SysInspectionItemVO>> inspectionListPage(@Valid @RequestBody SysInspectionItemQueryPageDTO queryDTO) {
        return Result.success(medicalItemService.inspectionListPage(queryDTO));
    }

    /**
     * 检查项目下拉：开单、预约、报告录入都要用 → 只要求登录，不受类级岗位权限码限制。
     *
     * <p>「全量预载」和「输入关键字搜索」是同一个下拉的两种用法，合成一个接口：
     * 不传 keyword/limit 返回全部启用项目，传了就在项目名/项目码上模糊匹配并截断条数。
     */
    @Operation(summary = "检查项目下拉选择列表（可按关键字，不分页）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/inspection/selectList")
    public Result<List<SysInspectionItemSelectListVO>> inspectionSelectList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer limit) {
        return Result.success(medicalItemService.inspectionSelectList(keyword, limit));
    }

    @Operation(summary = "获取检查项目详情")
    @GetMapping("/inspection/getById")
    public Result<SysInspectionItemVO> getInspectionItem(@RequestParam Long id) {
        return Result.success(medicalItemService.getInspectionItem(id));
    }

    @PreAuthorize("hasAuthority('medtech:inspectionItems:add')")
    @Operation(summary = "新增或修改检查项目")
    @PostMapping("/inspectionUpsert")
    public Result<Void> inspectionUpsert(@Valid @RequestBody SysInspectionItemUpsertDTO upsertDTO) {
        medicalItemService.inspectionUpsert(upsertDTO);
        return Result.success("操作成功", null);
    }

    @PreAuthorize("hasAuthority('medtech:inspectionItems:delete')")
    @Operation(summary = "删除检查项目")
    @DeleteMapping("/inspection/deleteById")
    public Result<Void> deleteInspectionItem(@RequestParam Long id) {
        medicalItemService.deleteInspectionItem(id);
        return Result.success("删除成功", null);
    }

    // 检验项目

    @Operation(summary = "查询检验项目列表")
    @PostMapping("/laboratoryListPage")
    public Result<PageResult<SysLaboratoryItemVO>> laboratoryListPage(@Valid @RequestBody SysLaboratoryItemQueryPageDTO queryDTO) {
        return Result.success(medicalItemService.laboratoryListPage(queryDTO));
    }

    /**
     * 检验项目下拉：开单、标本采集、结果录入都要用 → 只要求登录。
     * 同检查项目：不传参数返回全部启用项目，传 keyword/limit 即关键字检索。
     */
    @Operation(summary = "检验项目下拉选择列表（可按关键字，不分页）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/laboratory/selectList")
    public Result<List<SysLaboratoryItemSelectListVO>> laboratorySelectList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer limit) {
        return Result.success(medicalItemService.laboratorySelectList(keyword, limit));
    }

    @Operation(summary = "获取检验项目详情")
    @GetMapping("/laboratory/getById")
    public Result<SysLaboratoryItemVO> getLaboratoryItem(@RequestParam Long id) {
        return Result.success(medicalItemService.getLaboratoryItem(id));
    }

    @PreAuthorize("hasAuthority('medtech:inspectionItems:add')")
    @Operation(summary = "新增或修改检验项目")
    @PostMapping("/laboratoryUpsert")
    public Result<Void> laboratoryUpsert(@Valid @RequestBody SysLaboratoryItemUpsertDTO upsertDTO) {
        medicalItemService.laboratoryUpsert(upsertDTO);
        return Result.success("操作成功", null);
    }

    @PreAuthorize("hasAuthority('medtech:inspectionItems:delete')")
    @Operation(summary = "删除检验项目")
    @DeleteMapping("/laboratory/deleteById")
    public Result<Void> deleteLaboratoryItem(@RequestParam Long id) {
        medicalItemService.deleteLaboratoryItem(id);
        return Result.success("删除成功", null);
    }

    // 检验项目明细

    @Operation(summary = "获取检验项目明细列表")
    @GetMapping("/laboratory/detail/list")
    public Result<List<SysLaboratoryItemDetailVO>> getLaboratoryItemDetailList(@RequestParam Long laboratoryItemId) {
        return Result.success(medicalItemService.laboratoryDetailList(laboratoryItemId));
    }

    @PreAuthorize("hasAuthority('medtech:inspectionItems:add')")
    @Operation(summary = "新增或修改检验项目明细")
    @PostMapping("/laboratory/detailUpsert")
    public Result<Void> laboratoryItemDetailUpsert(@Valid @RequestBody SysLaboratoryItemDetailUpsertDTO upsertDTO) {
        medicalItemService.laboratoryDetailUpsert(upsertDTO);
        return Result.success("操作成功", null);
    }

    @PreAuthorize("hasAuthority('medtech:inspectionItems:delete')")
    @Operation(summary = "删除检验项目明细")
    @DeleteMapping("/laboratory/detail/deleteById")
    public Result<Void> deleteLaboratoryItemDetail(@RequestParam Long id) {
        medicalItemService.deleteLaboratoryDetail(id);
        return Result.success("删除成功", null);
    }
}
