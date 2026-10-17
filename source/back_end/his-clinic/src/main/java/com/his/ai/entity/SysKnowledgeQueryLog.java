package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库问答观测日志（只追加不改；反馈列可更新）。
 */
@Data
@TableName("sys_knowledge_query_log")
public class SysKnowledgeQueryLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 问题 */
    private String question;

    /** 回答（降级时为检索原文拼接） */
    private String answer;

    /** 命中文档列表（标题分号分隔，最多 5 个） */
    private String hitDocs;

    /** 命中块数 */
    private Integer hitCount;

    /** 是否模型降级（0-正常 1-降级） */
    private Integer degraded;

    /** 降级原因 */
    private String degradeReason;

    /** 耗时（毫秒） */
    private Integer latencyMs;

    /** 用户反馈（0-未反馈 1-有用 2-无用） */
    private Integer feedback;

    /** 提问人ID */
    private Long operatorId;

    /** 提问人 */
    private String operatorName;

    /** 提问时间 */
    private LocalDateTime createTime;
}
