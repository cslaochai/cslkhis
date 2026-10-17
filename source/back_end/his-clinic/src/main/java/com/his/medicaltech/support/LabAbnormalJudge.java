package com.his.medicaltech.support;

import com.his.common.enums.LabAbnormalFlagEnum;
import com.his.common.util.TextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 检验结果异常判定（纯函数，无外部依赖）。
 */
@NoArgsConstructor
public final class LabAbnormalJudge {

    /**
     * 未判定留痕的固定前缀。
     * <p>
     * <b>为什么需要这个常量：</b> 「未判定」在异常标志里没有独立取值
     * （词表只有 0-正常/1-偏高/2-偏低/3-异常），所以库里未判定与正常都是 0。
     * 如果只靠标志位，读取层就会把「不知道」渲染成「正常」——同一个坑换了个位置。
     * 判定依据全部写在判定留痕里，用这个前缀区分两种语义：
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
            return new Verdict(LabAbnormalFlagEnum.NORMAL.getCode(), "", true, null);
        }
        return new Verdict(LabAbnormalFlagEnum.ABNORMAL.getCode(),
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
                return new Verdict(LabAbnormalFlagEnum.LOW.getCode(),
                        String.format("低于参考范围（%s）", range.describe()), true, null);
            }
        }
        if (upper != null) {
            boolean above = range.upperInclusive() ? value > upper + EPSILON : value >= upper - EPSILON;
            if (above) {
                return new Verdict(LabAbnormalFlagEnum.HIGH.getCode(),
                        String.format("高于参考范围（%s）", range.describe()), true, null);
            }
        }
        return new Verdict(LabAbnormalFlagEnum.NORMAL.getCode(), "", true, null);
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
     * <b>这是本类唯一的取文案入口：</b> 未判定时标志位也是 0，只传标志位会把「未判定」显示成「正常」。
     *
     * @param flag      异常标志
     * @param judgeNote 判定留痕，可为 null（历史数据没有留痕）
     */
    public static String getText(Integer flag, String judgeNote) {
        return isUnjudged(judgeNote) ? "未判定" : LabAbnormalFlagEnum.getText(flag);
    }

    /**
     * 判定结论
     */
    public record Verdict(int flag, String description, boolean judged, String note) {

        public static Verdict notJudged(String reason) {
            return new Verdict(LabAbnormalFlagEnum.NORMAL.getCode(), "", false, NOTE_UNJUDGED_PREFIX + reason);
        }
    }
}
