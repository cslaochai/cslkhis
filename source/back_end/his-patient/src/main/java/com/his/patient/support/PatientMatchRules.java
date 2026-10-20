package com.his.patient.support;

import com.his.common.enums.PatientMatchLevelEnum;
import com.his.common.util.TextUtil;
import com.his.patient.entity.BizPatient;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 患者重复建档的判级口径（服务端唯一口径）
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PatientMatchRules {

    /**
     * 计算两份档案的匹配级别。
     *
     * <p>字段为空一律**不参与判定**：不知道就当作不匹配，宁可让人去核实，
     * 也不能因为"两边都空"就当成"两边一样"——那会把所有没填身份证的人合并成一堆。
     */
    public static Integer match(BizPatient a, BizPatient b) {
        if (a == null || b == null) {
            return PatientMatchLevelEnum.MANUAL.getCode();
        }
        // 1 身份证号（去空格、忽略大小写；身份证尾位可能是 X）
        if (TextUtil.hasText(a.getIdCard()) && TextUtil.hasText(b.getIdCard())
                && a.getIdCard().trim().equalsIgnoreCase(b.getIdCard().trim())) {
            return PatientMatchLevelEnum.ID_CARD.getCode();
        }
        // 2 姓名 + 性别 + 出生日期（三者都必须有值）
        if (nameEquals(a, b) && a.getGender() != null && a.getGender().equals(b.getGender())
                && a.getBirthDate() != null && a.getBirthDate().equals(b.getBirthDate())) {
            return PatientMatchLevelEnum.NAME_GENDER_BIRTH.getCode();
        }
        // 3 姓名 + 手机号
        if (nameEquals(a, b) && TextUtil.hasText(a.getPhone()) && TextUtil.hasText(b.getPhone())
                && a.getPhone().trim().equals(b.getPhone().trim())) {
            return PatientMatchLevelEnum.NAME_PHONE.getCode();
        }
        return PatientMatchLevelEnum.MANUAL.getCode();
    }

    /**
     * 交叉命中时取更强的那条作为整组的级别（同一对档案可能同时命中 2 和 3）
     */
    public static Integer stronger(Integer x, Integer y) {
        if (x == null) {
            return y;
        }
        if (y == null) {
            return x;
        }
        return Math.min(x, y);
    }

    /**
     * 用于把多份档案聚成一组的 key。同 key 即"值得放到一起看"。
     */
    public static String groupKey(Integer level, BizPatient p) {
        if (level == null || p == null) {
            return null;
        }
        PatientMatchLevelEnum item = PatientMatchLevelEnum.fromCode(level);
        if (item == null) {
            return TextUtil.hasText(p.getPatientName()) ? "NAME:" + TextUtil.trimToEmpty(p.getPatientName()) : null;
        }
        return switch (item) {
            case ID_CARD -> TextUtil.hasText(p.getIdCard())
                    ? "IDC:" + TextUtil.trimToEmpty(p.getIdCard()).toUpperCase() : null;
            case NAME_GENDER_BIRTH -> (TextUtil.hasText(p.getPatientName())
                    && p.getGender() != null && p.getBirthDate() != null)
                    ? "NGB:" + TextUtil.trimToEmpty(p.getPatientName()) + "|" + p.getGender() + "|" + p.getBirthDate() : null;
            case NAME_PHONE -> (TextUtil.hasText(p.getPatientName()) && TextUtil.hasText(p.getPhone()))
                    ? "NP:" + TextUtil.trimToEmpty(p.getPatientName()) + "|" + TextUtil.trimToEmpty(p.getPhone()) : null;
            case MANUAL ->
                    TextUtil.hasText(p.getPatientName()) ? "NAME:" + TextUtil.trimToEmpty(p.getPatientName()) : null;
        };
    }

    /**
     * 是否强依据。强依据才谈得上"基本可确认是同一人"
     */
    public static boolean isStrong(Integer level) {
        return level != null && level == PatientMatchLevelEnum.ID_CARD.getCode();
    }

    /**
     * 该等级要求的理由长度下限。
     *
     * <p>身份证相同的合并不需要长篇解释（客观依据摆着）；而"仅同名"就合并是高危动作，
     * 必须写清是谁怎么核实的 —— 用一个字符的理由把两个人合成一个，事故就是这么来的。
     */
    public static int minReasonLength(Integer level) {
        return isStrong(level) ? 2 : 10;
    }

    /**
     * 命中依据的字段值快照（写进合并审计，事后能一眼看出当初凭什么合的）
     */
    public static String matchSnapshot(Integer level, BizPatient a, BizPatient b) {
        if (level == null) {
            return null;
        }
        PatientMatchLevelEnum item = PatientMatchLevelEnum.fromCode(level);
        if (item == null) {
            return "patient_name=" + TextUtil.trimToEmpty(a.getPatientName()) + "（仅同名，无其余字段命中）";
        }
        return switch (item) {
            case ID_CARD -> "id_card=" + TextUtil.trimToEmpty(a.getIdCard());
            case NAME_GENDER_BIRTH -> "patient_name=" + TextUtil.trimToEmpty(a.getPatientName())
                    + "; gender=" + a.getGender()
                    + "; birth_date=" + (a.getBirthDate() == null ? "" : a.getBirthDate());
            case NAME_PHONE -> "patient_name=" + TextUtil.trimToEmpty(a.getPatientName())
                    + "; phone=" + TextUtil.trimToEmpty(a.getPhone());
            case MANUAL -> "patient_name=" + TextUtil.trimToEmpty(a.getPatientName()) + "（仅同名，无其余字段命中）";
        };
    }

    private static boolean nameEquals(BizPatient a, BizPatient b) {
        return TextUtil.hasText(a.getPatientName()) && TextUtil.hasText(b.getPatientName())
                && a.getPatientName().trim().equals(b.getPatientName().trim());
    }
}
