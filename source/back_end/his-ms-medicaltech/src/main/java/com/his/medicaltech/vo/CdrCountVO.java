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

    /** 计数项标识（事件类型码） */
    @Schema(description = "标识")
    private String key;

    /** 计数项中文名（由事件类型枚举 label 渲染） */
    @Schema(description = "中文名")
    private String label;

    /** 该类事件的条数 */
    @Schema(description = "条数")
    private Integer count;
}
