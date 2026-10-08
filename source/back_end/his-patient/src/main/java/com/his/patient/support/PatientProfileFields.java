package com.his.patient.support;

import com.his.common.util.TextUtil;
import com.his.patient.entity.BizPatient;
import com.his.patient.enums.PatientProfileCoverageEnum;
import com.his.patient.enums.PatientProfileFieldEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * 患者档案关键字段清单与完整度评分（P5.1 EMPI / P5.3 数据质量共用）
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PatientProfileFields {

    /**
     * 字段名（前端字段名）→ 中文名。顺序即展示顺序
     */
    public static final Map<String, String> FIELDS = build();
    /**
     * 档案分组键 → 关键字段中文名。
     *
     * <p>只列**同时有两份存储**的字段：`过敏史`、`既往病史` 在患者基本信息里是文本字段，
     * 同时又有结构化表（药物过敏史 / 既往疾病史）；
     * `联系人` 也一样（患者基本信息.contact_name vs 患者联系方式）。
     * 手术史/家族史/用药史没有对应的患者基本信息文本字段，不在此表。
     */
    private static final Map<String, String> PROFILE_KEY_TO_LABEL = PatientProfileCoverageEnum.all();

    private static Map<String, String> build() {
        Map<String, String> m = new LinkedHashMap<>();
        for (PatientProfileFieldEnum e : PatientProfileFieldEnum.values()) {
            m.put(e.getCode(), e.getLabel());
        }
        // 必须用 unmodifiableMap 而不是 Map.copyOf：copyOf 会丢掉 LinkedHashMap 的插入顺序，
        // 页面上的"关键字段清单"就是按这个顺序渲染的。
        return java.util.Collections.unmodifiableMap(m);
    }

    /**
     * 对一份患者档案评分
     */
    public static ProfileScore score(BizPatient p) {
        List<String> missing = new ArrayList<>();
        int filled = 0;
        if (p == null) {
            missing.addAll(FIELDS.values());
            return new ProfileScore(0, FIELDS.size(), missing);
        }
        if (TextUtil.hasText(p.getPatientName())) filled++;
        else missing.add(FIELDS.get("patientName"));
        if (p.getGender() != null) filled++;
        else missing.add(FIELDS.get("gender"));
        if (p.getBirthDate() != null) filled++;
        else missing.add(FIELDS.get("birthDate"));
        if (TextUtil.hasText(p.getIdCard())) filled++;
        else missing.add(FIELDS.get("idCard"));
        if (TextUtil.hasText(p.getPhone())) filled++;
        else missing.add(FIELDS.get("phone"));
        if (TextUtil.hasText(p.getAddress())) filled++;
        else missing.add(FIELDS.get("address"));
        if (TextUtil.hasText(p.getNation())) filled++;
        else missing.add(FIELDS.get("nation"));
        if (TextUtil.hasText(p.getOccupation())) filled++;
        else missing.add(FIELDS.get("occupation"));
        if (p.getMaritalStatus() != null) filled++;
        else missing.add(FIELDS.get("maritalStatus"));
        if (TextUtil.hasText(p.getBloodType())) filled++;
        else missing.add(FIELDS.get("bloodType"));
        if (TextUtil.hasText(p.getContactName())) filled++;
        else missing.add(FIELDS.get("contactName"));
        if (TextUtil.hasText(p.getContactPhone())) filled++;
        else missing.add(FIELDS.get("contactPhone"));
        if (TextUtil.hasText(p.getAllergyHistory())) filled++;
        else missing.add(FIELDS.get("allergyHistory"));
        if (TextUtil.hasText(p.getMedicalHistory())) filled++;
        else missing.add(FIELDS.get("medicalHistory"));
        if (TextUtil.hasText(p.getMedicalInsuranceType())) filled++;
        else missing.add(FIELDS.get("medicalInsuranceType"));
        return new ProfileScore(filled, FIELDS.size(), missing);
    }

    /**
     * 用结构化档案表的实际数据，修正完整度的缺失判据。
     *
     * <p>为什么需要这一步：同一语义在本系统里存了两份 —— `患者基本信息.allergy_history` 是一段自由文本，
     * `药物过敏史` 是结构化表（过敏原 / 严重程度 / 发作日期）。若完整度只看文本，就会出现
     * "健康档案里明明列着青霉素过敏，同一张卡片上却标『缺过敏史』"这种自相矛盾的页面。
     * 患者全景的完整度必须以**库里实际有没有这项数据**为准，所以由调用方把结构化表命中的分组传进来。
     *
     * <p>注意：这是**补足**而不是**覆盖** —— 只把"文本为空但结构化表有数据"的项从不缺列表里摘掉，
     * 不会因为结构化表为空就把文本已填的项算成缺。
     *
     * @param base        文本字段的原始评分
     * @param coveredKeys 已有数据的档案分组键（allergy / pastDisease / contact）
     */
    public static ProfileScore applyProfileCoverage(ProfileScore base, Set<String> coveredKeys) {
        if (base == null || coveredKeys == null || coveredKeys.isEmpty()) {
            return base;
        }
        List<String> missing = new ArrayList<>();
        for (String label : base.missingFields()) {
            boolean covered = PROFILE_KEY_TO_LABEL.entrySet().stream()
                    .anyMatch(e -> e.getValue().equals(label) && coveredKeys.contains(e.getKey()));
            if (!covered) {
                missing.add(label);
            }
        }
        if (missing.size() == base.missingFields().size()) {
            return base;
        }
        return new ProfileScore(base.totalCount() - missing.size(), base.totalCount(), missing);
    }

    /**
     * 评分结果。
     */
    public record ProfileScore(int completeCount, int totalCount, List<String> missingFields) {

        /**
         * 完整度百分比，保留一位小数
         */
        public double rate() {
            if (totalCount <= 0) {
                return 0D;
            }
            return Math.round(completeCount * 1000D / totalCount) / 10D;
        }
    }
}
