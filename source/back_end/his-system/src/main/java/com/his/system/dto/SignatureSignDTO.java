package com.his.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发起签名入参（管理员补签场景；常规签名由业务动作自动触发）。
 */
@Data
public class SignatureSignDTO {

    /**
     * 签名对象类型（1-住院病历 2-门诊病历 3-住院医嘱）
     */
    @NotNull(message = "签名对象类型不能为空")
    private Integer bizType;

    /**
     * 签名对象ID
     */
    @NotNull(message = "签名对象ID不能为空")
    private Long bizId;

    /**
     * 签名场景，缺省为 5-补签
     */
    private Integer signScene;

    /**
     * 补签原因（补签必填，写进 remark）
     */
    private String remark;
}
