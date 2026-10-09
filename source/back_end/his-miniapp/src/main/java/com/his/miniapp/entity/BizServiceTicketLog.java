package com.his.miniapp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工单流转记录（建表见 sql/221）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_service_ticket_log")
public class BizServiceTicketLog extends BaseEntity {

    /**
     * 工单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long messageId;

    /**
     * 工单号（冗余，排查不用 join）
     */
    private String messageNo;

    /**
     * 动作：0-提交 1-受理 2-客服回复 3-办结 4-患者补充 5-关闭 6-患者撤单 7-患者重开
     */
    private Integer action;

    /**
     * 内容（回复正文 / 处理结果 / 撤单原因）
     */
    private String content;

    /**
     * 患者是否可见（0-内部备注 1-患者可见）
     */
    private Integer visibleToPatient;

    /**
     * 操作人类型（1-患者 2-院内）
     */
    private Integer operatorType;

    /**
     * 操作人账号
     */
    private String operator;

    /**
     * 操作人姓名
     */
    private String operatorName;
}
