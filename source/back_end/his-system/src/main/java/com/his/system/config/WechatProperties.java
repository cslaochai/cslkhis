package com.his.system.config;

import com.his.common.util.TextUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信小程序运行时配置（yml 的 his.wechat.miniapp.* 段）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.wechat.miniapp")
public class WechatProperties {

    private boolean enabled = false;

    private String appId = "";

    private String appSecret = "";

    /**
     * 订阅消息模板ID，key 为业务场景码（regist_success / report_ready / queue_called ...）
     */
    private Map<String, String> templates = new HashMap<>();

    public boolean ready() {
        return enabled && TextUtil.hasText(appId) && TextUtil.hasText(appSecret);
    }

    public String templateOf(String scene) {
        return templates.get(scene);
    }
}
