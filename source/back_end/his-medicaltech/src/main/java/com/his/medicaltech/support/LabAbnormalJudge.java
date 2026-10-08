package com.his.medicaltech.support;

import com.his.common.util.TextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 检验结果异常判定（纯函数，无外部依赖）。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LabAbnormalJudge {

    /**
     * 正常
     */
    public static final int NORMAL = 0;

    /**
     * 偏高
     */
    public static final int HIGH = 1;

    /**
     * 偏低
     */
    public static final int LOW = 2;

    /**
     * 异常（定性结果不符，无法区分高低）
     */
    public static final int ABNORMAL = 3;

    /**
     * 未判定留痕的固定前缀。
     * <p>
     * <b>为什么需要这个常量：</b> 「未判定」在 {@code abnormal_flag} 里没有独立取值
     * （列注释只有 0-正常/1-偏高/2-偏低/3-异常），所以 DB 里未判定与正常都是 0。
     * 如果只靠 flag，读取层就会把「不知道」渲染成「正常」——同一个坑换了个位置。
     * 判定依据全部写在 {@code judge_note} 里，用这个前缀区分两种语义：
     * 以它开头 = 没有结论，否则 = 有结论。
     */
    public static final String NOTE_UNJUDGED_PREFIX = "未判定：";

    private static final double EPSILON = 1e-9;

    public static Verdict judge(String resultValue, LabReferenceRange range) {
        if (range == null || !range.usable()) {
            String raw = range == null ? "" : range.describe();
            return Verdict.notJudged(TextUtil.hasText(raw)
                    ? "参考区间无法解析（" + raw + "）"
                    : "参考区间缺失");
        }
        if (!TextUtil.hasText(resultValue)) {
            return Verdict.notJudged("结果值为空");
        }

        if (range.isQualitative()) {
            return judgeQualitative(resultValue, range);
        }
        return judgeQuantitative(resultValue, range);
    }

    private static Verdict judgeQualitative(String resultValue, LabReferenceRange range) {
        String actual = LabReferenceRangeParser.normalizeQualitative(resultValue);
        Set<String> acceptable = range.acceptableQualitative();
        if (acceptable.contains(actual)) {
            return new Verdict(NORMAL, "", true, null);
        }
        return new Verdict(ABNORMAL,
                String.format("结果「%s」与参考「%s」不符", actual, range.describe()),
                true, null);
    }

    private static Verdict judgeQuantitative(String resultValue, LabReferenceRange range) {
        Double value = LabReferenceRangeParser.firstNumber(resultValue);
        if (value == null) {
            return Verdict.notJudged("结果值非数值");
        }

        Double lower = range.lower();
        Double upper = range.upper();

        if (lower != null) {
            boolean below = range.lowerInclusive() ? value < lower - EPSILON : value <= lower + EPSILON;
            if (below) {
                return new Verdict(LOW,
                        String.format("低于参考范围（%s）", range.describe()), true, null);
            }
        }
        if (upper != null) {
            boolean above = range.upperInclusive() ? value > upper + EPSILON : value >= upper - EPSILON;
            if (above) {
                return new Verdict(HIGH,
                        String.format("高于参考范围（%s）", range.describe()), true, null);
            }
        }
        return new Verdict(NORMAL, "", true, null);
    }

    /**
     * 枚举名，用于日志与前端展示
     */
    public static String getText(Integer flag) {
        if (flag == null) {
            return "未知";
        }
        return switch (flag) {
            case NORMAL -> "正常";
            case HIGH -> "偏高";
            case LOW -> "偏低";
            case ABNORMAL -> "异常";
            default -> "未知";
        };
    }

    /**
     * 判断这条留痕是不是「没有结论」。
     *
     * @param judgeNote 判定留痕
     */
    public static boolean isUnjudged(String judgeNote) {
        return judgeNote != null && judgeNote.startsWith(NOTE_UNJUDGED_PREFIX);
    }

    /**
     * 带留痕的展示文案。
     * <p>
     * <b>必须用这个重载而不是 {@link #getText(Integer)}：</b> 未判定时
     * {@code abnormal_flag} 也是 0，只传 flag 会把「未判定」显示成「正常」。
     *
     * @param flag      异常标志
     * @param judgeNote 判定留痕，可为 null（历史数据没有留痕）
     */
    public static String getText(Integer flag, String judgeNote) {
        return isUnjudged(judgeNote) ? "未判定" : getText(flag);
    }

    /**
     * 判定结论
     */
    public record Verdict(int flag, String description, boolean judged, String note) {

        public static Verdict notJudged(String reason) {
            return new Verdict(NORMAL, "", false, NOTE_UNJUDGED_PREFIX + reason);
        }
    }
}
