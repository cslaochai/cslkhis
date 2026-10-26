package com.his.outpatient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 取消听班呼叫入参（sql/297）。
 */
@Data
public class DutyCallCancelDTO {

    /** 呼叫单ID */
    @NotNull(message = "呼叫单ID不能为空")
    private Long id;

    /** 取消事由（必填：撤了呼叫得说清为什么，否则事后说不明白） */
    @NotBlank(message = "取消事由不能为空")
    @Size(max = 200, message = "取消事由过长")
    private String cancelReason;
}
