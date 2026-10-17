package com.his.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * 运营问数路由兜底（模型选表）输出。
 */
@Data
public class OperationQaRouteLlmOutputDTO {

    /**
     * 问题涉及的数据表名列表（只允许从表目录选择）
     */
    private List<String> tables;
}
