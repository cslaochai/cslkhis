package com.his.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * 欠费管控的「费用大类」口径（1-药品 2-检查 3-检验 4-治疗 5-护理 6-手术 7-输血 8-监护）。
 *
 * <p><b>注意：这不是 {@code com.his.common.enums.PaymentItemTypeEnum}</b>。
 * PaymentItemTypeEnum 是结算账单明细的项目类型（1-挂号费 2-西药 …），两者码值口径完全不同，
 * 严禁互相套用。
 */
@Getter
public enum ArrearsFeeClassEnum {

    DRUG(1, "药品"),
    EXAM(2, "检查"),
    LAB(3, "检验"),
    TREAT(4, "治疗"),
    NURSING(5, "护理"),
    OPERATION(6, "手术"),
    TRANSFUSION(7, "输血"),
    MONITOR(8, "监护");

    private final int code;
    private final String label;

    ArrearsFeeClassEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 解析逗号分隔的码值串（如 "2,3,4"）为码值集合；忽略空白片段与非数字片段；
     * 串为空时回落到默认停费口径：检查、检验、治疗。
     */
    public static Set<Integer> parseCodes(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of(EXAM.code, LAB.code, TREAT.code);
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> s.matches("\\d+"))
                .map(Integer::valueOf)
                .collect(Collectors.toSet());
    }

    public static ArrearsFeeClassEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ArrearsFeeClassEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        ArrearsFeeClassEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ArrearsFeeClassEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
}
