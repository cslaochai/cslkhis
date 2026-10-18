package com.his.ai.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问答观测日志出参。
 */
@Data
public class KnowledgeQueryLogVO {

    /** 日志ID（Long 序列化为字符串，防前端精度丢失） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 提问原文 */
    private String question;

    /** 回答（降级时为检索原文拼接） */
    private String answer;

    /** 命中文档列表（标题分号分隔，最多 5 个） */
    private String hitDocs;

    /** 命中块数 */
    private Integer hitCount;

    /** 是否模型降级（0-正常 1-降级） */
    private Integer degraded;

    /** 降级原因（未降级时为空） */
    private String degradeReason;

    /** 耗时（毫秒） */
    private Integer latencyMs;

    /** 用户反馈（0-未反馈 1-有用 2-无用） */
    private Integer feedback;

    /** 提问操作人姓名 */
    private String operatorName;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
