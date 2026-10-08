package com.his.emr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 代煎单作废入参（本表没有删除路径，作废是唯一让它停止流转的动作）
 */
@Data
public class TcmDecoctCancelDTO {

    @NotNull(message = "代煎单ID不能为空")
    private Long id;

    /**
     * 原因
     */
    @NotBlank(message = "作废必须填写原因")
    private String reason;
}
