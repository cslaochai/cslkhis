package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 一条分诊记录（分诊卡回显 / 历史留痕）。
 */
@Data
@Schema(description = "门诊分诊记录")
public class TriageRecordVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
    @Schema(description = "体温(℃)")
    private BigDecimal temperature;

    /**
     * 脉搏(次/分)
     */
    @Schema(description = "脉搏(次/分)")
    private Integer pulse;

    /**
     * 呼吸(次/分)
     */
    @Schema(description = "呼吸(次/分)")
    private Integer respiration;

    /**
     * 收缩压(mmHg)
     */
    @Schema(description = "收缩压(mmHg)")
    private Integer systolicBp;

    /**
     * 舒张压(mmHg)
     */
    @Schema(description = "舒张压(mmHg)")
    private Integer diastolicBp;

    /**
     * 血氧饱和度(%)
     */
    @Schema(description = "血氧饱和度(%)")
    private Integer spo2;

    /**
     * 身高(cm)
     */
    @Schema(description = "身高(cm)")
    private BigDecimal height;

    /**
     * 体重(kg)
     */
    @Schema(description = "体重(kg)")
    private BigDecimal weight;

    /**
     * BMI
     */
    @Schema(description = "BMI")
    private BigDecimal bmi;

    /**
     * 疼痛评分
     */
    @Schema(description = "疼痛评分(0~10)")
    private Integer painScore;

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 分诊等级（1-危重 2-急症 3-亚急 4-非急）
     */
    @Schema(description = "分诊等级（1-危重 2-急症 3-亚急 4-非急）")
    private Integer triageLevel;

    /**
     * 等级文案由后端给，前端不自己拼
     */
    @Schema(description = "分诊等级文案")
    private String triageLevelText;

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
     * 分诊护士员工ID
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

    /**
     * 备注
     */
    private String remark;
}
