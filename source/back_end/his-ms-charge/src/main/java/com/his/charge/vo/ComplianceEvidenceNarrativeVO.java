package com.his.charge.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 一次合规审核的「证据叙事包」：把依据包压平成模型可读的事实文本块。
 */
@Data
public class ComplianceEvidenceNarrativeVO {

    /**
     * 结算清单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long settlementId;

    /**
     * 结算清单号
     */
    private String settlementNo;

    /**
     * DRG 分组码（未分组为空）
     */
    private String drgCode;

    /**
     * 患者标识（性别 + 年龄口径，如「女，45岁」）
     */
    private String patientTag;

    /**
     * 清单诊断（编码 + 名称）
     */
    private String diagnosisText;

    /**
     * 病历叙述（主诉/现病史/既往史/查体/辅检/诊疗计划/诊断）
     */
    private String recordNarrative;

    /**
     * 本次诊疗项目名清单（账单行 + 处方药 + 检验 + 检查）
     */
    private String orderNames;

    /**
     * 检验结果摘要（项名：结果值单位（参考范围））
     */
    private String labSummary;

    /**
     * 依据缺失说明（「不适用」规则的归因口径）
     */
    private List<String> missingList = new ArrayList<>();
}
