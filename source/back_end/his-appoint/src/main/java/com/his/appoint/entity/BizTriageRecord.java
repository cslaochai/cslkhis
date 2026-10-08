package com.his.appoint.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门诊分诊记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_triage_record")
public class BizTriageRecord extends BaseEntity {

    /**
     * 队列ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long queueId;

    /**
     * 挂号ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

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
     * 患者号
     */
    private String patientNo;

    /**
     * 体温(℃)
     */
    private BigDecimal temperature;

    /**
     * 脉搏(次/分)
     */
    private Integer pulse;

    /**
     * 呼吸(次/分)
     */
    private Integer respiration;

    /**
     * 收缩压(mmHg)
     */
    private Integer systolicBp;

    /**
     * 舒张压(mmHg)
     */
    private Integer diastolicBp;

    /**
     * 血氧饱和度(%)
     */
    private Integer spo2;

    /**
     * 身高(cm)
     */
    private BigDecimal height;

    /**
     * 体重(kg)
     */
    private BigDecimal weight;

    /**
     * BMI（由身高体重算得后落库，不靠前端传）
     */
    private BigDecimal bmi;

    /**
     * 疼痛评分(0~10)
     */
    private Integer painScore;

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 分诊等级（1-危重 2-急症 3-亚急 4-非急）
     */
    private Integer triageLevel;

    /**
     * 分配诊室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomId;

    /**
     * 分配诊室名称
     */
    private String roomName;

    /**
     * 分诊护士员工ID（同站内信 receiver_id 口径，不是用户的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long triageNurseId;

    /**
     * 分诊护士姓名
     */
    private String triageNurseName;

    /**
     * 分诊时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime triageTime;
}
