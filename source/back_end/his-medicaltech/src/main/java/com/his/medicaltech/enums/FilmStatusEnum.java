package com.his.medicaltech.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 胶片状态（检查胶片用量.film_status，字典 his_film_status，sql/138）。
 */
@Getter
public enum FilmStatusEnum {

    /** 已登记：技师录了用量，还没出片也没收钱 */
    REGISTERED(1, "已登记"),
    /** 已打印：打印机出片了 */
    PRINTED(2, "已打印"),
    /** 已发放：交到患者/病区手上 */
    DELIVERED(3, "已发放"),
    /** 已作废：登记错了；已记账的不能走这个态，那笔钱要红冲 */
    INVALID(4, "已作废");

    private final Integer code;
    private final String desc;

    FilmStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FilmStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FilmStatusEnum e : values()) {
            if (Objects.equals(e.getCode(), code)) {
                return e;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    /** 展示用：null 或不在枚举内返回空串（不把「未知」渲染给用户看） */
    public static String getText(Integer code) {
        FilmStatusEnum item = getByCode(code);
        return item == null ? "" : item.getDesc();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        FilmStatusEnum item = getByCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.getDesc();
    }
}
