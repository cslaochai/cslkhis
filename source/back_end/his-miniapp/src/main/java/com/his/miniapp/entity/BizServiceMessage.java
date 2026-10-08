package com.his.miniapp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 患者端留言（客服台「转人工」的落点）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_service_message")
public class BizServiceMessage extends BaseEntity {

    /** 留言单号 */
    private String messageNo;

    /** 留言用户ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /** 就诊人ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 就诊人姓名（快照） */
    private String patientName;

    /** 联系电话 */
    private String contactPhone;

    /** 留言分类（同 sys_faq.category_code） */
    private String categoryCode;

    /** 留言内容 */
    private String content;

    /**
     * 工单状态（sql/221 重定义，全仓唯一口径）
     * <p>0-待受理 1-处理中 2-已办结 3-已关闭。
     * 每一步流转都写 {@code biz_service_ticket_log}，患者端时间轴读那张表。
     */
    private Integer status;

    /** 优先级（0-普通 1-紧急） */
    private Integer priority;

    /** 受理人账号（服务端取登录人，不由前端传） */
    private String acceptBy;

    /** 受理人姓名 */
    private String acceptByName;

    /** 受理时间 */
    private LocalDateTime acceptTime;

    /** 关闭人账号 */
    private String closeBy;

    /** 关闭时间 */
    private LocalDateTime closeTime;

    /** 关闭原因（患者撤单 / 客服关闭都要写，不写就是一笔说不清的账） */
    private String closeReason;

    /** 最后一次客服回复时间 */
    private LocalDateTime lastReplyTime;

    /** 客服回复次数 */
    private Integer replyCount;

    /** 处理人 */
    private String handleBy;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 处理结果 */
    private String handleResult;
}
