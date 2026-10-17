package com.his.common.enums;

import com.his.common.util.TextUtil;
import lombok.Getter;

/**
 * 过敏史类型枚举（药物/食物/其他）。
 * <p>
 * 词表外一律按脏数据处理：这一列没有「未知」档，替患者编一个「其他」等于替他做了一次临床判断。
 * <p>
 * 码值本身即落库文案（中文串），与字典不是同一口径，改这里就是改数据。
 */
@Getter
public enum AllergyTypeEnum {

    DRUG("药物"),
    FOOD("食物"),
    OTHER("其他");

    /**
     * 后缀通配词：写成「霉素」「西林」这类词根，命中即判药物
     */
    private static final String[] DRUG_KEYS = {"青霉素", "头孢", "磺胺", "阿司匹林", "碘", "造影剂", "甲氨蝶呤",
            "华法林", "对乙酰氨基酚", "链霉素", "庆大霉素", "左氧氟沙星", "环丙沙星", "阿莫西林", "布洛芬",
            "卡马西平", "苯妥英钠", "别嘌醇", "霉素", "西林", "沙星"};

    private static final String[] FOOD_KEYS = {"海鲜", "虾", "蟹", "芒果", "花生", "牛奶", "鸡蛋", "鱼", "贝",
            "坚果", "大豆", "小麦"};

    private final String code;

    AllergyTypeEnum(String code) {
        this.code = code;
    }

    public static AllergyTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (AllergyTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 与空串不合法）
     */
    public static boolean isValid(String code) {
        return code != null && !code.isEmpty() && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在词表内（脏数据）一律返回空串，不回落到合法文案。
     */
    public static String getText(String code) {
        AllergyTypeEnum item = fromCode(code);
        return item == null ? "" : item.code;
    }

    /**
     * 异常 / 审计用码值 → 文案。null 或词表外返回「未知(n)」，保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(String code) {
        AllergyTypeEnum item = fromCode(code);
        if (item != null) {
            return item.code;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }

    /**
     * 按关键词猜类型 —— 只在**一次性迁移**（主档自由文本 → 结构化行）时使用。
     *
     * <p>为什么猜而不是一律填「其他」：存量 14 条文本里「青霉素」「头孢类」「磺胺类」占了大半，
     * 全部落成「其他」会让这一列在页面上毫无信息量。猜的依据是药物/食物的通用词，
     * 猜不中落「其他」，**不会**猜成「危及生命」这类会改变临床判断的字段取值。
     * 猜出的结果连同来源一起写进备注，页面上能分辨它是怎么来的。
     */
    public static String guess(String text) {
        if (!TextUtil.hasText(text)) {
            return OTHER.code;
        }
        String t = text.trim();
        if (containsAny(t, DRUG_KEYS)) {
            return DRUG.code;
        }
        if (containsAny(t, FOOD_KEYS)) {
            return FOOD.code;
        }
        return OTHER.code;
    }

    private static boolean containsAny(String text, String... keys) {
        for (String k : keys) {
            if (text.contains(k)) {
                return true;
            }
        }
        return false;
    }
}
