package com.his.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 知识文档录入请求（手工录入 / 文件导入统一入口）。
 */
@Data
public class KnowledgeIngestDTO {

    /**
     * 标题（必填）
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 分类（就诊须知/科室介绍/检查注意事项/药品说明书）
     */
    private String category;

    /**
     * 原始全文（必填）
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 来源类型（2-手工录入 3-文件导入），默认 2
     */
    private Integer sourceType;
}
