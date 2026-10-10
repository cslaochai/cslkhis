package com.his.emr.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 发药入参
 */
@Data
public class DrugDispenseDTO {

    /**
     * 发药记录ID（单行发药时必填）
     */
    private Long id;

    /**
     * 处方ID（整单发药时必填）
     */
    private Long prescriptionId;

    /**
     * 复核药师ID（麻醉药品、第一类精神药品**必填**）。
     */
    private Long checkerId;

    /**
     * 超限量理由。
     */
    private String overLimitReason;
}
