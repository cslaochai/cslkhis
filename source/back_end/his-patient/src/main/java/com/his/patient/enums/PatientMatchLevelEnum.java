package com.his.patient.enums;

import com.his.patient.entity.BizPatient;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * 患者重复档案例档案匹配分级（P5.1 EMPI）
 *
 * <p><b>实测依据（2026-09-19，变动前先重新实测）</b>：
 * 全库 44 个患者里，手机号 {@code 18888888888} 下挂着 <b>10 个不同的人</b>
 * （陈柴/刘德勇/刘亦菲/老王/…），{@code 13900000001} 挂 2 个 —— 都是造数时共用的号码。
 * 唯一一对同名（"老刘"）身份证分别是 430726199102122257 / 430726199845261258、
 * 出生日期也不同 —— <b>那是真两个人</b>。
 *
 * <p>所以分级必须这么定，不能按常识放宽：
 * <ul>
 *   <li>{@link #ID_CARD} 强依据 —— 只有身份证号算数。</li>
 *   <li>{@link #NAME_PHONE} 手机号只能**配合姓名**降级为"疑似"，
 *       <b>绝不允许单独用手机号判重复</b>（一号码挂 10 人，按它合并会一次毁掉 10 份档案）。</li>
 *   <li>{@link #MANUAL} 仅同名（性别/生日/证件都不同）—— 排在最可疑的位置提醒人别合并。</li>
 * </ul>
 *
 * <p>另外一条底线：**服务端自己算级别，不采信前端传的**（前端可传，但以服务端结果为准）。
 * 否则改一个请求参数就能把"仅同名"说成"身份证相同"，绕过人工核实。
 */
@Getter
public enum PatientMatchLevelEnum {

    /** 1-身份证号相同（强依据，唯一可直接采信的） */
    ID_CARD(1, "身份证号相同"),
    /** 2-姓名 + 性别 + 出生日期全等 */
    NAME_GENDER_BIRTH(2, "姓名+性别+出生日期相同"),
    /** 3-姓名 + 手机号相同 */
    NAME_PHONE(3, "姓名+手机号相同"),
    /** 4-人工判定（无字段命中，通常是仅同名） */
    MANUAL(4, "仅姓名相同（未命中任何强字段）");

    private final int code;
    private final String label;

    PatientMatchLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PatientMatchLevelEnum fromCode(int code) {
        for (PatientMatchLevelEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        PatientMatchLevelEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }

    /** 无级别显示为破折号，脏码值要显式暴露出来，不能糊成某个合法级别。 */
    public static String text(Integer level) {
        if (level == null) {
            return "—";
        }
        String label = labelOf(level);
        return label == null ? "未知(" + level + ")" : label;
    }

    /** 是否强依据。强依据才谈得上"基本可确认是同一人" */
    public static boolean isStrong(Integer level) {
        return level != null && level == ID_CARD.getCode();
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
     * 计算两份档案的匹配级别（服务端唯一口径）。
     *
     * <p>字段为空一律**不参与判定**：不知道就当作不匹配，宁可让人去核实，
     * 也不能因为"两边都空"就当成"两边一样"——那会把所有没填身份证的人合并成一堆。
     */
    public static Integer match(BizPatient a, BizPatient b) {
        if (a == null || b == null) {
            return MANUAL.getCode();
        }
        // 1 身份证号（去空格、忽略大小写；身份证尾位可能是 X）
        if (hasText(a.getIdCard()) && hasText(b.getIdCard())
                && a.getIdCard().trim().equalsIgnoreCase(b.getIdCard().trim())) {
            return ID_CARD.getCode();
        }
        // 2 姓名 + 性别 + 出生日期（三者都必须有值）
        if (nameEquals(a, b) && a.getGender() != null && a.getGender().equals(b.getGender())
                && a.getBirthDate() != null && a.getBirthDate().equals(b.getBirthDate())) {
            return NAME_GENDER_BIRTH.getCode();
        }
        // 3 姓名 + 手机号
        if (nameEquals(a, b) && hasText(a.getPhone()) && hasText(b.getPhone())
                && a.getPhone().trim().equals(b.getPhone().trim())) {
            return NAME_PHONE.getCode();
        }
        return MANUAL.getCode();
    }

    /** 命中依据的字段值快照（写进合并审计，事后能一眼看出当初凭什么合的） */
    public static String matchSnapshot(Integer level, BizPatient a, BizPatient b) {
        if (level == null) {
            return null;
        }
        PatientMatchLevelEnum item = fromCode(level);
        if (item == null) {
            return "patient_name=" + trim(a.getPatientName()) + "（仅同名，无其余字段命中）";
        }
        return switch (item) {
            case ID_CARD -> "id_card=" + trim(a.getIdCard());
            case NAME_GENDER_BIRTH -> "patient_name=" + trim(a.getPatientName())
                    + "; gender=" + a.getGender()
                    + "; birth_date=" + (a.getBirthDate() == null ? "" : a.getBirthDate());
            case NAME_PHONE -> "patient_name=" + trim(a.getPatientName())
                    + "; phone=" + trim(a.getPhone());
            case MANUAL -> "patient_name=" + trim(a.getPatientName()) + "（仅同名，无其余字段命中）";
        };
    }

    /**
     * 用于把多份档案聚成一组的 key。同 key 即"值得放到一起看"。
     *
     * <p><b>关键字段为空一律返回 null（不成组）</b>。这一条不是洁癖 —— 不做的话后果很重：
     * 未填身份证的档案会全部落进 {@code "IDC:"} 这同一个 key，于是"医嘱验证患者甲 + 老王 +
     * 两个同名同生日患者"会被显示成**一个重复组**（实测踩到过：一个组里塞了 7 个毫不相干的人）。
     * 组是按 key 分的，所以绝不能给空值一个共享的 key。
     */
    public static String groupKey(Integer level, BizPatient p) {
        if (level == null || p == null) {
            return null;
        }
        PatientMatchLevelEnum item = fromCode(level);
        if (item == null) {
            return hasText(p.getPatientName()) ? "NAME:" + trim(p.getPatientName()) : null;
        }
        return switch (item) {
            case ID_CARD -> hasText(p.getIdCard())
                    ? "IDC:" + trim(p.getIdCard()).toUpperCase() : null;
            case NAME_GENDER_BIRTH -> (hasText(p.getPatientName())
                    && p.getGender() != null && p.getBirthDate() != null)
                    ? "NGB:" + trim(p.getPatientName()) + "|" + p.getGender() + "|" + p.getBirthDate() : null;
            case NAME_PHONE -> (hasText(p.getPatientName()) && hasText(p.getPhone()))
                    ? "NP:" + trim(p.getPatientName()) + "|" + trim(p.getPhone()) : null;
            case MANUAL -> hasText(p.getPatientName()) ? "NAME:" + trim(p.getPatientName()) : null;
        };
    }

    /** 交叉命中时取更强的那条作为整组的级别（同一对档案可能同时命中 2 和 3） */
    public static Integer stronger(Integer x, Integer y) {
        if (x == null) {
            return y;
        }
        if (y == null) {
            return x;
        }
        return Math.min(x, y);
    }

    private static boolean nameEquals(BizPatient a, BizPatient b) {
        return hasText(a.getPatientName()) && hasText(b.getPatientName())
                && a.getPatientName().trim().equals(b.getPatientName().trim());
    }

    private static boolean hasText(String s) {
        return StringUtils.hasText(s);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    /** 生日为空的档案不能进 L2 组（供上层提示"该档案缺出生日期"） */
    public static boolean birthDateMissing(BizPatient p) {
        LocalDate d = p == null ? null : p.getBirthDate();
        return d == null;
    }
}
