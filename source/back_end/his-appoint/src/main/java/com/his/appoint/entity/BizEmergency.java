package com.his.appoint.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


/**
 * 急诊记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_emergency")
public class BizEmergency extends BaseEntity {

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
     * 患者号
     */
    private String patientNo;

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
     * 联系电话
     */
    private String phone;

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 分诊级别（1-I级濒危 2-II级危重 3-III级急症 4-IV级非急症）
     */
    private Integer triageLevel;

    /**
     * 区域（红区/黄区/绿区）
     */
    private String zone;

    /**
     * 绿色通道（胸痛中心/卒中中心/创伤中心/无）
     */
    private String greenChannel;

    /**
     * 接诊科室ID
     */
    @NotNull(message = "患者信息不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 接诊科室
     */
    private String deptName;

    /**
     * 接诊医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 接诊医生
     */
    private String doctorName;

    /**
     * 派单方式（0-历史未记 1-登记指定 2-系统按当日排班派单 3-无在岗医生入池待派单），
     * 见 {@link com.his.common.enums.EmergencyAssignTypeEnum}
     */
    private Integer assignType;

    /**
     * 未派单原因（assignType=3 时有值；写库前一律截到 200 字符）
     */
    private String unassignedReason;

    /**
     * 该级别的应接诊时限（分钟，登记时刻按系统参数快照；0=即刻）
     */
    private Integer targetSeeMinutes;

    /**
     * 生命体征（JSON格式）
     */
    private String vitalSigns;

    /**
     * 初步诊断
     */
    private String diagnosis;

    /**
     * 处理措施
     */
    private String treatment;

    /**
     * 急诊状态（1-候诊 2-诊治中 3-留观 4-转住院 5-离院 6-死亡）
     */
    private Integer emergencyStatus;

    /**
     * 留观床位号
     */
    private String observationBed;

    /**
     * 留观病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long observationWardId;

    /**
     * 留观床位ID（床位，同一张床同时只允许一名在观患者）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long observationBedId;

    /**
     * 开始留观时间
     */
    private LocalDateTime observationStartTime;

    /**
     * 结束留观时间（转住院/离院/死亡时写入）
     */
    private LocalDateTime observationEndTime;

    /**
     * 转住院产生的入院记录ID（入院记录，闭环回指）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 入急诊时间
     */
    private LocalDateTime admissionTime;

    /**
     * 开始诊治时间
     */
    private LocalDateTime diagnosisTime;

    /**
     * 结束时间
     */
    private LocalDateTime finishTime;

    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
