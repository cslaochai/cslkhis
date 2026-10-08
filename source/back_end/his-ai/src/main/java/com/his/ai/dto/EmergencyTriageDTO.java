package com.his.ai.dto;

import lombok.Data;

/**
 * 急诊分诊建议入参
 */
@Data
public class EmergencyTriageDTO {

    /**
     * 急诊记录ID，可空
     */
    private Long emergencyId;

    /**
     * 主诉（覆盖库中值）
     */
    private String chiefComplaint;

    /**
     * 生命体征（覆盖库中值），支持 JSON 或「T39.5 P130 BP80/50」文本
     */
    private String vitalSigns;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 年龄（覆盖库中值）
     */
    private Integer age;

    /**
     * 是否调用模型做语义补充，默认 true。
     * 设为 false 时只返回硬规则结论，用于需要确定性结果的场合。
     */
    private Boolean useModel;
}
