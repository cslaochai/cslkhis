package com.his.ai.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识文档列表项。
 */
@Data
public class KnowledgeDocListVO {

    /** 文档ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 文档标题 */
    private String title;

    /** 文档分类 */
    private String category;

    /**
     * 来源类型（1-内置示例 2-手工录入 3-文件导入）
     */
    private Integer sourceType;

    /**
     * 版本号
     */
    private String version;

    /**
     * 来源名称（诊疗指南/论坛/院内制度等）
     */
    private String sourceName;

    /**
     * 切块数量
     */
    private Integer chunkCount;

    /**
     * 状态（0-正常 1-停用）
     */
    private Integer status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
