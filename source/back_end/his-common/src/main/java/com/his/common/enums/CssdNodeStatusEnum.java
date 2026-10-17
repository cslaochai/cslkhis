package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * CSSD 消毒供应节点状态枚举（码值口径 = 器械包状态字段与 CSSD 追溯记录节点字段的列注释）。
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

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 轨迹动作文案：未知码值渲染「未知(码值)」 */
    public static String getActionLabelOrUnknown(Integer code) {
        CssdNodeStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getActionLabel();
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }

    public static CssdNodeStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CssdNodeStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CssdNodeStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CssdNodeStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CssdNodeStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
