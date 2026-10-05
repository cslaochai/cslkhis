package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 护理文书 VO（三测单 / 护理记录单 / 生命体征监测共用）。
 */
@Data
public class NursingRecordVO implements Serializable {

    /**
     * 文书ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 护理文书号
     */
    private String recordNo;

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
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 病区名称（快照）
     */
    private String wardName;

    /**
     * 床号（快照）
     */
    private String bedNo;

    /**
     * 文书类型（1-三测单 2-护理记录单 3-生命体征监测）
     */
    private Integer nursingType;

    /**
     * 文书类型文案
     */
    private String nursingTypeText;

    /**
     * 测量 / 记录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime measureTime;

    /**
     * 测量日期（yyyy-MM-dd，前端三测单按天分列用）
     */
    private String measureDate;

    /**
     * 时点（HH:mm，前端三测单按时间点画曲线用）
     */
    private String measureClock;

    /**
     * 班次（1-白班 2-小夜班 3-大夜班）
     */
    private Integer shift;

    /**
     * 班次文案
     */
    private String shiftText;

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
     * 血压文案（如 "120/80"；缺任一值为 "—"）
     */
    private String bloodPressureText;

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
     * 护理级别（1-特级护理 2-一级护理 3-二级护理 4-三级护理）
     */
    private Integer nursingLevel;

    /**
     * 护理级别文案
     */
    private String nursingLevelText;

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
     * 文书状态（1-草稿 2-已提交 3-已归档）
     */
    private Integer recordStatus;

    /**
     * 文书状态文案
     */
    private String recordStatusText;

    /**
     * 是否可编辑（已归档为 false）
     */
    private Boolean canEdit;

    /**
     * 备注
     */
    private String remark;
}
