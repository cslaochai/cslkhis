package com.his.clinical.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 病历更正申请入参：对已提交/归档病历发起更正审批。
 */
@Data
public class EmrCorrectionApplyDTO {

    /**
     * 病历ID
     */
    @NotNull(message = "请选择要更正的病历")
    private Long recordId;

    /**
     * 更正类型（1-经治医生更正 2-上级代改）
     */
    @NotNull(message = "请选择更正类型")
    @AllowedValues(values = {"1", "2"}, message = "更正类型取值不合法")
    private Integer correctionType;

    /**
     * 更正原因/事由
     */
    @NotBlank(message = "请填写更正原因")
    private String reason;

    /**
     * 拟更正内容说明
     */
    private String plannedContent;

    /**
     * 备注（可选）
     */
    private String remark;
}
