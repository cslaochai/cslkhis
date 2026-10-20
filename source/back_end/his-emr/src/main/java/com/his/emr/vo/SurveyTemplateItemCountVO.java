package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 满意度：一次捞多张卷的题目数（列表页那一列，避免逐行count 的 N+1）。
 */
@Data
public class SurveyTemplateItemCountVO implements Serializable {

    /**
     * 问卷模板ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /**
     * 该模板下的题目数
     */
    private Long cnt;
}