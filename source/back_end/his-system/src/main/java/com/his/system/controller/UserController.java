package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.SysUserQueryPageDTO;
import com.his.system.dto.SysUserPasswordUpsertDTO;
import com.his.system.dto.SysUserUpsertDTO;
import com.his.system.service.SysUserService;
import com.his.system.vo.SysUserListVO;
import com.his.system.vo.UserDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('system:user:list')")
public class UserController {

    private final SysUserService userService;

    @Operation(summary = "分页查询用户列表")
    @PostMapping("/listPage")
    public Result<PageResult<SysUserListVO>> listPage(@RequestBody SysUserQueryPageDTO queryDTO) {
        return Result.success(userService.queryUserPage(queryDTO));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/getById")
    public Result<UserDetailVO> getById(@RequestParam Long id) {
        UserDetailVO userDetailVO = userService.getUserDetail(id);
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
        return Result.success(userService.getSelfProfile());
    }

    @PreAuthorize("hasAuthority('system:user:add')")
    @Operation(summary = "新增或修改用户")
    @PostMapping("/userUpsert")
    public Result<Void> userUpsert(@RequestBody @Valid SysUserUpsertDTO upsertDTO) {
        userService.upsertUser(upsertDTO);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:user:delete')")
    @Operation(summary = "删除用户")
    @DeleteMapping("/deleteById")
    public Result<Void> remove(@RequestParam Long userId) {
        userService.removeUser(userId);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('system:user:edit')")
    @Operation(summary = "重置密码")
    @PostMapping("/resetPassword")
    public Result<Void> resetPassword(@RequestBody SysUserPasswordUpsertDTO resetDTO) {
        userService.resetUserPassword(resetDTO);
        return Result.success();
    }
}
