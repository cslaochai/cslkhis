package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 合规审核的逐规则判定行：证据判定环节要把命中规则逐条摆给模型看，只需要规则身份与判定依据。
 */
@Data
@NoArgsConstructor
public class ComplianceAuditItemRowVO {

    /**
     * 规则编码
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 判定结果（1-命中 2-通过 3-不适用）
     */
    private Integer result;

    /**
     * 判定依据（含「不适用」的原因）
     */
    private String evidence;
}
