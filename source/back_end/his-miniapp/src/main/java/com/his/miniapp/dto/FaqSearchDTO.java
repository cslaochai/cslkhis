package com.his.miniapp.dto;

import com.his.common.base.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Data;

/**
 * 常见问题查询（关键词 + 分类）。
 */
@Data
@Schema(name = "FaqSearchDTO", description = "常见问题查询")
public class FaqSearchDTO extends PageParam {

    /** 搜索关键词（口语提问即可，后端切词后匹配） */
    private String keyword;

    /** 分类编码（空 = 全部） */
    private String categoryCode;

}
