package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 知识库导入任务出参（批量上传 → 异步解析 → 预览确认 → 异步入库）。
 */
@Data
public class KnowledgeImportTaskVO {

    /** 任务ID（Long 序列化为字符串，防前端精度丢失） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 原始文件名 */
    private String fileName;

    /** 状态（1-解析中 2-待确认 3-入库中 4-完成 5-失败 6-已取消） */
    private Integer status;

    /** 进度 0-100；入库阶段为切块级精确进度，解析中为 null（前端转圈） */
    private Integer progress;

    /** 文档标题（解析后默认文件名去扩展名，确认入库时可改） */
    private String title;

    /** 分类（默认取标题） */
    private String category;

    /** 版本号 */
    private String version;

    /** 内容来源（诊疗指南/论坛等） */
    private String sourceName;

    /** 解析全文长度（字符数） */
    private Integer contentLength;

    /** 解析文本预览（前 3000 字符，确认入库前核对） */
    private String contentPreview;

    /** 失败原因 */
    private String errorMsg;
}
