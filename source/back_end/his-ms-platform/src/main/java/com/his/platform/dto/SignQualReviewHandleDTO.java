package com.his.platform.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 签名资格复核处理入参。
 */
@Data
public class SignQualReviewHandleDTO {

    @NotNull(message = "复核单ID不能为空")
    private Long id;

    /**
     * 处理动作（1-保留证书 2-吊销证书）；吊销时备注必填
     */
    @NotNull(message = "处理动作不能为空")
    @AllowedValues(values = {"1", "2"}, message = "处理动作取值不合法")
    private Integer action;

    /**
     * 处理意见（吊销时作为吊销原因，必填）
     */
    private String remark;
}
