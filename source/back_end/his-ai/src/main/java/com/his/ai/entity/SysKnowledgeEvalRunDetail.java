package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 知识库评测运行明细（每用例一条，只追加不改）。
 */
@Data
@TableName("sys_knowledge_eval_run_detail")
public class SysKnowledgeEvalRunDetail {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 运行ID */
    private Long runId;

    /** 用例ID */
    private Long caseId;

    private String question;

    /** 期望命中文档ID（分号分隔） */
    private String expectedDocIds;

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
