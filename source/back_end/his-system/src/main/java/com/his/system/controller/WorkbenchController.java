package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.WorkbenchRoleConfigUpsertDTO;
import com.his.system.dto.WorkbenchWidgetPageQueryDTO;
import com.his.system.dto.WorkbenchWidgetUpsertDTO;
import com.his.system.service.WorkbenchService;
import com.his.system.vo.WorkbenchConfigVO;
import com.his.system.vo.WorkbenchDataVO;
import com.his.system.vo.WorkbenchRoleConfigVO;
import com.his.system.vo.WorkbenchWidgetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门户工作台控制器：登录后首页卡片的配置与取数，以及卡片注册表、角色卡片配置的维护入口。
 */
@Tag(name = "门户工作台")
@RestController
@RequestMapping("/workbench")
@RequiredArgsConstructor
public class WorkbenchController {

    private final WorkbenchService workbenchService;

    /**
     * 取当前登录角色的工作台配置：角色编码、登录落点范围与要渲染的卡片清单（按展示顺序）。
     * 用于首页工作台外壳铺卡片前的一次性拉取，外壳只认卡片编码不认角色。
     */
    @Operation(summary = "获取当前角色的工作台配置")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/config")
    public Result<WorkbenchConfigVO> config() {
        return Result.success(workbenchService.getConfig());
    }

    /**
     * 一次取回当前角色全部卡片的聚合数据，每卡按编码索引，单卡取数失败只在该卡上带说明。
     * 用于首页工作台的内容取数：一份请求喂满整页卡片，某卡挂掉不影响其余卡片渲染。
     */
    @Operation(summary = "获取当前角色全部卡片的聚合数据")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/data")
    public Result<List<WorkbenchDataVO>> data() {
        return Result.success(workbenchService.getData());
    }

    /**
     * 按编码、名称等条件分页查询卡片注册表。
     * 用于系统管理「工作台卡片管理」页的查询卡与表格。
     */
    @Operation(summary = "卡片注册表分页查询")
    @PreAuthorize("hasAuthority('system:workbench:config')")
    @PostMapping("/widget/listPage")
    public Result<PageResult<WorkbenchWidgetVO>> widgetListPage(@Valid @RequestBody WorkbenchWidgetPageQueryDTO queryDTO) {
        return Result.success(workbenchService.widgetListPage(queryDTO));
    }

    /**
     * 新增或修改卡片注册项（卡片编码、名称、所属区块、取数键与占宽等），传主键即改、不传即建。
     * 用于「工作台卡片管理」页的新增与编辑弹框保存，改完该角色首页按下一次取数就按新配置走。
     */
    @Operation(summary = "新增或修改卡片")
    @PreAuthorize("hasAuthority('system:workbench:config')")
    @PostMapping("/widgetUpsert")
    public Result<Void> widgetUpsert(@Valid @RequestBody WorkbenchWidgetUpsertDTO upsertDTO) {
        workbenchService.widgetUpsert(upsertDTO);
        return Result.success("保存成功", null);
    }

    /**
     * 按卡片主键删除注册项，物理删。
     * 用于「工作台卡片管理」页表格行的删除动作；这张卡在各角色配置里的引用行会一并清掉，不留指向已删卡片的孤儿配置。
     */
    @Operation(summary = "删除卡片")
    @PreAuthorize("hasAuthority('system:workbench:config')")
    @DeleteMapping("/widget/deleteById")
    public Result<Void> widgetDelete(@RequestParam Long widgetId) {
        workbenchService.widgetDelete(widgetId);
        return Result.success("删除成功", null);
    }

    /**
     * 取指定角色已勾选的卡片与登录落点配置。
     * 用于系统管理「角色管理」的工作台配置弹框回显。
     */
    @Operation(summary = "获取某角色的卡片配置")
    @PreAuthorize("hasAuthority('system:workbench:config')")
    @GetMapping("/roleConfig")
    public Result<WorkbenchRoleConfigVO> roleConfig(@RequestParam Long roleId) {
        return Result.success(workbenchService.roleConfig(roleId));
    }

    /**
     * 保存指定角色能看到的卡片集合与登录落点。
     * 用于同一个配置弹框的保存：勾完卡片整体落库，该角色下次进首页就是这套卡片。
     */
    @Operation(summary = "保存某角色的卡片配置")
    @PreAuthorize("hasAuthority('system:workbench:config')")
    @PostMapping("/roleConfigUpsert")
    public Result<Void> roleConfigUpsert(@Valid @RequestBody WorkbenchRoleConfigUpsertDTO upsertDTO) {
        workbenchService.roleConfigUpsert(upsertDTO);
        return Result.success("保存成功", null);
    }
}
