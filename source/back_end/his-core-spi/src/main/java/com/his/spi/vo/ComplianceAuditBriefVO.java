package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 合规审核结论摘要：一次审核挂在哪张结算清单上，以及各条规则的判定明细。
 */
@Data
@NoArgsConstructor
public class ComplianceAuditBriefVO {

    /**
     * 审核记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 审核单号
     */
    private String auditNo;

    /**
     * 结算清单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long settlementId;

    /**
     * 结算清单号
     */
    private String settlementNo;

    /**
     * 全部规则判定明细（含通过与不适用）
     */
    private List<ComplianceAuditItemRowVO> items;
}
