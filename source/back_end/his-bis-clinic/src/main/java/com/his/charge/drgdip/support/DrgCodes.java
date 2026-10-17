package com.his.charge.drgdip.support;

import com.his.common.util.TextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 分组规则用的 ICD 码归一（集合表与病案首页两侧比码前的唯一口径）。
 */
@NoArgsConstructor
public final class DrgCodes {

    /**
     * 归一：去空白、去点、转大写；空值出空串（空串永不等任何码，等于「这个维度没有值」）
     */
    public static String norm(String code) {
        if (!TextUtil.hasText(code)) {
            return "";
        }
        return code.trim().toUpperCase().replace(".", "").replace(" ", "");
    }

    /**
     * 归一并去重保序（首页同名诊断重复录入时不重复参与判定）
     */
    public static List<String> normList(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        Set<String> kept = new LinkedHashSet<>();
        for (String code : codes) {
            String norm = norm(code);
            if (!norm.isEmpty()) {
                kept.add(norm);
            }
        }
        return kept.isEmpty() ? List.of() : List.copyOf(new ArrayList<>(kept));
    }

    /**
     * 逗号拼接的编码串（明细用聚合函数一次带回的形状）拆回归一码列表；空串出空列表
     */
    public static List<String> split(String joined) {
        if (!TextUtil.hasText(joined)) {
            return List.of();
        }
        return normList(Arrays.asList(joined.split(",")));
    }
}
