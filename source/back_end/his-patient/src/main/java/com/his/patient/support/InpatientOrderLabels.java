package com.his.patient.support;

/**
 * 住院<b>医嘱</b>枚举文案（注意与 {@link InpatientLabels} 区分：那个是「住院证」的文案）。
 *
 * <p><b>铁律：未知码值一律渲染成「未知(码值)」，绝不回落成某个合法值。</b>
 * 医嘱状态回落成"已完成"、执行状态回落成"已执行"，等于把没做的事记成做了，
 * 直接污染"医嘱-收费-病历"四核对（同检验「未判定 ≠ 正常」是同一个坑）。
 */
public final class InpatientOrderLabels {

    private InpatientOrderLabels() {
    }

    /**
     * 医嘱类型：1-长期 2-临时
     * <p>两者的生命周期完全不同：长期医嘱只能"停止"（已执行次数不可回退），临时医嘱执行一次即完结。
     */
    public static String orderTypeText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "长期";
            case 2 -> "临时";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 医嘱模板/组套共享范围：1-个人 2-科室 3-全院（sql/142）
     * <p>决定「谁能看见、谁能改」：个人只有本人，科室是本 dept 内，全院所有人可见。
     */
    public static String templateScopeText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "个人";
            case 2 -> "科室";
            case 3 -> "全院";
            default -> "未知(" + code + ")";
        };
    }

    /** 医嘱类别：1-药品 2-检查 3-检验 4-治疗 5-护理 6-手术 7-输血 8-监护 9-其他 10-临床营养 */
    public static String orderClassText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "药品";
            case 2 -> "检查";
            case 3 -> "检验";
            case 4 -> "治疗";
            case 5 -> "护理";
            case 6 -> "手术";
            case 7 -> "输血";
            case 8 -> "监护";
            case 9 -> "其他";
            case 10 -> "临床营养";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 医嘱状态：1-待校对 2-已校对 3-执行中 4-已完成 5-已停止 6-已作废 7-已退回
     * <p>7-已退回本期只保留文案映射（护士退回流程属 P2 护理工作站），保留它是为了给出明确的渲染出口。
     */
    public static String orderStatusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "待校对";
            case 2 -> "已校对";
            case 3 -> "执行中";
            case 4 -> "已完成";
            case 5 -> "已停止";
            case 6 -> "已作废";
            case 7 -> "已退回";
            default -> "未知(" + code + ")";
        };
    }

    /** 执行状态：1-待执行 2-已执行 3-已跳过 4-已退回 */
    public static String execStatusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "待执行";
            case 2 -> "已执行";
            case 3 -> "已跳过";
            case 4 -> "已退回";
            default -> "未知(" + code + ")";
        };
    }

    /** 医嘱来源：1-医生 2-模板 3-组套 */
    public static String sourceText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "医生";
            case 2 -> "模板";
            case 3 -> "组套";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 是否加急：0-否 1-是
     * <p>这是执行队列的**第一排序键**，文案错了会直接影响护士先做谁。
     */
    public static String isUrgentText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 0 -> "普通";
            case 1 -> "加急";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 医嘱类别 → 记账项目类型（`费用记账流水的项目类型`）。
     *
     * <p><b>这是一处既有限制，不是设计选择</b>：本项目 `item_type` 只有
     * 「1挂号费 2西药 3中成药 4中药饮片 5检查 6检验 7治疗」七类，
     * <b>没有护理/床位/手术/输血/监护/临床营养类目</b>，所以 5~10 一律归到 7（治疗）。
     * 后果：靠 `item_type` 区分不了"护理费"和"治疗费"。真实 HIS 会有完整的收费项目类别字典，
     * 本项目要到补价表/项目类别字典（P3 日清单范围）时才能细化。
     */
    public static int chargeItemTypeOf(Integer orderClass) {
        if (orderClass == null) {
            return 7;
        }
        return switch (orderClass) {
            case 1 -> 2;
            case 2 -> 5;
            case 3 -> 6;
            default -> 7;
        };
    }

    /**
     * 判断医嘱类别是否为「药品」（药品才受用药安全 CDSS 约束，P6 接）
     */
    public static boolean isDrug(Integer orderClass) {
        return orderClass != null && orderClass == 1;
    }
}
