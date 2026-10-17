package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交班弹框里的「待交班清单」行 —— 交班前的实时视图（不落库，落库的是提交后的明细快照）。
 */
@Data
public class EmergencyHandoverPendingVO {

    /**
     * 急诊记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long emergencyId;

    /**
     * 急诊号
     */
    private String emergencyNo;

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
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 分诊级别（1-I级濒危 2-II级危重 3-III级急症 4-IV级非急症）
     */
    private Integer triageLevel;

    /**
     * 分诊级别文案（Ⅰ级濒危…）
     */
    private String triageLevelText;

    /**
     * 急诊状态（1-候诊 2-诊治中 3-留观 4-转住院 5-离院 6-死亡）
     */
    private Integer emergencyStatus;

    /**
     * 急诊状态文案
     */
    private String emergencyStatusText;

    /**
     * 就诊科室名称
     */
    private String deptName;

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 初步诊断
     */
    private String diagnosis;

    /**
     * 当前负责医生ID（NULL = 在待派单池里无人负责）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 当前负责医生姓名
     */
    private String doctorName;

    /**
     * 无人指派标记（true = 本条必须由交班落到具体人头上）
     */
    private Boolean poolFlag;

    /**
     * 入急诊时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime admissionTime;

    /**
     * 应接诊时限（分钟，登记时快照）
     */
    private Integer targetSeeMinutes;

    /**
     * 已候诊分钟数（只有候诊中才算到当下）
     */
    private Long waitMinutes;

    /**
     * 候诊超时档位：0-未超时 1-超时 2-严重超时
     */
    private Integer overdueLevel;

    /**
     * 候诊超时文案
     */
    private String overdueText;

    /**
     * 留观床位号
     */
    private String observationBed;

    /**
     * 开始留观时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime observationStartTime;

    /**
     * 已留观小时数（向下取整；未留观为空）
     */
    private Long obsHours;

    /**
     * 留观档位：0-未达预警 1-超预警 2-超上限
     */
    private Integer obsLevel;

    /**
     * 留观档位文案
     */
    private String obsLevelText;
}
