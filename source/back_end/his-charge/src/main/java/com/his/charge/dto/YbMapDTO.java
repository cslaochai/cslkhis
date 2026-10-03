package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 人工对照入参（已存在旧对照则为换对照/覆盖）。
 */
@Data
public class YbMapDTO {

    /**
     * 院内项目类型（1-药品 2-诊疗项目 3-检验项目 4-耗材）
     */
    @NotNull(message = "项目类型不能为空")
    private Integer itemType;

    /**
     * 院内项目ID
     */
    @NotNull(message = "院内项目ID不能为空")
    private Long itemId;

    /**
     * 医保目录ID
     */
    @NotNull(message = "医保目录ID不能为空")
    private Long catalogId;
}
