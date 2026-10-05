package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 退回整改 DTO（任一审核级通用，缺陷明细 + 整改要求必填）
 */
@Data
public class RecordQcFlowReturnDTO implements Serializable {

    /**
     * 流转单ID
     */
    @NotNull(message = "流转单ID不能为空")
    private Long flowId;

    /**
     * 缺陷明细
     */
    @NotBlank(message = "缺陷明细不能为空——退回必须写清缺陷")
    private String defectDetail;

    /**
     * 整改要求
     */
    @NotBlank(message = "整改要求不能为空")
    private String requirement;

    /**
     * 整改期限（可空；yyyy-MM-dd）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate returnDeadline;

    /**
     * 审核意见（可空）
     */
    private String opinion;
}
