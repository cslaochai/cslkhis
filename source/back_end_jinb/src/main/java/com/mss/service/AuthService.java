package com.mss.service;

import com.mss.dto.ChangePasswordDTO;
import com.mss.dto.LoginRequestDTO;
import com.mss.entity.MssMenu;
import com.mss.vo.LoginVO;
import com.mss.vo.UserInfoVO;
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
