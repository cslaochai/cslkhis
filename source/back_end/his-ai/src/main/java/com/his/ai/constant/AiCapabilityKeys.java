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
     * 患者端报告解读（大白话版）
     * <p>
     * 与 {@link #LAB_INTERPRET} 的区别：<b>读者不同，纪律不同</b>。
     * lab_interpret 的读者是检验技师/医生，输出结论草稿；本能力的读者是患者本人，
     * 只允许输出「这项查什么 + 你的值 + 参考范围 + 高/低通常意味着什么」，
     * <b>禁止给诊断、禁止给用药建议、禁止给分级处置</b>。
     * 事实层（哪些项异常、是否危急值）由代码算，白话层由 {@code sys_lab_plain_item} 词典给，
     * 模型只负责把这两者串成一段通顺的话 —— 所以模型不可用时本能力照样可用。
     */
    public static final String PATIENT_REPORT_EXPLAIN = "patient_report_explain";

    /**
     * 患者端费用解释
     * <p>
     * 回答「为什么我要自付这么多」。答案完全来自账单明细的医保目录类别拆分
     * （甲类全额纳入、乙类先自付一部分、丙类/自费全额自付），是确定性计算，
     * 模型只负责把数字串成一句话。模型不可用时照常返回拆分表。
     */
    public static final String PATIENT_FEE_EXPLAIN = "patient_fee_explain";

    /**
     * 患者端导诊口语归一（P2-患者端）
     * <p>
     * 这是患者端<b>唯一允许模型参与的导诊环节</b>，位置由 {@code MiniappTriageServiceImpl}
     * 的注释点死：模型只做两件事 —— 把口语主诉归一成症状词、生成补充追问，
     * <b>不得决定推荐哪个科室</b>。科室推荐始终由 {@code biz_triage_rule} 关键词规则给出。
     * <p>
     * 模型不可用时本能力返回原始主诉（{@code source=rule}），患者侧无感：
     * 关键词命中本来就是按原始文本走的，归一只是提高命中率的增益项。
     */
    public static final String PATIENT_TRIAGE_NORMALIZE = "patient_triage_normalize";

    /**
     * 连通性自检
     */
    public static final String HEALTH_CHECK = "health_check";

    private AiCapabilityKeys() {
    }
}
