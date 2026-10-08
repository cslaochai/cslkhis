package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 病历文书归档入参（支持批量）。
 */
@Data
public class InpatientRecordArchiveDTO implements Serializable {

    /**
     * 文书ID列表（批量归档）
     */
    @NotEmpty(message = "请选择要归档的文书")
    private List<Long> ids;

    /**
     * 归档说明（必填）
     */
    @NotBlank(message = "归档说明必填（归档是单向门，必须留痕说明归档依据/批次）")
    private String remark;
}
