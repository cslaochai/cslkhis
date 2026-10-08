package com.his.system.controller;

import com.his.common.base.Result;
import com.his.system.dto.MenuUpsertDTO;
import com.his.system.service.SysMenuService;
import com.his.system.vo.MenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class MenuController {

    private final SysMenuService sysMenuService;

    @Operation(summary = "查询菜单树")
    @PreAuthorize("hasAnyAuthority('system:menu:list', 'system:role:list')")
    @GetMapping("/tree")
    public Result<List<MenuVO>> tree() {
        return Result.success(sysMenuService.tree());
    }

    @Operation(summary = "获取菜单详情")
    @PreAuthorize("hasAnyAuthority('system:menu:list', 'system:role:list')")
    @GetMapping("/getById")
    public Result<MenuVO> getInfo(@RequestParam Long menuId) {
        return Result.success(sysMenuService.getInfo(menuId));
    }

    @Operation(summary = "获取当前用户菜单")
    @GetMapping("/userMenus")
    public Result<List<MenuVO>> userMenus() {
        return Result.success(sysMenuService.userMenus());
    }

    @PreAuthorize("hasAuthority('system:menu:add')")
    @Operation(summary = "新增或修改菜单")
    @PostMapping("/menuUpsert")
    public Result<Void> menuUpsert(@Valid @RequestBody MenuUpsertDTO upsertDTO) {
        return Result.success(sysMenuService.upsert(upsertDTO), null);
    }

    @PreAuthorize("hasAuthority('system:menu:delete')")
    @Operation(summary = "删除菜单")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long menuId) {
        sysMenuService.delete(menuId);
        return Result.success("删除成功", null);
    }
}
