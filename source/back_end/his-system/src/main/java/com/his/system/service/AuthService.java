package com.his.system.service;

import com.his.system.dto.ChangePasswordDTO;
import com.his.system.dto.LoginRequestDTO;
import com.his.system.dto.SwitchPostDTO;
import com.his.system.vo.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 登录鉴权服务：口令认证、岗位身份 token 的签发与当前登录人身份读取的收口。
 */
public interface AuthService {

    /**
     * 口令认证 + 签发岗位 token。
     *
     * <p>失败分支（无角色、所选角色不属于本人、角色下没有任何科室岗位）都必须先落一条登录日志再抛错 ——
     * 这类「账号存在但进不去」同样是要留痕的安全事件。
     */
    LoginVO login(LoginRequestDTO loginRequestDTO, HttpServletRequest request);

    /**
     * 取登录口令加密用的 SM2 公钥。匿名可取，给登录页和小程序的登录页用。
     */
    PublicKeyVO publicKey();

    /**
     * 当前登录身份：账号、姓名、当前岗位科室，以及角色名称与按钮权限码集合。
     * 顶栏加载「我的身份」时取一次，权限码同时灌进前端按钮显隐判定；分诊、排班、预约等页面按当前科室取数也读它。
     */
    UserLoginVO currentUserInfo();

    /**
     * 当前登录人名下的角色清单与正在使用的角色。
     * 供登录后的身份解析环节单独回查角色（不带姓名、科室与权限码的轻量口径）。
     */
    UserRolesVO currentUserRoles();

    /**
     * 当前登录人的可选岗位（角色 × 科室，只含当前生效的）。
     * 顶栏「切换岗位」弹框列出可切条目时取数。
     */
    List<EmployeePostVO> currentUserPosts();

    /**
     * 按「角色 + 科室」成对切换岗位并重签 token，岗位行不存在时拒发。
     * 顶栏「切换岗位」选定一条岗位后调用，换完必须立刻用新 token 覆盖本地旧 token，否则接口仍按旧身份收口。
     */
    SwitchPostVO switchPost(SwitchPostDTO switchPostDTO);

    /**
     * 改的是**当前登录人**的口令，userId 取自 token 而不是入参 —— 接收 userId 等于任何人可改任何人的密码。
     */
    void updatePassword(ChangePasswordDTO changePasswordDTO);

    /**
     * 记一次登出事件到登录日志，取不到登录态则不留痕。
     * 客户端退出登录时调用，用于事后回看某人何时下线。
     */
    void logout(HttpServletRequest request);
}
