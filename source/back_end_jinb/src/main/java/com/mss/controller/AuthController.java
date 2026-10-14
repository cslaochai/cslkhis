package com.mss.controller;

import com.mss.common.base.Result;
import com.mss.dto.ChangePasswordDTO;
import com.mss.dto.LoginRequestDTO;
import com.mss.entity.MssMenu;
import com.mss.service.AuthService;
import com.mss.vo.LoginVO;
import com.mss.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证：登录 / 当前用户信息 / 菜单 / 登出 / 改密
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequestDTO dto, HttpServletRequest request) {
        return Result.success("登录成功", authService.login(dto, request));
    }

    @Operation(summary = "当前用户信息")
    @GetMapping("/info")
    public Result<UserInfoVO> info() {
        return Result.success(authService.info());
    }

    @Operation(summary = "当前用户可见菜单树（按当前角色）")
    @GetMapping("/menus")
    public Result<List<MssMenu>> menus() {
        return Result.success(authService.menus());
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success("已退出登录", null);
    }

    @Operation(summary = "修改密码")
    @PostMapping("/changePassword")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        authService.changePassword(dto);
        return Result.success("密码修改成功", null);
    }
}
