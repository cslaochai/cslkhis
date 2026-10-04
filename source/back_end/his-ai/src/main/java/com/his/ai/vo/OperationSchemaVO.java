package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 运营问数可查询数据域（白名单表）的展示项。
 */
@Data
@Schema(description = "可查询数据域")
public class OperationSchemaVO {

    @Schema(description = "表名")
    private String tableName;

    @Schema(description = "用途说明")
    private String description;
}
