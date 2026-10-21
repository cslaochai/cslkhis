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

    /** 历史版本记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 文档ID（Long 序列化为字符串，防前端精度丢失） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long docId;

    /** 文档标题（历史快照） */
    private String title;

    /** 文档分类 */
    private String category;

    /** 来源类型（1-内置示例 2-手工录入 3-文件导入） */
    private Integer sourceType;

    /** 版本号 */
    private String version;

    /** 来源名称（诊疗指南/论坛/院内制度等） */
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
