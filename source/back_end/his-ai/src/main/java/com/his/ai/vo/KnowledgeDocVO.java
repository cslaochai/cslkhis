package com.his.ai.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识文档详情（含原文）。
 */
@Data
public class KnowledgeDocVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String title;

    private String category;

    private Integer sourceType;

    private String content;

    private Integer chunkCount;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
