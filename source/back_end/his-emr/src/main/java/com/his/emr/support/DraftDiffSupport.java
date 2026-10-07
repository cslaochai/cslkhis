package com.his.emr.support;

import com.his.common.util.TextUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 草稿 → 终稿字符级差异分段（LCS）。
 *
 * <p>产出的分段 JSON 供 AI 管理台渲染（红删绿增），也是后续把「修改行为」
 * 换算成 SFT 监督信号的原料。字符级对中文病历足够：医生改的是措辞和数字，
 * 不存在英文单词那种分词需求。
 */
@Component
public class DraftDiffSupport {

    /**
     * 单侧文本入库上限：草稿/终稿超长截断，避免 TEXT 列被整段病历撑爆
     */
    public static final int TEXT_MAX = 2000;

    /**
     * 分段 JSON 文本总长上限（MEDIUMTEXT 内留足余量，超长时末段截断）
     */
    private static final int DIFF_JSON_MAX = 8000;

    /**
     * LCS 动规上限：超过该长度按「全删全增」一笔带过（2000×2000 的表约 4MB，可接受）
     */
    private static final int LCS_LIMIT = 2000;

    private static void append(List<Segment> segments, int type, char c) {
        if (!segments.isEmpty() && segments.get(segments.size() - 1).type == type) {
            Segment last = segments.get(segments.size() - 1);
            last.text += c;
        } else {
            segments.add(new Segment(type, String.valueOf(c)));
        }
    }

    private static String escape(String text) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * 计算差异分段。
     *
     * @return 段列表，type：0-相同 1-删（草稿有终稿无） 2-增（终稿有草稿无）
     */
    public List<Segment> diff(String draft, String finalText) {
        List<Segment> segments = new ArrayList<>();
        String left = TextUtil.cut(draft, TEXT_MAX, "");
        String right = TextUtil.cut(finalText, TEXT_MAX, "");
        if (left.equals(right)) {
            if (!left.isEmpty()) {
                segments.add(new Segment(0, left));
            }
            return segments;
        }
        if (left.length() > LCS_LIMIT || right.length() > LCS_LIMIT) {
            if (!left.isEmpty()) {
                segments.add(new Segment(1, left));
            }
            if (!right.isEmpty()) {
                segments.add(new Segment(2, right));
            }
            return segments;
        }

        int n = left.length();
        int m = right.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                dp[i][j] = left.charAt(i) == right.charAt(j)
                        ? dp[i + 1][j + 1] + 1
                        : Math.max(dp[i + 1][j], dp[i][j + 1]);
            }
        }
        int i = 0;
        int j = 0;
        while (i < n && j < m) {
            if (left.charAt(i) == right.charAt(j)) {
                append(segments, 0, left.charAt(i));
                i++;
                j++;
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                append(segments, 1, left.charAt(i));
                i++;
            } else {
                append(segments, 2, right.charAt(j));
                j++;
            }
        }
        while (i < n) {
            append(segments, 1, left.charAt(i));
            i++;
        }
        while (j < m) {
            append(segments, 2, right.charAt(j));
            j++;
        }
        return segments;
    }

    /**
     * 相邻同类段合并，压掉 LCS 逐字符输出的碎片（数千段 JSON 会把管理台拖垮）。
     * 合并后总长仍超 DIFF_JSON_MAX 时丢弃尾部段 —— 留痕以头部差异为准，不能因超长整行失败。
     */
    public String toJson(List<Segment> segments) {
        List<Segment> merged = new ArrayList<>();
        for (Segment segment : segments) {
            if (!merged.isEmpty() && merged.get(merged.size() - 1).type == segment.type) {
                Segment last = merged.get(merged.size() - 1);
                last.text += segment.text;
            } else {
                merged.add(new Segment(segment.type, segment.text));
            }
        }
        StringBuilder builder = new StringBuilder("[");
        int used = 1;
        for (Segment segment : merged) {
            String cell = "{\"type\":" + segment.type
                    + ",\"text\":\"" + escape(segment.text) + "\"}";
            if (used + cell.length() + 1 > DIFF_JSON_MAX) {
                break;
            }
            if (used > 1) {
                builder.append(',');
                used++;
            }
            builder.append(cell);
            used += cell.length();
        }
        builder.append(']');
        return builder.toString();
    }

    public static class Segment {
        public final int type;
        public String text;

        public Segment(int type, String text) {
            this.type = type;
            this.text = text;
        }

        public int getType() {
            return type;
        }

        public String getText() {
            return text;
        }
    }
}
