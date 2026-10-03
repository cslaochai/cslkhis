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
}
