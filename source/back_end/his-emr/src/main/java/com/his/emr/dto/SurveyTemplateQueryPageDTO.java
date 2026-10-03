package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 问卷模板分页入参。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SurveyTemplateQueryPageDTO extends PageParam implements Serializable {

    /** 问卷名称/编号模糊 */
    private String keyword;

    /** 适用场景（1-出院随访 2-门诊 3-住院在院 4-体检） */
    private Integer scene;

    /** 状态（1-启用 2-停用） */
    private Integer status;
}
