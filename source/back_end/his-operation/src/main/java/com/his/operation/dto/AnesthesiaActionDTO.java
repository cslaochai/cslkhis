package com.his.operation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 麻醉记录单动作（提交 / 审核 / 计费）共用入参。
 */
@Data
public class AnesthesiaActionDTO implements Serializable {

    /**
     * 麻醉记录单ID / PACU 记录ID（按动作语义取用）
     */
    @NotNull(message = "记录ID不能为空")
    private Long id;

    /**
     * 备注
     */
    private String remark;
}
