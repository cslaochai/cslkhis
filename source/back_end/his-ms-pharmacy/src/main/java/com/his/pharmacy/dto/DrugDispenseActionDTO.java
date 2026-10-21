package com.his.pharmacy.dto;

import lombok.Data;

/**
 * 发药入参（整单/单行发药动作）。
 *
 * <p>类名 {@code DrugDispenseActionDTO}：本模块已有 {@code DrugDispenseDTO}（住院领药发药动作，
 * dispensingId/dispenseBy），两者形状不同，避免同包冲突。
 */
@Data
public class DrugDispenseActionDTO {

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
