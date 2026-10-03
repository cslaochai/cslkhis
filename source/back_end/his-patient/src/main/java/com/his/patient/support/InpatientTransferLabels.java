package com.his.patient.support;

/**
 * 转科枚举文案（P4.2）。
 *
 * <p><b>铁律：未知码值一律渲染成「未知(码值)」，绝不回落成某个合法值。</b>
 * 转科状态回落成"已完成"等于把一次没接手的转科记成完成了 ——
 * 与「未判定 ≠ 正常」「医嘱未知状态不能显示成已完成」是同一条线。
 */
public final class InpatientTransferLabels {

    private InpatientTransferLabels() {
    }

    /**
     * 转科类型：1-普通转科 2-急诊转科 3-转入ICU 4-ICU转出
     */
    public static String typeText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "普通转科";
            case 2 -> "急诊转科";
            case 3 -> "转入ICU";
            case 4 -> "ICU转出";
            default -> "未知(" + code + ")";
        };
    }

    public static boolean isValidType(Integer code) {
        return code != null && code >= 1 && code <= 4;
    }

    /**
     * 转科状态：0-待接收 1-已完成 2-已取消
     */
    public static String statusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 0 -> "待接收";
            case 1 -> "已完成";
            case 2 -> "已取消";
            default -> "未知(" + code + ")";
        };
    }
}
