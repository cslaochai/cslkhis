package com.his.miniapp.support;

import com.his.miniapp.entity.SysFaq;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 常见问题检索：把患者的口语提问切成检索词，再对条目打分。
 *
 * <p><b>为什么不用数据库 LIKE 一条了事</b>：患者打的是「报告多久出来啊」，
 * 库里的问题写的是「报告多久能出来」，整串 LIKE 命中不了，
 * 而患者只会得出一个结论 —— 这个客服什么都搜不到。
 * 所以要把输入切成 2~4 字的片段分别去撞，撞中的词越多排越前。
 *
 * <p><b>为什么不给答案也加权</b>：答案里出现「报告」两个字的条目很多，
 * 按答案匹配会把一堆不相关的排上来。权重是 keywords(3) > question(2) > answer(1)，
 * 答案命中只作兜底。
 */
public final class FaqSearchSupport {

    /** 疑问词与虚词：切词时剔掉，否则「怎么」「可以」会命中几乎所有条目 */
    private static final Set<String> STOP = Set.of(
            "怎么", "怎样", "如何", "什么", "为什么", "可以", "能不能", "能否", "是否",
            "需要", "要不要", "多少", "几个", "哪里", "哪儿", "请问", "麻烦", "一下",
            "我的", "我要", "我想", "怎么办", "多久", "哪些", "这个", "那个", "之后", "以后");

    /** 单个检索词的最大长度（超过 4 字几乎撞不到东西） */
    private static final int MAX_TERM_LENGTH = 4;

    /** 检索词个数上限：患者贴一整段话进来时不能生成上百个词 */
    private static final int MAX_TERMS = 8;

    private FaqSearchSupport() {
    }

    /**
     * 输入 → 检索词列表。
     * <p>先按标点切短语，再对每个短语按 2~MAX_TERM_LENGTH 滑窗取词。
     */
    public static List<String> splitTerms(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        Set<String> terms = new LinkedHashSet<>();
        for (String phrase : keyword.trim().split("[\\s，。？！、；：,.;:?!/]+")) {
            if (!StringUtils.hasText(phrase)) {
                continue;
            }
            // 只处理中文片段：英文/数字串原样作为一个词
            for (String seg : phrase.split("[^一-龥]+")) {
                if (!StringUtils.hasText(seg)) {
                    continue;
                }
                if (seg.length() <= MAX_TERM_LENGTH) {
                    if (!STOP.contains(seg)) {
                        terms.add(seg);
                    }
                    continue;
                }
                for (int n = 2; n <= MAX_TERM_LENGTH; n++) {
                    for (int i = 0; i + n <= seg.length(); i++) {
                        String candidate = seg.substring(i, i + n);
                        if (!STOP.contains(candidate)) {
                            terms.add(candidate);
                        }
                    }
                }
            }
        }
        return new ArrayList<>(terms).stream().limit(MAX_TERMS).toList();
    }

    /**
     * 单条命中得分（0 = 没命中）。
     */
    public static int scoreOf(SysFaq faq, List<String> terms) {
        if (terms == null || terms.isEmpty()) {
            return 0;
        }
        String keywords = faq.getKeywords() == null ? "" : faq.getKeywords();
        String question = faq.getQuestion() == null ? "" : faq.getQuestion();
        String answer = faq.getAnswer() == null ? "" : faq.getAnswer();
        int score = 0;
        for (String term : terms) {
            if (keywords.contains(term)) {
                score += 3;
            } else if (question.contains(term)) {
                score += 2;
            } else if (answer.contains(term)) {
                score += 1;
            }
        }
        return score;
    }
}
