package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 纠纷/投诉处理跟踪流水 VO（追加式台账，一条动作一行）。
 */
@Data
public class DisputeFlowVO implements Serializable {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operateTime;
}
