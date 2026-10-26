package com.his.outpatient.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 听班呼叫流水（sql/297）：状态流转留痕，只增不改。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_duty_call_log")
public class BizDutyCallLog extends BaseEntity {

    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;

    /** 呼叫单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long callId;

    /** 呼叫单号 */
    private String callNo;

    /** 动作：1-呼叫 2-到岗 3-超时升级 4-取消 5-通知失败（DutyCallActionEnum） */
    private Integer actionType;

    /** 动作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actionTime;

    /** 动作前状态 */
    private Integer fromStatus;

    /** 动作后状态 */
    private Integer toStatus;

    /** 操作人ID（系统触发为空） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /** 操作人姓名（系统触发为空） */
    private String operatorName;

    /** 动作说明 */
    private String actionNote;
}
