package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * LIS 室内质控结果与失控处理（室内质控记录）
 *
 * <p>status：1 在控 / 2 警告 / 3 失控（服务端按 Westgard 规则判定后写入，不接受前端传）。
 * handle_status：0 无需处理 / 1 待处理 / 2 已处理；失控记录生成时自动置 1 待处理。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_lis_qc_record")
public class BizLisQcRecord extends BaseEntity {

    /** 质控计划ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;

    /** 质控计划编号 */
    private String planNo;

    /** 检验项目编码 */
    private String itemCode;

    /** 检验项目名称 */
    private String itemName;

    /** 仪器名称 */
    private String instrumentName;

    /** 质控水平（1-低值 2-中值 3-高值） */
    private Integer qcLevel;

    /** 质控日期 */
    private LocalDate qcDate;

    /** 质控时间 */
    private LocalDateTime qcTime;

    /** 质控测定值 */
    private BigDecimal resultValue;

    /** Z 值 =（测定值-靶值）/SD，服务端算 */
    private BigDecimal zScore;

    /** 质控状态（1-在控 2-警告 3-失控） */
    private Integer status;

    /** 命中的 Westgard 规则（逗号分隔） */
    private String violatedRules;

    /** 操作人 */
    private String operator;

    /** 失控处理（0-无需处理 1-待处理 2-已处理） */
    private Integer handleStatus;

    /** 失控原因分析 */
    private String handleCause;

    /** 纠正措施 */
    private String handleMeasure;

    /** 处理人 */
    private String handleBy;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 复核人（不得与处理人同一人） */
    private String reviewBy;

    /** 复核时间 */
    private LocalDateTime reviewTime;
}
