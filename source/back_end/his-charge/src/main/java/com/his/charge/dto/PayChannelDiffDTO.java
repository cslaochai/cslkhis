package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 长款/短款处理入参（定性必须留文字依据）
 */
@Data
public class PayChannelDiffDTO {

    /**
     * 渠道流水台账ID
     */
    @NotNull(message = "台账行不能为空")
    private Long id;

    /**
     * 处理结论：2-长款（渠道有本地无） 3-短款（金额不符待核）
     */
    @NotNull(message = "处理结论不能为空")
    private Integer handleType;

    /**
     * 长款/短款处理说明
     */
    @NotBlank(message = "处理说明不能为空（长短款定性必须留依据）")
    private String handleRemark;
}
