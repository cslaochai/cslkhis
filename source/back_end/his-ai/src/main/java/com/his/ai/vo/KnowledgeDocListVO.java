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

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String title;

    private String category;

    /**
     * 来源类型（1-内置示例 2-手工录入 3-文件导入）
     */
    private Integer sourceType;

    /**
     * 切块数量
     */
    private Integer chunkCount;

    /**
     * 状态（0-正常 1-停用）
     */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
