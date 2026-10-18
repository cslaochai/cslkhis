package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.SysDrugPageQueryDTO;
import com.his.system.dto.SysDrugSelectDTO;
import com.his.system.dto.SysDrugUpsertDTO;
import com.his.system.service.SysDrugService;
import com.his.system.vo.SysDrugSelectListVO;
import com.his.system.vo.SysDrugVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 药品管理控制器：药品主数据的分页维护与各岗位选药的下拉取数入口。
 */
@Tag(name = "药品管理")
@RestController
@RequestMapping("/system/drug")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'pharmacy:stock:list', 'pharmacy:purchase:list')")
public class DrugController {

    private final SysDrugService sysDrugService;

    /**
     * 按名称、类别等条件分页查询药品目录。
     * 用于药品字典维护环节的列表取数；开方、入库等岗位选药走下拉接口，不用这份分页。
     */
    @Operation(summary = "分页查询药品列表")
    @PostMapping("/listPage")
    public Result<PageResult<SysDrugVO>> listPage(@Valid @RequestBody SysDrugPageQueryDTO queryDTO) {
        return Result.success(sysDrugService.listPage(queryDTO));
    }

    /**
     * 药品下拉。开方/发药/入库/盘点各岗位都要选药，属通用参照数据 →
     * 方法级放开成只要求登录，别让类级的 {@code pharmacy:stock:list} 一类权限把开方端拦成 403。
     */
    @Operation(summary = "药品下拉选择列表（不分页）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/selectList")
    public Result<List<SysDrugSelectListVO>> selectList(@Valid SysDrugSelectDTO queryDTO) {
        return Result.success(sysDrugService.selectList(queryDTO));
    }

    /**
     * 按药品主键取单条药品主数据（规格、单位、剂量、价格等）。
     * 供药品维护环节的表单回显，以及只拿到药品标识就要补齐本品属性的节点使用。
     */
    @Operation(summary = "获取药品详情")
    @GetMapping("/getById")
    public Result<SysDrugVO> getInfo(@RequestParam Long drugId) {
        return Result.success(sysDrugService.getInfo(drugId));
    }

    /**
     * 新增或修改药品，传主键即改、不传即建。
     * 用于药品字典维护环节写入主数据；药房库存与采购都只认这里维护出来的药品。
     */
    @PreAuthorize("hasAuthority('pharmacy:stock:add')")
    @Operation(summary = "新增或修改药品")
    @PostMapping("/drugUpsert")
    public Result<Void> drugUpsert(@Valid @RequestBody SysDrugUpsertDTO upsertDTO) {
        sysDrugService.upsert(upsertDTO);
        return Result.success("操作成功", null);
    }

    /**
     * 按药品主键删除药品。
     * 用于药品字典维护环节下架不再使用的药；已被处方、库存或采购引用过的药品不应当从这里删。
     */
    @PreAuthorize("hasAuthority('pharmacy:stock:delete')")
    @Operation(summary = "删除药品")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long drugId) {
        sysDrugService.delete(drugId);
        return Result.success("删除成功", null);
    }
}
