package com.his.ai.constant;

/**
 * AI 能力标识。
 * <p>
 * 这些常量由业务代码显式传给执行器，<b>绝不允许由模型输出来决定调用哪个能力</b>。
 * 一旦改成模型自主选择，系统就从「工作流」退化成了「Agent」，
 * 可审计性与延迟预算同时失效（详见 docs/AI能力接入方案.md §3.0.4）。
 * <p>
 * 命名需与 application.yml 的 {@code ai.features.<key>}、{@code ai.timeouts.<key>} 保持一致。
 */
public final class AiCapabilityKeys {

    /**
     * ICD-10 智能编码
     */
    public static final String ICD10 = "icd10";

    /**
     * 处方合理性审核
     */
    public static final String DRUG_AUDIT = "drug_audit";

    /**
     * 病历内涵质控
     */
    public static final String EMR_QC = "emr_qc";

    /**
     * 检验结果解读（P1-1）
     */
    public static final String LAB_INTERPRET = "lab_interpret";

    /**
     * 急诊分诊建议（P1-2）
     */
    public static final String EMERGENCY_TRIAGE = "emergency_triage";

    /**
     * 病历文本结构化抽取（P1-3）
     * <p>
     * 把一段自由文本（医生手打、从外院系统粘贴、后续的语音转写结果）拆进病历的各个字段。
     */
    public static final String EMR_EXTRACT = "emr_extract";

    /**
     * 病历草拟（P1-3）
     * <p>
     * 由主诉 + 查体 + 体征组织出「现病史」草稿。
     * <b>只草拟现病史</b> —— 不生成诊断（诊断权在医生）、不生成处理意见/医嘱
     * （医嘱有法律效力，必须由医生开立）。
     */
    public static final String EMR_DRAFT = "emr_draft";

    /**
     * 连通性自检
     */
    public static final String HEALTH_CHECK = "health_check";

    private AiCapabilityKeys() {
    }
}
