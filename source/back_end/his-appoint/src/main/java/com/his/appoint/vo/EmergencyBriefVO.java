package com.his.appoint.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 急诊记录跨域摘要：AI 分诊建议读取急诊事实的最小字段集。
 */
@Data
@NoArgsConstructor
public class EmergencyBriefVO {

    /**
     * 急诊记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 急诊编号
     */
    private String emergencyNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 年龄（岁）
     */
    private Integer age;

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 生命体征原文（JSON 或「T39.5 P130 BP80/50」文本，解析归调用方）
     */
    private String vitalSigns;

    /**
     * 分诊级别（1-危急 2-重症 3-急症 4-非急症）
     */
    private Integer triageLevel;

    /**
     * 分区（红区/黄区/绿区）
     */
    private String zone;

    /**
     * 绿色通道（胸痛中心/卒中中心/创伤中心/无）
     */
    private String greenChannel;
}
