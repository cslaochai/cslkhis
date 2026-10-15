package com.his.appoint.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class BizEmergencyUpsertDTO {

    /**
     * 急诊号
     */
    private String emergencyNo;

    /**
     * 患者ID
     */
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
    @AllowedValues(values = {"1", "2", "9"}, message = "性别取值不合法")
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 身份证号（新患者模式）
     */
    private String idCard;

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
    @AllowedValues(values = {"1", "2", "3", "4"}, message = "分诊级别取值不合法")
    private Integer triageLevel;

    /**
     * 区域（红区/黄区/绿区）
     */
    @AllowedValues(values = {"红区", "黄区", "绿区"}, message = "急诊区域取值不合法")
    private String zone;

    /**
     * 绿色通道（胸痛中心/卒中中心/创伤中心/无）
     */
    @AllowedValues(values = {"胸痛中心", "卒中中心", "创伤中心", "无"}, message = "绿色通道取值不合法")
    private String greenChannel;

    /**
     * 接诊科室ID
     */
    @NotNull(message = "患者信息不能为空")
    private Long deptId;

    /**
     * 接诊科室
     */
    private String deptName;

    /**
     * 接诊医生ID
     */
    private Long doctorId;

    /**
     * 接诊医生
     */
    private String doctorName;

    /**
     * 未派单原因（医生留空时选填；服务端会兜底成「当班无在岗医生」并截到 200 字符，
     */
    private String unassignedReason;

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
    @AllowedValues(values = {"1", "2", "3", "4", "5", "6"}, message = "急诊状态取值不合法")
    private Integer emergencyStatus;

    /**
     * 留观床位号
     */
    private String observationBed;

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

}
