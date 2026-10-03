package com.his.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 中药代煎单状态（sql/139，落在中药代煎单的代煎状态列）。
 *
 * <p>只能单向推进：待煎 → 已煎 → 已取。没有「撤销一步」的口子 ——
 * 煎好又倒回去在真实药房里是不存在的事件（药已经煎成汤液了），
 * 要撤回只能作废整单并重新发药，所以这里也不提供「回退」动作。
 */
@Getter
@AllArgsConstructor
public enum TcmDecoctStatusEnum {

    /**
     * 1-待煎（发药完成即生成）
     */
    PENDING(1, "待煎"),

    /**
     * 2-已煎（煎好并按剂封装）
     */
    DECOCTED(2, "已煎"),

    /**
     * 3-已取（患者取走，终态）
     */
    PICKED(3, "已取"),

    /**
     * 9-已作废（终态，必须写原因）
     */
    CANCELLED(9, "已作废");

    private final Integer code;
    private final String label;

    public static String labelOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (TcmDecoctStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item.label;
            }
        }
        return String.valueOf(code);
    }
}
