package com.his.medicaltech.enums;

import com.his.common.util.TextUtil;

/**
 * 数据质量规则集（P5.3）。
 *
 * <p><b>设计铁律（血泪换来的）</b>：
 * <ol>
 *   <li><b>每条规则都要有分母</b>。只报"命中 3 条"是没用的——必须先说清
 *       "在多少条里命中了 3 条"。分母为 0 的规则等于规则没生效，比没有规则更危险：
 *       它会让人以为"这类问题我们查过了，很干净"。</li>
 *   <li><b>每条规则都要能定位到具体记录</b>。报"完整性得分 92 分"没人能整改；
 *       报"患者ID=2100... 缺身份证号"才能派人去补。</li>
 *   <li><b>规则枚举与 SQL 实现必须一一对应</b>，数量对不上要在启动时炸掉
 *       （见 {@code QualityServiceImpl} 的注册表自检），不能静默少跑几条。</li>
 * </ol>
 *
 * <p>码值口径以<b>后端枚举</b>为准，不以表注释为准：性别统一按
 * 1-男 2-女（{@code com.his.common.enums.SysGenderEnum}），
 * 表注释里写的"0-女"与本库数据事实相反。
 */
public enum QualityRule {

    // 完整性
    PT_IDENTITY_MISS("PT-IDENTITY-MISS", "患者身份信息不完整", QualityDimension.COMPLETENESS,
            QualitySeverity.HIGH, "biz_patient", "在册患者",
            "补齐身份证号 / 家庭住址 / 出生日期；患者主索引靠身份证做唯一归并，缺了就无法自动识别同一人",
            "《医疗机构病历管理规定》患者身份识别要求"),

    IPR_ADMIT_DOC_MISS("IPR-ADMIT-DOC-MISS", "住院记录缺少入院记录 / 首次病程", QualityDimension.COMPLETENESS,
            QualitySeverity.HIGH, "biz_admission", "住院记录",
            "补写入院记录与首次病程记录（入院记录应在入院 24 小时内完成）",
            "《病历书写基本规范》入院记录 24 小时时限"),

    ADM_DISCHARGE_DOC_MISS("ADM-DISCHARGE-DOC-MISS", "已出院但无出院记录", QualityDimension.COMPLETENESS,
            QualitySeverity.HIGH, "biz_admission", "已出院住院记录",
            "补写出院记录并完成归档；出院无记录会导致病案首页与结算清单没有依据",
            "《病历书写基本规范》出院记录要求"),

    RX_DETAIL_MISS("RX-DETAIL-MISS", "处方没有明细行", QualityDimension.COMPLETENESS,
            QualitySeverity.HIGH, "biz_prescription", "处方",
            "补处方明细或作废该处方；只有处方头没有明细，收费与发药都无从追溯",
            "《处方管理办法》处方内容完整性要求"),

    LAB_APPLY_REC_MISS("LAB-APPLY-REC-MISS", "检验申请没有对应检验记录", QualityDimension.COMPLETENESS,
            QualitySeverity.MEDIUM, "biz_laboratory_apply", "检验申请",
            "确认是未执行还是漏建记录；未执行的申请应取消并说明原因",
            "检验全流程闭环要求"),

    MR_DIAG_CODE_MISS("MR-DIAG-CODE-MISS", "病历有诊断名称但无诊断编码", QualityDimension.COMPLETENESS,
            QualitySeverity.MEDIUM, "biz_medical_record", "门诊病历",
            "补 ICD-10 诊断编码；无编码无法参与 DRG/DIP 与疾病统计",
            "《电子病历系统应用水平分级评价》诊断编码要求"),

    // 一致性
    CHARGE_DISCOUNT_ALLOC_MISS("CHARGE-DISCOUNT-ALLOC-MISS", "收费单优惠未分摊到明细", QualityDimension.CONSISTENCY,
            QualitySeverity.MEDIUM, "biz_settlement_bill", "含优惠的账单",
            "把主表优惠金额按项目分摊写回收费明细；否则退费与对账时主表明细对不上",
            "收费对账一致性要求"),

    ALLERGY_DUAL_MISS("ALLERGY-DUAL-MISS", "过敏史只存在于自由文本，未落结构化表", QualityDimension.CONSISTENCY,
            QualitySeverity.HIGH, "biz_patient", "有过敏史文本的患者",
            "把患者主档的过敏史文本同步到结构化过敏表（biz_patient_allergy）；用药审核只读结构化表，不同步会导致过敏拦截静默失效",
            "《医疗质量安全核心制度要点》查对制度 + 用药安全"),

    CHARGE_SUM_MISMATCH("CHARGE-SUM-MISMATCH", "收费单总金额与明细合计不符", QualityDimension.CONSISTENCY,
            QualitySeverity.HIGH, "biz_settlement_bill", "有明细的账单",
            "核对收费明细并修正金额；主表明细不平会直接导致财务对账差异",
            "财务收费一致性要求"),

    // 及时性
    CRITICAL_OVERDUE_HANDLE("CRITICAL-OVERDUE-HANDLE", "危急值超过处置时限仍未处置", QualityDimension.TIMELINESS,
            QualitySeverity.HIGH, "biz_critical_value", "危急值记录",
            "立即联系接收医师完成处置并记录处置措施；危急值闭环不得停留在「已接收」",
            "《医疗质量安全核心制度要点》危急值报告制度"),

    CRITICAL_NOTIFY_MISS("CRITICAL-NOTIFY-MISS", "危急值尚未完成通知", QualityDimension.TIMELINESS,
            QualitySeverity.HIGH, "biz_critical_value", "未作废的危急值",
            "确认通知是否真的发出（无开单医生时应发兜底接收人），并把通知时间落库",
            "《医疗质量安全核心制度要点》危急值报告制度"),

    ADM_DOC_LATE("ADM-DOC-LATE", "入院超过 24 小时仍未完成入院记录 / 首次病程", QualityDimension.TIMELINESS,
            QualitySeverity.HIGH, "biz_admission", "已入院超 24 小时的住院记录",
            "补写入院记录，完成时间以首次创建时间为准；超时同时会影响病历甲级评分",
            "《病历书写基本规范》入院记录 24 小时时限"),

    DISCHARGE_ARCHIVE_LATE("DISCHARGE-ARCHIVE-LATE", "出院超过 7 天仍未归档出院记录", QualityDimension.TIMELINESS,
            QualitySeverity.MEDIUM, "biz_admission", "已出院超 7 天的住院记录",
            "完成出院记录并提交归档（状态需为已归档）；超期未归档属于病历管理缺陷",
            "《医疗机构病历管理规定》归档时限"),

    LAB_AUDIT_LATE("LAB-AUDIT-LATE", "检验出结果超过 24 小时仍未审核", QualityDimension.TIMELINESS,
            QualitySeverity.MEDIUM, "biz_laboratory_record", "已出结果的检验记录",
            "完成审核并发布报告；未审核结果不能作为诊疗依据",
            "检验报告审核制度"),

    // 唯一性
    IPR_KEY_DOC_DUP("IPR-KEY-DOC-DUP", "同一次住院存在多份关键文书", QualityDimension.UNIQUENESS,
            QualitySeverity.MEDIUM, "biz_inpatient_record", "住院记录数",
            "入院记录 / 首次病程 / 出院记录在一次住院内应各只有一份；查明重复原因后作废多余文书并留痕，不要物理删除",
            "《病历书写基本规范》文书唯一性"),

    LAB_RESULT_DUP("LAB-RESULT-DUP", "同一检验记录同一项目存在重复结果行", QualityDimension.UNIQUENESS,
            QualitySeverity.HIGH, "biz_lab_result", "检验结果行",
            "清理重复结果行（保留一条并说明依据）；重复结果会让医生误读为「做过两次」",
            "检验结果唯一性要求"),

    PT_IDCARD_DUP("PT-IDCARD-DUP", "同一身份证号对应多条患者档案", QualityDimension.UNIQUENESS,
            QualitySeverity.MEDIUM, "biz_patient", "有身份证的患者",
            "到患者主索引（EMPI）做疑似重复判定并按规范合并；合并前必须核对就诊记录归属",
            "患者主索引唯一性要求"),

    REGIST_DUP_SAME_DAY("REGIST-DUP-SAME-DAY", "同一患者同科室同日重复挂号", QualityDimension.UNIQUENESS,
            QualitySeverity.LOW, "biz_appoint_info", "有效挂号记录",
            "确认是否重复挂号或退号未生效；重复挂号会造成重复收费与重复排队",
            "门诊挂号唯一性要求"),

    // 有效性
    GENDER_IDCARD_CONFLICT("GENDER-IDCARD-CONFLICT", "患者性别与身份证不符", QualityDimension.VALIDITY,
            QualitySeverity.HIGH, "biz_patient", "身份证合法的患者",
            "以身份证第 17 位奇偶（奇男偶女）为准核对并修正性别；性别错误会直接影响性别专属诊断与用药判断",
            "《电子病历系统应用水平分级评价》数据有效性"),

    IDCARD_FORMAT_INVALID("IDCARD-FORMAT-INVALID", "身份证号格式非法", QualityDimension.VALIDITY,
            QualitySeverity.MEDIUM, "biz_patient", "填了身份证的患者",
            "修正为 18 位合法身份证号；非法身份证号无法参与实名核验与医保结算",
            "实名制就诊要求"),

    CODE_VALUE_INVALID("CODE-VALUE-INVALID", "关键码值超出字典范围", QualityDimension.VALIDITY,
            QualitySeverity.MEDIUM, "多表", "可校验码值的关键表行数",
            "按后端枚举修正码值；未知码值在前端会渲染成「未知(n)」，不会回落成合法值，所以必须源头修",
            "《电子病历系统应用水平分级评价》数据字典一致性"),

    DATE_REVERSE("DATE-REVERSE", "日期倒挂（出院时间早于入院时间）", QualityDimension.VALIDITY,
            QualitySeverity.HIGH, "biz_admission", "填了出院时间的住院记录",
            "核对并修正入院 / 出院时间；时间倒挂会让住院天数、床位周转、DRG 分组全部失真",
            "病案首页数据准确性要求"),

    AMOUNT_INVALID("AMOUNT-INVALID", "金额为负或数量非法", QualityDimension.VALIDITY,
            QualitySeverity.HIGH, "biz_fee_record", "记账明细行",
            "修正金额与数量；负金额应通过退费流程表达，不能直接写负数",
            "财务数据有效性要求");

    private final String code;
    private final String name;
    private final QualityDimension dimension;
    private final QualitySeverity severity;
    private final String tableName;
    private final String checkedDesc;
    private final String suggestion;
    private final String basis;

    QualityRule(String code, String name, QualityDimension dimension, QualitySeverity severity,
                String tableName, String checkedDesc, String suggestion, String basis) {
        this.code = code;
        this.name = name;
        this.dimension = dimension;
        this.severity = severity;
        this.tableName = tableName;
        this.checkedDesc = checkedDesc;
        this.suggestion = suggestion;
        this.basis = basis;
    }

    /**
     * 未知规则码返回 null（调用方需显式报"未知规则(xxx)"，不做静默回落）
     */
    public static QualityRule parse(String code) {
        if (!TextUtil.hasText(code)) {
            return null;
        }
        for (QualityRule r : values()) {
            if (r.code.equalsIgnoreCase(code.trim())) {
                return r;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public QualityDimension getDimension() {
        return dimension;
    }

    public QualitySeverity getSeverity() {
        return severity;
    }

    public String getTableName() {
        return tableName;
    }

    public String getCheckedDesc() {
        return checkedDesc;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public String getBasis() {
        return basis;
    }
}
