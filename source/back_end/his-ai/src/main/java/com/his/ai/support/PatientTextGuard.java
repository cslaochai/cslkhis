package com.his.ai.support;

import com.his.common.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 患者可见文案的硬闸。
 * <p>
 * <b>为什么需要它：</b>报告解读 / 费用解释的模型层只负责「把事实串成通顺的话」，
 * 但模型偶尔会越界补一句「提示缺铁性贫血」「建议服用铁剂」。
 * 这句话进了患者端就是<b>无医生背书的诊断与医嘱</b> —— 患者不会去找医生核实，他会直接信。
 * 词典侧有种子脚本的断言挡着，模型侧没有；这个闸就是模型侧的那道闸。
 * <p>
 * <b>它是硬规则，不是模型自评。</b>his-ai 的纪律是「只有硬规则能给出最高等级结论」，
 * 模型最大只能给到「关注」。这里连「关注」都不给：患者端根本不该出现分级处置，
 * 那是医生的事。所以命中即整体丢弃该条文案，回落到规则文案，并留日志。
 */
@Slf4j
@Component
public class PatientTextGuard {

    /**
     * 诊断性词汇：出现即认定越界。
     * 「贫血」「糖尿病」这类病名本身不是禁用词（词典里「空腹血糖偏高需要复查」是正当科普），
     * 真正要拦的是<b>把结论落到这个人身上</b>的表述。
     */
    private static final List<Pattern> DIAGNOSIS = List.of(
            Pattern.compile("确诊"),
            Pattern.compile("诊断[为是]"),
            Pattern.compile("你(可能|应该|估计)?(得|患|是)[了有]?[\\u4e00-\\u9fa5]{0,6}(病|炎|癌|症|瘤)"),
            Pattern.compile("您(可能|应该|估计)?(得|患|是)[了有]?[\\u4e00-\\u9fa5]{0,6}(病|炎|癌|症|瘤)"),
            Pattern.compile("患有|患病|罹患")
    );

    /**
     * 用药性词汇：任何涉及「怎么吃药」的表述。
     */
    private static final List<Pattern> MEDICATION = List.of(
            Pattern.compile("服用|口服|含服|外用|注射|输液|静滴|肌肉注射"),
            Pattern.compile("剂量|用量|每次\\d|一日\\d|一天\\d|每日\\d"),
            Pattern.compile("建议[你您]?(吃|用|买|服)"),
            Pattern.compile("开[一点一]?[药方]"),
            Pattern.compile("\\d+(\\.\\d+)?\\s*(mg|g|ml|ug|iu|片|粒|支|袋|瓶)", Pattern.CASE_INSENSITIVE)
    );

    /**
     * 绝对化表述：医疗科普里不存在「一定」。
     */
    private static final List<Pattern> ABSOLUTE = List.of(
            Pattern.compile("一定[会是要]"),
            Pattern.compile("必须|肯定会|百分之百"),
            Pattern.compile("不用[看去]医生|无需就[诊医]|不用管")
    );

    /**
     * 检查一段患者可见文案是否安全。
     *
     * @param text 待检文案（来自模型输出）
     * @return true-可展示；false-越界，调用方必须回落到规则文案
     */
    public boolean isSafe(String text) {
        if (!TextUtil.hasText(text)) {
            return false;
        }
        return firstHit(text) == null;
    }

    /**
     * 过闸：安全则返回原文，越界返回 null（调用方回落到规则文案）。
     *
     * @param text   待检文案
     * @param source 来源标记，只用于日志定位（如 capabilityKey）
     */
    public String guard(String text, String source) {
        if (!TextUtil.hasText(text)) {
            return null;
        }
        Pattern hit = firstHit(text);
        if (hit == null) {
            return text.trim();
        }
        // 留痕但不抛出：文案丢弃不是故障，是闸门生效。抛异常会让整个能力降级，反而丢了规则层事实。
        log.warn("[AI-患者文案闸] 拦截越界文案 source={} pattern={} text={}", source, hit.pattern(), text);
        return null;
    }

    private Pattern firstHit(String text) {
        for (Pattern pattern : DIAGNOSIS) {
            if (pattern.matcher(text).find()) {
                return pattern;
            }
        }
        for (Pattern pattern : MEDICATION) {
            if (pattern.matcher(text).find()) {
                return pattern;
            }
        }
        for (Pattern pattern : ABSOLUTE) {
            if (pattern.matcher(text).find()) {
                return pattern;
            }
        }
        return null;
    }
}
