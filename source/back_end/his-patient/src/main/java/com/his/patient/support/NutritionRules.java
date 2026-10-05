package com.his.patient.support;

import com.his.patient.enums.*;

import java.util.*;

/**
 * 营养膳食口径（sql/168）。前端 lib/nutrition.js 与本类逐字对齐，页面不得另写一份映射。
 *
 * <p>三条不可让的口径：
 * <ol>
 *   <li><b>NRS2002 总分与「有无营养风险」由本类算</b>：总分 = 受损(0~3) + 严重度(0~3) + 年龄≥70 加 1，
 *       <b>≥3 判为有营养风险</b>（CSPEN 口径）。分数是能凑的，判定不能交给浏览器。</li>
 *   <li><b>膳食类型目录只在这里有一份</b>：热量/蛋白目标、供应餐次、是否走食堂订餐，
 *       都从 {@link Diet} 带出，营养师可改目标量但改不了 routeNeedsMeal 这条规则。</li>
 *   <li><b>只有口服（route=1）才进订餐</b>：管饲制剂由营养科发放、肠外营养走静配与医嘱执行链，
 *       两者都不该出现在食堂配送单上 —— 给肠外营养的患者订一份"饭"是错的。</li>
 * </ol>
 */
public final class NutritionRules {

    /**
     * NRS2002 营养风险阈值：总分 ≥3 为有营养风险
     */
    public static final int NRS_RISK_CUTOFF = 3;

    // 筛查
    /**
     * NRS2002 阴性者复筛间隔（天）—— "每周复筛"落成一列日期，不靠人记
     */
    public static final int RE_SCREEN_DAYS = 7;
    /**
     * 待指定饮食：医嘱文本认不出饮食类型时派生方案用的占位码。
     *
     * <p>必须有这一档 —— 膳食方案的 diet_code 列 NOT NULL，更重要的是"认不出就不建方案"等于
     * 让这条膳食医嘱从营养科的待接收队列里**静默消失**，患者一顿饭都吃不上且没人知道。
     * 占位方案进待接收列表，但 {@code TO_DETERMINE} 不许被"接收"（见 DietServiceImpl.planConfirm）：
     * 先改成真实饮食类型再接，饮食类型决定食堂做什么饭。
     */
    public static final String CODE_TO_DETERMINE = "TO_DETERMINE";
    /**
     * 目录与字典 his_diet_type 逐字对齐（sql/168 §6）
     */
    public static final List<Diet> DIETS = List.of(
            new Diet("NORMAL", "普食", DietCategoryEnum.BASIC.getCode(), DietRouteEnum.ORAL.getCode(), 2000, 70, "1,2,3", "无发热、无吞咽困难、消化功能正常"),
            new Diet("SOFT", "软食", DietCategoryEnum.BASIC.getCode(), DietRouteEnum.ORAL.getCode(), 1800, 65, "1,2,3", "老年、咀嚼困难、低热或术后恢复期"),
            new Diet("HALF_LIQUID", "半流质", DietCategoryEnum.BASIC.getCode(), DietRouteEnum.ORAL.getCode(), 1500, 55, "1,2,3,4", "发热、吞咽困难、口腔及胃肠道术后过渡"),
            new Diet("LIQUID", "流质", DietCategoryEnum.BASIC.getCode(), DietRouteEnum.ORAL.getCode(), 1000, 45, "1,2,3,4", "急性重症、口腔食道手术后，短期使用"),
            new Diet("DIABETES", "糖尿病饮食", DietCategoryEnum.THERAPY.getCode(), DietRouteEnum.ORAL.getCode(), 1600, 75, "1,2,3,4",
                    "按理想体重 25~30kcal/kg，碳水占 45~60%，定时定量分餐"),
            new Diet("LOW_SALT", "低盐低脂饮食", DietCategoryEnum.THERAPY.getCode(), DietRouteEnum.ORAL.getCode(), 1800, 65, "1,2,3",
                    "钠 <2~3g/日、胆固醇 <300mg；高血压、心衰、肾病"),
            new Diet("HIGH_PROTEIN", "高蛋白饮食", DietCategoryEnum.THERAPY.getCode(), DietRouteEnum.ORAL.getCode(), 2200, 100, "1,2,3",
                    "蛋白 1.2~1.5g/kg；消耗性疾病、术前纠正低蛋白血症"),
            new Diet("LOW_PROTEIN", "低蛋白饮食", DietCategoryEnum.THERAPY.getCode(), DietRouteEnum.ORAL.getCode(), 1800, 40, "1,2,3",
                    "蛋白 0.5~0.8g/kg；慢性肾功能不全非透析期"),
            new Diet("KIDNEY", "肾病饮食", DietCategoryEnum.THERAPY.getCode(), DietRouteEnum.ORAL.getCode(), 1800, 35, "1,2,3",
                    "低盐 + 限钾限磷 + 优质低蛋白，按透析与否调整"),
            new Diet("GOUT", "痛风饮食", DietCategoryEnum.THERAPY.getCode(), DietRouteEnum.ORAL.getCode(), 1800, 50, "1,2,3",
                    "嘌呤 <150mg/日，禁动物内脏、海鲜与酒"),
            new Diet("LOW_FIBER", "少渣饮食", DietCategoryEnum.THERAPY.getCode(), DietRouteEnum.ORAL.getCode(), 1800, 60, "1,2,3",
                    "腹泻、肠道手术前、痔疮及消化道出血恢复期"),
            new Diet("OCCULT_BLOOD", "隐血试验饮食", DietCategoryEnum.TEST.getCode(), DietRouteEnum.ORAL.getCode(), 1800, 60, "1,2,3",
                    "便隐血检查前 3 天禁铁剂、动物血、绿叶菜"),
            new Diet("CHOLYCYST", "胆囊造影饮食", DietCategoryEnum.TEST.getCode(), DietRouteEnum.ORAL.getCode(), 1600, 55, "1,2",
                    "前一日少渣晚餐，次日高脂肪餐诱发胆囊收缩"),
            new Diet("ENT", "肠内营养", DietCategoryEnum.SUPPORT.getCode(), DietRouteEnum.TUBE.getCode(), 1500, 75, null,
                    "整蛋白/短肽/疾病特异型制剂，管饲或口服，25~30kcal/kg；管饲不发食堂餐"),
            new Diet("PN", "肠外营养", DietCategoryEnum.SUPPORT.getCode(), DietRouteEnum.IV.getCode(), 1800, 80, null,
                    "经中心静脉/PICC 输注三腔袋，由静配中心配制，走医嘱执行链"),
            new Diet("ONS", "口服营养补充", DietCategoryEnum.SUPPORT.getCode(), DietRouteEnum.ORAL.getCode(), 2000, 80, "1,2,3,4",
                    "在常规饮食外加用特殊医学用途配方食品 400~600kcal/日"),
            // 占位档：不进下拉选项（见 DietServiceImpl.dietTypeOptions 的过滤），只用于医嘱派生时认不出类型
            new Diet(CODE_TO_DETERMINE, "待指定饮食", DietCategoryEnum.BASIC.getCode(), DietRouteEnum.ORAL.getCode(), null, null, null,
                    "医嘱文本认不出饮食类型的占位，营养科必须先改成真实饮食类型才能接收"));
    /**
     * 常用阈值：营养筛查率目标（等级评审口径，只作提示不判定）
     */
    public static final java.math.BigDecimal TARGET_SCREEN_RATE = new java.math.BigDecimal("90.00");
    /**
     * 膳食医嘱执行率目标
     */
    public static final java.math.BigDecimal TARGET_DIET_CONFIRM_RATE = new java.math.BigDecimal("95.00");
    /**
     * 营养会诊及时应答率目标
     */
    public static final java.math.BigDecimal TARGET_CONSULT_ONTIME_RATE = new java.math.BigDecimal("90.00");
    /**
     * 订餐签收率目标
     */
    public static final java.math.BigDecimal TARGET_MEAL_SIGN_RATE = new java.math.BigDecimal("95.00");

    // 饮食类型目录
    private static final Map<String, Diet> BY_CODE = new LinkedHashMap<>();
    /**
     * 状态机：待配餐→已配餐→已配送→已签收，只许一级推进；4-已取消是旁路
     */
    private static final Map<Integer, Integer> MEAL_NEXT = Map.of(
            MealDeliverStatusEnum.PENDING.getCode(), MealDeliverStatusEnum.PREPARED.getCode(),
            MealDeliverStatusEnum.PREPARED.getCode(), MealDeliverStatusEnum.DELIVERED.getCode(),
            MealDeliverStatusEnum.DELIVERED.getCode(), MealDeliverStatusEnum.SIGNED.getCode());

    static {
        for (Diet d : DIETS) {
            BY_CODE.put(d.code(), d);
        }
    }

    private NutritionRules() {
    }

    public static String screenTypeText(Integer type) {
        NutritionScreenTypeEnum item = type == null ? null : NutritionScreenTypeEnum.fromCode(type);
        if (item == null) {
            return "未知";
        }
        // PG-SGA 在营养科表单里的叫法比枚举全名短，保留科室口径
        return switch (item) {
            case NRS2002 -> "NRS2002 营养风险筛查";
            case PG_SGA -> "PG-SGA 主观整体评估";
            case MNA -> "MNA 老年微型营养评估";
        };
    }

    public static String screenSourceText(Integer source) {
        String label = NutritionScreenSourceEnum.labelOf(source);
        return label == null ? "未知" : label;
    }

    /**
     * NRS2002 总分 = 受损 + 严重度 + 年龄项；非 NRS2002 量表由营养师直接给总分。
     */
    public static int totalScore(Integer screenType, Integer impair, Integer severity, Integer ageScore,
                                 Integer submittedTotal) {
        if (screenType != null && screenType == NutritionScreenTypeEnum.NRS2002.getCode()) {
            return nz(impair) + nz(severity) + nz(ageScore);
        }
        return nz(submittedTotal);
    }

    /**
     * 营养风险判定：NRS2002 按 ≥3；PG-SGA 由营养师分级（这里沿用提交总分 ≥3 的同一阈值口径），
     * MNA 简化为总分 ≥11 判为"有营养风险/存在营养不良"（MNA 满分 17，11 分为常用切点）。
     */
    public static int riskFlag(Integer screenType, int total) {
        if (screenType != null && screenType == NutritionScreenTypeEnum.MNA.getCode()) {
            return total < 11 ? 1 : 0;
        }
        return total >= NRS_RISK_CUTOFF ? 1 : 0;
    }

    /**
     * BMI 服务端算，前端不传（身高 cm、体重 kg）
     */
    public static java.math.BigDecimal bmiOf(java.math.BigDecimal heightCm, java.math.BigDecimal weightKg) {
        if (heightCm == null || weightKg == null || heightCm.signum() <= 0) {
            return null;
        }
        double m = heightCm.doubleValue() / 100D;
        return java.math.BigDecimal.valueOf(weightKg.doubleValue() / (m * m))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public static Diet dietOf(String code) {
        return code == null ? null : BY_CODE.get(code);
    }

    public static String dietCategoryText(Integer category) {
        String label = DietCategoryEnum.labelOf(category);
        return label == null ? "未知" : label;
    }

    public static String routeText(Integer route) {
        String label = DietRouteEnum.labelOf(route);
        return label == null ? "未知" : label;
    }

    // 订餐

    /**
     * 该给食途径是否需要食堂按餐配送（只有口服要）
     */
    public static boolean needsMealDelivery(Integer route) {
        return route != null && route == DietRouteEnum.ORAL.getCode();
    }

    /**
     * 订餐拆行用的餐次列表；mealTypes 为空则返回空表（该方案不订餐）
     */
    public static List<Integer> mealTypesOf(String mealTypes) {
        if (mealTypes == null || mealTypes.isBlank()) {
            return List.of();
        }
        List<Integer> list = new ArrayList<>();
        for (String s : Arrays.asList(mealTypes.split(","))) {
            try {
                int v = Integer.parseInt(s.trim());
                if (v >= MealTypeEnum.BREAKFAST.getCode() && v <= MealTypeEnum.SNACK.getCode() && !list.contains(v)) {
                    list.add(v);
                }
            } catch (NumberFormatException ignored) {
                // 脏数据里的非数字餐次直接跳过，不让一行配置毁掉整批订餐
            }
        }
        return list;
    }

    public static String mealTypesText(String mealTypes) {
        List<Integer> list = mealTypesOf(mealTypes);
        if (list.isEmpty()) {
            return "不订餐";
        }
        return list.stream().map(NutritionRules::mealTypeText).reduce((a, b) -> a + "、" + b).orElse("-");
    }

    /**
     * 从医嘱正文猜饮食类型码（校对派生膳食方案时用）。
     *
     * <p>orderClass=10 的医嘱 item_code 是手输自定义码（药房摆药链刻意不接这一类），
     * 所以先认 CN-* 前缀里的码，再退到名称关键字匹配；认不出返回 null，调用方落
     * {@link #CODE_TO_DETERMINE} 占位方案进待接收队列，由营养科改成真实饮食类型后再接 ——
     * <b>宁可让营养师选一次，也不要瞎猜成"普食"</b>，糖尿病人被判成普食是会出事的。
     */
    public static String guessDietCode(String itemCode, String itemName) {
        Diet exact = dietOf(itemCode);
        if (exact != null) {
            return exact.code();
        }
        if (itemCode != null && itemCode.toUpperCase().startsWith("CN-")) {
            Diet byPrefix = dietOf(itemCode.substring(3).toUpperCase());
            if (byPrefix != null) {
                return byPrefix.code();
            }
        }
        String text = itemName == null ? "" : itemName;
        if (text.contains("肠外") || text.contains("全静脉营养") || text.contains("三腔袋")) {
            return "PN";
        }
        if (text.contains("肠内") || text.contains("鼻饲") || text.contains("管饲")) {
            return "ENT";
        }
        if (text.contains("口服营养补充") || text.contains("特殊医学用途")) {
            return "ONS";
        }
        if (text.contains("糖尿病")) {
            return "DIABETES";
        }
        if (text.contains("低盐") || text.contains("低脂")) {
            return "LOW_SALT";
        }
        if (text.contains("高蛋白")) {
            return "HIGH_PROTEIN";
        }
        if (text.contains("低蛋白") || text.contains("肾病")) {
            return text.contains("肾病") ? "KIDNEY" : "LOW_PROTEIN";
        }
        if (text.contains("痛风")) {
            return "GOUT";
        }
        if (text.contains("少渣")) {
            return "LOW_FIBER";
        }
        if (text.contains("隐血")) {
            return "OCCULT_BLOOD";
        }
        if (text.contains("胆囊造影")) {
            return "CHOLYCYST";
        }
        if (text.contains("半流")) {
            return "HALF_LIQUID";
        }
        if (text.contains("流质") || text.contains("清流")) {
            return "LIQUID";
        }
        if (text.contains("软食")) {
            return "SOFT";
        }
        if (text.contains("普食") || text.contains("普通饮食")) {
            return "NORMAL";
        }
        return null;
    }

    public static String mealTypeText(Integer mealType) {
        String label = MealTypeEnum.labelOf(mealType);
        return label == null ? "未知" : label;
    }

    // 膳食方案

    public static String mealStatusText(Integer status) {
        String label = MealDeliverStatusEnum.labelOf(status);
        return label == null ? "未知" : label;
    }

    /**
     * 允许的下一个状态（null 表示没有下一步，即已签收或已取消）
     */
    public static Integer mealNextStatus(Integer status) {
        return status == null ? null : MEAL_NEXT.get(status);
    }

    // 会诊类别
    // 会诊是会诊申请记录域的东西，类别码值与时限的唯一口径在
    // com.his.patient.support.ConsultationLabels（CATEGORY_NUTRITION / onTime），
    // 这里不再抄一份 —— 抄了就会漂移。

    public static boolean isMealStatus(Integer status) {
        return status != null && status >= MealDeliverStatusEnum.PENDING.getCode() && status <= MealDeliverStatusEnum.CANCELED.getCode();
    }

    public static String planStatusText(Integer status) {
        String label = PlanStatusEnum.labelOf(status);
        return label == null ? "未知" : label;
    }

    public static String confirmStatusText(Integer status) {
        String label = DietConfirmStatusEnum.labelOf(status);
        return label == null ? "未知" : label;
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }

    /**
     * 饮食类型。
     *
     * @param code      饮食码（字典 his_diet_type）
     * @param name      饮食名称
     * @param category  饮食类别（1-基本 2-治疗 3-诊断试验 4-营养支持）
     * @param route     默认给食途径（1-口服 2-管饲 3-静脉）
     * @param calorie   默认每日热量目标 kcal
     * @param protein   默认每日蛋白目标 g
     * @param mealTypes 默认供应餐次（his_meal_type 值，逗号分隔；null 表示不走订餐）
     * @param desc      配方/适用说明
     */
    public record Diet(String code, String name, int category, int route, Integer calorie, Integer protein,
                       String mealTypes, String desc) {
    }
}
