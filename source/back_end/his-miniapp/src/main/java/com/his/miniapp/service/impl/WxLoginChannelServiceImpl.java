package com.his.miniapp.service.impl;

import com.his.miniapp.service.WxLoginChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * 微信登录出口（小程序一期口子，同 M7/M8 打印形态）。
 */
@Slf4j
@Service
public class WxLoginChannelServiceImpl implements WxLoginChannelService {

    /**
     * 用 wx.login 的临时 code 换取 openid。
     *
     * @param code 小程序 wx.login 返回的临时凭证（一次性）
     * @return openid（唯一标识一个微信用户）
     */
    public String code2Session(String code) {
        String openid = "mock_wx_" + sha256(code).substring(0, 24);
        log.info("[微信登录口子] ===== 模拟调 jscode2session 换取 openid =====");
        log.info("[微信登录口子] code={} → openid={}（真对接时配置 HIS_WX_MINIAPP_APPID/SECRET 走 jscode2session）",
                code, openid);
        return openid;
    }

    /**
     * 确定性（同一 code 永远同一 openid）是刻意保证：验证脚本里
     * 「登录 → 退出 → 再登录」要能命中有 openid 绑定的同一账号。
     */
    private static String sha256(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
