package com.his.medicaltech.support;

import com.his.common.enums.SysGenderEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参考区间解析器：把人工维护的自由文本区间解析为 {@link LabReferenceRange}。
 * <p>
 * <b>设计原则：宁可不判定，不可猜。</b>
 * 解析失败一律返回 {@link LabReferenceRange.Kind#UNPARSABLE}。
 * 一个「猜出来的」参考区间比没有区间危险得多 —— 它会让系统自信地报出错误结论，
 * 而且不会留下任何异常痕迹（详见 {@link LabAbnormalJudge} 类注释里的同款教训）。
 * <p>
 * 另一个刻意的行为：区间带性别分支（男120-160/女110-150）而患者性别未知时，
 * 同样返回 UNPARSABLE。<b>取男取女都是错的</b>，血红蛋白按男性标准判女性患者的贫血
 * 是会漏诊的。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LabReferenceRangeParser {

    private static final Pattern FIRST_NUMBER = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?");

    /**
     * 定性结果的取值词表。只认整串匹配，避免把 Rh阳性误判成「阳性」。
     */
    private static final Set<String> QUALITATIVE_VOCAB = Set.of(
            "阴性", "阳性", "弱阳性", "正常", "相合", "未见异常", "未检出", "无",
            "negative", "neg", "positive", "pos", "normal");

    /**
     * 简写符号到定性词的映射
     */
    private static final List<String[]> QUALITATIVE_SYMBOLS = List.of(
            new String[]{"-", "阴性"},
            new String[]{"±", "弱阳性"},
            new String[]{"+", "阳性"},
            new String[]{"++", "阳性"},
            new String[]{"+++", "阳性"});

    private static final char[] DASHES = {'-', '－', '—', '–', '~', '～'};

    /**
     * 解析参考区间。
     *
     * @param rawRange 原始区间文本，可为空
     * @param gender   患者性别（1-男 2-女），区间含性别分支时必填
     */
    public static LabReferenceRange parse(String rawRange, Integer gender) {
        String text = normalize(rawRange);
        if (!StringUtils.hasText(text)) {
            return LabReferenceRange.unparsable(rawRange);
        }

        // 1) 性别分支：必须先于其它规则，否则男/女前缀会被当成噪声
        if (text.indexOf('男') >= 0 || text.indexOf('女') >= 0) {
            return parseGendered(text, gender, rawRange);
        }

        // 2) 去掉「早晨:」「空腹:」这类时间/条件前缀
        int colon = text.lastIndexOf(':');
        if (colon >= 0 && colon < text.length() - 1) {
            text = text.substring(colon + 1);
        }

        return parseSingle(text, rawRange);
    }

    /**
     * 判断一段文本是否看起来是定性结果（供结果值归一使用）
     */
    public static String normalizeQualitative(String value) {
        String text = normalize(value);
        if (!StringUtils.hasText(text)) {
            return "";
        }
        for (String[] entry : QUALITATIVE_SYMBOLS) {
            if (entry[0].equals(text)) {
                return entry[1];
            }
        }
        return text;
    }

    /**
     * 取结果值里的第一个数字，取不到返回 null
     */
    public static Double firstNumber(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        Matcher matcher = FIRST_NUMBER.matcher(normalize(value));
        if (!matcher.find()) {
            return null;
        }
        try {
            return Double.parseDouble(matcher.group());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // 内部

    private static LabReferenceRange parseGendered(String text, Integer gender, String rawRange) {
        int maleIndex = text.indexOf('男');
        int femaleIndex = text.indexOf('女');

        String malePart = null;
        String femalePart = null;
        if (maleIndex >= 0 && femaleIndex > maleIndex) {
            malePart = text.substring(maleIndex + 1, femaleIndex);
            femalePart = text.substring(femaleIndex + 1);
        } else if (femaleIndex >= 0 && maleIndex > femaleIndex) {
            femalePart = text.substring(femaleIndex + 1, maleIndex);
            malePart = text.substring(maleIndex + 1);
        }

        if (!StringUtils.hasText(malePart) && !StringUtils.hasText(femalePart)) {
            // 只有「根据性别及生理期不同」这类描述，没有实际数值
            return LabReferenceRange.unparsable(rawRange);
        }

        // 性别未知时不做任何猜测：取男取女都可能漏诊
        SysGenderEnum g = SysGenderEnum.fromCode(gender);
        if (g == null) {
            return LabReferenceRange.unparsable(rawRange);
        }
        String chosen = g == SysGenderEnum.MALE ? malePart : femalePart;
        if (!StringUtils.hasText(chosen)) {
            return LabReferenceRange.unparsable(rawRange);
        }
        chosen = trimSeparators(chosen);
        chosen = stripLeadingLabel(chosen);
        return parseSingle(chosen, rawRange);
    }

    private static LabReferenceRange parseSingle(String text, String rawRange) {
        String value = trimSeparators(stripUnitSuffix(text));
        if (!StringUtils.hasText(value)) {
            return LabReferenceRange.unparsable(rawRange);
        }

        // 定性
        LabReferenceRange qualitative = tryQualitative(value, rawRange);
        if (qualitative != null) {
            return qualitative;
        }

        // 单侧：< / ≤ / > / ≥
        LabReferenceRange oneSided = tryOneSided(value, rawRange);
        if (oneSided != null) {
            return oneSided;
        }

        // 双侧区间：必须恰好两个数字，否则一律判为不可解析
        String dashed = replaceDashes(value);
        String[] parts = dashed.split("-", -1);
        if (parts.length == 2) {
            Double lower = parseNumber(parts[0]);
            Double upper = parseNumber(parts[1]);
            if (lower != null && upper != null && lower <= upper) {
                return LabReferenceRange.range(lower, upper, true, true, rawRange);
            }
        }
        return LabReferenceRange.unparsable(rawRange);
    }

    private static LabReferenceRange tryQualitative(String value, String rawRange) {
        for (String[] entry : QUALITATIVE_SYMBOLS) {
            if (entry[0].equals(value)) {
                return LabReferenceRange.qualitative(Set.of(entry[1]), rawRange);
            }
        }
        // 「阴性/阳性」这一类：每一段都必须是已知定性词，否则整体判为不可解析
        if (value.indexOf('/') > 0) {
            Set<String> acceptable = new LinkedHashSet<>();
            boolean allKnown = true;
            for (String token : value.split("/")) {
                String normalized = normalizeQualitative(token);
                if (!QUALITATIVE_VOCAB.contains(normalized)) {
                    allKnown = false;
                    break;
                }
                acceptable.add(normalized);
            }
            if (allKnown && !acceptable.isEmpty()) {
                return LabReferenceRange.qualitative(acceptable, rawRange);
            }
            return null;
        }
        String normalized = normalizeQualitative(value);
        if (QUALITATIVE_VOCAB.contains(normalized)) {
            return LabReferenceRange.qualitative(Set.of(normalized), rawRange);
        }
        return null;
    }

    private static LabReferenceRange tryOneSided(String value, String rawRange) {
        String body;
        boolean inclusive;
        if (value.startsWith("<") || value.startsWith("≤")) {
            inclusive = value.startsWith("≤");
            body = value.substring(1);
            Double upper = parseNumber(body);
            return upper == null ? null : LabReferenceRange.upperOnly(upper, inclusive, rawRange);
        }
        if (value.startsWith(">") || value.startsWith("≥")) {
            inclusive = value.startsWith("≥");
            body = value.substring(1);
            Double lower = parseNumber(body);
            return lower == null ? null : LabReferenceRange.lowerOnly(lower, inclusive, rawRange);
        }
        // <= 与 >= 已被 normalize 转成 ≤ 与 ≥
        return null;
    }

    private static Double parseNumber(String text) {
        String value = trimSeparators(text);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 去掉 {@code /HP}、{@code /ul} 这类非数字后缀（0-5/HP → 0-5）。
     * 只剥掉「斜杠 + 纯字母」且前面是纯数字/小数点/短横的情况，
     * 避免误伤「阴性/阳性」与「A/B/AB/O型」。
     */
    private static String stripUnitSuffix(String text) {
        int slash = text.indexOf('/');
        if (slash <= 0) {
            return text;
        }
        String tail = text.substring(slash + 1);
        String head = text.substring(0, slash);
        if (tail.matches("[A-Za-z]+") && head.matches("[\\d.\\-]+")) {
            return head;
        }
        return text;
    }

    private static String stripLeadingLabel(String text) {
        int colon = text.lastIndexOf(':');
        if (colon >= 0 && colon < text.length() - 1) {
            return text.substring(colon + 1);
        }
        return text;
    }

    private static String replaceDashes(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        for (char ch : text.toCharArray()) {
            boolean isDash = false;
            for (char dash : DASHES) {
                if (ch == dash) {
                    isDash = true;
                    break;
                }
            }
            builder.append(isDash ? '-' : ch);
        }
        return builder.toString();
    }

    private static String trimSeparators(String text) {
        String value = text;
        while (value.startsWith("/") || value.startsWith(",") || value.startsWith(";")
                || value.startsWith("、") || value.startsWith(" ")) {
            value = value.substring(1);
        }
        while (value.endsWith("/") || value.endsWith(",") || value.endsWith(";")
                || value.endsWith("、") || value.endsWith(" ")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    /**
     * 全角转半角 + 去掉空白。库里同时存在 {@code ４-１０} 与 {@code 4-10} 两种写法。
     */
    private static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder(raw.length());
        for (char ch : raw.toCharArray()) {
            if (ch == 0x3000) {
                continue;
            }
            if (ch >= 0xFF01 && ch <= 0xFF5E) {
                builder.append((char) (ch - 0xFEE0));
            } else if (!Character.isWhitespace(ch)) {
                builder.append(ch);
            }
        }
        String text = builder.toString();
        return text.replace("<=", "≤").replace(">=", "≥");
    }
}
