package com.his.medicaltech.support;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 输血闭环枚举文案与<b>输血相容性规则</b>。
 *
 * <p><b>铁律：未知码值一律渲染成「未知(码值)」，绝不回落成某个合法值。</b>
 * 状态回落成"已完成"、品种回落成"红细胞悬液"，等于把没核实的事记成核实了 ——
 * 与手术/会诊/转科、检验「未判定 ≠ 正常」是同一条原则。
 *
 * <p>本类里<b>唯一不返回文案、只返回布尔值的那组方法</b>
 * （{@link #isAboCompatible} / {@link #isRhCompatible}）是全项目最不能出错的一段：
 * ABO 不相容输注是<b>致死性</b>医疗差错，一旦放过，后果不是"数据不好看"。
 * 因此它们按"宁严不宽"实现：血型取不到 / 品种不认识 → 一律判<b>不相容</b>，
 * 由调用方给出"为什么被拒"的文案。宽进严出的反例在这里是致命的。
 */
public final class TransfusionLabels {

    private TransfusionLabels() {
    }

    // 一、流程状态（与 sql/39 注释、前端筛选值必须逐一对齐）

    public static final int ST_PENDING_CROSSMATCH = 0;
    public static final int ST_CROSSMATCHED = 1;
    public static final int ST_ISSUED = 2;
    public static final int ST_INFUSING = 3;
    public static final int ST_FINISHED = 4;
    public static final int ST_CANCELLED = 5;

    /** 状态文案：0-待配血 1-已配血 2-已发血 3-输注中 4-已完成 5-已取消 */
    public static String statusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case ST_PENDING_CROSSMATCH -> "待配血";
            case ST_CROSSMATCHED -> "已配血";
            case ST_ISSUED -> "已发血";
            case ST_INFUSING -> "输注中";
            case ST_FINISHED -> "已完成";
            case ST_CANCELLED -> "已取消";
            default -> "未知(" + code + ")";
        };
    }

    /** 是否"未完成"（待配血 / 已配血 / 已发血 / 输注中）—— 工作台角标用 */
    public static boolean isUnfinished(Integer status) {
        return status != null
                && (status == ST_PENDING_CROSSMATCH || status == ST_CROSSMATCHED
                || status == ST_ISSUED || status == ST_INFUSING);
    }

    /** 是否"在途"（已配血 / 已发血 / 输注中）—— 防重复申请用 */
    public static boolean isActive(Integer status) {
        return status != null
                && (status == ST_CROSSMATCHED || status == ST_ISSUED || status == ST_INFUSING);
    }

    // 一·五、用血分级审批（sql/93；《医疗机构临床用血管理办法》）
    //   <400ml 上级医师（主治及以上）；400~799ml 科主任；≥800ml 医务科。
    //   折算口径：ml 直取；1U≈200ml（红细胞/全血）；1治疗量≈250ml（血小板/冷沉淀）；
    //   折不出来一律按最高级 —— 宁可多审一级，不可少审。

    /** 审批状态：0-待审批 1-已通过 2-已驳回 3-急诊待补审 */
    public static final int AP_PENDING = 0;
    public static final int AP_APPROVED = 1;
    public static final int AP_REJECTED = 2;
    public static final int AP_MAKEUP_PENDING = 3;

    /** 折算：1U（红细胞/全血）≈ 200ml */
    public static final int ML_PER_UNIT = 200;
    /** 折算：1治疗量（血小板/冷沉淀）≈ 250ml */
    public static final int ML_PER_THERAPEUTIC = 250;

    public static String approveStatusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case AP_PENDING -> "待审批";
            case AP_APPROVED -> "已通过";
            case AP_REJECTED -> "已驳回";
            case AP_MAKEUP_PENDING -> "急诊待补审";
            default -> "未知(" + code + ")";
        };
    }

    public static String approveLevelText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "上级医师（主治及以上）";
            case 2 -> "科主任";
            case 3 -> "医务科";
            default -> "未知(" + code + ")";
        };
    }

    /**
     * 折算申请量为毫升数；折不出来返回 null（调用方按最高级处理）。
     */
    public static Integer amountToMl(BigDecimal plannedAmount, String amountUnit) {
        if (plannedAmount == null || !StringUtils.hasText(amountUnit)) {
            return null;
        }
        return switch (amountUnit.trim()) {
            case "ml", "ML", "ml)", "毫升" -> plannedAmount.intValue();
            case "U", "u" -> plannedAmount.intValue() * ML_PER_UNIT;
            case "治疗量" -> plannedAmount.intValue() * ML_PER_THERAPEUTIC;
            default -> null;
        };
    }

    /**
     * 按折算量推导审批级别（服务端单点；ml 折不出来按最高级 3）。
     */
    public static int approveLevelOf(Integer amountMl) {
        if (amountMl == null || amountMl >= 800) {
            return 3;
        }
        return amountMl >= 400 ? 2 : 1;
    }

    /**
     * 配血/发血闸门：已通过，或急诊补审中（先配血发血、事后补办）才放行。
     */
    public static boolean approveGateOpen(Integer approveStatus, Integer isEmergency) {
        if (Objects.equals(AP_APPROVED, approveStatus)) {
            return true;
        }
        return Objects.equals(AP_MAKEUP_PENDING, approveStatus)
                && Objects.equals(1, isEmergency);
    }

    // 二、配血状态（与流程状态分离：配血不合时流程停在 0，但这条必须能看见）

    public static final int CM_PENDING = 0;
    public static final int CM_PARTIAL = 1;
    public static final int CM_ALL_MATCHED = 2;
    public static final int CM_INCOMPATIBLE = 3;

    /** 0-待配血 1-配血中（未配齐）2-全部相合且配齐 3-存在配血不合 */
    public static String crossmatchStatusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case CM_PENDING -> "待配血";
            case CM_PARTIAL -> "配血中（未配齐）";
            case CM_ALL_MATCHED -> "全部相合";
            case CM_INCOMPATIBLE -> "存在配血不合";
            default -> "未知(" + code + ")";
        };
    }

    // 三、血液品种（1~6）

    public static final int CMP_RED_CELL = 1;
    public static final int CMP_PLASMA = 2;
    public static final int CMP_PLATELET = 3;
    public static final int CMP_CRYOPRECIPITATE = 4;
    public static final int CMP_WHOLE_BLOOD = 5;
    public static final int CMP_OTHER = 6;

    private static final Map<Integer, String> COMPONENTS = new LinkedHashMap<>();

    static {
        COMPONENTS.put(CMP_RED_CELL, "红细胞悬液");
        COMPONENTS.put(CMP_PLASMA, "血浆");
        COMPONENTS.put(CMP_PLATELET, "血小板");
        COMPONENTS.put(CMP_CRYOPRECIPITATE, "冷沉淀");
        COMPONENTS.put(CMP_WHOLE_BLOOD, "全血");
        COMPONENTS.put(CMP_OTHER, "其他");
    }

    public static Map<Integer, String> components() {
        return COMPONENTS;
    }

    public static String componentText(Integer code) {
        if (code == null) {
            return "—";
        }
        return COMPONENTS.getOrDefault(code, "未知(" + code + ")");
    }

    public static boolean isValidComponent(Integer code) {
        return code != null && COMPONENTS.containsKey(code);
    }

    /** 是否为"红细胞类"（含全血）：ABO 按红细胞规则判相容 */
    public static boolean isRedCellGroup(Integer component) {
        return component != null && (component == CMP_RED_CELL || component == CMP_WHOLE_BLOOD);
    }

    /** 是否为"血浆类"（含冷沉淀）：ABO 按血浆规则判相容 */
    public static boolean isPlasmaGroup(Integer component) {
        return component != null && (component == CMP_PLASMA || component == CMP_CRYOPRECIPITATE);
    }

    // 四、血型（ABO + Rh）

    private static final List<String> ABO_TYPES = List.of("A", "B", "O", "AB");

    /** Rh 用中文单字存（阳/阴），不用 +/-，避免与"阴性/阳性"文本混用两套写法 */
    public static final String RH_POSITIVE = "阳";
    public static final String RH_NEGATIVE = "阴";

    public static List<String> aboTypes() {
        return ABO_TYPES;
    }

    public static boolean isValidAbo(String abo) {
        return abo != null && ABO_TYPES.contains(abo.trim().toUpperCase());
    }

    /** 规整 ABO 写法（小写 / 前后空格一律归一，避免"A "与"A"被当成两种血型） */
    public static String normalizeAbo(String abo) {
        return abo == null ? null : abo.trim().toUpperCase();
    }

    public static boolean isValidRh(String rh) {
        if (rh == null) {
            return false;
        }
        String s = rh.trim();
        return RH_POSITIVE.equals(s) || RH_NEGATIVE.equals(s)
                || "阳性".equals(s) || "阴性".equals(s)
                || "+".equals(s) || "-".equals(s);
    }

    /** Rh 归一：+ / 阳性 → 阳；- / 阴性 → 阴 */
    public static String normalizeRh(String rh) {
        if (rh == null) {
            return null;
        }
        String s = rh.trim();
        if (RH_POSITIVE.equals(s) || "阳性".equals(s) || "+".equals(s)) {
            return RH_POSITIVE;
        }
        if (RH_NEGATIVE.equals(s) || "阴性".equals(s) || "-".equals(s)) {
            return RH_NEGATIVE;
        }
        return s;
    }

    /** 中文血型文案（列表展示用，如「A 型 Rh(+)」） */
    public static String bloodTypeText(String abo, String rh) {
        if (abo == null && rh == null) {
            return "—";
        }
        String a = abo == null ? "?" : abo;
        String r = RH_NEGATIVE.equals(rh) ? "Rh(-)" : (RH_POSITIVE.equals(rh) ? "Rh(+)" : "Rh(?)");
        return a + " 型 " + r;
    }

    // 相容性规则（本类最要紧的一段）

    /**
     * 红细胞输注的 ABO 相容表：键 = 受血者，值 = 可接受的血袋血型。
     *
     * <pre>
     *   受血者 A  ← 可接受 A、O
     *   受血者 B  ← 可接受 B、O
     *   受血者 AB ← 可接受 A、B、AB、O
     *   受血者 O  ← 只能 O
     * </pre>
     *
     * <p>O 型是"万能供者"、AB 型是"万能受者"，这句话只对<b>红细胞</b>成立 ——
     * 换到血浆上方向正好相反，见 {@link #PLASMA_ACCEPT}。
     * 把这两个表混成一个，是输血系统里最经典也最致命的一个错误。
     */
    private static final Map<String, Set<String>> RBC_ACCEPT = Map.of(
            "A", Set.of("A", "O"),
            "B", Set.of("B", "O"),
            "AB", Set.of("A", "B", "AB", "O"),
            "O", Set.of("O")
    );

    /**
     * 血浆（含冷沉淀）输注的 ABO 相容表：方向与红细胞<b>相反</b>。
     *
     * <pre>
     *   受血者 A  ← 可接受 A、AB
     *   受血者 B  ← 可接受 B、AB
     *   受血者 AB ← 只能 AB
     *   受血者 O  ← 可接受 A、B、AB、O（万能受者）
     * </pre>
     *
     * <p>道理：血浆里的<b>抗体</b>才是风险源，O 型血浆同时含抗 A 与抗 B，
     * 只能给 O 型患者。写成红细胞那一套，就会把 O 型血浆发给 A 型患者 —— 溶血。
     */
    private static final Map<String, Set<String>> PLASMA_ACCEPT = Map.of(
            "A", Set.of("A", "AB"),
            "B", Set.of("B", "AB"),
            "AB", Set.of("AB"),
            "O", Set.of("A", "B", "AB", "O")
    );

    /**
     * ABO 是否相容（宁严不宽：血型取不到、品种不认识 → 判<b>不相容</b>）。
     *
     * <p>只有血小板与"其他"不做 ABO 硬拦：临床上允许跨 ABO 输注血小板
     * （抗体可被受者血浆稀释），强行阻断会拦掉合规操作。
     * 但 Rh 对<b>所有</b>品种一律硬拦，见 {@link #isRhCompatible}。
     */
    public static boolean isAboCompatible(Integer component, String patientAbo, String bagAbo) {
        String p = normalizeAbo(patientAbo);
        String b = normalizeAbo(bagAbo);
        if (p == null || b == null) {
            return false;
        }
        if (isRedCellGroup(component)) {
            return RBC_ACCEPT.getOrDefault(p, Set.of()).contains(b);
        }
        if (isPlasmaGroup(component)) {
            return PLASMA_ACCEPT.getOrDefault(p, Set.of()).contains(b);
        }
        // 血小板 / 其他：不做 ABO 硬拦（但血型字段仍必须填对，见 isValidAbo 校验）
        return true;
    }

    /**
     * Rh 是否相容：<b>患者 Rh 阴性时，血袋必须 Rh 阴性</b>（对所有品种生效）。
     *
     * <p>反方向是允许的：Rh 阳性患者可以接受 Rh 阴性血（Rh 阴性血稀缺，
     * 现实中也不该因为有 Rh 阳性患者就浪费），因此这里只做单向判定。
     *
     * <p>输入取不到 → 判不相容（宁严不宽）。
     */
    public static boolean isRhCompatible(String patientRh, String bagRh) {
        String p = normalizeRh(patientRh);
        String b = normalizeRh(bagRh);
        if (p == null || b == null) {
            return false;
        }
        if (RH_NEGATIVE.equals(p)) {
            return RH_NEGATIVE.equals(b);
        }
        if (RH_POSITIVE.equals(p)) {
            return RH_POSITIVE.equals(b) || RH_NEGATIVE.equals(b);
        }
        // p 既不是阴也不是阳（脏值）→ 明确判"不相容"，绝不因"看起来没问题"而放行
        return false;
    }

    /** 相容性被拒时的可执行文案（必须写清"哪一袋、什么血型、为什么不行"） */
    public static String incompatibleReason(Integer component, String patientAbo, String patientRh,
                                            String bagNo, String bagAbo, String bagRh) {
        List<String> reasons = new ArrayList<>();
        if (!isAboCompatible(component, patientAbo, bagAbo)) {
            reasons.add("ABO 不相容（受血者 " + patientAbo + " 型不可接受 " + bagAbo + " 型"
                    + componentText(component) + "）");
        }
        if (!isRhCompatible(patientRh, bagRh)) {
            reasons.add("Rh 不相容（受血者 Rh " + patientRh + " 性不可接受 Rh " + bagRh + " 性血袋）");
        }
        if (reasons.isEmpty()) {
            return null;
        }
        return "血袋 " + bagNo + "：" + String.join("；", reasons);
    }

    // 五、配血结论 / 血袋状态

    public static final int BAG_PENDING = 0;
    public static final int BAG_CROSSMATCHED = 1;
    public static final int BAG_ISSUED = 2;
    public static final int BAG_INFUSED = 3;

    /** 血袋状态：0-待配血 1-已配血 2-已发血 3-已输注 */
    public static String bagStatusText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case BAG_PENDING -> "待配血";
            case BAG_CROSSMATCHED -> "已配血";
            case BAG_ISSUED -> "已发血";
            case BAG_INFUSED -> "已输注";
            default -> "未知(" + code + ")";
        };
    }

    /** 配血结论：1-相合 2-不合 */
    public static String crossmatchResultText(Integer code) {
        if (code == null) {
            return "—";
        }
        return switch (code) {
            case 1 -> "相合";
            case 2 -> "不合";
            default -> "未知(" + code + ")";
        };
    }

    // 六、输血反应类型（受控字典：禁止自由文本，避免"发热"与"发热反应"统计不到一起）

    private static final List<String> REACTION_TYPES = List.of(
            "发热反应",
            "过敏反应",
            "急性溶血反应",
            "迟发性溶血反应",
            "细菌污染反应",
            "循环超负荷",
            "输血相关急性肺损伤",
            "输血相关移植物抗宿主病",
            "其他"
    );

    public static List<String> reactionTypes() {
        return REACTION_TYPES;
    }

    public static boolean isValidReactionType(String type) {
        return type != null && REACTION_TYPES.contains(type.trim());
    }

    // 七、时长文案

    public static String durationText(Long minutes) {
        if (minutes == null) {
            return "—";
        }
        if (minutes < 60) {
            return minutes + " 分钟";
        }
        long h = minutes / 60;
        long m = minutes % 60;
        return m == 0 ? h + " 小时" : h + " 小时 " + m + " 分钟";
    }
}
