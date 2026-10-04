package com.his.ai.dto;

import lombok.Data;

/**
 * 知识文档分页查询。
 */
@Data
public class KnowledgeDocQueryPageDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    /**
     * 标题模糊
     */
    private String title;

    /**
     * 分类精确
     */
    private String category;
}
