package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交班台账明细行（出参）：当时"这个人交到那个人手上、交代了什么"的定格记录。
 */
@Data
public class EmergencyHandoverItemVO {

    /**
     * 明细ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 交班单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long handoverId;

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
     * 分诊级别
     */
    private Integer triageLevel;

    /**
     * 分诊级别文案
     */
    private String triageLevelText;

    /**
     * 交班时该患者的急诊状态（1-候诊 2-诊治中 3-留观）
     */
    private Integer emergencyStatus;

    /**
     * 交班时的急诊状态文案（定格）
     */
    private String emergencyStatusText;

    /**
     * 交班时的负责医生ID（NULL = 当时无人指派）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromDoctorId;

    /**
     * 交班时的负责医生姓名
     */
    private String fromDoctorName;

    /**
     * 接续责任人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long takeDoctorId;

    /**
     * 接续责任人姓名
     */
    private String takeDoctorName;

    /**
     * 去向/处置交代
     */
    private String disposition;

    /**
     * 逐条补充交代（过敏史/管路/家属联系方式等，截到 300）
     */
    private String handoverNote;

    /**
     * 候诊已等多久（定格）
     */
    private Long waitMinutes;

    /**
     * 已留观小时数（定格）
     */
    private Long obsHours;

    /**
     * 超时档位定格（0-未超时 1-超时 2-严重超时）
     */
    private Integer overdueLevel;

    /**
     * 候诊超时文案（定格）
     */
    private String overdueText;

    /**
     * 提交时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
