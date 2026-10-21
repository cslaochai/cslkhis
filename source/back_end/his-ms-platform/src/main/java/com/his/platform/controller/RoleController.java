package com.his.platform.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.platform.dto.RoleMenuUpsertDTO;
import com.his.platform.dto.SysRoleQueryDTO;
import com.his.platform.dto.SysRolePageQueryDTO;
import com.his.platform.dto.SysRoleUpsertDTO;
import com.his.platform.service.SysRoleService;
import com.his.platform.vo.RoleSelectListVO;
import com.his.platform.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器：角色档案的增删改查与角色-菜单授权的接口入口。
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('org:employee:list', 'system:role:list', 'system:user:list')")
public class RoleController {

    private final SysRoleService sysRoleService;

    /**
     * 角色分页：按角色名称模糊、状态筛选，结果按排序号升序。
     * 用于系统管理「角色管理」页面的查询卡与角色表格。
     */
    @Operation(summary = "查询角色列表")
    @PostMapping("/listPage")
    public Result<PageResult<RoleVO>> listPage(@Valid @RequestBody SysRolePageQueryDTO queryDTO) {
        return Result.success(sysRoleService.listPage(queryDTO));
    }

    /**
     * 角色下拉。全岗位通用：给员工/用户分配角色、以后凡是「选一个角色」的地方都要用，
     * 所以只要求登录 —— 类级的 {@code system:role:list} 会把非管理岗的请求拦成 403。
     */
    @Operation(summary = "查询角色下拉列表")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/selectList")
    public Result<List<RoleSelectListVO>> selectList(@Valid @RequestBody SysRoleQueryDTO queryDTO) {
        return Result.success(sysRoleService.selectList(queryDTO));
    }

    /**
     * 单个角色详情：按角色 id 取名称、编码、类型、状态、排序号等完整字段。
     * 用于「角色管理」页「编辑角色」弹框打开时回显表单（新增场景不取数）。
     */
    @Operation(summary = "获取角色详情")
    @GetMapping("/getById")
    public Result<RoleVO> getInfo(@RequestParam Long roleId) {
        return Result.success(sysRoleService.getInfo(roleId));
    }

    /**
     * 新增或修改角色：id 为空走新增并自动生成角色编码，非空走修改并失效相关权限缓存。
     * 用于「角色管理」页的新增与编辑弹框保存，两条路共用这一个入口与同一个按钮权限。
     */
    @PreAuthorize("hasAuthority('system:role:add')")
    @Operation(summary = "新增或修改角色")
    @PostMapping("/roleUpsert")
    public Result<Void> roleUpsert(@Valid @RequestBody SysRoleUpsertDTO upsertDTO) {
        return Result.success(sysRoleService.upsert(upsertDTO), null);
    }

    /**
     * 删除角色：系统内置角色拒绝删除，删除同时清掉该角色的菜单授权与权限缓存。
     * 用于「角色管理」列表行的删除按钮，前端确认后调用。
     */
    @PreAuthorize("hasAuthority('system:role:delete')")
    @Operation(summary = "删除角色")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long roleId) {
        sysRoleService.delete(roleId);
        return Result.success("删除成功", null);
    }

    /**
     * 角色已授权的菜单 id 清单：只回给前端该角色当前勾选的节点。
     * 用于「角色管理」页「配置菜单权限」弹框打开时初始化权限树勾选。
     */
    @Operation(summary = "查询角色已配置的菜单ID")
    @GetMapping("/getMenuIds")
    public Result<List<Long>> getMenuIds(@RequestParam Long roleId) {
        return Result.success(sysRoleService.getMenuIds(roleId));
    }

    /**
     * 保存角色的菜单授权：以传入菜单集合为该角色最终权限（整表替换，缺失即取消），空集合表示清空。
     * 用于「角色管理」页「配置菜单权限」弹框保存；保存后权限缓存即时失效，下一个请求按新授权收口。
     */
    @PreAuthorize("hasAuthority('system:role:add')")
    @Operation(summary = "保存角色菜单权限")
    @PostMapping("/saveRoleMenu")
    public Result<Void> saveRoleMenu(@RequestBody @Valid RoleMenuUpsertDTO upsertDTO) {
        sysRoleService.saveRoleMenu(upsertDTO);
        return Result.success("保存成功", null);
    }
}
