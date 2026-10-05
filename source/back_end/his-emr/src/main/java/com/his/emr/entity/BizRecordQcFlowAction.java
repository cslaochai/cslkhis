package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 质控流转动作时间线（质控流转动作时间线）
 */
@Data
@TableName("biz_record_qc_flow_action")
public class BizRecordQcFlowAction {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 流转单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long flowId;

    /**
     * 动作发生级（1-科级 2-病案室 3-医务处）
     */
    private Integer level;

    /**
     * 动作（1-发起送审 2-审核通过 3-退回整改 4-整改提交 5-终审通过）
     */
    private Integer action;

    /**
     * 审核意见
     */
    private String opinion;

    /**
     * 缺陷明细（退回时必填）
     */
    private String defectDetail;

    /**
     * 整改要求（退回时必填）
     */
    private String requirement;

    /**
     * 操作人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 动作时间
     */
    private LocalDateTime actionTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
}
