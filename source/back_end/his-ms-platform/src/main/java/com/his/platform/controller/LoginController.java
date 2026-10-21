package com.his.platform.controller;

import com.his.common.base.Result;
import com.his.platform.dto.ChangePasswordDTO;
import com.his.platform.dto.LoginRequestDTO;
import com.his.platform.dto.SwitchPostDTO;
import com.his.platform.service.AuthService;
import com.his.platform.vo.*;
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

    /**
     * 取当前有效的口令加密公钥，供前端把明文口令加密后再上网。
     * 用于管理端与患者小程序登录页、修改密码弹框提交口令之前。
     */
    @Operation(summary = "获取登录口令加密公钥（S匿名可取）")
    @GetMapping("/publicKey")
    public Result<PublicKeyVO> publicKey() {
        return Result.success("获取成功", authService.publicKey());
    }

    /**
     * 账号口令登录：校验通过后签发登录令牌，并带回本次身份与权限集合。
     * 用于管理端登录页与患者小程序的账密登录入口。
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO, HttpServletRequest request) {
        return Result.success("登录成功", authService.login(loginRequestDTO, request));
    }

    /**
     * 当前登录者身份全景：姓名、所在科室、角色编码与按钮权限码一次带回。
     * 用于顶栏用户区渲染与前端按钮级权限判定；业务页面取「自己所在科室」也走这里。
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserLoginVO> getUserInfo() {
        return Result.success(authService.currentUserInfo());
    }

    /**
     * 当前登录者名下真实存在且仍在有效期内的岗位清单（角色 × 科室成对）。
     * 用于顶栏「切换岗位」弹框的可选身份列表。
     */
    @Operation(summary = "获取当前用户的岗位列表（角色 × 科室，只含当前生效的）")
    @GetMapping("/postList")
    public Result<List<EmployeePostVO>> postList() {
        return Result.success(authService.currentUserPosts());
    }

    /**
     * 退出登录：为本次登录留一条登出记录，令牌由前端自行丢弃。
     * 用于患者小程序「我的」页退出登录；管理端顶栏退出只清本地令牌，不调本接口。
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return Result.success("登出成功", null);
    }

    /**
     * 修改本人登录口令：校验旧口令后写入新口令，新旧口令均已加密提交。
     * 用于顶栏用户菜单「修改密码」弹框保存。
     */
    @Operation(summary = "修改密码")
    @PostMapping("/changePassword")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO request) {
        authService.updatePassword(request);
        return Result.success("密码修改成功", null);
    }

    /**
     * 当前登录者名下的角色清单（编码 + 名称）。
     * 供只需单独取角色的场景使用；顶栏的角色名解析已由当前用户信息一次带回。
     */
    @Operation(summary = "获取当前用户角色列表")
    @GetMapping("/roles")
    public Result<UserRolesVO> getUserRoles() {
        return Result.success(authService.currentUserRoles());
    }

    /**
     * 切换岗位：按角色 + 科室成对换取新身份的登录令牌，拒绝对不存在组合发令牌。
     * 用于顶栏「切换岗位」确认后换令牌，随后重拉菜单与数据范围。
     */
    @Operation(summary = "切换岗位（角色 × 科室）")
    @PostMapping("/switchPost")
    public Result<SwitchPostVO> switchPost(@RequestBody @Valid SwitchPostDTO request) {
        return Result.success("岗位切换成功", authService.switchPost(request));
    }
}
