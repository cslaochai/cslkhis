package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发起申诉（仅「待确认」可发起；理由必填，材料说明写清附了什么）。
 */
@Data
public class DeductAppealDTO {

    @NotNull(message = "扣款通知ID不能为空")
    private Long id;

    /**
     * 申诉理由
     */
    @NotBlank(message = "申诉理由不能为空")
    private String appealReason;

    /**
     * 申诉材料说明
     */
    private String appealMaterial;
}
