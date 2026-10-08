package com.his.medicaltech.enums;

import com.his.common.util.TextUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

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

    public static TransfusionReactionTypeEnum fromCode(String code) {
        if (!TextUtil.hasText(code)) {
            return null;
        }
        String trimmed = code.trim();
        for (TransfusionReactionTypeEnum item : values()) {
            if (item.code.equals(trimmed)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null / 空白 / 词表外一律返回空串 ——
     * 输血反应类型是临床上报口径，把看不懂的值说成「其他」等于把没核实的反应记成了别的反应。
     */
    public static String getText(String code) {
        TransfusionReactionTypeEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或词表外返回「未知(n)」（null 本身渲染成「未知」），保留原始值便于排查。
     */
    public static String labelOrUnknown(String code) {
        TransfusionReactionTypeEnum item = fromCode(code);
        return item == null ? (!TextUtil.hasText(code) ? "未知" : "未知(" + code + ")") : item.label;
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
}
