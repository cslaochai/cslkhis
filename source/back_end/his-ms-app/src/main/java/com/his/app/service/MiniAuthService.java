package com.his.app.service;

import com.his.app.dto.WxLoginDTO;
import com.his.app.vo.MiniWxLoginVO;

/**
 * 患者端认证服务：微信登录口子。
 */
public interface MiniAuthService {

    /**
     * wx.login code 换 openid 登录：已绑定 → 发 token；未绑定 → bound=false
     */
    MiniWxLoginVO wxLogin(WxLoginDTO dto);
}
