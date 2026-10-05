package com.his.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 医保审核证据判定入参（G-07）。
 * <p>对一条<b>已跑完</b>的合规审核记录做证据判定；没有命中规则时直接拒绝，不浪费模型调用。</p>
 */
@Data
public class InsuranceEvidenceDTO {

    /**
     * 合规审核记录ID
     */
    @NotNull(message = "审核记录ID不能为空")
    private Long auditId;
}
