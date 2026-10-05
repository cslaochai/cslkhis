package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.OrderDictQueryPageDTO;
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
 *
 * <p>约定：查询 GET、写操作 POST、路径驼峰。
 *
 * <p><b>为什么不复用 {@code /system/dict/dataUpsert}</b>：那个口子要 {@code system:dict:add}，
 * 而医嘱字典挂在住院业务菜单下（{@code ipd:orderDict:*}）—— 为了在这页能保存就把系统字典的
 * 写权限发给医生，等于把「患者性别」「收费项目类别」的改写权一起交出去。
 * 这里另开一个只认三种 dictType 的窄口，权限与数据边界都收在自己手里。
 *
 * <p><b>{@code @PreAuthorize} 全部标在方法上，不标类</b>：类级注解会静默覆盖所有没写自己注解的方法（G5b）。
 */
@Tag(name = "医嘱基础字典")
@RestController
@RequestMapping("/patient/inpatient/order/dict")
@RequiredArgsConstructor
public class OrderDictController {

    private final OrderDictService orderDictService;

    @PreAuthorize("hasAuthority('ipd:orderDict:list')")
    @Operation(summary = "字典分页（管理页按 途径 / 频次 / 剂量单位 分 Tab）")
    @GetMapping("/listPage")
    public Result<IPage<OrderDictListVO>> listPage(@Valid OrderDictQueryPageDTO query) {
        return Result.success(orderDictService.listPage(query));
    }

    @PreAuthorize("hasAuthority('ipd:orderDict:list')")
    @Operation(summary = "启用的字典项（下拉用，与医生站字典缓存同一份数据）")
    @GetMapping("/selectList")
    public Result<List<OrderDictListVO>> selectList(@RequestParam String dictType) {
        return Result.success(orderDictService.selectList(dictType));
    }

    @PreAuthorize("hasAuthority('ipd:orderDict:add')")
    @Operation(summary = "新增/修改字典项（新增和修改同一接口、同一个 :add 权限码）")
    @PostMapping("/upsert")
    public Result<String> upsert(@RequestBody @Valid OrderDictUpsertDTO dto) {
        Long id = orderDictService.upsert(dto);
        return Result.success("已保存", id == null ? null : String.valueOf(id));
    }

    @PreAuthorize("hasAuthority('ipd:orderDict:delete')")
    @Operation(summary = "删除字典项（逻辑删，历史医嘱仍按原值渲染）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id, @RequestParam String dictType) {
        orderDictService.deleteById(id, dictType);
        return Result.success("已删除", null);
    }
}
