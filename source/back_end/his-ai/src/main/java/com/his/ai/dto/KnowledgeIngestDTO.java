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
     * 分类（必填；就诊须知/科室介绍/检查注意事项/药品说明书等）
     */
    @NotBlank(message = "分类不能为空")
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

    /**
     * 版本号（必填；如 v1.0 / 2026.09）
     */
    @NotBlank(message = "版本不能为空")
    private String version;

    /**
     * 来源名称（必填；诊疗指南/论坛/院内制度/药品说明书等）
     */
    @NotBlank(message = "内容来源不能为空")
    private String sourceName;
}
