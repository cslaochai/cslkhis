package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知出参
 */
@Data
public class SysMessageVO {

    /**
     * 消息ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long messageId;

    /**
     * 消息编号
     */
    private String messageNo;

    /**
     * 发送渠道
     */
    private String channel;

    /**
     * 接收人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long receiverId;

    /**
     * 接收人姓名
     */
    private String receiverName;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 业务类型
     */
    private String bizType;

    /** 关联业务ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bizId;

    /**
     * 紧急度:info-普通 warning-待办提醒 urgent-紧急(危急值,置顶)
     */
    private String severity;

    /**
     * 结构化负载(JSON 字符串)
     */
    private String payload;

    /**
     * 处理状态:0-待处理 1-已处理 2-已关闭,NULL-通知型
     */
    private Integer handleStatus;

    /** 发送状态（0-待发送 1-已发送 2-发送失败） */
    private Integer sendStatus;

    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    /**
     * 已读状态：0-未读 1-已读
     */
    private Integer readStatus;

    /**
     * 阅读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;
}
