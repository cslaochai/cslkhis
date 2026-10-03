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

    @Schema(description = "是否通过：存在 errorLevel=3 的问题即不通过")
    private boolean pass;

    @Schema(description = "问题总数")
    private int findingCount;

    @Schema(description = "最高严重程度，无问题时为 0")
    private int maxErrorLevel;

    @Schema(description = "硬规则命中的问题数")
    private int hardRuleCount;

    @Schema(description = "模型给出的问题数")
    private int llmCount;

    @Schema(description = "问题列表，按严重程度降序")
    private List<DrugAuditFindingVO> findings = new ArrayList<>();

    @Schema(description = "写入 biz_clinical_rule_check 的记录ID，未落库时为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long checkId;

    @Schema(description = "校验单号，未落库时为空")
    private String checkNo;

    /**
     * 耗时（毫秒）
     */
    @Schema(description = "耗时（毫秒）")
    private long latencyMs;
}
