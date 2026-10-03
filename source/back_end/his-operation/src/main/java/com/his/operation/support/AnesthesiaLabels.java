package com.his.operation.support;

import java.math.BigDecimal;
import java.util.List;

/**
 * 手术麻醉链枚举文案（ASA / 气道 / 访视结论 / 记录状态 / PACU / 清点等）。
 *
 * <p><b>铁律：未知码值一律渲染成「未知(码值)」，绝不回落成某个合法值。</b>
 * 未知 ASA 显示成"Ⅱ级"、未知清点结果显示成"一致"，
 * 等于把没核实的事记成核实了 —— 与检验「未判定 ≠ 正常」是同一条原则。
 *
 * <p>这里刻意<b>不建字典</b>：这些码值是医生在麻醉单上逐项打分的临床口径，
 * 不是给人维护的字典；两边（后端 VO 文案 / 前端 lib/anesthesia.js）各自单点，
 * 与 G18 的 {@code QcTexts} 同款做法。
 */
public final class AnesthesiaLabels {

    private AnesthesiaLabels() {
    }

    // ASA 分级

    /** ASA 分级：1-Ⅰ 2-Ⅱ 3-Ⅲ 4-Ⅳ 5-Ⅴ（Ⅴ 级 = 濒死、不接受手术基本活不过 24 小时） */
    public static String asaText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "Ⅰ级";
            case 2 -> "Ⅱ级";
            case 3 -> "Ⅲ级";
            case 4 -> "Ⅳ级";
            case 5 -> "Ⅴ级";
            default -> "未知(" + code + ")";
        };
    }

    public static boolean isValidAsa(Integer code) {
        return code != null && code >= 1 && code <= 5;
    }

    /** ASA 分级 + 急诊 E 标志的完整展示（如「Ⅲ级 E（急诊）」） */
    public static String asaFullText(Integer grade, Integer emergency) {
        String base = asaText(grade);
        if (Integer.valueOf(1).equals(emergency)) {
            return base + " E（急诊）";
        }
        return base;
    }

    // 气道评估

    /** Mallampati 分级：1-Ⅰ 2-Ⅱ 3-Ⅲ 4-Ⅳ */
    public static String mallampatiText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "Ⅰ级（可见软腭/悬雍垂）";
            case 2 -> "Ⅱ级（可见软腭/咽峡弓）";
            case 3 -> "Ⅲ级（仅见软腭）";
            case 4 -> "Ⅳ级（仅见硬腭）";
            default -> "未知(" + code + ")";
        };
    }

    public static boolean isValidMallampati(Integer code) {
        return code != null && code >= 1 && code <= 4;
    }

    /** 颈部活动度：1-正常 2-受限 3-强直 */
    public static String neckMobilityText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "正常";
            case 2 -> "受限";
            case 3 -> "强直";
            default -> "未知(" + code + ")";
        };
    }

    /** 禁食禁饮：0-未禁食 1-已按要求禁食 2-急诊饱胃 */
    public static String npoText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 0 -> "未禁食";
            case 1 -> "已按要求禁食";
            case 2 -> "急诊饱胃（返流误吸高危）";
            default -> "未知(" + code + ")";
        };
    }

    public static boolean isValidNpo(Integer code) {
        return code != null && code >= 0 && code <= 2;
    }

    // 访视结论

    public static final int VISIT_CONCLUSION_OK = 1;
    public static final int VISIT_CONCLUSION_HOLD = 2;
    public static final int VISIT_CONCLUSION_CONSULT = 3;

    public static String visitConclusionText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "可施行麻醉";
            case 2 -> "暂缓手术";
            case 3 -> "需会诊/进一步评估";
            default -> "未知(" + code + ")";
        };
    }

    public static boolean isValidVisitConclusion(Integer code) {
        return code != null && code >= 1 && code <= 3;
    }

    /** 访视状态：0-草稿 1-已完成 */
    public static String visitStatusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 0 -> "草稿";
            case 1 -> "已完成";
            default -> "未知(" + code + ")";
        };
    }

    // 麻醉记录

    public static final int RECORD_DRAFT = 0;
    public static final int RECORD_SUBMITTED = 1;
    public static final int RECORD_AUDITED = 2;

    public static String recordStatusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 0 -> "记录中";
            case 1 -> "已提交";
            case 2 -> "已审核";
            default -> "未知(" + code + ")";
        };
    }

    /** 气道管理方式：0-无 1-气管插管 2-喉罩 3-面罩 4-其他 */
    public static String airwayDeviceText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 0 -> "无（保留自主呼吸）";
            case 1 -> "气管插管";
            case 2 -> "喉罩";
            case 3 -> "面罩";
            case 4 -> "其他";
            default -> "未知(" + code + ")";
        };
    }

    public static boolean isValidAirwayDevice(Integer code) {
        return code != null && code >= 0 && code <= 4;
    }

    /** 通气方式：1-自主呼吸 2-辅助通气 3-控制通气 */
    public static String ventilationText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "自主呼吸";
            case 2 -> "辅助通气";
            case 3 -> "控制通气";
            default -> "未知(" + code + ")";
        };
    }

    /** 麻醉效果：1-满意 2-欠佳 3-失败改麻醉方式 */
    public static String effectText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "满意";
            case 2 -> "欠佳";
            case 3 -> "失败改麻醉方式";
            default -> "未知(" + code + ")";
        };
    }

    /** 术后去向：1-回病房 2-入PACU 3-入ICU */
    public static String dispositionText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "回病房";
            case 2 -> "入PACU";
            case 3 -> "入ICU";
            default -> "未知(" + code + ")";
        };
    }

    public static boolean isValidDisposition(Integer code) {
        return code != null && code >= 1 && code <= 3;
    }

    // 用药

    /** 用药阶段：1-诱导 2-维持 3-苏醒 */
    public static String medPhaseText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "诱导";
            case 2 -> "维持";
            case 3 -> "苏醒";
            default -> "未知(" + code + ")";
        };
    }

    /** 给药途径：1-静脉推注 2-静脉泵注 3-静脉滴注 4-吸入 5-肌注 6-椎管内 7-局麻浸润 8-其他 */
    public static String medRouteText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "静脉推注";
            case 2 -> "静脉泵注";
            case 3 -> "静脉滴注";
            case 4 -> "吸入";
            case 5 -> "肌注";
            case 6 -> "椎管内";
            case 7 -> "局麻浸润";
            case 8 -> "其他";
            default -> "未知(" + code + ")";
        };
    }

    public static boolean isValidMedPhase(Integer code) {
        return code != null && code >= 1 && code <= 3;
    }

    // PACU

    public static final int PACU_IN = 0;
    public static final int PACU_OUT = 1;

    /** 出室 Aldrete 评分阈值（≥ 9 才允许按标准出室） */
    public static final int ALDRETE_DISCHARGE_MIN = 9;

    public static String pacuStatusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 0 -> "在室观察";
            case 1 -> "已出室";
            default -> "未知(" + code + ")";
        };
    }

    /** 清醒程度：1-完全清醒 2-嗜睡可唤醒 3-未清醒 */
    public static String awarenessText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "完全清醒";
            case 2 -> "嗜睡可唤醒";
            case 3 -> "未清醒";
            default -> "未知(" + code + ")";
        };
    }

    /** PACU 出室去向：1-回病房 2-转ICU 3-继续留观 */
    public static String pacuDispositionText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "回病房";
            case 2 -> "转ICU";
            case 3 -> "继续留观";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * Aldrete 五项逐项相加得总分。
     *
     * <p>任一项为 null 当作「未评分」整体返回 null —— 不能把缺项当成 0 分再算个总分出来，
     * 那样"意识没评"会变成"意识 0 分"，看着像评得很差，实际是根本没评。
     */
    public static Integer aldreteTotal(Integer activity, Integer respiration, Integer circulation,
                                       Integer consciousness, Integer spo2) {
        List<Integer> parts = List.of(activity, respiration, circulation, consciousness, spo2);
        int total = 0;
        for (Integer p : parts) {
            if (p == null) {
                return null;
            }
            if (p < 0 || p > 2) {
                throw new IllegalArgumentException("Aldrete 各项只能在 0~2 分之间，当前值=" + p);
            }
            total += p;
        }
        return total;
    }

    // 器械清点

    public static final int COUNT_PHASE_NONE = 0;
    public static final int COUNT_PHASE_BEFORE = 1;
    public static final int COUNT_PHASE_CLOSURE = 2;
    public static final int COUNT_PHASE_FINAL = 3;

    public static final int COUNT_STATUS_RUNNING = 0;
    public static final int COUNT_STATUS_DONE = 1;
    public static final int COUNT_STATUS_DIFF = 2;
    public static final int COUNT_STATUS_ABORT = 3;

    public static final int COUNT_RESULT_SAME = 1;
    public static final int COUNT_RESULT_DIFF = 2;

    public static String countPhaseText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 0 -> "未开始";
            case 1 -> "术前清点完成";
            case 2 -> "关体前清点完成";
            case 3 -> "关体后清点完成";
            default -> "未知(" + code + ")";
        };
    }

    public static String countStatusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 0 -> "清点中";
            case 1 -> "三轮一致，清点完成";
            case 2 -> "存在清点差异";
            case 3 -> "异常终止";
            default -> "未知(" + code + ")";
        };
    }

    /** 单阶段清点结果：1-一致 2-不一致 */
    public static String countResultText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "一致";
            case 2 -> "不一致";
            default -> "未知(" + code + ")";
        };
    }

    /** 清点明细类别：1-器械 2-敷料 3-缝针 4-刀片 5-其他 */
    public static String countCategoryText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "器械";
            case 2 -> "敷料";
            case 3 -> "缝针";
            case 4 -> "刀片";
            case 5 -> "其他";
            default -> "未知(" + code + ")";
        };
    }

    public static boolean isValidCountCategory(Integer code) {
        return code != null && code >= 1 && code <= 5;
    }

    // 计费

    public static final int CHARGE_PENDING = 0;
    public static final int CHARGE_DONE = 1;
    public static final int CHARGE_FAILED = 2;

    public static String chargeStatusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 0 -> "未计费";
            case 1 -> "已计费";
            case 2 -> "计费失败";
            default -> "未知(" + code + ")";
        };
    }

    /** 收费来源：1-麻醉记录 2-PACU复苏 3-手术（预留） */
    public static String chargeSourceText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "麻醉记录";
            case 2 -> "PACU复苏";
            case 3 -> "手术";
            default -> "未知(" + code + ")";
        };
    }

    // 通用

    /** 是否标志：0-否 1-是（"未知(9)"不能被当"否"，这是同一条铁律的另一处） */
    public static String yesNoText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 0 -> "否";
            case 1 -> "是";
            default -> "未知(" + code + ")";
        };
    }

    /** 失败原因列宽（手术麻醉计费明细.fail_reason / *.charge_fail_reason 都是 VARCHAR(500)） */
    private static final int FAIL_REASON_MAX = 480;

    /**
     * 失败原因入库前截到列宽。
     *
     * <p>这不是"为了好看"：计费失败原因里会带上数据库/远程调用的原始异常文本，
     * 一次 {@code Data too long} 就会把"记账失败"升级成 500 —— 结果是
     * **失败原因太长导致整条业务写不进去**，用户看到的是白屏而不是"这笔钱没计上"。
     * 记不下的部分丢掉，比整行写不进去强。
     */
    public static String clipReason(String reason) {
        if (reason == null) {
            return null;
        }
        return reason.length() <= FAIL_REASON_MAX ? reason : reason.substring(0, FAIL_REASON_MAX) + "…（已截断）";
    }

    /** 时长文案（分钟 → "1 小时 20 分钟"） */
    public static String durationText(Long minutes) {
        if (minutes == null) {
            return "—";
        }
        if (minutes < 60) {
            return minutes + " 分钟";
        }
        long h = minutes / 60;
        long m = minutes % 60;
        return m == 0 ? h + " 小时" : h + " 小时 " + m + " 分钟";
    }

    /**
     * 向上取整整小时（麻醉监护按小时计价，不足 1 小时按 1 小时）。
     *
     * <p>0 分钟不能折算成 0 小时然后收 0 元 —— 那等于"这台手术没有麻醉监护"，
     * 与"取不到单价就不计费"是同一件事的两面：**要么按事实计费，要么明确报失败**。
     */
    public static BigDecimal billHours(Long minutes) {
        if (minutes == null || minutes <= 0) {
            return BigDecimal.ONE;
        }
        long hours = (minutes + 59) / 60;
        return BigDecimal.valueOf(hours);
    }
}
