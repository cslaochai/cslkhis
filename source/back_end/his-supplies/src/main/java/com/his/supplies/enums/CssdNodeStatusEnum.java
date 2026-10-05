package com.his.supplies.enums;

import lombok.Getter;

/**
 * CSSD 消毒供应节点状态枚举（码值口径 = biz_cssd_pack.status / biz_cssd_trace.node_type 列注释）。
 *
 * <p>同一码值在包裹上是状态（「已回收」），在流转轨迹上是动作（「回收」），
 * 用 {@link #getLabel()} 与 {@link #getActionLabel()} 两个文案区分，避免两套 Map 各说各话。
 */
@Getter
public enum CssdNodeStatusEnum {

    RECEIVED(1, "已回收", "回收"),
    WASHING(2, "清洗中", "清洗"),
    PACKED(3, "已打包", "打包"),
    STERILIZING(4, "灭菌中", "灭菌"),
    STORED(5, "待发放", "储存"),
    ISSUED(6, "已发放", "发放");

    private final int code;
    private final String label;
    private final String actionLabel;

    CssdNodeStatusEnum(int code, String label, String actionLabel) {
        this.code = code;
        this.label = label;
        this.actionLabel = actionLabel;
    }

    public static CssdNodeStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CssdNodeStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /** 包裹状态文案：未知码值渲染「未知(码值)」，绝不回落成合法值 */
    public static String labelOf(Integer code) {
        CssdNodeStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /** 轨迹动作文案：未知码值渲染「未知(码值)」 */
    public static String actionLabelOf(Integer code) {
        CssdNodeStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getActionLabel();
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
