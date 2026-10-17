package com.his.ai.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评测用例出参。
 */
@Data
public class KnowledgeEvalCaseVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String question;

    /** 期望命中文档ID（分号分隔） */
    private String expectedDocIds;

    /** 期望命中文档标题（分号分隔） */
    private String expectedDocTitles;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
