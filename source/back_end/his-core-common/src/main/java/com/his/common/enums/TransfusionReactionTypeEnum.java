package com.his.common.enums;

import com.his.common.util.TextUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * 输血反应类型枚举（受控集合：禁止自由文本，否则「发热」与「发热反应」统计不到一起）。
 */
@Getter
public enum TransfusionReactionTypeEnum {

    FEBRILE("发热反应"),
    ALLERGIC("过敏反应"),
    ACUTE_HEMOLYTIC("急性溶血反应"),
    DELAYED_HEMOLYTIC("迟发性溶血反应"),
    BACTERIAL_CONTAMINATION("细菌污染反应"),
    CIRCULATORY_OVERLOAD("循环超负荷"),
    TRALI("输血相关急性肺损伤"),
    TA_GVHD("输血相关移植物抗宿主病"),
    OTHER("其他");

    private final String code;
    private final String label;

    TransfusionReactionTypeEnum(String code) {
        this.code = code;
        this.label = code;
    }

    /**
     * 词表是否合法（写入侧校验用；null 与空串都不合法）
     */
    public static boolean isValid(String type) {
        return fromCode(type) != null;
    }

    /**
     * 下拉候选（词表顺序即上报频次顺序）
     */
    public static List<String> options() {
        List<String> list = new ArrayList<>();
        for (TransfusionReactionTypeEnum item : values()) {
            list.add(item.code);
        }
        return list;
    }

    public static TransfusionReactionTypeEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (TransfusionReactionTypeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        TransfusionReactionTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        TransfusionReactionTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        TransfusionReactionTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
