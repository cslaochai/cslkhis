package com.his.miniapp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 常见问题分类（带条数，用于客服页分类标签）。
 */
@Data
@Schema(name = "FaqCategoryVO", description = "常见问题分类")
public class FaqCategoryVO {

    /** 分类编码 */
    private String categoryCode;

    /** 分类名称 */
    private String categoryName;

    /** 该分类下启用条数 */
    private Integer count;

    /** 排序号 */
    private Integer sortOrder;
}
