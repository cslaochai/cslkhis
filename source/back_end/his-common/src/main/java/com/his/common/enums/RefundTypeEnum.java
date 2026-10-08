package com.his.common.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 退费类型（字典 his_refund_type）—— 退费申请单上的「要退哪一类钱」。
 */
public enum RefundTypeEnum {

    /**
     * 退药：西药 + 中成药 + 中药饮片
     */
    DRUG(1, "退药", Arrays.asList(
            PaymentItemTypeEnum.WESTERN_MEDICINE.getCode(),
            PaymentItemTypeEnum.CHINESE_PATENT_MEDICINE.getCode(),
            PaymentItemTypeEnum.CHINESE_HERBAL_MEDICINE.getCode())),
    EXAMINATION(2, "退检查", Collections.singletonList(
            PaymentItemTypeEnum.EXAMINATION.getCode())),
    LABORATORY(3, "退检验", Collections.singletonList(
            PaymentItemTypeEnum.LABORATORY_TEST.getCode())),
    TREATMENT(4, "退治疗", Collections.singletonList(
            PaymentItemTypeEnum.TREATMENT.getCode())),
    /**
     * 全部退费：账单下所有还没退过的行
     */
    ALL(5, "全部退费", null);

    private final Integer code;
    private final String desc;
    private final List<Integer> itemTypes;

    RefundTypeEnum(Integer code, String desc, List<Integer> itemTypes) {
        this.code = code;
        this.desc = desc;
        this.itemTypes = itemTypes;
    }

    public static RefundTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RefundTypeEnum typeEnum : values()) {
            if (typeEnum.code.equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

    /**
     * 判断退费类型码是否受支持（Controller/Service 入参校验用，避免各写一份 switch）。
     */
    public static boolean supported(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 可选类型码 -> 项目类型集合的只读视图（报表/校验想直接查映射时用）。
     */
    public static Map<Integer, List<Integer>> itemTypeMap() {
        Map<Integer, List<Integer>> map = new java.util.LinkedHashMap<>();
        for (RefundTypeEnum typeEnum : values()) {
            map.put(typeEnum.code, typeEnum.itemTypes);
        }
        return Collections.unmodifiableMap(map);
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 本类型涵盖的项目类型；{@code null} 表示不限（全部退费）。
     */
    public List<Integer> getItemTypes() {
        return itemTypes;
    }

    /**
     * 项目类型是否落在该退费类型的范围内（ALL 收全部）。
     */
    public boolean covers(Integer itemType) {
        return itemTypes == null || (itemType != null && itemTypes.contains(itemType));
    }
}
