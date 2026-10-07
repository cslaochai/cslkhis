package com.his.system.service.impl;

import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.util.TextUtil;
import com.his.system.config.WechatProperties;
import com.his.system.service.WechatSubscribeSender;
import com.his.system.vo.WechatSubscribeSendPayloadVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信订阅消息发送器（患者端小程序出站通知通道骨架）。
 *
 * <p><b>失败绝不外抛：</b>通知只是「让用户更快知道」，发送失败不能让挂号/发报告的业务回滚，
 * 调用方拿到的返回值是 null（成功）或已截断的失败原因（自行决定落 send_status=2）。
 *
 * <p><b>未配置即静默降级：</b>{@code wechat.miniapp.enabled=false} 或缺 appid/secret 时
 * 直接返回「未启用」，不打外网请求 —— dev 环境没有密钥是常态，通道要能空转。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatSubscribeSenderImpl implements WechatSubscribeSender {

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";
    private static final String TOKEN_CACHE_KEY = "wechat:miniapp:access_token";
    /**
     * 微信 token 有效期 7200s，提前 300s 续期，避免边界过期竞态
     */
    private static final long TOKEN_TTL_SECONDS = 6900L;
    /**
     * 失败原因列宽有限，一律截断（AGENTS.md 铁律：超长会把"记录失败"升级成 500）
     */
    private static final int MAX_REASON_LEN = 200;

    private final WechatProperties wechatProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送订阅消息。
     *
     * @param openid 收件人 openid
     * @param scene  业务场景码（映射到 yml 模板ID）
     * @param page   点击跳转的小程序页面路径，可空
     * @param data   模板字段（key 为微信模板里的 thing/character_string 等字段名，value 为纯文本）。
     *               <b>刻意保留 {@code Map<String, String>} 而不建 VO</b>：键由各场景在 yml 里
     *               配的模板决定（{@code thing1} / {@code date2} …），编译期无从得知，
     *               属于外部契约的动态字典，不是可枚举的数据契约。
     * @return null=发送成功；非 null=失败原因（已截断）
     */
    public String send(String openid, String scene, String page, Map<String, String> data) {
        if (!wechatProperties.ready()) {
            return "微信通道未启用或未配置 appid/secret";
        }
        if (openid == null || openid.isBlank()) {
            return "该账号未绑定微信openid";
        }
        String templateId = wechatProperties.templateOf(scene);
        if (templateId == null || templateId.isBlank()) {
            return "场景[" + scene + "]未配置订阅消息模板";
        }
        try {
            String token = getAccessToken();
            if (token == null) {
                return "获取access_token失败";
            }
            WechatSubscribeSendPayloadVO body = new WechatSubscribeSendPayloadVO();
            body.setTouser(openid);
            body.setTemplateId(templateId);
            if (page != null && !page.isBlank()) {
                body.setPage(page);
            }
            Map<String, WechatSubscribeSendPayloadVO.WechatSubscribeFieldVO> fields = new LinkedHashMap<>();
            if (data != null) {
                data.forEach((k, v) -> {
                    WechatSubscribeSendPayloadVO.WechatSubscribeFieldVO field =
                            new WechatSubscribeSendPayloadVO.WechatSubscribeFieldVO();
                    field.setValue(v == null ? "" : v);
                    fields.put(k, field);
                });
            }
            body.setData(fields);

            String resp = HttpUtil.createPost(SEND_URL + "?access_token=" + token)
                    .body(objectMapper.writeValueAsString(body))
                    .timeout(5000)
                    .execute()
                    .body();
            JsonNode node = objectMapper.readTree(resp);
            int errcode = node.path("errcode").asInt(-1);
            if (errcode == 0) {
                return null;
            }
            // 43101 = 用户未订阅/拒绝：属预期内，调用方不必告警
            return TextUtil.cut("errcode=" + errcode + " " + node.path("errmsg").asText(""), MAX_REASON_LEN);
        } catch (Exception e) {
            log.warn("微信订阅消息发送异常 scene={} openid={} err={}", scene, openid, e.getMessage());
            return TextUtil.cut("发送异常: " + e.getMessage(), MAX_REASON_LEN);
        }
    }

    /**
     * access_token 走 Redis 共享（多实例下微信侧同一 appid 只有一份有效 token，
     * 各自刷新会互相踢失效）。缓存值带过期，miss 时回源。
     */
    private String getAccessToken() {
        try {
            String cached = stringRedisTemplate.opsForValue().get(TOKEN_CACHE_KEY);
            if (cached != null && !cached.isBlank()) {
                return cached;
            }
            String resp = HttpUtil.get(TOKEN_URL
                    + "?grant_type=client_credential"
                    + "&appid=" + wechatProperties.getAppId()
                    + "&secret=" + wechatProperties.getAppSecret(), 5000);
            JsonNode node = objectMapper.readTree(resp);
            String token = node.path("access_token").asText(null);
            if (token != null && !token.isBlank()) {
                stringRedisTemplate.opsForValue().set(TOKEN_CACHE_KEY, token, TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
                return token;
            }
            log.warn("获取微信access_token失败：{}", resp);
            return null;
        } catch (Exception e) {
            log.warn("获取微信access_token异常：{}", e.getMessage());
            return null;
        }
    }

}
