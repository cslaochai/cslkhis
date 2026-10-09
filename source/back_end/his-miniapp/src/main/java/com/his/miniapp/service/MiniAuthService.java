package com.his.miniapp.service;

import com.his.miniapp.dto.WxLoginDTO;
import com.his.miniapp.vo.MiniWxLoginVO;

/**
 * 患者端认证服务：微信登录口子。
 */
public interface MiniAuthService {

    /**
     * wx.login code 换 openid 登录：已绑定 → 发 token；未绑定 → bound=false
     */
    MiniWxLoginVO wxLogin(WxLoginDTO dto);
}
