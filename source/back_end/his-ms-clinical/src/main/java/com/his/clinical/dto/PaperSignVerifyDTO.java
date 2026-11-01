package com.his.clinical.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 纸质签名核验登记入参：医师无电子证书时，病案管理科对纸质病历手写签名人工核验后登记。
 * 纸质核验只作为归档的替代凭证，不回写病历电子签名状态。
 */
@Data
@Schema(description = "纸质签名核验登记入参")
public class PaperSignVerifyDTO {

    /**
     * 归档记录ID
     */
    @Schema(description = "归档记录ID")
    @NotNull(message = "归档记录ID不能为空")
    private Long archiveId;

    /**
     * 纸质签名件编号（纸质病历号/档案号）
     */
    @Schema(description = "纸质签名件编号")
    @NotBlank(message = "纸质签名件编号不能为空")
    @Size(max = 64, message = "纸质签名件编号不能超过64个字符")
    private String paperCertNo;

    /**
     * 核验说明（核验范围、纸质件与系统内容一致性结论等）
     */
    @Schema(description = "核验说明")
    @NotBlank(message = "核验说明不能为空")
    @Size(max = 500, message = "核验说明不能超过500个字符")
    private String remark;
}
