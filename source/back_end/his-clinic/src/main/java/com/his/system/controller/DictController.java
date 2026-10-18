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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理控制器：数据字典的类型与字典项维护、按类型取数以及字典缓存重建的接口入口。
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DictController {

    private final SysDictTypeService sysDictTypeService;
    private final SysDictDataService sysDictDataService;
    private final DictCacheService dictCacheService;

    /**
     * 取全部字典类型（含系统内置与操作员自建）。
     * 用于系统管理「数据字典」页左栏的类型清单，也是自建类型行的编辑、删除入口所依赖的那份列表。
     */
    @Operation(summary = "查询字典类型列表（下拉/字典缓存取数）")
    @GetMapping("/type/selectList")
    public Result<List<SysDictTypeVO>> typeList() {
        return Result.success(sysDictTypeService.selectList());
    }

    /**
     * 按类型主键取单个字典类型的定义。
     * 供只拿到类型主键、需要整条类型信息回显的环节使用；页面左栏走的是类型列表接口。
     */
    @Operation(summary = "获取字典类型详情")
    @GetMapping("/type/getById")
    public Result<SysDictTypeVO> getTypeInfo(@RequestParam Long typeId) {
        return Result.success(sysDictTypeService.getInfo(typeId));
    }

    /**
     * 新增或修改字典类型，传主键即改、不传即建。
     * 用于数据字典页左栏的新增与编辑类型弹框保存，页面只对自建类型开放这两个入口。
     */
    @Operation(summary = "新增或修改字典类型")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @PostMapping("/typeUpsert")
    public Result<Void> typeUpsert(@Valid @RequestBody SysDictTypeUpsertDTO upsertDTO) {
        sysDictTypeService.upsert(upsertDTO);
        return Result.success("操作成功", null);
    }

    /**
     * 按类型主键删除字典类型。
     * 用于数据字典页左栏类型行的删除确认，页面只对自建类型开放这个入口。
     */
    @Operation(summary = "删除字典类型")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @DeleteMapping("/type/deleteById")
    public Result<Void> removeType(@RequestParam Long typeId) {
        sysDictTypeService.delete(typeId);
        return Result.success("删除成功", null);
    }

    /**
     * 按一个或多个字典类型编码取字典项，优先命中缓存。
     * 用于各业务页面挂载时拉下拉候选、把码值翻译成名称。
     */
    @Operation(summary = "根据字典类型查询字典数据（优先从Redis缓存获取）")
    @PostMapping("/data/selectList")
    public Result<List<SysDictDataVO>> getDictDataByType(@Valid @RequestBody DictDataQueryDTO queryDTO) {
        return Result.success(sysDictDataService.selectList(queryDTO));
    }

    /**
     * 批量取字典项并按字典类型分组返回，一次往返拿齐多套字典。
     * 用于一个页面同时需要好几个字典（性别、状态、类别等）做筛选下拉与列表码值翻译的场景。
     */
    @Operation(summary = "批量获取字典数据（按字典类型分组）")
    @PostMapping("/data/selectGroup")
    public Result<List<DictTypeGroupVO>> getDictDataMap(@Valid @RequestBody DictDataQueryDTO queryDTO) {
        return Result.success(sysDictDataService.selectGroup(queryDTO));
    }

    /**
     * 按字典项主键取单条字典项。
     * 供需要单独回显一条字典项（如按字典项反查其所属类型与扩展值）的环节使用；右栏表格走类型取数接口。
     */
    @Operation(summary = "获取字典数据详情")
    @GetMapping("/data/detail/getById")
    public Result<SysDictDataVO> getDataInfo(@RequestParam Long dataId) {
        return Result.success(sysDictDataService.getInfo(dataId));
    }

    /**
     * 新增或修改字典项，传主键即改、不传即建。
     * 用于数据字典页右栏字典值的新增与编辑弹框保存；停用的字典项只在下拉里被过滤掉，不删数据。
     */
    @Operation(summary = "新增或修改字典数据")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @PostMapping("/dataUpsert")
    public Result<Void> dataUpsert(@Valid @RequestBody SysDictDataUpsertDTO upsertDTO) {
        sysDictDataService.upsert(upsertDTO);
        return Result.success("操作成功", null);
    }

    /**
     * 按字典项主键删除字典项。
     * 用于数据字典页右栏字典值行的删除确认；只想让某个取值不再可选时用停用，不要用删除。
     */
    @Operation(summary = "删除字典数据")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @DeleteMapping("/data/deleteById")
    public Result<Void> removeData(@RequestParam Long dataId) {
        sysDictDataService.delete(dataId);
        return Result.success("删除成功", null);
    }

    /**
     * 重建全部字典缓存。
     * 用于字典项在库端直接变更或批量铺底导入后，让读缓存的码值翻译立刻拿到最新内容，不必重启应用。
     */
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @Operation(summary = "刷新所有字典缓存")
    @PostMapping("/refreshCache")
    public Result<Void> refreshCache() {
        dictCacheService.refreshAllDictCache();
        return Result.success("缓存刷新成功", null);
    }

    /**
     * 清空全部字典缓存。
     * 用于缓存内容怀疑与实际字典不一致时的运维动作，清掉后由读取侧回源重建。
     */
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @Operation(summary = "清空所有字典缓存")
    @DeleteMapping("/clearCache")
    public Result<Void> clearCache() {
        dictCacheService.clearAllDictCache();
        return Result.success("缓存清空成功", null);
    }
}
