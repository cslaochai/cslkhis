package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 处方审核结果。
 */
@Data
@Schema(description = "处方审核结果")
public class DrugAuditResultVO {

    /**
     * 是否降级
     */
    @Schema(description = "是否已降级：true 表示模型未参与，仅硬规则结论")
    private boolean degraded;

    /**
     * 降级原因
     */
    @Schema(description = "降级原因，未降级时为空")
    private String degradeReason;

    /** 是否审核通过（存在严重级问题即不通过） */
    @Schema(description = "是否通过：存在 errorLevel=3 的问题即不通过")
    private boolean pass;

    /** 问题总数 */
    @Schema(description = "问题总数")
    private int findingCount;

    /** 最高错误级别（1-提示 2-警告 3-严重），无问题时为 0 */
    @Schema(description = "最高严重程度，无问题时为 0")
    private int maxErrorLevel;

    /** 硬规则命中的问题数 */
    @Schema(description = "硬规则命中的问题数")
    private int hardRuleCount;

    /** 模型给出的问题数 */
    @Schema(description = "模型给出的问题数")
    private int llmCount;

    /** 问题列表，按错误级别降序 */
    @Schema(description = "问题列表，按严重程度降序")
    private List<DrugAuditFindingVO> findings = new ArrayList<>();

    /** 临床校验记录ID，未落库时为空 */
    @Schema(description = "写入 biz_clinical_rule_check 的记录ID，未落库时为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long checkId;

    /** 校验单号，未落库时为空 */
    @Schema(description = "校验单号，未落库时为空")
    private String checkNo;

    /**
     * 耗时（毫秒）
     */
    @Schema(description = "耗时（毫秒）")
    private long latencyMs;
}
