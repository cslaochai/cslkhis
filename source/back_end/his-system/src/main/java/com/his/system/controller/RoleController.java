package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.RoleMenuUpsertDTO;
import com.his.system.dto.SysRoleQueryDTO;
import com.his.system.dto.SysRoleQueryPageDTO;
import com.his.system.dto.SysRoleUpsertDTO;
import com.his.system.service.SysRoleService;
import com.his.system.vo.RoleSelectListVO;
import com.his.system.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('org:employee:list', 'system:role:list', 'system:user:list')")
public class RoleController {

    private final SysRoleService roleService;

    @Operation(summary = "查询角色列表")
    @PostMapping("/listPage")
    public Result<PageResult<RoleVO>> listPage(@RequestBody SysRoleQueryPageDTO queryDTO) {
        return Result.success(roleService.listPage(queryDTO));
    }

    /**
     * 角色下拉。全岗位通用：给员工/用户分配角色、以后凡是「选一个角色」的地方都要用，
     * 所以只要求登录 —— 类级的 {@code system:role:list} 会把非管理岗的请求拦成 403。
     */
    @Operation(summary = "查询角色下拉列表")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/selectList")
    public Result<List<RoleSelectListVO>> selectList(@RequestBody SysRoleQueryDTO queryDTO) {
        return Result.success(roleService.selectList(queryDTO));
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/getById")
    public Result<RoleVO> getInfo(@RequestParam Long roleId) {
        return Result.success(roleService.getInfo(roleId));
    }

    @PreAuthorize("hasAuthority('system:role:add')")
    @Operation(summary = "新增或修改角色")
    @PostMapping("/roleUpsert")
    public Result<Void> roleUpsert(@RequestBody SysRoleUpsertDTO upsertDTO) {
        return Result.success(roleService.upsert(upsertDTO), null);
    }

    @PreAuthorize("hasAuthority('system:role:delete')")
    @Operation(summary = "删除角色")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long roleId) {
        roleService.delete(roleId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "查询角色已配置的菜单ID")
    @GetMapping("/getMenuIds")
    public Result<List<Long>> getMenuIds(@RequestParam Long roleId) {
        return Result.success(roleService.getMenuIds(roleId));
    }

    @PreAuthorize("hasAuthority('system:role:add')")
    @Operation(summary = "保存角色菜单权限")
    @PostMapping("/saveRoleMenu")
    public Result<Void> saveRoleMenu(@RequestBody @Valid RoleMenuUpsertDTO upsertDTO) {
        roleService.saveRoleMenu(upsertDTO);
        return Result.success("保存成功", null);
    }
}
