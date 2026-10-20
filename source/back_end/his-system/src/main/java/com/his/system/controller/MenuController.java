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

    /**
     * 全量菜单树：不受当前登录角色过滤，排序确定。
     * 用于菜单管理页的树表格，以及角色管理「菜单权限」授权弹框的可勾选全集。
     */
    @Operation(summary = "查询菜单树")
    @PreAuthorize("hasAnyAuthority('system:menu:list', 'system:role:list')")
    @GetMapping("/tree")
    public Result<List<MenuVO>> tree() {
        return Result.success(sysMenuService.tree());
    }

    /**
     * 单个菜单节点详情。
     * 用于菜单管理页修改弹框的回显取数。
     */
    @Operation(summary = "获取菜单详情")
    @PreAuthorize("hasAnyAuthority('system:menu:list', 'system:role:list')")
    @GetMapping("/getById")
    public Result<MenuVO> getInfo(@RequestParam Long menuId) {
        return Result.success(sysMenuService.getInfo(menuId));
    }

    /**
     * 当前登录岗位可见的菜单树（只认本次会话的角色，不是全部角色的并集）。
     * 用于登录后构建侧边导航、路由白名单校验与角色落地页解析。
     */
    @Operation(summary = "获取当前用户菜单")
    @GetMapping("/userMenus")
    public Result<List<MenuVO>> userMenus() {
        return Result.success(sysMenuService.userMenus());
    }

    /**
     * 新增或修改菜单节点：入参带主键走修改，不带走新增，返回结果文案。
     * 用于菜单管理页新增/修改弹框保存。
     */
    @PreAuthorize("hasAuthority('system:menu:add')")
    @Operation(summary = "新增或修改菜单")
    @PostMapping("/menuUpsert")
    public Result<Void> menuUpsert(@Valid @RequestBody MenuUpsertDTO upsertDTO) {
        return Result.success(sysMenuService.upsert(upsertDTO), null);
    }

    /**
     * 删除菜单节点。
     * 用于菜单管理页树表格行的删除操作。
     */
    @PreAuthorize("hasAuthority('system:menu:delete')")
    @Operation(summary = "删除菜单")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long menuId) {
        sysMenuService.delete(menuId);
        return Result.success("删除成功", null);
    }
}
