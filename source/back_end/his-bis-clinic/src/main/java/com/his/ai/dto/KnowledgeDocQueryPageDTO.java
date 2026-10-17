package com.his.ai.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识文档分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeDocQueryPageDTO extends PageParam {

    /**
     * 标题模糊
     */
    private String title;

    /**
     * 分类精确
     */
    private String category;
}
