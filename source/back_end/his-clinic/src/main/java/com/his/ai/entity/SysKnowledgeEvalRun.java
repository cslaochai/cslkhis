package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 知识库评测运行汇总（只追加不改）。
 */
@Data
@TableName("sys_knowledge_eval_run")
public class SysKnowledgeEvalRun {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 评测用例数 */
    private Integer caseCount;

    /** 命中期望文档的用例数 */
    private Integer hitCaseCount;

    /** 召回率（%，0-100） */
    private BigDecimal recallAtK;

    /** 平均倒数排名（MRR，0-1） */
    private BigDecimal mrr;

    /** 平均检索耗时（毫秒） */
    private Integer avgLatencyMs;

    /** 评测时配置快照（切块参数/embedding） */
    private String configSnapshot;

    /** 状态（2-完成 3-失败） */
    private Integer status;

    /** 失败原因 */
    private String errorMsg;

    private Long operatorId;

    private String operatorName;

    /** 创建时间 */
    private LocalDateTime createTime;
}
