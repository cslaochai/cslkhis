package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 问数结果的单列定义。
 */
@Data
@Schema(description = "问数结果列定义")
public class OperationColumnVO {

    /** 列标识（c1、c2…），与行数据按下标对应 */
    @Schema(description = "列标识（c1、c2…），行数据与列按下标对应")
    private String key;

    /** 列展示名 */
    @Schema(description = "列名（SQL 中文别名）")
    private String label;
}
