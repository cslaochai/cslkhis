package com.his.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信小程序运行时配置（yml 的 {@code wechat.miniapp.*} 段）。
 *
 * <p>与 {@code ai.*} 同一口径：部署期配置随代码走，密钥只从环境变量取、不落库不进仓库。
 * appid/secret 任一为空即视为「通道未开通」，发送侧整体降级为不发送，不阻断业务。
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.miniapp")
public class WechatProperties {

    private boolean enabled = false;

    private String appId = "";

    private String appSecret = "";

    /** 订阅消息模板ID，key 为业务场景码（regist_success / report_ready / queue_called ...） */
    private Map<String, String> templates = new HashMap<>();

    public boolean ready() {
        return enabled && notBlank(appId) && notBlank(appSecret);
    }

    public String templateOf(String scene) {
        return templates.get(scene);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
