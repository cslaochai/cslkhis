package com.his.common.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 退费类型（字典 {@code his_refund_type}）—— 退费申请单上的「要退哪一类钱」。
 *
 * <p>它不是收费口径，而是<b>临床退货口径</b>：药房只退药、检查科只退没做过的检查，
 * 所以申请单必须能按类别圈定范围，而不是一句「退 ¥30」。
 *
 * <p>四层模型后，类别落到费用记账流水的项目类型列（即 {@link PaymentItemTypeEnum}）上：
 * 一种退费类型对应一组项目类型，收费台按行退费（不走申请单）时可以细到某一条记账行，
 * 而申请单只有「类型 + 金额」两个旋钮，所以它的金额必须能由该类下的<b>整条</b>账单行拼出来。
 */
public enum RefundTypeEnum {

    /** 退药：西药 + 中成药 + 中药饮片 */
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
    /** 全部退费：账单下所有还没退过的行 */
    ALL(5, "全部退费", null);

    private final Integer code;
    private final String desc;
    private final List<Integer> itemTypes;

    RefundTypeEnum(Integer code, String desc, List<Integer> itemTypes) {
        this.code = code;
        this.desc = desc;
        this.itemTypes = itemTypes;
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
     * 项目类型是否落在该退费类型的范围内（ALL 收全部）。
     */
    public boolean covers(Integer itemType) {
        return itemTypes == null || (itemType != null && itemTypes.contains(itemType));
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
}
