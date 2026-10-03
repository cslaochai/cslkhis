package com.his.medicaltech.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 心电报告审核 / 退回入参（sql/173）。
 */
@Data
public class EcgAuditDTO {

    @NotNull(message = "缺少报告ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportId;

    /** 审核意见（通过时选填）/ 退回原因（退回时必填） */
    private String reason;
}
