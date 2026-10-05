package com.his.patient.support;

/**
 * 住院<b>医嘱</b>域纯计算 / 判定工具（注意与 {@link InpatientLabels} 区分：那个是「住院证」的校验）。
 *
 * <p><b>码值 → 文案的映射已下沉到对应枚举</b>（{@code labelOf} 展示用、{@code labelOrUnknown} 异常 / 审计用），
 * 本类只保留与码值无关的记账项目类型折算、药品判定等纯计算。
 */
public final class InpatientOrderLabels {

    private InpatientOrderLabels() {
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
