package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 出诊计划的就诊状态枚举（这个班次此刻接不接患者，与停诊状态不是一件事）
 */
@Getter
public enum ConsultStatusEnum {

    /**
     * 待开始：班次尚未开诊
     */
    NOT_STARTED(0, "待开始"),
    /**
     * 接诊中：正在看诊
     */
    CONSULTING(1, "接诊中"),
    /**
     * 暂停：临时停接（医生离岗），恢复后继续
     */
    PAUSED(2, "暂停");

    private final int code;
    private final String label;

    ConsultStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static ConsultStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ConsultStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ConsultStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ConsultStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ConsultStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
