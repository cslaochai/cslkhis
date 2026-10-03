package com.his.emr.support;

/**
 * 病历形式质控规则清单。
 *
 * <p><b>本枚举是"规则存在性"的唯一权威</b>：{@link QcRuleEngine} 必须为每一条规则给出实现，
 * 启动时自检（{@link QcRuleEngine#selfCheck()}），少一条直接启动失败。
 * 宁可起不来，也不要"规则静默消失、报表看着是干净的"——
 * 一个不报错的空规则比没有规则更危险。
 *
 * <p><b>严重度的分配口径</b>（改动即改变甲级率，不可随意调）：
 * <ul>
 *   <li>{@link QcSeverity#FATAL}（否决项）= 病历不成立：缺主诉 / 缺现病史 / 缺诊断 /
 *       缺过敏史 / 记录类文书无正文 / 性别与诊断矛盾 / 生命体征越界 / 已归档未提交。</li>
 *   <li>{@link QcSeverity#MAJOR}（重要）= 影响诊疗安全或后续 DRG 入组：缺既往史 / 缺诊断编码 /
 *       缺诊疗计划 / 无书写医生 / 主诉无持续时间 / 记录时间晚于提交时间。</li>
 *   <li>{@link QcSeverity#MINOR}（提示）= 不规范但可接受：主诉过短 / 文书标题缺失 / 就诊信息缺失。</li>
 * </ul>
 *
 * <p><b>字段是否"没写"的判断不能用同一个口径</b>：「既往史：无」「过敏史：无」是合法记录，
 * 所以 {@link #C03}、{@link #C04} 走 {@link QcRuleEngine} 里的「史类」判定，
 * 不能直接套 {@code isPlaceholderOnly}（它会判「无」为占位）。
 * 详见 {@code ClinicalTextMatcher#isBlank} 的注释。
 */
public enum QcRule {

    // 完整性

    C01("C01", "主诉缺失或仅有占位内容", QcDimension.COMPLETENESS, QcSeverity.FATAL,
            QcScope.OUTPATIENT_AND_ENTRY, "主诉", false,
            "按「症状 + 持续时间」补写主诉，不要只写「主诉」或「无」",
            "《病历书写基本规范》第三条：主诉指促使患者就诊的主要症状（或体征）及持续时间"),

    C02("C02", "现病史缺失或仅有占位内容", QcDimension.COMPLETENESS, QcSeverity.FATAL,
            QcScope.OUTPATIENT_AND_ENTRY, "现病史", false,
            "补写发病情况、症状演变、伴随症状、诊疗经过与一般情况",
            "《病历书写基本规范》第四条：现病史是病历的核心内容，不得以「无」代替"),

    C03("C03", "既往史缺失或仅有占位内容", QcDimension.COMPLETENESS, QcSeverity.MAJOR,
            QcScope.OUTPATIENT_AND_ENTRY, "既往史", true,
            "补写既往疾病史、手术外伤史、输血史、传染病史；确无者写「无」",
            "《病历书写基本规范》第五条：既往史须逐项记录，写「无」视为已记录"),

    C04("C04", "过敏史缺失或仅有占位内容", QcDimension.COMPLETENESS, QcSeverity.FATAL,
            QcScope.OUTPATIENT_AND_ENTRY, "过敏史", true,
            "逐条写明过敏药物/食物及反应；确无者必须显式写「无过敏史」",
            "用药安全前置项：biz_inpatient_record.allergy_history 表注释明确要求不得留空代替「否认」"),

    C05("C05", "诊断缺失或仅有占位内容", QcDimension.COMPLETENESS, QcSeverity.FATAL,
            QcScope.OUTPATIENT_AND_ENTRY, "诊断", false,
            "写出本次就诊的完整诊断；不能只写「待查」而不给出方向",
            "《病历书写基本规范》第十四条"),

    C06("C06", "有诊断名称但缺 ICD 编码", QcDimension.COMPLETENESS, QcSeverity.MAJOR,
            QcScope.OUTPATIENT_AND_ENTRY, "诊断编码", false,
            "通过 ICD 编码推荐或人工补录诊断编码，否则病案首页与 DRG 无法入组",
            "医保结算清单与 DRG/DIP 分组均以编码为依据，只有名称无法入组"),

    C07("C07", "诊疗计划缺失或仅有占位内容", QcDimension.COMPLETENESS, QcSeverity.MAJOR,
            QcScope.OUTPATIENT_AND_ENTRY, "诊疗计划", false,
            "写明检查计划、治疗用药、复诊安排；「遵医嘱」不是诊疗计划",
            "《病历书写基本规范》第十四条：须有诊疗计划或处理意见"),

    C08("C08", "未记录书写医生", QcDimension.COMPLETENESS, QcSeverity.MAJOR,
            QcScope.ALL, "书写医生", false,
            "补记书写医生；无签名病历不得归档",
            "三级医院评审要求病历须有医师签名，无签名病历属缺陷病历"),

    C09("C09", "记录类文书缺正文", QcDimension.COMPLETENESS, QcSeverity.FATAL,
            QcScope.INPATIENT_NOTE, "正文", false,
            "补写该文书的正文内容；病程记录须记录病情变化与诊疗措施",
            "《病历书写基本规范》第八条及第三章：病程记录等不得为空文书"),

    // 规范性

    F01("F01", "主诉缺少症状持续时间", QcDimension.REGULARITY, QcSeverity.MAJOR,
            QcScope.OUTPATIENT_AND_ENTRY, "主诉", false,
            "主诉须写为「症状 + 持续时间」，如「咳嗽发热4天」",
            "《病历书写基本规范》第三条：主诉必须包含持续时间"),

    F02("F02", "主诉含诊断性结论用语", QcDimension.REGULARITY, QcSeverity.MAJOR,
            QcScope.OUTPATIENT_AND_ENTRY, "主诉", false,
            "主诉只写症状与体征，不写诊断结论；把「考虑肺炎」移到诊断栏",
            "主诉不得使用诊断用语，避免先入为主影响诊断思路"),

    F03("F03", "主诉有效长度不足", QcDimension.REGULARITY, QcSeverity.MINOR,
            QcScope.OUTPATIENT_AND_ENTRY, "主诉", false,
            "主诉过于笼统（如「头疼」「不适」），补充具体症状部位、性质与持续时间",
            "主诉需能支撑诊断推理，过短无法体现就诊原因"),

    F04("F04", "文书标题未填写", QcDimension.REGULARITY, QcSeverity.MINOR,
            QcScope.INPATIENT, "文书标题", false,
            "补写文书标题，如「2016-05-12 主任医师查房记录」",
            "biz_inpatient_record.record_title 表注释：默认取类型文案，可自定"),

    F05("F05", "门诊病历未记录就诊科室或就诊日期", QcDimension.REGULARITY, QcSeverity.MINOR,
            QcScope.OUTPATIENT, "就诊信息", false,
            "补记就诊科室与就诊日期，否则无法归集到科室质控统计",
            "门诊病历须载明就诊科室与就诊时间，用于科室质控与统计"),

    // 逻辑性

    L01("L01", "性别与诊断矛盾", QcDimension.LOGIC, QcSeverity.FATAL,
            QcScope.ALL, "性别", false,
            "核对患者性别与诊断；若为录入错误请更正性别或诊断",
            "诊断必须与患者性别一致，是病案质控的经典单否项"),

    L02("L02", "生命体征数值超出医学取值范围", QcDimension.LOGIC, QcSeverity.FATAL,
            QcScope.ALL, "生命体征", false,
            "核对体温/脉搏/呼吸/血压记录值，超出可存活范围必为录入错误",
            "体温 34.0~43.0℃、脉搏 20~250 次/分、呼吸 5~60 次/分、收缩压 40~300mmHg"),

    L03("L03", "血压数值矛盾", QcDimension.LOGIC, QcSeverity.FATAL,
            QcScope.ALL, "血压", false,
            "核对血压记录顺序，收缩压必须大于舒张压",
            "收缩压 ≤ 舒张压属于不可能的生理状态，必为录入错误"),

    L04("L04", "记录时间晚于提交时间", QcDimension.LOGIC, QcSeverity.MAJOR,
            QcScope.INPATIENT, "记录时间", false,
            "核对文书记录时间与提交时间，记录时间不应晚于提交时间",
            "时间序矛盾说明该文书声称记录的内容在提交时尚未发生"),

    L05("L05", "文书已归档但未记录提交时间", QcDimension.LOGIC, QcSeverity.FATAL,
            QcScope.INPATIENT, "提交时间", false,
            "补记提交时间或退回文书状态，归档必须经过提交",
            "归档是提交的后续状态，未经提交即归档说明状态机被绕过"),

    L06("L06", "归档时间早于提交时间", QcDimension.LOGIC, QcSeverity.MAJOR,
            QcScope.INPATIENT, "归档时间", false,
            "核对归档与提交时间；归档不可能发生在提交之前",
            "时间序矛盾，说明两个时间戳至少有一个不可信");

    private final String code;

    private final String name;

    private final QcDimension dimension;

    private final QcSeverity severity;

    private final QcScope scope;

    private final String fieldName;

    /**
     * 该字段是否属于「史」类 —— 允许写「无」，只禁止留空或留模板残渣。
     * 这是引擎选择占位判定的开关，不是展示字段。
     */
    private final boolean historyField;

    private final String suggestion;

    private final String basis;

    QcRule(String code, String name, QcDimension dimension, QcSeverity severity, QcScope scope,
           String fieldName, boolean historyField, String suggestion, String basis) {
        this.code = code;
        this.name = name;
        this.dimension = dimension;
        this.severity = severity;
        this.scope = scope;
        this.fieldName = fieldName;
        this.historyField = historyField;
        this.suggestion = suggestion;
        this.basis = basis;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public QcDimension getDimension() {
        return dimension;
    }

    public QcSeverity getSeverity() {
        return severity;
    }

    public QcScope getScope() {
        return scope;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isHistoryField() {
        return historyField;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public String getBasis() {
        return basis;
    }

    /**
     * 按编码取规则，取不到即返 null（调用方负责报错，不做静默兜底）
     */
    public static QcRule ofCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (QcRule rule : values()) {
            if (rule.code.equalsIgnoreCase(code.trim())) {
                return rule;
            }
        }
        return null;
    }
}
