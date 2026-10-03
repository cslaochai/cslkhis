package com.his.ai.dto;

import com.his.common.base.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 调用审计分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI 调用审计分页查询入参")
public class AiAuditLogQueryPageDTO extends PageParam {

    /**
     * 能力标识
     */
    @Schema(description = "能力标识：icd10 / drug_audit / emr_qc")
    private String capabilityKey;

    @Schema(description = "调用状态：1-成功 2-失败 3-超时 4-降级 5-熔断")
    private Integer status;

    /**
     * 业务类型
     */
    @Schema(description = "业务类型，如 prescription / medical_record")
    private String bizType;

    /**
     * 业务ID
     */
    @Schema(description = "业务ID")
    private Long bizId;

    /**
     * 调用人
     */
    @Schema(description = "操作人账号")
    private String operator;

    /**
     * 开始日期
     */
    @Schema(description = "开始日期 yyyy-MM-dd")
    private String startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期 yyyy-MM-dd")
    private String endDate;
}
