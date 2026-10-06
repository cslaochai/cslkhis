package com.his.medicaltech.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 发血入参：输血科把配血相合的血袋发往病区。
 *
 * <p>只有<b>全部血袋配血相合且袋数配齐</b>（{@code crossmatch_status=2}）才允许发血 ——
 * 少配一袋就发血，输注量对不上申请量；有不合的袋就发血，等于把安全隐患交给护士去发现。
 */
@Data
public class TransfusionIssueDTO implements Serializable {

    /**
     * 输血申请单ID（必填）
     */
    @NotNull(message = "输血申请单ID不能为空")
    private Long applyId;

    /**
     * 发血备注（如"已核对血袋外观"）
     */
    private String issueRemark;

    /**
     * 备注
     */
    private String remark;
}
