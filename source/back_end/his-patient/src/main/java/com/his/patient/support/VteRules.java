package com.his.patient.support;

import com.his.common.util.TextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.his.patient.enums.VteMeasureTypeEnum;

import java.util.Arrays;
import java.util.List;

/**
 * VTE 防控的口径常量（措施项 / 推荐矩阵 / 枚举文案）—— 前后端唯一事实源，
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VteRules {

    /**
     * 措施码：基础预防
     */
    public static final String CODE_BASIC = "BASIC";
    /**
     * 措施码：物理预防
     */
    public static final String CODE_PHYSICAL = "PHYSICAL";
    /**
     * 措施码：药物预防
     */
    public static final String CODE_DRUG = "DRUG";
    public static final List<Measure> MEASURES = List.of(
            new Measure(CODE_BASIC, VteMeasureTypeEnum.BASIC.getCode(), "基础预防", "健康教育、早期活动/踝泵运动、避免脱水、慎用止血药"),
            new Measure(CODE_PHYSICAL, VteMeasureTypeEnum.PHYSICAL.getCode(), "物理预防", "梯度压力袜（GCS）/间歇充气加压装置（IPC）/足底静脉泵（VFP）"),
            new Measure(CODE_DRUG, VteMeasureTypeEnum.DRUG.getCode(), "药物预防", "低分子肝素/普通肝素/利伐沙班等；有活动性出血等禁忌者禁用"));

    /**
     * 按风险等级推荐应落实的措施码。
     * 低危 → 基础；中危 → 基础 + 物理；高危/极高危 → 基础 + 物理 + 药物。
     */
    public static List<String> codesOf(Integer riskLevel) {
        if (riskLevel == null || riskLevel <= 1) {
            return List.of(CODE_BASIC);
        }
        if (riskLevel == 2) {
            return List.of(CODE_BASIC, CODE_PHYSICAL);
        }
        return List.of(CODE_BASIC, CODE_PHYSICAL, CODE_DRUG);
    }

    public static Measure measureOf(String code) {
        for (Measure m : MEASURES) {
            if (m.code().equals(code)) {
                return m;
            }
        }
        return null;
    }

    /**
     * 中高危 = 中风险及以上（Caprini ≥3 分）
     */
    public static boolean isHighRisk(Integer riskLevel) {
        return riskLevel != null && riskLevel >= 2;
    }

    public static String measureCodeText(String code) {
        Measure m = measureOf(code);
        return m == null ? "" : m.name();
    }

    /**
     * 逗号分隔的码串是否在合法集合内
     */
    public static boolean allCodesValid(List<String> codes) {
        return codes != null && !codes.isEmpty()
                && codes.stream().allMatch(c -> measureOf(c) != null);
    }

    /**
     * 解析前端传入的措施码串（空返回空列表）
     */
    public static List<String> parseCodes(String raw) {
        if (!TextUtil.hasText(raw)) {
            return List.of();
        }
        return Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    /**
     * 措施项定义（顺序即页面展示顺序）
     */
    public static record Measure(String code, int type, String name, String desc) {
    }
}
