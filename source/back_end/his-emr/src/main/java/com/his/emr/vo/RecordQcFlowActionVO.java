package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 三级质控流转动作 VO（时间线）
 */
@Data
public class RecordQcFlowActionVO {

    /**
     * 主键
     */
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
     * 缺陷明细
     */
    private String defectDetail;

    /**
     * 整改要求
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

    // 文案
    private String levelText;
    private String actionText;
}
