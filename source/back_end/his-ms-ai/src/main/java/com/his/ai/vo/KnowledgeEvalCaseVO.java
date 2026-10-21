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

    /** 评测用例记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 用例问题文本 */
    private String question;

    /** 期望命中文档ID（分号分隔） */
    private String expectedDocIds;

    /** 期望命中文档标题（分号分隔） */
    private String expectedDocTitles;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
