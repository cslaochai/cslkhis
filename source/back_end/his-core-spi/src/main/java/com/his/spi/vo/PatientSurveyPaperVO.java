package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 患者端-满意度问卷卷面（题目清单 + 已有作答，供重填回显）。
 */
@Data
public class PatientSurveyPaperVO {

    /**
     * 发放单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispatchId;

    /**
     * 问卷名称
     */
    private String templateName;

    /**
     * 就诊科室
     */
    private String deptName;

    /**
     * 是否已填过（1-是，重填会覆盖原答卷）
     */
    private Integer answered;

    /**
     * 题目清单
     */
    private List<PatientSurveyQuestionVO> items;

    /**
     * 已填写的开放意见（未填过为空）
     */
    private String commentText;
}
