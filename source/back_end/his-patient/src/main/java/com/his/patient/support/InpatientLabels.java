package com.his.patient.support;

/**
 * 住院域纯计算 / 校验工具。
 *
 * <p><b>码值 → 文案的映射已下沉到对应枚举</b>（{@code labelOf} 展示用、{@code labelOrUnknown} 异常 / 审计用），
 * 本类不再承担任何文案渲染，只保留与码值无关的参数合法性校验。
 */
public final class InpatientLabels {

    private InpatientLabels() {
    }

    /**
     * 入院途径合法值校验
     */
    public static boolean isValidAdmitWay(Integer code) {
        return code != null && code >= 1 && code <= 4;
    }

    /**
     * 离院方式合法值校验
     */
    public static boolean isValidDischargeWay(Integer code) {
        return code != null && (code == 1 || code == 2 || code == 3 || code == 4 || code == 5 || code == 9);
    }
}
