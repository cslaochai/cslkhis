package com.his.medicaltech.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 放射报告审核动作入参（审核通过 / 退回重写共用，sql/138）。
 *
 * <p>两个动作都用原因：审核通过时它是可选的审核意见，
 * 退回时它是**必填**的退回原因（服务端判定，不是靠前端约束 ——
 * 「不说为什么就退回」是最让人恼火的协作方式）。
 */
@Data
public class RadioReportAuditDTO {

    @NotNull(message = "缺少报告")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportId;

    /** 审核意见；退回原因为必填，见 Service 内校验 */
    @Size(max = 500, message = "审核意见不能超过 500 字")
    private String reason;
}
