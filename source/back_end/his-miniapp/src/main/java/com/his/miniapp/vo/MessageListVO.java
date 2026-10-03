package com.his.miniapp.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 患者端消息中心列表行。
 */
@Data
public class MessageListVO implements Serializable {

    /** 消息ID（字符串化防 BIGINT 精度丢失） */
    private String messageId;

    /** 消息编号 */
    private String messageNo;

    /** 发送渠道（system-站内信 sms-短信 wechat-微信 email-邮件） */
    private String channel;

    /** 接收人ID（字符串化防 BIGINT 精度丢失） */
    private String receiverId;

    /** 接收人姓名 */
    private String receiverName;

    /** 标题 */
    private String title;

    /** 正文 */
    private String content;

    /** 业务类型 */
    private String bizType;

    /** 关联业务ID（字符串化防 BIGINT 精度丢失） */
    private String bizId;

    /** 紧急度（info-普通 warning-待办提醒 urgent-紧急） */
    private String severity;

    /** 阅读状态（0-未读 1-已读） */
    private Integer readStatus;

    /** 发送状态（0-待发送 1-已发送 2-发送失败） */
    private Integer sendStatus;

    /** 发送时间 */
    private String sendTime;
}
