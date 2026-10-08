package com.his.medicaltech.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 计数条目（key + 中文名 + 条数）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "计数条目")
public class CdrCountVO {

    @Schema(description = "标识")
    private String key;

    @Schema(description = "中文名")
    private String label;

    @Schema(description = "条数")
    private Integer count;
}
