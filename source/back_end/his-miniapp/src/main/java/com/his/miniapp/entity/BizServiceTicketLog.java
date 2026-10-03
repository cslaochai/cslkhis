package com.his.miniapp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工单流转记录（建表见 {@code sql/221}）。
 *
 * <p><b>它有两条命</b>：患者端是「进展时间轴」（患者提交后能看到有人受理了、什么时候办的），
 * 客服端是「证据链」（谁在什么时候把这张单改成什么状态）。
 * 少了这张表，工单就只剩一个终态 —— 患者只知道"已办结"，不知道中间发生了什么，
 * 客服也没有任何留痕能证明自己处理过。
 *
 * <p>{@code visibleToPatient=0} 是内部备注：客服之间交接用，患者看不到
 * （客服写「这人上次也投诉过收费」不该被患者看见）。
 *
 * <p><b>只追加不修改</b>：流转记录就是流水，改历史等于改账。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_service_ticket_log")
public class BizServiceTicketLog extends BaseEntity {

    /** 工单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long messageId;

    /** 工单号（冗余，排查不用 join） */
    private String messageNo;

    /** 动作：0-提交 1-受理 2-客服回复 3-办结 4-患者补充 5-关闭 6-患者撤单 7-患者重开 */
    private Integer action;

    /** 内容（回复正文 / 处理结果 / 撤单原因） */
    private String content;

    /** 患者是否可见（0-内部备注 1-患者可见） */
    private Integer visibleToPatient;

    /** 操作人类型（1-患者 2-院内） */
    private Integer operatorType;

    /** 操作人账号 */
    private String operator;

    /** 操作人姓名 */
    private String operatorName;
}
