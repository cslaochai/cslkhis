package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 患者端-待填满意度问卷（小程序「满意度评价」列表行）。
 */
@Data
public class PatientSurveyTaskVO {

    /**
     * 发放单ID（提交答卷时回传）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispatchId;

    /**
     * 问卷名称
     */
    private String templateName;

    /**
     * 适用场景文案
     */
    private String sceneText;

    /**
     * 就诊科室
     */
    private String deptName;

    /**
     * 回收截止时间（yyyy-MM-dd HH:mm:ss，空=不限期）
     */
    private String expireTime;

    /**
     * 发放时间（yyyy-MM-dd HH:mm:ss）
     */
    private String pushTime;
}
