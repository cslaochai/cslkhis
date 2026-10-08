package com.his.system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 合理用药审查的一组入参（一组 = 一张处方/一条医嘱的全部药品行）
 */
@Data
public class DrugRationalGroupDTO {

    /**
     * 调用方定位标识（通常是处方ID）
     */
    @NotNull(message = "分组标识不能为空")
    private Long groupId;

    /**
     * 明细项集合
     */
    @NotEmpty(message = "药品明细不能为空")
    @Valid
    private List<DrugRationalItemDTO> items;
}
