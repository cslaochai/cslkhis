package com.his.system.service;

import com.his.system.dto.ChangePasswordDTO;
import com.his.system.dto.LoginRequestDTO;
import com.his.system.dto.SwitchPostDTO;
import com.his.system.vo.LoginVO;
import com.his.system.vo.PublicKeyVO;
import com.his.system.vo.UserLoginVO;
import com.his.system.vo.UserRolesVO;
import com.his.system.vo.EmployeePostVO;
import com.his.system.vo.SwitchPostVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

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

    UserLoginVO currentUserInfo();

    UserRolesVO currentUserRoles();

    List<EmployeePostVO> currentUserPosts();

    SwitchPostVO switchPost(SwitchPostDTO switchPostDTO);

    /**
     * 改的是**当前登录人**的口令，userId 取自 token 而不是入参 —— 接收 userId 等于任何人可改任何人的密码。
     */
    void updatePassword(ChangePasswordDTO changePasswordDTO);

    void logout(HttpServletRequest request);
}
