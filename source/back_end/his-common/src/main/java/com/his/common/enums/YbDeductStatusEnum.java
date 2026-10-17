package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 医保扣款状态码
 */
public enum YbDeductStatusEnum {

    /**
     * 待确认：收到通知尚未申诉或确认，唯一可编辑/可作废的状态
     */
    PENDING_CONFIRM(1, "待确认"),

    /**
     * 申诉中：已提交材料，等医保局回复
     */
    APPEALING(2, "申诉中"),

    /**
     * 申诉成功：医保局撤销扣款，无需缴回
     */
    APPEAL_SUCCESS(3, "申诉成功"),

    /**
     * 维持扣款待缴：申诉驳回或直接确认，待向医保基金缴回
     */
    WAIT_PAY(4, "维持扣款待缴"),

    /**
     * 已缴回：资金退回医保基金，闭环
     */
    PAID_BACK(5, "已缴回"),

    /**
     * 已作废：误录/重复录入
     */
    CANCELLED(6, "已作废");

    private final int code;

    private final String label;

    YbDeductStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 与库里 Integer 列比较：null 视为不匹配
     */
    public boolean matches(Integer v) {
        return v != null && v == code;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static YbDeductStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (YbDeductStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        YbDeductStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        YbDeductStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        YbDeductStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
