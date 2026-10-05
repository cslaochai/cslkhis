package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 审核通过 / 整改提交 DTO（两者都只需流转单 + 意见）
 */
@Data
public class RecordQcFlowOpinionDTO implements Serializable {

    /**
     * 流转单ID
     */
    @NotNull(message = "流转单ID不能为空")
    private Long flowId;

    /**
     * 审核意见 / 整改说明（可空）
     */
    private String opinion;
}
