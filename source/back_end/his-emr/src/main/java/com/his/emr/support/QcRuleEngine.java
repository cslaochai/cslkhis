package com.his.emr.support;

import com.his.common.enums.SysGenderEnum;
import com.his.common.support.ClinicalTextMatcher;
import com.his.common.util.TextUtil;
import com.his.emr.enums.*;
import com.his.emr.vo.QcIssueVO;
import com.his.emr.vo.QcResultVO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 病历形式质控规则引擎。
 */
@Slf4j
@Component
public class QcRuleEngine {

    /**
     * 满分
     */
    private static final int FULL_SCORE = 100;

    /**
     * 甲级病历分数线（三级医院评审通用口径）
     */
    private static final int GRADE_A_SCORE = 90;

    /**
     * 乙级病历分数线
     */
    private static final int GRADE_B_SCORE = 75;

    /**
     * 主诉有效长度下限（去空白与占位符号后）
     */
    private static final int MIN_CHIEF_COMPLAINT_LENGTH = 4;

    /**
     * 证据原文截断长度
     */
    private static final int EVIDENCE_MAX_LENGTH = 60;

    private static final BigDecimal TEMPERATURE_MIN = new BigDecimal("34.0");

    private static final BigDecimal TEMPERATURE_MAX = new BigDecimal("43.0");

    private static final int PULSE_MIN = 20;

    private static final int PULSE_MAX = 250;

    private static final int RESPIRATION_MIN = 5;

    private static final int RESPIRATION_MAX = 60;

    private static final int SYSTOLIC_MIN = 40;

    private static final int SYSTOLIC_MAX = 300;

    private static final int DIASTOLIC_MIN = 20;

    private static final int DIASTOLIC_MAX = 200;

    /**
     * 主诉里的持续时间要素
     */
    private static final Pattern DURATION_PATTERN =
            Pattern.compile("[0-9０-９一二三四五六七八九十半两]+\\s*(天|日|周|月|年|小时|分钟|余)(?![末前])");

    /**
     * 主诉里不该出现的诊断性结论用语。
     * 字面匹配一律走 {@link ClinicalTextMatcher#containsAffirmed}，
     * 避免把"否认…诊断"读成肯定表述。
     */
    private static final List<String> DIAGNOSIS_HINTS =
            List.of("诊断", "确诊", "考虑", "拟诊", "疑似", "可能性大");

    /**
     * 女性专属诊断词
     */
    private static final List<String> FEMALE_ONLY_WORDS =
            List.of("妊娠", "分娩", "子宫", "卵巢", "月经", "输卵管", "阴道", "宫颈", "胎盘", "产褥", "剖宫产");

    /**
     * 男性专属诊断词
     */
    private static final List<String> MALE_ONLY_WORDS =
            List.of("前列腺", "睾丸", "精囊", "阴茎", "附睾");

    /**
     * 诊疗计划字段可能被套用的模板标签（门诊叫「处理意见」，住院叫「诊疗计划」）
     */
    private static final List<String> PLAN_LABELS = List.of("诊疗计划", "处理意见", "治疗计划");

    /**
     * 等级：有否决项必为丙级（单否项压过分数），否则按分数线
     */
    private static String gradeOf(int vetoCount, int score) {
        if (vetoCount > 0) {
            return "丙";
        }
        if (score >= GRADE_A_SCORE) {
            return "甲";
        }
        return score >= GRADE_B_SCORE ? "乙" : "丙";
    }

    private static String summarize(QcResultVO result) {
        if (result.getIssues().isEmpty()) {
            return String.format("未发现问题，得分 %d 分，质量等级甲级", result.getScore());
        }
        String fields = result.getIssues().stream()
                .limit(3)
                .map(QcIssueVO::getFieldName)
                .distinct()
                .reduce((a, b) -> a + "、" + b)
                .orElse("");
        return String.format("命中 %d 条问题（否决项 %d 条），得分 %d 分，质量等级%s级，判定%s；优先整改：%s",
                result.getIssueCount(), result.getVetoCount(), result.getScore(), result.getGrade(),
                result.isPass() ? "通过" : "不通过", fields);
    }

    private static boolean applicable(QcRuleEnum rule, QcSnapshot snapshot) {
        return switch (rule.getScope()) {
            case OUTPATIENT -> snapshot.getSource() == QcRecordSourceEnum.OUTPATIENT;
            case OUTPATIENT_AND_ENTRY ->
                    snapshot.getSource() == QcRecordSourceEnum.OUTPATIENT || snapshot.isInpatientEntry();
            case INPATIENT -> snapshot.getSource() == QcRecordSourceEnum.INPATIENT;
            case INPATIENT_NOTE -> snapshot.isInpatientNote();
            case ALL -> true;
        };
    }

    /**
     * 主诉是否"没有实质内容"。没有实质内容时，F01/F02/F03 一律不判 ——
     * 缺主诉已经由 C01 报过一次，同一件事不该在三处重复扣分。
     */
    private static boolean isChiefComplaintBlank(QcSnapshot s) {
        return ClinicalTextMatcher.isPlaceholderOnly(s.getChiefComplaint(), "主诉");
    }

    // 适用范围

    /**
     * 统一的"字段没写"判定。返回空表示不构成问题。
     * 「未填写」与「只有占位内容」要分开说 —— 前者是漏填，后者是套了模板没替换，
     * 整改动作不一样（一个要补，一个要改内容）。
     */
    private static Optional<String> missing(String value, boolean absent, String label) {
        if (!absent) {
            return Optional.empty();
        }
        return Optional.of(ClinicalTextMatcher.isBlank(value)
                ? String.format("「%s」未填写", label)
                : String.format("「%s」只有占位/模板内容，未记录实质信息", label));
    }

    // 规则实现：穷尽 switch，少一条编译不过

    /**
     * 证据原文：压平空白、截断。空值给「（空）」而不是空串 ——
     * 界面上"没有证据"和"证据是空字符串"必须能区分开。
     */
    private static String evidenceOf(String text) {
        if (!TextUtil.hasText(text)) {
            return "（空）";
        }
        return TextUtil.ellipsis(text.replaceAll("\\s+", " ").trim(), EVIDENCE_MAX_LENGTH);
    }

    // 辅助

    /**
     * 启动自检：规则的"存在性"必须能被观测到。
     * 三个维度各自至少要有一条规则 —— 否则前端的维度筛选会点出一个永远空的列表，
     * 而"空列表"和"这个维度没问题"在界面上长得一模一样。
     */
    @PostConstruct
    public void selfCheck() {
        Map<QcDimensionEnum, Integer> counts = new EnumMap<>(QcDimensionEnum.class);
        Set<String> codes = new LinkedHashSet<>();
        for (QcRuleEnum rule : QcRuleEnum.values()) {
            counts.merge(rule.getDimension(), 1, Integer::sum);
            if (!codes.add(rule.getCode())) {
                throw new IllegalStateException("[病案质控] 规则编码重复：" + rule.getCode());
            }
            if (QcRuleEnum.ofCode(rule.getCode()) != rule) {
                throw new IllegalStateException("[病案质控] 规则编码无法反查：" + rule.getCode());
            }
        }
        for (QcDimensionEnum dimension : QcDimensionEnum.values()) {
            if (counts.getOrDefault(dimension, 0) == 0) {
                throw new IllegalStateException("[病案质控] 维度 " + dimension.getText() + " 没有任何规则");
            }
        }
        log.info("[病案质控] 规则引擎就绪：{} 条规则，维度分布 {}",
                QcRuleEnum.values().length, counts);
    }

    /**
     * 执行质控。
     *
     * @param snapshot 病历快照
     * @param qcType   质控类型：null 或 0 表示三个维度全跑；1/2/3 表示只跑对应维度
     * @return 质控结论（含问题明细）
     */
    public QcResultVO inspect(QcSnapshot snapshot, Integer qcType) {
        QcDimensionEnum only = QcDimensionEnum.ofCode(qcType);
        List<QcIssueVO> issues = new ArrayList<>();
        Set<Integer> dimensions = new LinkedHashSet<>();

        for (QcRuleEnum rule : QcRuleEnum.values()) {
            if (only != null && rule.getDimension() != only) {
                continue;
            }
            if (!applicable(rule, snapshot)) {
                continue;
            }
            dimensions.add(rule.getDimension().getCode());
            check(rule, snapshot).ifPresent(issues::add);
        }

        QcResultVO result = new QcResultVO();
        result.setRecordSource(snapshot.getSource().getCode());
        result.setRecordId(snapshot.getRecordId());
        result.setRecordNo(snapshot.getRecordNo());
        result.setPatientId(snapshot.getPatientId());
        result.setPatientName(snapshot.getPatientName());
        result.setDeptName(snapshot.getDeptName());
        result.setRecordType(snapshot.getRecordType());
        result.setRecordTypeText(snapshot.recordTypeText());
        result.setDimensions(new ArrayList<>(dimensions));
        result.setIssues(issues);
        result.setIssueCount(issues.size());

        int deduct = issues.stream().mapToInt(issue -> issue.getDeduct() == null ? 0 : issue.getDeduct()).sum();
        int score = Math.max(0, FULL_SCORE - deduct);
        int severityMax = issues.stream()
                .mapToInt(issue -> issue.getSeverity() == null ? 0 : issue.getSeverity())
                .max().orElse(0);
        int vetoCount = (int) issues.stream().filter(issue -> issue.getSeverity() != null
                && issue.getSeverity() == QcSeverityEnum.FATAL.getCode()).count();
        // 通过 = 没有严重度 ≥2 的问题。提示项（严重度 1）只扣分，不判不通过
        boolean pass = issues.stream()
                .allMatch(issue -> issue.getSeverity() == null || issue.getSeverity() < QcSeverityEnum.MAJOR.getCode());

        result.setScore(score);
        result.setSeverityMax(severityMax);
        result.setVetoCount(vetoCount);
        result.setPass(pass);
        result.setGrade(gradeOf(vetoCount, score));
        result.setSummary(summarize(result));
        return result;
    }

    private Optional<QcIssueVO> check(QcRuleEnum rule, QcSnapshot s) {
        return switch (rule) {
            case C01 -> missing(s.getChiefComplaint(),
                    ClinicalTextMatcher.isPlaceholderOnly(s.getChiefComplaint(), "主诉"), "主诉")
                    .map(detail -> QcIssueVO.of(rule, detail, evidenceOf(s.getChiefComplaint())));

            case C02 -> missing(s.getPresentIllness(),
                    ClinicalTextMatcher.isPlaceholderOnly(s.getPresentIllness(), "现病史"), "现病史")
                    .map(detail -> QcIssueVO.of(rule, detail, evidenceOf(s.getPresentIllness())));

            // 既往史/过敏史：「无」是合法记录，只判"留空"与"模板残渣"
            case C03 -> missing(s.getPastHistory(),
                    ClinicalTextMatcher.isBlank(s.getPastHistory())
                            || ClinicalTextMatcher.isLabelRepeatOnly(s.getPastHistory(), "既往史"), "既往史")
                    .map(detail -> QcIssueVO.of(rule, detail, evidenceOf(s.getPastHistory())));

            case C04 -> missing(s.getAllergyHistory(),
                    ClinicalTextMatcher.isBlank(s.getAllergyHistory())
                            || ClinicalTextMatcher.isLabelRepeatOnly(s.getAllergyHistory(), "过敏史"), "过敏史")
                    .map(detail -> QcIssueVO.of(rule, detail, evidenceOf(s.getAllergyHistory())));

            case C05 -> missing(TextUtil.hasText(s.getDiagnosisText()) ? s.getDiagnosisText() : s.getDiagnosisCode(),
                    ClinicalTextMatcher.isPlaceholderOnly(s.getDiagnosisText(), "诊断"), "诊断")
                    .map(detail -> QcIssueVO.of(rule, detail, evidenceOf(s.getDiagnosisText())));

            case C06 -> {
                if (!TextUtil.hasText(s.getDiagnosisName()) || TextUtil.hasText(s.getDiagnosisCode())) {
                    yield Optional.empty();
                }
                yield Optional.of(QcIssueVO.of(rule,
                        String.format("诊断「%s」只有名称没有 ICD 编码，病案首页与 DRG 无法入组",
                                TextUtil.ellipsis(s.getDiagnosisName(), 40)),
                        evidenceOf(s.getDiagnosisCode())));
            }

            case C07 -> missing(s.getTreatmentPlan(),
                    ClinicalTextMatcher.isPlaceholderOnly(s.getTreatmentPlan())
                            || PLAN_LABELS.stream().anyMatch(label ->
                            ClinicalTextMatcher.isLabelRepeatOnly(s.getTreatmentPlan(), label)), "诊疗计划")
                    .map(detail -> QcIssueVO.of(rule, detail, evidenceOf(s.getTreatmentPlan())));

            case C08 -> {
                if (s.getDoctorId() != null || TextUtil.hasText(s.getDoctorName())) {
                    yield Optional.empty();
                }
                yield Optional.of(QcIssueVO.of(rule, "「书写医生」未记录，无签名病历不得归档", "（空）"));
            }

            case C09 -> missing(s.getCourseNote(),
                    ClinicalTextMatcher.isPlaceholderOnly(s.getCourseNote()), "正文")
                    .map(detail -> QcIssueVO.of(rule,
                            String.format("%s未写正文：%s", s.recordTypeText(), detail),
                            evidenceOf(s.getCourseNote())));

            case F01 -> {
                if (isChiefComplaintBlank(s)) {
                    yield Optional.empty();
                }
                if (DURATION_PATTERN.matcher(s.getChiefComplaint()).find()) {
                    yield Optional.empty();
                }
                yield Optional.of(QcIssueVO.of(rule,
                        String.format("主诉「%s」未写明症状持续时间", TextUtil.ellipsis(s.getChiefComplaint(), 30)),
                        evidenceOf(s.getChiefComplaint())));
            }

            case F02 -> {
                if (isChiefComplaintBlank(s)) {
                    yield Optional.empty();
                }
                Optional<String> hit = DIAGNOSIS_HINTS.stream()
                        .filter(hint -> ClinicalTextMatcher.containsAffirmed(s.getChiefComplaint(), hint))
                        .findFirst();
                yield hit.map(hint -> QcIssueVO.of(rule,
                        String.format("主诉含诊断性结论用语「%s」，主诉只应写症状与体征", hint),
                        evidenceOf(s.getChiefComplaint())));
            }

            case F03 -> {
                if (isChiefComplaintBlank(s)) {
                    yield Optional.empty();
                }
                int length = ClinicalTextMatcher.effectiveLength(s.getChiefComplaint());
                if (length >= MIN_CHIEF_COMPLAINT_LENGTH) {
                    yield Optional.empty();
                }
                yield Optional.of(QcIssueVO.of(rule,
                        String.format("主诉有效长度仅 %d 字（要求 ≥%d 字），过于笼统",
                                length, MIN_CHIEF_COMPLAINT_LENGTH),
                        evidenceOf(s.getChiefComplaint())));
            }

            case F04 -> {
                if (TextUtil.hasText(s.getRecordTitle())) {
                    yield Optional.empty();
                }
                yield Optional.of(QcIssueVO.of(rule,
                        String.format("%s未填写文书标题", s.recordTypeText()), "（空）"));
            }

            case F05 -> {
                List<String> lacks = new ArrayList<>();
                if (!TextUtil.hasText(s.getDeptName())) {
                    lacks.add("就诊科室");
                }
                if (s.getRecordTime() == null) {
                    lacks.add("就诊日期");
                }
                if (lacks.isEmpty()) {
                    yield Optional.empty();
                }
                yield Optional.of(QcIssueVO.of(rule,
                        String.format("门诊病历缺少%s，无法归集到科室质控统计", String.join("与", lacks)),
                        "（空）"));
            }

            case L01 -> {
                if (s.getGender() == null || !TextUtil.hasText(s.getDiagnosisText())) {
                    yield Optional.empty();
                }
                // 性别码值按被查表本身的口径：住院病历文书 / 门诊病历是 1-男 2-女
                // （患者基本信息才是 0-女 1-男，两套约定不同，这里吃的不是 patient.gender）。
                // 落在 1/2 之外一律不判 —— 码值本身不可信时，任何"矛盾"结论都不可信。
                SysGenderEnum g = SysGenderEnum.fromCode(s.getGender());
                List<String> words = switch (s.getGender()) {
                    case 1 -> FEMALE_ONLY_WORDS;
                    case 2 -> MALE_ONLY_WORDS;
                    default -> List.of();
                };
                Optional<String> conflict = words.stream()
                        .filter(word -> ClinicalTextMatcher.containsAffirmed(s.getDiagnosisText(), word))
                        .findFirst();
                yield conflict.map(word -> QcIssueVO.of(rule,
                        String.format("患者性别为%s，诊断却出现%s专属表述「%s」",
                                g == null ? "未知" : g.getLabel(), g == SysGenderEnum.MALE ? "男性" : (g == SysGenderEnum.FEMALE ? "女性" : "未知"), word),
                        evidenceOf(s.getDiagnosisText())));
            }

            case L02 -> {
                List<String> outOfRange = new ArrayList<>();
                BigDecimal temperature = s.getTemperature();
                if (temperature != null
                        && (temperature.compareTo(TEMPERATURE_MIN) < 0 || temperature.compareTo(TEMPERATURE_MAX) > 0)) {
                    outOfRange.add(String.format("体温 %s℃（可存活范围 %.1f~%.1f）",
                            temperature.toPlainString(), TEMPERATURE_MIN.doubleValue(), TEMPERATURE_MAX.doubleValue()));
                }
                if (s.getPulse() != null && (s.getPulse() < PULSE_MIN || s.getPulse() > PULSE_MAX)) {
                    outOfRange.add(String.format("脉搏 %d 次/分（范围 %d~%d）", s.getPulse(), PULSE_MIN, PULSE_MAX));
                }
                if (s.getRespiration() != null && (s.getRespiration() < RESPIRATION_MIN || s.getRespiration() > RESPIRATION_MAX)) {
                    outOfRange.add(String.format("呼吸 %d 次/分（范围 %d~%d）",
                            s.getRespiration(), RESPIRATION_MIN, RESPIRATION_MAX));
                }
                if (s.getSystolicPressure() != null
                        && (s.getSystolicPressure() < SYSTOLIC_MIN || s.getSystolicPressure() > SYSTOLIC_MAX)) {
                    outOfRange.add(String.format("收缩压 %d mmHg（范围 %d~%d）",
                            s.getSystolicPressure(), SYSTOLIC_MIN, SYSTOLIC_MAX));
                }
                if (s.getDiastolicPressure() != null
                        && (s.getDiastolicPressure() < DIASTOLIC_MIN || s.getDiastolicPressure() > DIASTOLIC_MAX)) {
                    outOfRange.add(String.format("舒张压 %d mmHg（范围 %d~%d）",
                            s.getDiastolicPressure(), DIASTOLIC_MIN, DIASTOLIC_MAX));
                }
                if (outOfRange.isEmpty()) {
                    yield Optional.empty();
                }
                yield Optional.of(QcIssueVO.of(rule,
                        "生命体征数值超出医学可存活范围：" + String.join("；", outOfRange) + "，必为录入错误",
                        "（见描述）"));
            }

            case L03 -> {
                Integer systolic = s.getSystolicPressure();
                Integer diastolic = s.getDiastolicPressure();
                if (systolic == null || diastolic == null || systolic > diastolic) {
                    yield Optional.empty();
                }
                yield Optional.of(QcIssueVO.of(rule,
                        String.format("收缩压 %d 不高于舒张压 %d，该血压值不可能成立", systolic, diastolic),
                        "（见描述）"));
            }

            case L04 -> {
                if (s.getSubmitTime() == null || s.getRecordTime() == null
                        || !s.getRecordTime().isAfter(s.getSubmitTime())) {
                    yield Optional.empty();
                }
                yield Optional.of(QcIssueVO.of(rule,
                        String.format("记录时间 %s 晚于提交时间 %s，该文书声称记录的内容在提交时尚未发生",
                                s.getRecordTime(), s.getSubmitTime()),
                        "（见描述）"));
            }

            case L05 -> {
                if (s.getRecordStatus() == null || s.getRecordStatus() != 3 || s.getSubmitTime() != null) {
                    yield Optional.empty();
                }
                yield Optional.of(QcIssueVO.of(rule,
                        String.format("%s状态为已归档，却没有提交时间记录", s.recordTypeText()), "（空）"));
            }

            case L06 -> {
                if (s.getArchiveTime() == null || s.getSubmitTime() == null
                        || !s.getArchiveTime().isBefore(s.getSubmitTime())) {
                    yield Optional.empty();
                }
                yield Optional.of(QcIssueVO.of(rule,
                        String.format("归档时间 %s 早于提交时间 %s", s.getArchiveTime(), s.getSubmitTime()),
                        "（见描述）"));
            }
        };
    }
}
