package com.his.patient.support;

/**
 * 转科域纯校验工具。
 *
 * <p><b>码值 → 文案的映射已下沉到对应枚举</b>（{@code labelOf} 展示用、{@code labelOrUnknown} 异常 / 审计用），
 * 本类仅保留转科类型 / 状态的合法性校验。
 */
public final class InpatientTransferLabels {

    private InpatientTransferLabels() {
    }

    public static boolean isValidType(Integer code) {
        return code != null && code >= 1 && code <= 4;
    }
}
