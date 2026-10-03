package com.his.operation.support;

/**
 * 手术申请单枚举文案（状态 / 手术级别 / 切口等级 / 麻醉方式 / 急诊标志）。
 *
 * <p><b>铁律：未知码值一律渲染成「未知(码值)」，绝不回落成某个合法值。</b>
 * 状态回落成"已完成"、切口等级回落成"Ⅰ类"，等于把没核实的事记成核实了 ——
 * 和检验「未判定 ≠ 正常」、病历「未知文书类型」是同一条原则。
 *
 * <p>麻醉方式码刻意与 {@code LabAbnormalJudge} 之外**保持独立**：麻醉方式属于手术侧口径
 * （1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他），不要图省事复用"给药途径"之类的码表。
 */
public final class OperationApplyLabels {

    private OperationApplyLabels() {
    }

    // 状态码（与 sql/38 的注释、前端筛选值必须逐一对齐）

    public static final int ST_PENDING_SCHEDULE = 0;
    public static final int ST_SCHEDULED = 1;
    public static final int ST_PREOP_CHECKED = 2;
    public static final int ST_FINISHED = 3;
    public static final int ST_CANCELLED = 4;

    /** 状态文案：0-待排期 1-已排期 2-术前核对完成 3-已完成 4-已取消 */
    public static String statusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case ST_PENDING_SCHEDULE -> "待排期";
            case ST_SCHEDULED -> "已排期";
            case ST_PREOP_CHECKED -> "术前核对完成";
            case ST_FINISHED -> "已完成";
            case ST_CANCELLED -> "已取消";
            default -> "未知(" + code + ")";
        };
    }

    /** 是否处于"在途"（会占手术间时段、算未完成数） */
    public static boolean isActive(Integer status) {
        return status != null
                && (status == ST_SCHEDULED || status == ST_PREOP_CHECKED || status == ST_FINISHED);
    }

    /** 是否"未完成"（待排期 / 已排期 / 术前核对完成）—— 工作台角标用 */
    public static boolean isUnfinished(Integer status) {
        return status != null
                && (status == ST_PENDING_SCHEDULE || status == ST_SCHEDULED || status == ST_PREOP_CHECKED);
    }

    /** 手术级别：1-一级 2-二级 3-三级 4-四级（四级 = 风险最高、最难，不是"第四台"） */
    public static String levelText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "一级";
            case 2 -> "二级";
            case 3 -> "三级";
            case 4 -> "四级";
            default -> "未知(" + code + ")";
        };
    }

    public static boolean isValidLevel(Integer code) {
        return code != null && code >= 1 && code <= 4;
    }

    /**
     * 切口等级：0-0类 1-Ⅰ类 2-Ⅱ类 3-Ⅲ类。
     * 注意是 <b>0~3</b> 而不是 1~4 —— 0 类（如经自然腔道）是合法值，
     * 用"非空即合法"或"1~3"去校验会把 0 类手术判成非法。
     */
    public static String incisionText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 0 -> "0类";
            case 1 -> "Ⅰ类";
            case 2 -> "Ⅱ类";
            case 3 -> "Ⅲ类";
            default -> "未知(" + code + ")";
        };
    }

    public static boolean isValidIncision(Integer code) {
        return code != null && code >= 0 && code <= 3;
    }

    /** 麻醉方式：1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他 */
    public static String anesthesiaText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "全身麻醉";
            case 2 -> "椎管内麻醉";
            case 3 -> "神经阻滞麻醉";
            case 4 -> "局部麻醉";
            case 5 -> "其他";
            default -> "未知(" + code + ")";
        };
    }

    public static boolean isValidAnesthesia(Integer code) {
        return code != null && code >= 1 && code <= 5;
    }

    /** 是否急诊手术文案：0-否 1-是 */
    public static String emergencyText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 0 -> "择期";
            case 1 -> "急诊";
            default -> "未知(" + code + ")";
        };
    }

    /** 时长文案（用于"手术时长 xx 分钟"） */
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
}
