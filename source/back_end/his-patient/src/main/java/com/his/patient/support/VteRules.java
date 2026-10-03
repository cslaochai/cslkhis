package com.his.patient.support;

import java.util.Arrays;
import java.util.List;

import com.his.patient.enums.VteMeasureTypeEnum;

/**
 * VTE 防控的口径常量（措施项 / 推荐矩阵 / 枚举文案）—— 前后端唯一事实源，
 * 前端 {@code src/lib/vte.js} 与之逐字对齐，改一边必须改另一边。
 *
 * <p><b>推荐矩阵为什么按风险等级分档</b>：Caprini 0~2 分的低危患者做药物预防是过度医疗
 * （出血风险大于血栓获益）；高危/极高危只做基础预防等于没防。分档是临床指南的硬要求，
 * 不是"可选勾选项"。
 */
public final class VteRules {

    /** 措施码：基础预防 */
    public static final String CODE_BASIC = "BASIC";
    /** 措施码：物理预防 */
    public static final String CODE_PHYSICAL = "PHYSICAL";
    /** 措施码：药物预防 */
    public static final String CODE_DRUG = "DRUG";

    private VteRules() {
    }

    /** 措施项定义（顺序即页面展示顺序） */
    public record Measure(String code, int type, String name, String desc) {
    }

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

    /** 中高危 = 中风险及以上（Caprini ≥3 分） */
    public static boolean isHighRisk(Integer riskLevel) {
        return riskLevel != null && riskLevel >= 2;
    }

    public static String measureCodeText(String code) {
        Measure m = measureOf(code);
        return m == null ? "未知(" + code + ")" : m.name();
    }

    public static String measureTypeText(Integer type) {
        return switch (type == null ? 0 : type) {
            case 1 -> "基础预防";
            case 2 -> "物理预防";
            case 3 -> "药物预防";
            default -> "未知(" + type + ")";
        };
    }

    public static String executeStatusText(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "待落实";
            case 1 -> "已落实";
            case 2 -> "禁忌未用";
            case 3 -> "患者拒绝";
            default -> "未知(" + status + ")";
        };
    }

    public static String eventTypeText(Integer type) {
        return switch (type == null ? 0 : type) {
            case 1 -> "深静脉血栓（DVT）";
            case 2 -> "肺栓塞（PE）";
            case 3 -> "预防相关出血";
            default -> "未知(" + type + ")";
        };
    }

    public static String onsetTypeText(Integer type) {
        return switch (type == null ? 0 : type) {
            case 1 -> "院内发生";
            case 2 -> "入院时已存在";
            default -> "未知(" + type + ")";
        };
    }

    public static String basisText(Integer basis) {
        return switch (basis == null ? 0 : basis) {
            case 1 -> "超声";
            case 2 -> "CT 肺动脉造影";
            case 3 -> "静脉造影";
            case 4 -> "临床诊断";
            case 5 -> "其他";
            default -> "未知(" + basis + ")";
        };
    }

    public static String outcomeText(Integer outcome) {
        return switch (outcome == null ? 0 : outcome) {
            case 1 -> "好转";
            case 2 -> "未愈";
            case 3 -> "死亡";
            case 4 -> "未知";
            default -> "未知(" + outcome + ")";
        };
    }

    /** 逗号分隔的码串是否在合法集合内 */
    public static boolean allCodesValid(List<String> codes) {
        return codes != null && !codes.isEmpty()
                && codes.stream().allMatch(c -> measureOf(c) != null);
    }

    /** 解析前端传入的措施码串（空返回空列表） */
    public static List<String> parseCodes(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
