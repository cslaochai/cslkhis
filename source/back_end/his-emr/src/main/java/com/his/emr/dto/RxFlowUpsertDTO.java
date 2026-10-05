package com.his.emr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处方流转单创建入参
 */
@Data
public class RxFlowUpsertDTO {
    @NotNull(message = "处方ID不能为空")
    private Long prescriptionId;
    @NotBlank(message = "流向机构名称不能为空")
    private String orgName;
    /**
     * 1-院外药店 2-基层医疗机构 3-线上药房，空默认 1
     */
    private Integer orgType;
    private String remark;
}
