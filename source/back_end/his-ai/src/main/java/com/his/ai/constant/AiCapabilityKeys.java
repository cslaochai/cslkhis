package com.his.ai.constant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * AI 能力标识。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
     * 事实层（哪些项异常、是否危急值）由代码算，白话层由检验项目白话词典给，
     * 模型只负责把这两者串成一段通顺的话 —— 所以模型不可用时本能力照样可用。
     */
    public static final String PATIENT_REPORT_EXPLAIN = "patient_report_explain";

    /**
     * 患者端影像报告解读（G-17，P6 起）。
     * <p>
     * 检查报告（CT/B超/放射/心电）的大白话解读，与 {@link #PATIENT_REPORT_EXPLAIN}
     * 分开建能力：读者同为患者，但数据形态完全不同（逐项数值 vs 叙事文本），
     * 提示词与输出契约不共用（纪律 8）。
     * <p>
     * <b>NMPA 三类证红线：只解读、不做诊断结论</b>——阴阳性/危急值是代码事实，
     * 检查介绍来自影像检查白话词典（可穷举禁走模型），
     * 模型只把描述/结论原文串成白话，逐段过 {@code PatientTextGuard}；
     * 模型不可用时白话段落缺位、词典与事实照常返回，绝不编白话。
     */
    public static final String PATIENT_IMAGING_EXPLAIN = "patient_imaging_explain";

    /**
     * 患者端导诊口语归一（P2-患者端）
     * <p>
     * 这是患者端<b>唯一允许模型参与的导诊环节</b>，位置由 {@code MiniappTriageServiceImpl}
     * 的注释点死：模型只做两件事 —— 把口语主诉归一成症状词、生成补充追问，
     * <b>不得决定推荐哪个科室</b>。科室推荐始终由分诊规则里的关键词规则给出。
     * <p>
     * 模型不可用时本能力返回原始主诉（{@code source=rule}），患者侧无感：
     * 关键词命中本来就是按原始文本走的，归一只是提高命中率的增益项。
     */
    public static final String PATIENT_TRIAGE_NORMALIZE = "patient_triage_normalize";

    /**
     * AI 运营问数（NL2SQL）。
     * <p>
     * 模型只做一件事：把管理者的自然语言问题翻译成一条受控 SELECT。
     * 生成结果必须过 {@code OperationSqlGuard} 白名单闸门才能执行，
     * 执行、截断、呈现全部由代码完成。这是「事实层代码算」纪律的受控例外，
     * 相关安全设计与验收见 docs/AI能力施工手册.md §6。
     */
    public static final String OPERATION_QA = "operation_qa";

    /**
     * 知识库问答（RAG）。
     * <p>纯检索增强生成：从院内知识库（就诊须知/科室介绍/检查注意事项/药品说明书等）切块召回 top-k，
     * 拼进提示词让模型基于真实文档作答。<b>只做解释与科普，不做任何医疗判定</b>——
     * 判定类（危急值/用药禁忌/分诊级别）走硬规则，不在本能力范围。
     * 模型不可用时降级为「直接返回检索到的原文片段」。
     */
    public static final String KNOWLEDGE_QA = "knowledge_qa";

    /**
     * 预问诊病史摘要（G-05）。
     * <p>
     * 患者提交的问卷是结构化量表（题目结构由 PrevisitQuestionnaireSupport 版本化），
     * 模型只做一件事：把量表答案凝成一段 ≤200 字的「就诊用病史摘要」给医生看，
     * <b>不下诊断、不给用药建议</b>。模型不可用时规则模板拼接答案，功能不缺位。
     * 摘要写回预问诊记录（summary_source 标 1-模型 2-规则），医生站只读参考。
     */
    public static final String PREVISIT_SUMMARY = "previsit_summary";

    /**
     * 随访话术草拟（G-06）。
     * <p>
     * 医护在随访任务上点「AI 拟话术」，模型按患者慢病档案/诊断/随访类型生成一段
     * 个性化随访话术草稿，<b>医生可改再保存</b>（与 emr_draft 同纪律：AI 草稿、医生终审）。
     * 话术会经站内信发到患者端，因此输出必须过 {@code PatientTextGuard}，
     * 越界即回落类型模板（source=rule）。
     */
    public static final String FOLLOWUP_COMPOSE = "followup_compose";

    /**
     * 医保审核证据判定（G-07，P3）。
     * <p>
     * 规则引擎对结算清单跑完合规审核并给出「命中」后，模型逐条阅读病历/检验/费用证据文本，
     * 判定证据与规则怀疑的关系：<b>supported（支持）/ refuted（反驳）/ insufficient（证据不足）</b>。
     * 纪律口径：规则结论（事实层）不被改写、不落库、不下「违规成立/处罚/扣款」结论，
     * 产物只用于提示人工复核；模型编造的规则码与超出枚举的 verdict 一律丢弃。
     * 模型不可用时返回 degraded=true，规则自身的 evidence/整改建议照常可见，判定不缺位（缺的只是增益）。
     */
    public static final String INSURANCE_EVIDENCE = "insurance_evidence";

    /**
     * 危重预警·病情恶化评分（G-12，P4）。
     * <p>
     * MEWS+SpO2 评分<b>全部由代码算</b>（{@code DeteriorationScoreRules}），评分是事实层；
     * 只有评分达到关注/高危阈值时才调模型，产出一段「观察与上报建议」草稿。
     * 纪律口径：<b>只提示不写库、不下诊断、不给处置医嘱</b>，预警级别由评分定，不由模型定；
     * 模型不可用时评分与预警级照常可见（缺的只是文字建议）。触达端是护士站/床位中心的床位图。
     */
    public static final String DETERIORATION_ALERT = "deterioration_alert";

    /**
     * AI 护理交接班摘要（G-13，P4）。
     * <p>
     * 病区×班次窗的护理事实（在院/新入/出院、体征越阈事件、高风险评估）由代码聚合（纪律 2），
     * 模型只把事实凝成一段 SBAR 式交班摘要草稿，<b>护士编辑终审后自行使用，不写库</b>。
     * 模型不可用时按事实列表拼规则模板（source=rule），摘要不缺位。
     */
    public static final String NURSING_HANDOVER = "nursing_handover";

    /**
     * 语音口述转写（G-14，P5）。
     * <p>
     * 医生工作站录音 → ASR（DashScope qwen3-asr-flash，同步调用）→ 转写文本。
     * 这是<b>非 LLM 能力</b>：不产出建议、不做结构化，只做音频→文本的确定性转换，
     * 因此不走 {@code AiExecutionService}（那是 LLM 结构化输出的通道），审计独立落行
     * （capability_key=voice_transcribe，input_digest 只落音频元数据，转写原文不落库）。
     * 产物的唯一去向是「医生编辑后的输入框」——医生改完再喂 {@link #EMR_DRAFT} 拟现病史。
     * <b>降级语义：转写失败如实报错，禁止造文本。</b>
     */
    public static final String VOICE_TRANSCRIBE = "voice_transcribe";

    /**
     * 连通性自检
     */
    public static final String HEALTH_CHECK = "health_check";

}
