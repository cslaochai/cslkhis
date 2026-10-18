package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.OrderDictPageQueryDTO;
import com.his.patient.dto.OrderDictUpsertDTO;
import com.his.patient.service.OrderDictService;
import com.his.patient.vo.OrderDictListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医嘱基础字典：给药途径 / 用药频次 / 剂量单位（sql/142）。
 */
@Tag(name = "医嘱基础字典")
@RestController
@RequestMapping("/patient/inpatient/order/dict")
@RequiredArgsConstructor
public class OrderDictController {

    private final OrderDictService orderDictService;

    /**
     * 按字典类别分页查询医嘱基础字典项，含停用项。
     * 用于医嘱基础字典管理页各分类页签的表格。
     */
    @PreAuthorize("hasAuthority('ipd:orderDict:list')")
    @Operation(summary = "字典分页（管理页按 途径 / 频次 / 剂量单位 分 Tab）")
    @GetMapping("/listPage")
    public Result<IPage<OrderDictListVO>> listPage(@Valid OrderDictPageQueryDTO query) {
        return Result.success(orderDictService.listPage(query));
    }

    /**
     * 取某一类医嘱基础字典的启用项，作为下拉候选。
     * 供医嘱录入侧把途径、频次、剂量单位翻译成可选项时取数。
     */
    @PreAuthorize("hasAuthority('ipd:orderDict:list')")
    @Operation(summary = "启用的字典项（下拉用，与医生站字典缓存同一份数据）")
    @GetMapping("/selectList")
    public Result<List<OrderDictListVO>> selectList(@RequestParam String dictType) {
        return Result.success(orderDictService.selectList(dictType));
    }

    /**
     * 新增或修改一条医嘱基础字典项，已用过的字典值不可改，要换值只能停用旧项再新增。
     * 用于医嘱基础字典管理页的新增/修改弹框保存。
     */
    @PreAuthorize("hasAuthority('ipd:orderDict:add')")
    @Operation(summary = "新增/修改字典项（新增和修改同一接口、同一个 :add 权限码）")
    @PostMapping("/upsert")
    public Result<String> upsert(@RequestBody @Valid OrderDictUpsertDTO dto) {
        Long id = orderDictService.upsert(dto);
        return Result.success("已保存", id == null ? null : String.valueOf(id));
    }

    /**
     * 按 id 与类别删除一条医嘱基础字典项，存量医嘱仍按其原值渲染。
     * 用于医嘱基础字典管理页行操作「删除」的二次确认之后。
     */
    @PreAuthorize("hasAuthority('ipd:orderDict:delete')")
    @Operation(summary = "删除字典项（逻辑删，历史医嘱仍按原值渲染）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id, @RequestParam String dictType) {
        orderDictService.deleteById(id, dictType);
        return Result.success("已删除", null);
    }
}
