package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 问答引用的知识来源。
 */
@Data
public class KnowledgeSourceVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long docId;

    /**
     * 文档标题
     */
    private String docTitle;

    /**
     * 分类
     */
    private String category;

    /**
     * 文档版本号
     */
    private String docVersion;

    /**
     * 文档来源名称（诊疗指南/论坛等）
     */
    private String docSourceName;

    /**
     * 片段摘要（原文截断）
     */
    private String snippet;
}
