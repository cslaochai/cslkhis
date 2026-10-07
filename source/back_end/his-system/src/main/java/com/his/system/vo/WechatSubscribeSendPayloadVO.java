package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 微信订阅消息出站请求体（{@code WechatSubscribeSenderImpl} POST 到
 * {@code https://api.weixin.qq.com/cgi-bin/message/subscribe/send}）。
 *
 * <p><b>字段名是微信侧的外部契约，不能随手改</b>：微信按 {@code touser / template_id /
 * page / data} 这几个固定键解析请求体，改一个键名 = 订阅消息永远发不出去，
 * 而且微信只回一个 errcode，不告诉你哪一步错了（现象是「发送成功但用户收不到」）。
 *
 * <p>本 VO 由 <b>Jackson</b> 序列化（{@code objectMapper.writeValueAsString}），
 * 所以保键名用 {@code @JsonProperty} 而不是 Hutool 的 {@code @Alias}
 * —— 用错注解的话 {@code templateId} 会被写成 {@code templateId} 发给微信。
 *
 * <p>曾用 {@code LinkedHashMap<String, Object>} 拼这个请求体 —— 键名是纯字符串字面量，
 * 编译器一个字都不管；而这份报文是对外承诺的内容。
 *
 * <p><b>{@code data} 内部为什么还是 Map</b>：它的键是<b>模板里定义的字段名</b>
 * （{@code thing1} / {@code date2} / {@code character_string3} …），由各业务场景
 * （报告出结果、预约提醒…）在 yml 里配的模板决定，编译期无从得知。
 * 这一层是真动态字典，不是数据契约；每个值的形状固定是 {@code {"value": "..."}}，
 * 故用 {@link WechatSubscribeFieldVO} 收口。
 */
@Data
// 不跳页时不发 page 键：与改造前「page 为空则整键不出现」保持一致
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WechatSubscribeSendPayloadVO implements Serializable {

    /**
     * 收件人 openid
     */
    private String touser;

    /**
     * 订阅消息模板ID（yml 里按场景码配置）
     */
    @JsonProperty("template_id")
    private String templateId;

    /**
     * 点击跳转的小程序页面路径；不跳转时为 null（整键不发）
     */
    private String page;

    /**
     * 模板字段值集合，键为模板里定义的字段名（thing1 / date2 等）
     */
    private Map<String, WechatSubscribeFieldVO> data;

    /**
     * 微信订阅消息的单个模板字段值（微信要求包成 {@code {"value": "..."}}）。
     */
    @Data
    public static class WechatSubscribeFieldVO implements Serializable {

        /**
         * 字段文本值（null 落成空串，避免微信侧因 null 拒绝整条消息）
         */
        private String value;
    }
}
