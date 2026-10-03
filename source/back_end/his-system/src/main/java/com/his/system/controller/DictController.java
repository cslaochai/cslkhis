package com.his.system.controller;

import com.his.common.base.Result;
import com.his.system.dto.DictDataQueryDTO;
import com.his.system.dto.SysDictDataUpsertDTO;
import com.his.system.dto.SysDictTypeUpsertDTO;
import com.his.system.service.DictCacheService;
import com.his.system.service.SysDictDataService;
import com.his.system.service.SysDictTypeService;
import com.his.system.vo.DictTypeGroupVO;
import com.his.system.vo.SysDictDataVO;
import com.his.system.vo.SysDictTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 字典管理控制器
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DictController {

    private final SysDictTypeService dictTypeService;
    private final SysDictDataService dictDataService;
    private final DictCacheService dictCacheService;

    @Operation(summary = "查询字典类型列表（下拉/字典缓存取数）")
    @GetMapping("/type/selectList")
    public Result<List<SysDictTypeVO>> typeList() {
        return Result.success(dictTypeService.selectList());
    }

    @Operation(summary = "获取字典类型详情")
    @GetMapping("/type/getById")
    public Result<SysDictTypeVO> getTypeInfo(@RequestParam Long typeId) {
        return Result.success(dictTypeService.getInfo(typeId));
    }

    @Operation(summary = "新增或修改字典类型")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @PostMapping("/typeUpsert")
    public Result<Void> typeUpsert(@RequestBody SysDictTypeUpsertDTO upsertDTO) {
        dictTypeService.upsert(upsertDTO);
        return Result.success("操作成功", null);
    }

    @Operation(summary = "删除字典类型")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @DeleteMapping("/type/deleteById")
    public Result<Void> removeType(@RequestParam Long typeId) {
        dictTypeService.delete(typeId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "根据字典类型查询字典数据（优先从Redis缓存获取）")
    @PostMapping("/data/selectList")
    public Result<List<SysDictDataVO>> getDictDataByType(@RequestBody DictDataQueryDTO queryDTO) {
        return Result.success(dictDataService.selectList(queryDTO));
    }

    @Operation(summary = "批量获取字典数据（按字典类型分组）")
    @PostMapping("/data/selectGroup")
    public Result<List<DictTypeGroupVO>> getDictDataMap(@RequestBody DictDataQueryDTO queryDTO) {
        return Result.success(dictDataService.selectGroup(queryDTO));
    }

    @Operation(summary = "获取字典数据详情")
    @GetMapping("/data/detail/getById")
    public Result<SysDictDataVO> getDataInfo(@RequestParam Long dataId) {
        return Result.success(dictDataService.getInfo(dataId));
    }

    @Operation(summary = "新增或修改字典数据")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @PostMapping("/dataUpsert")
    public Result<Void> dataUpsert(@RequestBody SysDictDataUpsertDTO upsertDTO) {
        dictDataService.upsert(upsertDTO);
        return Result.success("操作成功", null);
    }

    @Operation(summary = "删除字典数据")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @DeleteMapping("/data/deleteById")
    public Result<Void> removeData(@RequestParam Long dataId) {
        dictDataService.delete(dataId);
        return Result.success("删除成功", null);
    }

    @PreAuthorize("hasAuthority('system:dict:edit')")
    @Operation(summary = "刷新所有字典缓存")
    @PostMapping("/refreshCache")
    public Result<Void> refreshCache() {
        dictCacheService.refreshAllDictCache();
        return Result.success("缓存刷新成功", null);
    }

    @PreAuthorize("hasAuthority('system:dict:delete')")
    @Operation(summary = "清空所有字典缓存")
    @DeleteMapping("/clearCache")
    public Result<Void> clearCache() {
        dictCacheService.clearAllDictCache();
        return Result.success("缓存清空成功", null);
    }
}
