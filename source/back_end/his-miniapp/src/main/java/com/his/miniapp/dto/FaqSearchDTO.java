package com.his.miniapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 常见问题查询（关键词 + 分类）。
 */
@Data
@Schema(name = "FaqSearchDTO", description = "常见问题查询")
public class FaqSearchDTO {

    /** 搜索关键词（口语提问即可，后端切词后匹配） */
    private String keyword;

    /** 分类编码（空 = 全部） */
    private String categoryCode;

    @Min(value = 1, message = "pageNum最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "pageSize最小为1")
    @Max(value = 50, message = "pageSize最大为50")
    private Integer pageSize = 10;
}
