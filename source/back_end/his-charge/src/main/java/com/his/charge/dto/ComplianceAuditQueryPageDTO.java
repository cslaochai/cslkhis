package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 医保合规审核记录查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ComplianceAuditQueryPageDTO extends PageParam {

    /**
     * 结算清单ID
     */
    private Long settlementId;

    /**
     * 就诊锚点：挂号ID
     */
    private Long registId;

    /**
     * 风险等级：0-未发现 1-提示 2-关注 3-高危
     */
    private Integer riskLevel;

    /**
     * 审核类型：1-结算前自查 2-批量筛查 3-医保反馈复核
     */
    private Integer auditType;
}
