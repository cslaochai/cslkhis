package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 麻醉期间生命体征采样（麻醉期间生命体征）—— 只增不改。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_anesthesia_vital")
public class BizAnesthesiaVital extends BaseEntity implements Serializable {

    /**
     * 麻醉记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 采样时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sampleTime;

    /**
     * 收缩压（mmHg）
     */
    private Integer systolic;

    /**
     * 舒张压（mmHg）
     */
    private Integer diastolic;

    /**
     * 心率（次/分）
     */
    private Integer heartRate;

    /**
     * 呼吸频率（次/分）
     */
    private Integer respiration;

    /**
     * 体温（℃）
     */
    private BigDecimal temperature;

    /**
     * 脉搏血氧饱和度（%）
     */
    private Integer spo2;

    /**
     * 呼气末二氧化碳分压（mmHg）
     */
    private Integer etco2;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
}
