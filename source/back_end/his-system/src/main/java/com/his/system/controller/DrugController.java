package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.SysDrugQueryPageDTO;
import com.his.system.dto.SysDrugSelectDTO;
import com.his.system.dto.SysDrugUpsertDTO;
import com.his.system.service.SysDrugService;
import com.his.system.vo.SysDrugSelectListVO;
import com.his.system.vo.SysDrugVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 药品管理控制器
 */
@Tag(name = "药品管理")
@RestController
@RequestMapping("/system/drug")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'pharmacy:stock:list', 'pharmacy:purchase:list')")
public class DrugController {

    private final SysDrugService drugService;

    @Operation(summary = "分页查询药品列表")
    @PostMapping("/listPage")
    public Result<PageResult<SysDrugVO>> listPage(@RequestBody SysDrugQueryPageDTO queryDTO) {
        return Result.success(drugService.listPage(queryDTO));
    }

    /**
     * 药品下拉。开方/发药/入库/盘点各岗位都要选药，属通用参照数据 →
     * 方法级放开成只要求登录，别让类级的 {@code pharmacy:stock:list} 一类权限把开方端拦成 403。
     */
    @Operation(summary = "药品下拉选择列表（不分页）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/selectList")
    public Result<List<SysDrugSelectListVO>> selectList(SysDrugSelectDTO queryDTO) {
        return Result.success(drugService.selectList(queryDTO));
    }

    @Operation(summary = "获取药品详情")
    @GetMapping("/getById")
    public Result<SysDrugVO> getInfo(@RequestParam Long drugId) {
        return Result.success(drugService.getInfo(drugId));
    }

    @PreAuthorize("hasAuthority('pharmacy:stock:add')")
    @Operation(summary = "新增或修改药品")
    @PostMapping("/drugUpsert")
    public Result<Void> drugUpsert(@RequestBody SysDrugUpsertDTO upsertDTO) {
        drugService.upsert(upsertDTO);
        return Result.success("操作成功", null);
    }

    @PreAuthorize("hasAuthority('pharmacy:stock:delete')")
    @Operation(summary = "删除药品")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long drugId) {
        drugService.delete(drugId);
        return Result.success("删除成功", null);
    }
}
