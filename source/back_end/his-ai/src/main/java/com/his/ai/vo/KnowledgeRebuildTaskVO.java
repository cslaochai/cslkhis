package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 单文档重建索引任务出参（异步任务，前端轮询进度）。
 */
@Data
public class KnowledgeRebuildTaskVO {

    /** 任务ID（Long 序列化为字符串，防前端精度丢失） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    /** 文档ID（Long 序列化为字符串，防前端精度丢失） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long docId;

    /** 状态（1-进行中 2-完成 3-失败） */
    private Integer status;

    /** 进度 0-100，切块级精确进度 */
    private Integer progress;

    /** 重建完成后的切块数量（完成态才有） */
    private Integer chunkCount;

    /** 失败原因 */
    private String errorMsg;
}
