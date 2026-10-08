package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 护理文书（三测单 / 护理记录单 / 生命体征监测）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nursing_record")
public class BizNursingRecord extends BaseEntity implements Serializable {

    /**
     * 护理文书号（HL + yyyyMMdd + 4位序号）
     */
    private String recordNo;

    /**
     * 入院ID（入院记录的入院ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

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
     * 床号
     */
    private String bedNo;

    /**
     * 文书类型：1-三测单 2-护理记录单 3-生命体征监测
     */
    private Integer nursingType;

    /**
     * 测量 / 记录时间（三测单按时点唯一 —— 唯一索引的一部分）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime measureTime;

    /**
     * 班次：1-白班 2-小夜班 3-大夜班
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
     * 大便次数（次/日）
     */
    private Integer stoolCount;

    /**
     * 尿量（ml）
     */
    private Integer urineVolume;

    /**
     * 入量（ml）
     */
    private Integer intakeVolume;

    /**
     * 出量（ml）
     */
    private Integer outputVolume;

    /**
     * 护理级别：1-特级护理 2-一级护理 3-二级护理 4-三级护理
     */
    private Integer nursingLevel;

    /**
     * 护理措施与病情观察记录正文
     */
    private String nursingContent;

    /**
     * 记录护士ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nurseId;

    /**
     * 记录护士姓名
     */
    private String nurseName;

    /**
     * 文书状态：1-草稿 2-已提交 3-已归档
     */
    private Integer recordStatus;
}
