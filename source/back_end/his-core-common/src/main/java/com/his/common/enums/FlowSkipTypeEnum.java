package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 流转线类型（sys_flow_skip.skip_type）。
 * <p>PASS = 唯一正常出口；COND = 网关按条件判断的出口；REJECT = 驳回后落点（不配时直接终止实例）。
 */
@Getter
public enum FlowSkipTypeEnum {

    PASS("PASS", "正常流转"),
    COND("COND", "条件流转"),
    REJECT("REJECT", "驳回流转");

    private final String code;
    private final String label;

    FlowSkipTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static FlowSkipTypeEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (FlowSkipTypeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(String code) {
        FlowSkipTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        FlowSkipTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        FlowSkipTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
