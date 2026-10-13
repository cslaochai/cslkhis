package com.his.ai.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评测运行汇总出参。
 */
@Data
public class KnowledgeEvalRunVO {

    @JsonSerialize(using = ToStringSerializer.class)
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

    private String errorMsg;

    private String operatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
