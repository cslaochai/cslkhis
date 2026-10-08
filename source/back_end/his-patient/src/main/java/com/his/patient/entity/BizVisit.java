package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 就诊次—— 聚合"一次来院"的所有挂号/病历/处方/收费。
 */
@Data
@TableName("biz_visit")
public class BizVisit implements Serializable {

    /**
     * 就诊次ID
     */
    @TableId(value = "visit_id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long visitId;

    /**
     * 就诊次编号（VISIT + yyyyMMdd + 3位序号）
     */
    private String visitNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 就诊开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 就诊结束时间（未结束为 null）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 本次就诊总费用
     */
    private BigDecimal totalAmount;

    /**
     * 就诊状态（0-已取消 1-进行中 2-已完成）
     */
    private Integer visitStatus;

    /**
     * 关联的挂号ID列表（逗号分隔）
     */
    private String registIds;

    /**
     * 备注
     */
    private String remark;
}
