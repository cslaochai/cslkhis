package com.mis.service;

import com.mis.dto.ChangePasswordDTO;
import com.mis.dto.LoginRequestDTO;
import com.mis.entity.MssMenu;
import com.mis.vo.LoginVO;
import com.mis.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 认证服务：登录 / 当前用户信息 / 菜单 / 登出 / 改密
 */
public interface AuthService {

    LoginVO login(LoginRequestDTO dto, HttpServletRequest request);

    UserInfoVO info();

    List<MssMenu> menus();

    void logout();

    void changePassword(ChangePasswordDTO dto);
}
