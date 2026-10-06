package com.his.patient.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 健康档案六组里**没有字典表**的枚举码值（后端唯一口径）。
 *
 * <p>为什么不做成字典数据：这些值是随明细一起录入的描述性分类
 * （「过敏类型」「恢复情况」），不是需要跨模块共享、需要运营改的配置项。
 * 给每个都建一张字典表，结果是字典表里躺着一堆没人维护、与建表注释各说各话的码值。
 * 本工程既有的处理方式是：**无字典枚举以建表列注释为口径，在一处声明**，
 * 页面禁写第二套映射（前端对应 {@code src/lib/healthProfile.js}）。
 *
 * <p>与列注释的差异只有一处，是刻意加的：{@link #ALLERGY_SEVERITY} 比列注释
 * 「轻度/中度/重度/危及生命」多一个未评估。存量主档的 {@code allergy_history}
 * 是一句自由文本（如「青霉素」），迁移成结构化行时**没有任何严重程度信息**，
 * 而表列是 {@code NOT NULL}。此处的选择是显式写「未评估」并让页面照实显示 ——
 * 比默认编一个「中度」好得多：编出来的严重程度会真的被当成临床信息读。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HealthProfileEnums {

    /**
     * 过敏类型
     */
    public static final Set<String> ALLERGY_TYPE = ordered("药物", "食物", "其他");

    /**
     * 过敏严重程度（比列注释多「未评估」，理由见类注释）
     */
    public static final Set<String> ALLERGY_SEVERITY = ordered("轻度", "中度", "重度", "危及生命", "未评估");

    /**
     * 手术类型
     */
    public static final Set<String> SURGERY_TYPE = ordered("择期", "紧急", "急诊");

    /**
     * 手术恢复情况
     */
    public static final Set<String> RECOVERY_STATUS = ordered("良好", "一般", "差", "死亡");

    /**
     * 既往疾病当前控制情况
     */
    public static final Set<String> DISEASE_CURRENT_STATUS =
            ordered("已治愈", "控制良好", "未控制", "随访中");

    /**
     * 用药史：药物类型
     */
    public static final Set<String> DRUG_TYPE = ordered("处方药", "非处方药", "中药", "保健品");

    /**
     * 用药史：给药途径
     */
    public static final Set<String> DRUG_ROUTE = ordered("口服", "注射", "外用", "吸入");

    /**
     * 用药史：用药状态。
     * ⚠ 表列状态的 DEFAULT 是 '已完成'，那个值**不在**本枚举内。
     * 写入口必须显式给值（{@link #DEFAULT_MEDICATION_STATUS}），否则库里会长出第五种状态。
     */
    public static final Set<String> MEDICATION_STATUS = ordered("进行中", "已停用", "已换药", "已减量");

    /**
     * 新增用药史且未指定状态时的默认值
     */
    public static final String DEFAULT_MEDICATION_STATUS = "进行中";

    /**
     * 迁移生成、无法判断严重程度时的显式取值
     */
    public static final String SEVERITY_UNKNOWN = "未评估";

    /**
     * 按关键词猜过敏类型 —— 只在**一次性迁移**（主档自由文本 → 结构化行）时使用。
     *
     * <p>为什么猜而不是一律填「其他」：存量 14 条文本里「青霉素」「头孢类」「磺胺类」占了大半，
     * 全部落成「其他」会让这一列在页面上毫无信息量。猜的依据是药物/食物的通用词，
     * 猜不中落「其他」，**不会**猜成「危及生命」这类会改变临床判断的字段取值。
     * 猜出的结果连同来源一起写进备注，页面上能分辨它是怎么来的。
     */
    public static String guessAllergyType(String text) {
        if (text == null || text.isBlank()) {
            return "其他";
        }
        String t = text.trim();
        if (containsAny(t, "青霉素", "头孢", "磺胺", "阿司匹林", "碘", "造影剂", "甲氨蝶呤", "华法林",
                "对乙酰氨基酚", "链霉素", "庆大霉素", "左氧氟沙星", "环丙沙星", "阿莫西林", "布洛芬",
                "卡马西平", "苯妥英钠", "别嘌醇", "*霉素", "*西林", "*沙星", "*头孢")) {
            return "药物";
        }
        if (containsAny(t, "海鲜", "虾", "蟹", "芒果", "花生", "牛奶", "鸡蛋", "鱼", "贝", "坚果", "大豆", "小麦")) {
            return "食物";
        }
        return "其他";
    }

    private static boolean containsAny(String text, String... keys) {
        for (String k : keys) {
            if (k.startsWith("*")) {
                // 「*霉素」这类写法表示后缀通配，用包含判断即可覆盖
                if (text.contains(k.substring(1))) {
                    return true;
                }
            } else if (text.contains(k)) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> ordered(String... values) {
        Set<String> set = new LinkedHashSet<>();
        for (String v : values) {
            set.add(v);
        }
        return java.util.Collections.unmodifiableSet(set);
    }
}
