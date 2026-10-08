package com.his.operation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 取消手术入参。
 */
@Data
public class OperationCancelDTO implements Serializable {

    /**
     * 手术申请单ID（必填）
     */
    @NotNull(message = "手术申请单ID不能为空")
    private Long applyId;

    /**
     * 取消原因（必填：停台是一个临床决定，必须有人负责）
     */
    @NotBlank(message = "取消原因不能为空（停台是一个临床决定，必须有人负责）")
    private String cancelReason;
}
