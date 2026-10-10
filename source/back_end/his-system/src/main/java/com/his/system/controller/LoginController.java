package com.his.system.controller;

import com.his.common.base.Result;
import com.his.system.dto.ChangePasswordDTO;
import com.his.system.dto.LoginRequestDTO;
import com.his.system.dto.SwitchPostDTO;
import com.his.system.service.AuthService;
import com.his.system.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@PreAuthorize("permitAll()")
public class LoginController {

    private final AuthService authService;

    @Operation(summary = "获取登录口令加密公钥（S匿名可取）")
    @GetMapping("/publicKey")
    public Result<PublicKeyVO> publicKey() {
        return Result.success("获取成功", authService.publicKey());
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO, HttpServletRequest request) {
        return Result.success("登录成功", authService.login(loginRequestDTO, request));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserLoginVO> getUserInfo() {
        return Result.success(authService.currentUserInfo());
    }

    @Operation(summary = "获取当前用户的岗位列表（角色 × 科室，只含当前生效的）")
    @GetMapping("/postList")
    public Result<List<EmployeePostVO>> postList() {
        return Result.success(authService.currentUserPosts());
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return Result.success("登出成功", null);
    }

    @Operation(summary = "修改密码")
    @PostMapping("/changePassword")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO request) {
        authService.updatePassword(request);
        return Result.success("密码修改成功", null);
    }

    @Operation(summary = "获取当前用户角色列表")
    @GetMapping("/roles")
    public Result<UserRolesVO> getUserRoles() {
        return Result.success(authService.currentUserRoles());
    }

    @Operation(summary = "切换岗位（角色 × 科室）")
    @PostMapping("/switchPost")
    public Result<SwitchPostVO> switchPost(@RequestBody @Valid SwitchPostDTO request) {
        return Result.success("岗位切换成功", authService.switchPost(request));
    }
}
