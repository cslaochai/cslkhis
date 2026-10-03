package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 问卷模板下拉选项（出题/发放选卷用，不返回题目正文）。
 */
@Data
public class SurveyTemplateSelectListVO implements Serializable {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 模板编号 */
    private String templateNo;

    /** 问卷名称 */
    private String templateName;

    /** 适用场景（1-出院随访 2-门诊 3-住院在院 4-体检） */
    private Integer scene;

    /** 状态（1-启用 2-停用） */
    private Integer status;

    /** 题数（0 题的卷发出去就是空卷，下拉里先露出来） */
    private Integer itemCount;
}
