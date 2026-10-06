package com.his.system.support;

import com.his.common.exception.BusinessException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 成分对的归一化键
 * 「华法林 × 阿司匹林」和「阿司匹林 × 华法林」是同一条知识，书写顺序不该由录入者决定，
 * 所以入库前把两个关键字按<b>二进制序</b>排序后用 {@code &} 连接，唯一键建在这个键上。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DrugComponentPair {

    private static final String SEP = "&";

    /**
     * 归一化：去空白、同成分拒绝、按二进制序排定后拼接
     */
    public static String buildKey(String componentA, String componentB) {
        String a = normalize(componentA, "成分A");
        String b = normalize(componentB, "成分B");
        if (a.equals(b)) {
            throw new BusinessException("相互作用是同两种成分之间的事，两个成分不能相同");
        }
        return a.compareTo(b) <= 0 ? a + SEP + b : b + SEP + a;
    }

    /**
     * 键拆回两个成分（[a, b]），解析失败返回空列表
     */
    public static List<String> parts(String pairKey) {
        if (!StringUtils.hasText(pairKey) || !pairKey.contains(SEP)) {
            return List.of();
        }
        String[] split = pairKey.split(SEP);
        return split.length == 2 ? List.of(split[0], split[1]) : List.of();
    }

    private static String normalize(String value, String label) {
        String trimmed = safeTrim(value);
        // C 类保留：入站侧 DTO 已挂 @NotBlank，这里是归一化工具被内部调用时的兜底（注解跑不到这条路上）
        if (!StringUtils.hasText(trimmed)) {
            throw new BusinessException(label + "不能为空");
        }
        if (trimmed.contains(SEP)) {
            throw new BusinessException(label + "不能包含「" + SEP + "」，它会破坏成分对的唯一键");
        }
        return trimmed;
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
