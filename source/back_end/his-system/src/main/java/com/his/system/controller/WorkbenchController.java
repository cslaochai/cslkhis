package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.WorkbenchRoleConfigUpsertDTO;
import com.his.system.dto.WorkbenchWidgetQueryPageDTO;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门户工作台控制器。
 *
 * <p>{@code /config} 与 {@code /data} 是**所有角色**画首页的入口，只要求登录：
 * 挂上权限码会重演 G5b（非管理岗 403、首页开天窗）。真正的闸门在每张卡的
 * {@code permission} 上，由 Service 按当前角色的菜单授权过滤。
 */
@Tag(name = "门户工作台")
@RestController
@RequestMapping("/workbench")
@RequiredArgsConstructor
public class WorkbenchController {

    private final WorkbenchService workbenchService;

    @Operation(summary = "获取当前角色的工作台配置")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/config")
    public Result<WorkbenchConfigVO> config() {
        return Result.success(workbenchService.getConfig());
    }

    @Operation(summary = "获取当前角色全部卡片的聚合数据")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/data")
    public Result<List<WorkbenchDataVO>> data() {
        return Result.success(workbenchService.getData());
    }

    @Operation(summary = "卡片注册表分页查询")
    @PreAuthorize("hasAuthority('system:workbench:config')")
    @PostMapping("/widget/listPage")
    public Result<PageResult<WorkbenchWidgetVO>> widgetListPage(@RequestBody WorkbenchWidgetQueryPageDTO queryDTO) {
        return Result.success(workbenchService.widgetListPage(queryDTO));
    }

    @Operation(summary = "新增或修改卡片")
    @PreAuthorize("hasAuthority('system:workbench:config')")
    @PostMapping("/widgetUpsert")
    public Result<Void> widgetUpsert(@Valid @RequestBody WorkbenchWidgetUpsertDTO upsertDTO) {
        workbenchService.widgetUpsert(upsertDTO);
        return Result.success("保存成功", null);
    }

    @Operation(summary = "删除卡片")
    @PreAuthorize("hasAuthority('system:workbench:config')")
    @DeleteMapping("/widget/deleteById")
    public Result<Void> widgetDelete(@RequestParam Long widgetId) {
        workbenchService.widgetDelete(widgetId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取某角色的卡片配置")
    @PreAuthorize("hasAuthority('system:workbench:config')")
    @GetMapping("/roleConfig")
    public Result<WorkbenchRoleConfigVO> roleConfig(@RequestParam Long roleId) {
        return Result.success(workbenchService.roleConfig(roleId));
    }

    @Operation(summary = "保存某角色的卡片配置")
    @PreAuthorize("hasAuthority('system:workbench:config')")
    @PostMapping("/roleConfigUpsert")
    public Result<Void> roleConfigUpsert(@Valid @RequestBody WorkbenchRoleConfigUpsertDTO upsertDTO) {
        workbenchService.roleConfigUpsert(upsertDTO);
        return Result.success("保存成功", null);
    }
}
