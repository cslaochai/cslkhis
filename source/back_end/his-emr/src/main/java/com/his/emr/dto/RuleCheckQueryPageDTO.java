package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 临床规则校验记录查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RuleCheckQueryPageDTO extends PageParam {

    /**
     * 患者ID
     */
    private Long patientId;

    /** 规则类型（1-配伍禁忌 2-检验诊断关联性 3-用药合理性） */
    private Integer ruleType;

    /** 处理状态（1-待处理 2-已处理 3-已忽略） */
    private Integer checkStatus;
}
