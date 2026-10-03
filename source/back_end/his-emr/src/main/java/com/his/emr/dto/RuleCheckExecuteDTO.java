package com.his.emr.dto;

import lombok.Data;

/**
 * 执行临床规则校验入参
 */
@Data
public class RuleCheckExecuteDTO {

    /**
     * 病历ID
     */
    private Long recordId;

    /** 规则类型（1-配伍禁忌 2-检验诊断关联性 3-用药合理性） */
    private Integer ruleType;

    /**
     * 校验人
     */
    private String checkBy;

}
