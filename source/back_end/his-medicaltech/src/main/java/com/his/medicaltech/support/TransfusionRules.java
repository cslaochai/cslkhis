package com.his.medicaltech.support;

import com.his.medicaltech.enums.BloodComponentEnum;
import com.his.medicaltech.enums.BloodTypeEnum;
import com.his.medicaltech.enums.RhTypeEnum;
import com.his.medicaltech.enums.TransfusionApproveStatusEnum;
import com.his.medicaltech.enums.TransfusionStatusEnum;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 输血闭环的<b>临床判定与计算口径</b>：ABO/Rh 相容性、用血量折算、审批闸门、时长文案。
 *
 * <p><b>码值 → 文案不在这里</b>：一律走对应枚举（{@link TransfusionStatusEnum} /
 * {@link TransfusionApproveStatusEnum} / {@link BloodComponentEnum} /
 * {@link BloodTypeEnum} / {@link RhTypeEnum} 等）的 {@code getText}，
 * 需要保留原始脏值时走它们的 {@code labelOrUnknown}。
 *
 * <p>本类里<b>最不能出错的一段</b>是 {@link #isAboCompatible} / {@link #isRhCompatible}：
 * ABO 不相容输注是<b>致死性</b>医疗差错，一旦放过，后果不是「数据不好看」。
 * 因此按「宁严不宽」实现：血型取不到 / 品种不认识 → 一律判<b>不相容</b>，
 * 由调用方给出「为什么被拒」的文案。宽进严出的反例在这里是致命的。
 */
public final class TransfusionRules {

    private TransfusionRules() {
    }

    // 一、用血分级审批折算（sql/93；《医疗机构临床用血管理办法》）
    //   <400ml 上级医师（主治及以上）；400~799ml 科主任；≥800ml 医务科。
    //   折算口径：ml 直取；1U≈200ml（红细胞/全血）；1治疗量≈250ml（血小板/冷沉淀）；
    //   折不出来一律按最高级 —— 宁可多审一级，不可少审。

    /** 折算：1U（红细胞/全血）≈ 200ml */
    public static final int ML_PER_UNIT = 200;
    /** 折算：1治疗量（血小板/冷沉淀）≈ 250ml */
    public static final int ML_PER_THERAPEUTIC = 250;

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
        if (TransfusionApproveStatusEnum.APPROVED.is(approveStatus)) {
            return true;
        }
        return TransfusionApproveStatusEnum.MAKEUP_PENDING.is(approveStatus)
                && Integer.valueOf(1).equals(isEmergency);
    }

    /** 是否"未完成"（待配血 / 已配血 / 已发血 / 输注中）—— 工作台角标用 */
    public static boolean isUnfinished(Integer status) {
        return TransfusionStatusEnum.PENDING_CROSSMATCH.is(status)
                || TransfusionStatusEnum.CROSSMATCHED.is(status)
                || TransfusionStatusEnum.ISSUED.is(status)
                || TransfusionStatusEnum.INFUSING.is(status);
    }

    /** 是否"在途"（已配血 / 已发血 / 输注中）—— 防重复申请用 */
    public static boolean isActive(Integer status) {
        return TransfusionStatusEnum.CROSSMATCHED.is(status)
                || TransfusionStatusEnum.ISSUED.is(status)
                || TransfusionStatusEnum.INFUSING.is(status);
    }

    // 二、相容性规则（本类最要紧的一段）

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
        String p = BloodTypeEnum.normalizeAbo(patientAbo);
        String b = BloodTypeEnum.normalizeAbo(bagAbo);
        if (p == null || b == null) {
            return false;
        }
        if (BloodComponentEnum.isRedCellGroup(component)) {
            return RBC_ACCEPT.getOrDefault(p, Set.of()).contains(b);
        }
        if (BloodComponentEnum.isPlasmaGroup(component)) {
            return PLASMA_ACCEPT.getOrDefault(p, Set.of()).contains(b);
        }
        // 血小板 / 其他：不做 ABO 硬拦（但血型字段仍必须填对，见 BloodTypeEnum.isValidAbo 校验）
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
        String p = RhTypeEnum.normalizeRh(patientRh);
        String b = RhTypeEnum.normalizeRh(bagRh);
        if (p == null || b == null) {
            return false;
        }
        if (RhTypeEnum.RH_NEGATIVE.equals(p)) {
            return RhTypeEnum.RH_NEGATIVE.equals(b);
        }
        if (RhTypeEnum.RH_POSITIVE.equals(p)) {
            return RhTypeEnum.RH_POSITIVE.equals(b) || RhTypeEnum.RH_NEGATIVE.equals(b);
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
                    + BloodComponentEnum.getText(component) + "）");
        }
        if (!isRhCompatible(patientRh, bagRh)) {
            reasons.add("Rh 不相容（受血者 Rh " + patientRh + " 性不可接受 Rh " + bagRh + " 性血袋）");
        }
        if (reasons.isEmpty()) {
            return null;
        }
        return "血袋 " + bagNo + "：" + String.join("；", reasons);
    }

    /** 中文血型文案（列表展示用，如「A 型 Rh(+)」）；两个码都空时给「—」 */
    public static String bloodTypeText(String abo, String rh) {
        if (abo == null && rh == null) {
            return "—";
        }
        String a = abo == null ? "?" : abo;
        String r = RhTypeEnum.getRhText(rh);
        return a + " 型 " + (r.isEmpty() ? "Rh(?)" : r);
    }

    /** 时长文案（分钟 → "1 小时 20 分钟"） */
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
