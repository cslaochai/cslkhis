package com.his.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 按对象验签入参：验该对象**全部**签名（含作废的）。
 */
@Data
public class SignatureVerifyByBizDTO {

    /** 签名对象类型 */
    @NotNull(message = "签名对象类型不能为空")
    private Integer bizType;

    /** 签名对象ID */
    @NotNull(message = "签名对象ID不能为空")
    private Long bizId;
}
