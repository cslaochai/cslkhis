package com.his.clinical.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医疗纠纷 / 投诉处理跟踪台账（追加式，不承载状态）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_dispute_flow")
public class BizDisputeFlow extends BaseEntity {

    /**
     * 主单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long caseId;

    /**
     * 动作（受理/调查/协商/回复投诉人/封存病历/结案/撤销…）
     */
    private String action;

    /**
     * 动作前状态
     */
    private Integer fromStatus;

    /**
     * 动作后状态
     */
    private Integer toStatus;

    /**
     * 处理说明
     */
    private String content;

    /**
     * 操作人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operator;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
