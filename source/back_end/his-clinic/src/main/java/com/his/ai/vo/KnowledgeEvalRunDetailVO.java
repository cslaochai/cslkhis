package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 评测运行明细出参（每用例一条）。
 */
@Data
public class KnowledgeEvalRunDetailVO {

    /** 运行明细记录ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 用例ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long caseId;

    /** 用例问题文本 */
    private String question;

    /** 期望命中文档标题（分号分隔） */
    private String expectedDocTitles;

    /** 实际命中文档标题（前5个，分号分隔） */
    private String hitDocTitles;

    /** 实际命中块数 */
    private Integer hitCount;

    /** 期望文档最高命中位次（1起，0=未命中） */
    private Integer hitRank;

    /** 是否命中期望文档（1-命中 0-未命中） */
    private Integer hitFlag;
}
