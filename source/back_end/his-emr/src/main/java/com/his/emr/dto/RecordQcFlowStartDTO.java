package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 发起三级质控流转 DTO
 */
@Data
public class RecordQcFlowStartDTO implements Serializable {

    /** 病历ID */
    @NotNull(message = "病历ID不能为空")
    private Long recordId;

    /** 发起备注（可空） */
    private String remark;
}
