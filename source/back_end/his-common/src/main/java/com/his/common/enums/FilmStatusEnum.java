package com.his.common.enums;

import lombok.Getter;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * 胶片状态（检查胶片用量.film_status，字典 his_film_status，sql/138）。
 */
@Getter
public enum FilmStatusEnum {

    /**
     * 已登记：技师录了用量，还没出片也没收钱
     */
    REGISTERED(1, "已登记"),
    /**
     * 已打印：打印机出片了
     */
    PRINTED(2, "已打印"),
    /**
     * 已发放：交到患者/病区手上
     */
    DELIVERED(3, "已发放"),
    /**
     * 已作废：登记错了；已记账的不能走这个态，那笔钱要红冲
     */
    INVALID(4, "已作废");

    private final Integer code;
    private final String desc;

    FilmStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static FilmStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (FilmStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        FilmStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        FilmStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }

    public static String getLabelOrUnknown(Integer code) {
        FilmStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }
}
