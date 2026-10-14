package com.mis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 疑点稽核分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SuspicionQueryDTO extends BizPageParam {

    /** 规则类型 */
    private String ruleType;
    /** 状态（待核实/已确认/已排除） */
    private String status;
    /** 疑点来源（1-经办结算单 2-机构报盘） */
    private Integer bizSource;
}
