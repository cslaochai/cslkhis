package com.mis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 监管规则分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RuleQueryDTO extends BizPageParam {

    /** 规则类型 */
    private String ruleType;
    /** 状态（1-启用 0-停用） */
    private Integer status;
}
