package com.his.patient.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 营养科接收/退回膳食方案（批量，全成功或全不生效）。
 */
@Data
public class DietConfirmDTO {

    /** 主键ID集合 */
    @NotEmpty(message = "请选择要处理的膳食方案")
    private List<Long> ids;

    /** true-接收 false-退回 */
    @NotNull(message = "接收/退回不能为空")
    private Boolean accept;

    /** 退回原因（accept=false 必填） */
    private String rejectReason;
}
