package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.SysUserPasswordUpsertDTO;
import com.his.system.dto.SysUserPageQueryDTO;
import com.his.system.dto.SysUserUpsertDTO;
import com.his.system.service.SysUserService;
import com.his.system.vo.SysUserListVO;
import com.his.system.vo.UserDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器：系统账号的分页维护、详情回显与登录口令重置入口。
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('system:user:list')")
public class UserController {

    private final SysUserService sysUserService;

    /**
     * 按用户名、科室、状态等条件分页查询系统账号。
     * 用于系统管理「用户管理」页的账号表格，以及部门页按用户名搜人指派负责人时的候选取数。
     */
    @Operation(summary = "分页查询用户列表")
    @PostMapping("/listPage")
    public Result<PageResult<SysUserListVO>> listPage(@Valid @RequestBody SysUserPageQueryDTO queryDTO) {
        return Result.success(sysUserService.queryUserPage(queryDTO));
    }

    /**
     * 按账号主键取单条账号信息，敏感字段按原文返回。
     * 只服务用户管理的编辑弹框回填——表单是整对象提交，回显成打码值会把真号写坏；纯展示走本人档案接口。
     */
    @Operation(summary = "获取用户详情")
    @GetMapping("/getById")
    public Result<UserDetailVO> getById(@RequestParam Long id) {
        UserDetailVO userDetailVO = sysUserService.getUserDetail(id);
        return Result.success(userDetailVO);
    }

    /**
     * 本人档案（顶栏「用户信息」）：手机号/身份证/邮箱后端脱敏后出参。
     *
     * <p>只要求登录，不能挂 system:user:list —— 医生/护士/收费员都要看自己的档案。
     * 与 getById 分开是因为 getById 同时服务用户管理的编辑回显，脱敏值一旦被保存就写坏数据。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "本人档案（敏感字段已脱敏）")
    @GetMapping("/selfProfile")
    public Result<UserDetailVO> selfProfile() {
        return Result.success(sysUserService.getSelfProfile());
    }

    /**
     * 新增或修改系统账号，传主键即改、不传即建。
     * 用于系统管理「用户管理」页的新增与编辑弹框保存；管理员账号的用户名与状态、患者及其他类型账号都不允许从这里改。
     */
    @PreAuthorize("hasAuthority('system:user:add')")
    @Operation(summary = "新增或修改用户")
    @PostMapping("/userUpsert")
    public Result<Void> userUpsert(@RequestBody @Valid SysUserUpsertDTO upsertDTO) {
        sysUserService.upsertUser(upsertDTO);
        return Result.success();
    }

    /**
     * 按账号主键删除系统账号。
     * 用于「用户管理」页账号行的删除确认。
     */
    @PreAuthorize("hasAuthority('system:user:delete')")
    @Operation(summary = "删除用户")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long userId) {
        sysUserService.removeUser(userId);
        return Result.success();
    }

    /**
     * 把指定账号的登录口令重置为初始口令，并记一条「重置过」的审计（不记新口令）。
     * 用于「用户管理」页行内的重置密码动作；患者与其他类型账号走自助找回，不允许从这里重置。
     */
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Operation(summary = "重置密码")
    @PostMapping("/resetPassword")
    public Result<Void> resetPassword(@Valid @RequestBody SysUserPasswordUpsertDTO resetDTO) {
        sysUserService.resetUserPassword(resetDTO);
        return Result.success();
    }
}
