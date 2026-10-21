package com.his.spi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 合理用药审查的一组入参（一组 = 一张处方/一条医嘱的全部药品行）。
 */
@Data
@NoArgsConstructor
public class DrugRationalGroupDTO {

    /**
     * 调用方定位标识（通常是处方ID）
     */
    private Long groupId;

    /**
     * 组内药品明细行（名称与规格取单据快照）
     */
    private List<DrugRationalCheckItemDTO> items;
}
