package com.his.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 导入任务确认入库入参：预览解析文本后补充/修正元数据，触发异步切块建索引。
 */
@Data
public class KnowledgeImportCommitDTO {

    /** 导入任务ID（Long 入参，前端传字符串由 Jackson 自动反序列化） */
    @NotNull
    private Long id;

    /** 文档标题（默认取文件名去扩展名，必填） */
    @NotBlank(message = "标题不能为空")
    private String title;

    /** 分类（必填） */
    @NotBlank(message = "分类不能为空")
    private String category;

    /** 版本号（必填，如 v1.0） */
    @NotBlank(message = "版本不能为空")
    private String version;

    /** 内容来源（必填，诊疗指南/论坛/院内制度等） */
    @NotBlank(message = "内容来源不能为空")
    private String sourceName;
}
