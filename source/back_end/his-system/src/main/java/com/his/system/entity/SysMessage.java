package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知
 */
@Data
@TableName("sys_message")
public class SysMessage {
    /**
     * 消息ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long messageId;

    /**
     * 消息编号
     */
    private String messageNo;

    /**
     * 发送渠道:system-站内信 sms-短信 wechat-微信 email-邮件
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
     * 业务类型:regist-挂号 appointment-预约 prescription-处方 inspection-检查 report-报告 drug-药品
     */
    private String bizType;

    /**
     * 关联业务ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bizId;

    /**
     * 紧急度:info-普通 warning-待办提醒 urgent-紧急(危急值,置顶)。
     * 展示口径在前端 lib/messageCatalog.js；本列只服务 SQL 排序，见 sql/70。
     */
    private String severity;

    /**
     * 结构化负载(JSON 字符串):patientName/prescriptionNo/bedNo/opinion 等，前端卡片渲染用
     */
    private String payload;

    /**
     * 处理状态:0-待处理 1-已处理 2-已关闭,NULL-通知型(用 read_status 闭环)。
     * 待办型消息的业务侧办结时机（如危急值 handle()）负责把它推进到 1。
     */
    private Integer handleStatus;

    /**
     * 发送状态:0-待发送 1-已发送 2-发送失败
     */
    private Integer sendStatus;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 渠道发送失败原因（channel!=system 时留痕，写入前已截断）
     */
    private String errorMsg;

    /**
     * 阅读状态:0-未读 1-已读
     */
    private Integer readStatus;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
}
