package com.his.patient.support;

import com.his.patient.entity.BizInpatientRecord;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;

/**
 * 住院病历的<b>结构化要素清单</b>——「结构化率」的<b>唯一口径</b>。
 *
 * <p>为什么必须只有一个口径：如果统计端写一套字段清单、校验端写另一套，
 * 就会出现"报表说 85%、质控说 62%"这种场面，然后两边都会被质疑、最后谁都不信。
 * 所以清单在这里定义一次，统计、缺项定位、验收脚本全部走它。
 *
 * <p><b>什么算「填了」</b>：
 * <ul>
 *   <li>文本类：非 null 且非空白（{@code "  "} 不算填）。</li>
 *   <li>数值类：<b>非 null 即算填</b>，{@code 0} 也算填 —— 0 是一个合法的观测值
 *       （大便 0 次/日、尿量 0ml 都是真实且重要的结果），把 0 当"没填"会让三测单的
 *       结构化率永远差几条。这与「未判定 ≠ 正常」是同一条线：要区分
 *       <b>没有值</b>（null）和<b>值是 0</b>，不能把它们混成一个"空"。</li>
 * </ul>
 *
 * <p><b>分母按文书类型算</b>：{@code course_note}（病程正文）只对病程类文书计数。
 * 否则入院记录的"结构化率"永远差一项，医生为了凑分会往病程正文里填一句废话，
 * 报表好看了、病历更假了。
 */
public final class RecordStructuredFields {

    /**
     * 分组：病史
     */
    public static final String GROUP_HISTORY = "history";
    /**
     * 分组：生命体征
     */
    public static final String GROUP_VITAL = "vital";
    /**
     * 分组：体格检查
     */
    public static final String GROUP_EXAM = "exam";
    /**
     * 分组：诊疗过程与结论
     */
    public static final String GROUP_CONCLUSION = "conclusion";
    /**
     * 分组：会诊要素。
     * <p>会诊记录（record_type=9）由会诊完成时**系统回写**，不是医生书写的病程文书：
     * 它的要素只有"理由"和"结论"两项。若不单独给清单，它会套用 26 项要素清单、
     * 除了结论全是空 —— 全院结构化率会被一批系统生成的文书拖下去，
     * 而医生根本没写错任何东西（把系统文书算成医生的欠账，报表就没人信了）。
     */
    public static final String GROUP_CONSULT = "consult";

    /**
     * 分组：转科要素。
     * <p>转科记录（record_type=10）与会诊记录同理，也是**系统回写**的文书，
     * 要素只有"转科原因"和"交接与医嘱处置"两项。给它套 26 项清单，
     * 等于把系统文书的欠账记到医生头上。
     */
    public static final String GROUP_TRANSFER = "transfer";

    /**
     * 手术记录要素（P4.3）
     */
    public static final String GROUP_OPERATION = "operation";

    /**
     * 分组：输血要素。
     * <p>输血记录（record_type=11）由输血闭环完成时**系统回写**，与会诊（9）/转科（10）同理 ——
     * 它不是医生书写的病程文书，套 26 项通用清单只会让系统文书的欠账记到医生头上。
     */
    public static final String GROUP_TRANSFUSION = "transfusion";

    private static final Map<String, String> GROUP_LABELS = new LinkedHashMap<>();
    /**
     * 所有文书都计入的要素（26 项）
     */
    private static final List<KeyElement> BASE_ELEMENTS = List.of(
            // 病史（6）
            new KeyElement("chief_complaint", "主诉", GROUP_HISTORY, BizInpatientRecord::getChiefComplaint),
            new KeyElement("present_illness", "现病史", GROUP_HISTORY, BizInpatientRecord::getPresentIllness),
            new KeyElement("past_history", "既往史", GROUP_HISTORY, BizInpatientRecord::getPastHistory),
            new KeyElement("personal_history", "个人史", GROUP_HISTORY, BizInpatientRecord::getPersonalHistory),
            new KeyElement("family_history", "家族史", GROUP_HISTORY, BizInpatientRecord::getFamilyHistory),
            new KeyElement("allergy_history", "过敏史", GROUP_HISTORY, BizInpatientRecord::getAllergyHistory),
            // 生命体征（7）：数值列，这才叫结构化
            new KeyElement("temperature", "体温", GROUP_VITAL, BizInpatientRecord::getTemperature),
            new KeyElement("pulse", "脉搏", GROUP_VITAL, BizInpatientRecord::getPulse),
            new KeyElement("respiration", "呼吸", GROUP_VITAL, BizInpatientRecord::getRespiration),
            new KeyElement("systolic_pressure", "收缩压", GROUP_VITAL, BizInpatientRecord::getSystolicPressure),
            new KeyElement("diastolic_pressure", "舒张压", GROUP_VITAL, BizInpatientRecord::getDiastolicPressure),
            new KeyElement("height", "身高", GROUP_VITAL, BizInpatientRecord::getHeight),
            new KeyElement("weight", "体重", GROUP_VITAL, BizInpatientRecord::getWeight),
            // 体格检查（9）：按系统拆列，才定位得出"缺了腹部查体"
            new KeyElement("general_condition", "一般情况", GROUP_EXAM, BizInpatientRecord::getGeneralCondition),
            new KeyElement("skin_mucosa", "皮肤黏膜", GROUP_EXAM, BizInpatientRecord::getSkinMucosa),
            new KeyElement("head_neck", "头颈部", GROUP_EXAM, BizInpatientRecord::getHeadNeck),
            new KeyElement("chest_lung", "胸部及肺", GROUP_EXAM, BizInpatientRecord::getChestLung),
            new KeyElement("heart", "心脏", GROUP_EXAM, BizInpatientRecord::getHeart),
            new KeyElement("abdomen", "腹部", GROUP_EXAM, BizInpatientRecord::getAbdomen),
            new KeyElement("spine_limbs", "脊柱四肢", GROUP_EXAM, BizInpatientRecord::getSpineLimbs),
            new KeyElement("nervous_system", "神经系统", GROUP_EXAM, BizInpatientRecord::getNervousSystem),
            new KeyElement("specialist_exam", "专科检查", GROUP_EXAM, BizInpatientRecord::getSpecialistExam),
            // 诊疗过程与结论（4）
            new KeyElement("auxiliary_exam", "辅助检查", GROUP_CONCLUSION, BizInpatientRecord::getAuxiliaryExam),
            new KeyElement("diagnosis_name", "诊断名称", GROUP_CONCLUSION, BizInpatientRecord::getDiagnosisName),
            new KeyElement("diagnosis_code", "诊断编码", GROUP_CONCLUSION, BizInpatientRecord::getDiagnosisCode),
            new KeyElement("treatment_plan", "诊疗计划", GROUP_CONCLUSION, BizInpatientRecord::getTreatmentPlan)
    );
    /**
     * 仅病程类文书（首次病程 / 日常病程 / 术后首次病程）计入
     */
    private static final KeyElement COURSE_NOTE_ELEMENT =
            new KeyElement("course_note", "病程正文", GROUP_CONCLUSION, BizInpatientRecord::getCourseNote);
    /**
     * 会诊记录（record_type=9）的要素清单：只有"会诊理由"与"会诊结论"两项。
     *
     * <p>回写时会诊理由写在备注（文案形如"系统回写：会诊号 HZ…，会诊理由：…"），
     * 结论写在 {@code courseNote}。这两项**必然同时写下**，所以系统回写的会诊记录
     * 结构化率就是 100% —— 这是事实，不是凑分。
     */
    private static final List<KeyElement> CONSULT_ELEMENTS = List.of(
            new KeyElement("reason", "会诊理由", GROUP_CONSULT, BizInpatientRecord::getRemark),
            new KeyElement("course_note", "会诊结论", GROUP_CONSULT, BizInpatientRecord::getCourseNote)
    );
    /**
     * 转科记录（record_type=10）的要素清单：只有"转科原因"与"交接与医嘱处置"两项。
     *
     * <p>转科原因写在备注，交接小结与医嘱处置（含"哪几条医嘱停不掉"）写在
     * {@code courseNote}。两项都由系统在接收时一次性写下，所以回写的转科记录结构化率是 100%。
     */
    private static final List<KeyElement> TRANSFER_ELEMENTS = List.of(
            new KeyElement("reason", "转科原因", GROUP_TRANSFER, BizInpatientRecord::getRemark),
            new KeyElement("course_note", "交接与医嘱处置", GROUP_TRANSFER, BizInpatientRecord::getCourseNote)
    );
    /**
     * 手术记录（record_type=5）的要素清单：术前诊断 + 手术经过 + 来源申请单与术者。
     *
     * <p>这里要<b>刻意偏离</b> 26 项通用清单：手术记录的要素就是这三样 ——
     * "主诉/现病史/家族史/体格检查 8 个系统"对一台手术毫无意义。
     * 套用通用清单的后果不是"更严格"，而是系统回写的手术记录永远 2/26，
     * 凭空把全院结构化率拉低，而医生什么都没写错（同会诊记录、转科记录踩过的坑）。
     *
     * <p>注意 5-手术记录 P2 就已存在、医生本来就能手写 —— 手工写的那份也走这份清单，
     * 所以分母变小对它是**改善**而不是破坏。
     */
    private static final List<KeyElement> OPERATION_ELEMENTS = List.of(
            new KeyElement("preop_diag", "术前诊断", GROUP_OPERATION, BizInpatientRecord::getDiagnosisName),
            new KeyElement("course_note", "手术经过", GROUP_OPERATION, BizInpatientRecord::getCourseNote),
            new KeyElement("reason", "来源申请单与术者", GROUP_OPERATION, BizInpatientRecord::getRemark)
    );
    /**
     * 输血记录（record_type=11）的要素清单：输血成分与量 + 输血经过 + 疗效评估与反应处理。
     *
     * <p>同样刻意偏离 26 项通用清单。输血记录要回答的只有三件事：
     * <b>输了什么、怎么输的、输完怎么样</b>（含有没有不良反应、怎么处理的）。
     * 回写时这三项由同一个事务一次写下，所以系统回写的输血记录结构化率是 100%。
     *
     * <p>11-输血记录是 P4.4 <b>新增</b>的码值，因此它与 9/10 一样属于"系统专用"文书
     * （手工新增会被拒）—— 手工新增一条没有血袋、没有配血的"输血记录"就是假病历。
     */
    private static final List<KeyElement> TRANSFUSION_ELEMENTS = List.of(
            new KeyElement("component", "输血成分与量", GROUP_TRANSFUSION, BizInpatientRecord::getRemark),
            new KeyElement("course_note", "输血经过", GROUP_TRANSFUSION, BizInpatientRecord::getCourseNote),
            new KeyElement("efficacy", "疗效评估与反应处理", GROUP_TRANSFUSION, BizInpatientRecord::getTreatmentPlan)
    );

    static {
        GROUP_LABELS.put(GROUP_HISTORY, "病史要素");
        GROUP_LABELS.put(GROUP_VITAL, "生命体征");
        GROUP_LABELS.put(GROUP_EXAM, "体格检查");
        GROUP_LABELS.put(GROUP_CONCLUSION, "诊疗过程与结论");
        GROUP_LABELS.put(GROUP_CONSULT, "会诊要素");
        GROUP_LABELS.put(GROUP_TRANSFER, "转科要素");
        GROUP_LABELS.put(GROUP_OPERATION, "手术要素");
        GROUP_LABELS.put(GROUP_TRANSFUSION, "输血要素");
    }

    private RecordStructuredFields() {
    }

    /**
     * 该文书类型应具备的结构化要素清单（分母）。
     */
    public static List<KeyElement> elementsFor(Integer recordType) {
        if (InpatientRecordLabels.isConsultRecord(recordType)) {
            return CONSULT_ELEMENTS;
        }
        if (InpatientRecordLabels.isTransferRecord(recordType)) {
            return TRANSFER_ELEMENTS;
        }
        if (InpatientRecordLabels.isOperationRecord(recordType)) {
            return OPERATION_ELEMENTS;
        }
        if (InpatientRecordLabels.isTransfusionRecord(recordType)) {
            return TRANSFUSION_ELEMENTS;
        }
        if (!InpatientRecordLabels.isCourseRecord(recordType)) {
            return BASE_ELEMENTS;
        }
        List<KeyElement> list = new ArrayList<>(BASE_ELEMENTS);
        list.add(COURSE_NOTE_ELEMENT);
        return list;
    }

    /**
     * 全部要素（含 course_note），供前端展示"要素字典"用
     */
    public static List<KeyElement> allElements() {
        return elementsFor(3);
    }

    public static Map<String, String> groupLabels() {
        return GROUP_LABELS;
    }

    /**
     * 什么算「填了」：文本要非空白，数值非 null 即算（0 是合法观测值）。
     */
    public static boolean isFilled(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof CharSequence cs) {
            return StringUtils.hasText(cs);
        }
        return true;
    }

    /**
     * 该文书已填的要素数
     */
    public static int filledCount(BizInpatientRecord record) {
        return (int) elementsFor(record.getRecordType()).stream()
                .filter(e -> isFilled(e.getter().apply(record)))
                .count();
    }

    /**
     * 该文书应填的要素总数（分母）
     */
    public static int totalCount(BizInpatientRecord record) {
        return elementsFor(record.getRecordType()).size();
    }

    /**
     * 缺失要素的<b>中文名</b>列表（用于"不达标能定位到具体哪一份、缺哪一项"）。
     */
    public static List<String> missingLabels(BizInpatientRecord record) {
        List<String> missing = new ArrayList<>();
        for (KeyElement e : elementsFor(record.getRecordType())) {
            if (!isFilled(e.getter().apply(record))) {
                missing.add(e.label());
            }
        }
        return missing;
    }

    /**
     * 结构化率（百分数，保留 2 位）；分母为 0 时返回 null —— 不是 0、不是 100，
     * 「算不出来」和「算出来是 0」必须区分（同「未判定 ≠ 正常」）。
     */
    public static java.math.BigDecimal rate(int filled, int total) {
        if (total <= 0) {
            return null;
        }
        return java.math.BigDecimal.valueOf(filled * 100L)
                .divide(java.math.BigDecimal.valueOf(total), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 分组聚合：某分组下已填 / 应填
     */
    public static String groupOf(String code) {
        for (KeyElement e : allElements()) {
            if (Objects.equals(e.code(), code)) {
                return e.group();
            }
        }
        return null;
    }

    /**
     * 一个结构化要素：编码（= 库列名，前后端与脚本共用）、中文名、所属分组、取值函数。
     */
    public record KeyElement(String code, String label, String group,
                             Function<BizInpatientRecord, Object> getter) {
    }
}
