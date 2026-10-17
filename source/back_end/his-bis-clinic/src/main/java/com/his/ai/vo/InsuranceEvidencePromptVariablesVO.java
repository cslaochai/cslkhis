package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 医保合规证据判定提示词变量。
 */
@Data
public class InsuranceEvidencePromptVariablesVO implements PromptVariables {

    /**
     * 结算单号，未知时为「未知」
     */
    private String settlementNo;

    /**
     * DRG 分组码，未分组时为「未分组」
     */
    private String drgCode;

    /**
     * 患者标签（脱敏），未知时为「未知」
     */
    private String patientTag;

    /**
     * 诊断文本，未填写时为「未填写」
     */
    private String diagnosisText;

    /**
     * 命中的合规审核项明细（规则码｜规则名｜判定依据）
     */
    private String hitItemsText;

    /**
     * 病历叙述，无病历文本时为「无病历文本」
     */
    private String recordNarrative;

    /**
     * 本次结算涉及的项目名称，无则「无」
     */
    private String orderNames;

    /**
     * 检验摘要，无则「无」
     */
    private String labSummary;

    /**
     * 缺失项清单，无则「无」
     */
    private String missingText;
}