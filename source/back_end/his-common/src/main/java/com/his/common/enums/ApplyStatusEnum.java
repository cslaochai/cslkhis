package com.his.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 申请状态枚举（适用于检验/检查申请单）。
 */
@Getter
@AllArgsConstructor
public enum ApplyStatusEnum {

    /**
     * 1-已提交（已开单，未缴费）
     */
    SUBMITTED(1, "已提交"),

    /**
     * 2-已缴费
     */
    PAID(2, "已缴费"),

    /**
     * 3-已采样（<b>废弃</b>，执行态请读检验记录的记录状态）
     */
    @Deprecated
    SAMPLED(3, "已采样"),

    /**
     * 4-检验中（<b>废弃</b>，执行态请读执行记录）
     */
    @Deprecated
    TESTING(4, "检验中"),

    /**
     * 5-已出报告（<b>废弃</b>，执行态请读执行记录）
     */
    @Deprecated
    REPORTED(5, "已出报告"),

    /**
     * 6-已取消
     */
    CANCELLED(6, "已取消"),

    /**
     * 未知状态（兜底处理，防止解析异常）
     */
    UNKNOWN(0, "未知状态");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态描述
     */
    private final String label;

    public static ApplyStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ApplyStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ApplyStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ApplyStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ApplyStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}