package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 满意度问卷模板（sql/164）。
 *
 * <p>模板只回答「问什么」；「问谁、收没收回来」在满意度发放台账，
 * 「收回来的答案」在满意度答卷。三层不混，否则回收率无从算起。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_survey_template")
public class BizSurveyTemplate extends BaseEntity {

    /** 模板编号（ST+yyyyMMdd+4位） */
    private String templateNo;

    /** 问卷名称 */
    private String templateName;

    /** 适用场景（1-出院随访 2-门诊 3-住院在院 4-体检） */
    private Integer scene;

    /** 状态（1-启用 2-停用） */
    private Integer status;

    /** 说明（调查目的、口径、上报去向） */
    private String description;
}
