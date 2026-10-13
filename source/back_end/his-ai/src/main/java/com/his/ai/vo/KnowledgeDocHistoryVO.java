package com.his.ai.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识文档历史版本出参（留痕快照，含原文）。
 */
@Data
public class KnowledgeDocHistoryVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 文档ID（Long 序列化为字符串，防前端精度丢失） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long docId;

    private String title;

    private String category;

    private Integer sourceType;

    private String version;

    private String sourceName;

    /** 内容快照 */
    private String content;

    /** 变更类型（1-新建 2-编辑 3-回退） */
    private Integer changeType;

    /** 操作人 */
    private String operatorName;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
