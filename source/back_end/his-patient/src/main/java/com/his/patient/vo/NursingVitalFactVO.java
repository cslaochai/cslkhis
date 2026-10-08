package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 护理体征事实（一行护理文书的体征快照）。
 * <p>供跨模块消费（危重预警评分、交接班聚合），只携带事实不携带行为。</p>
 */
@Data
public class NursingVitalFactVO implements Serializable {

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 床号
     */
    private String bedNo;

    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 测量 / 记录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime measureTime;

    /**
     * 班次（1-白班 2-小夜班 3-大夜班）
     */
    private Integer shift;

    /**
     * 体温（℃）
     */
    private BigDecimal temperature;

    /**
     * 脉搏（次/分）
     */
    private Integer pulse;

    /**
     * 呼吸（次/分）
     */
    private Integer respiration;

    /**
     * 收缩压（mmHg）
     */
    private Integer systolicPressure;

    /**
     * 舒张压（mmHg）
     */
    private Integer diastolicPressure;

    /**
     * 血氧饱和度（%）
     */
    private Integer spo2;

    /**
     * 护理级别（1-特级护理 2-一级护理 3-二级护理 4-三级护理）
     */
    private Integer nursingLevel;

    /**
     * 护理措施与病情观察记录正文
     */
    private String nursingContent;
}
