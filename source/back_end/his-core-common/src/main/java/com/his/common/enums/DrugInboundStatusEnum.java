package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 药品入库单状态
 */
@Getter
public enum DrugInboundStatusEnum {

    PENDING_AUDIT(1, "待审核"),
    AUDITED(2, "已审核"),
    INBOUND(3, "已入库"),
    CANCELLED(4, "已取消");

    private final Integer code;
    private final String label;

    DrugInboundStatusEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static DrugInboundStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DrugInboundStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DrugInboundStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DrugInboundStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DrugInboundStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
