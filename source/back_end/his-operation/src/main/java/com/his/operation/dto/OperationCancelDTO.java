package com.his.operation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 取消手术入参。
 *
 * <p>只允许取消「待排期 / 已排期」的申请。<b>术前核对完成后不允许取消</b> ——
 * 患者已经进了手术区流程（核对完成意味着身份、术式、麻醉、过敏史都已确认），
 * 这时候要停台，需要的是"停手术"这件事本身的记录，而不是把整条申请抹成"从未发生"。
 * 与"已接收的转科不能再取消"是同一原则。
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
