package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 满意度问卷模板
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_survey_template")
public class BizSurveyTemplate extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 模板编号（ST+yyyyMMdd+4位）
     */
    private String templateNo;

    /**
     * 问卷名称
     */
    private String templateName;

    /**
     * 适用场景（1-出院随访 2-门诊 3-住院在院 4-体检）
     */
    private Integer scene;

    /**
     * 状态（1-启用 2-停用）
     */
    private Integer status;

    /**
     * 说明（调查目的、口径、上报去向）
     */
    private String description;
}
