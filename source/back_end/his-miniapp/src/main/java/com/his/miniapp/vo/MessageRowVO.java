package com.his.miniapp.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 患者端消息中心列表行（Mapper 行承载），对应 {@code MiniappMessageMapper#selectMyMessages}。
 *
 * <p>字段名与出参 {@link MessageListVO} 同形；{@code messageId}/{@code receiverId}/{@code bizId}
 * 在 SQL 侧已 CAST 成字符串，BIGINT 直出会在 JS 端丢精度。
 *
 * <p>{@code sendTime} 保持字符串：出参就是 {@code yyyy-MM-dd HH:mm:ss} 文本
 * （改造前 SQL 用 {@code DATE_FORMAT} 渲染），改成 {@code LocalDateTime} 会让小程序端
 * 拿到 ISO 的 {@code T} 分隔格式，属于破坏出参契约，故不动。
 */
@Data
public class MessageRowVO implements Serializable {

    /**
     * 消息ID（SQL 已 CAST 成字符串，防 BIGINT 精度丢失）
     */
    private String messageId;

    /**
     * 消息编号
     */
    private String messageNo;

    /**
     * 发送渠道（system-站内信 sms-短信 wechat-微信 email-邮件）
     */
    private String channel;

    /**
     * 接收人ID（SQL 已 CAST 成字符串，防 BIGINT 精度丢失）
     */
    private String receiverId;

    /**
     * 接收人姓名
     */
    private String receiverName;

    /**
     * 标题
     */
    private String title;

    /**
     * 正文
     */
    private String content;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 关联业务ID（SQL 已 CAST 成字符串，防 BIGINT 精度丢失）
     */
    private String bizId;

    /**
     * 紧急度（info-普通 warning-待办提醒 urgent-紧急）
     */
    private String severity;

    /**
     * 阅读状态（0-未读 1-已读）
     */
    private Integer readStatus;

    /**
     * 发送状态（0-待发送 1-已发送 2-发送失败）
     */
    private Integer sendStatus;

    /**
     * 发送时间（{@code yyyy-MM-dd HH:mm:ss} 文本，SQL 侧 DATE_FORMAT 渲染）
     */
    private String sendTime;
}
