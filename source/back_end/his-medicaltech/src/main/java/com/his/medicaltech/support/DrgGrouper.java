package com.his.medicaltech.support;

import com.his.common.util.TextUtil;
import com.his.medicaltech.mapper.DrgSimMapper;
import com.his.medicaltech.vo.DrgGroupRowVO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * DRG 真实入组流程骨架（单点收口）。
 *
 * <p>流程：主诊断校验 → 先期分组特征 → MDC 粗映射（展示用）→ CC/MCC 综合级
 * → ADRG 匹配（主诊断亚目 + 主手术）→ DRG 细分组（CC/MCC + 年龄 + 性别 + 先期）。
 *
 * <p>分组方案数据来自 sys_drg_group（含扩列与匹配键）；该表为空或查不到时诚实返回 QY，不编造。
 * 官方分组方案 / CC-MCC 目录 / 排除表到位后，本骨架自动生效，无需再改代码。
 */
@Component
public class DrgGrouper {

    public static final String QY_CODE = "QY";

    private final DrgSimMapper drgSimMapper;
    private final CcMccService ccMccService;

    public DrgGrouper(DrgSimMapper drgSimMapper, CcMccService ccMccService) {
        this.drgSimMapper = drgSimMapper;
        this.ccMccService = ccMccService;
    }

    /**
     * 入组入参（真实 HIS 全量维度）。
     */
    public record GroupInput(
            String mainDiagCode,
            String mainOperCode,
            List<String> otherDiagCodes,
            Integer gender,
            Integer age,
            Integer ageUnit,
            Integer inpatientDays,
            Integer deathFlag,
            Integer ventilatorHours,
            Integer birthWeight,
            Integer isSurgery) {
    }

    /**
     * 单条分组结果（纯值对象，避免组表耦合）。
     */
    public record GroupResult(
            String drgCode, String drgName, String mdc, String adrgCode,
            BigDecimal weight, BigDecimal payStandard, String ruleNote, boolean grouped) {
    }

    public GroupResult group(GroupInput in) {
        if (!TextUtil.hasText(in.mainDiagCode())) {
            return qy("主诊断编码为空，无法入组");
        }
        String cat3 = icdPrefix3(in.mainDiagCode());
        boolean preGroup = isPreGroup(in);
        String mdc = mdcOf(cat3);
        CcMccService.CcLevel ccLevel = ccMccService.assess(in.mainDiagCode(), in.otherDiagCodes());

        List<DrgGroupRowVO> groups = drgSimMapper.groupList();
        if (groups.isEmpty()) {
            return qy("分组方案表 sys_drg_group 为空，未接入官方分组方案，无法入组");
        }

        String adrg = matchAdrg(groups, cat3, in.mainOperCode());
        if (adrg == null) {
            return qy("未匹配到 ADRG（主诊断 " + cat3
                    + (TextUtil.hasText(in.mainOperCode()) ? " + 主手术 " + in.mainOperCode() : "")
                    + " 分组方案未覆盖或缺失）");
        }

        DrgGroupRowVO drg = matchDrg(groups, adrg, ccLevel, in, preGroup);
        if (drg == null) {
            return qy("ADRG " + adrg + " 下未匹配到 DRG 细分组（年龄/性别/CC-MCC/先期维度不匹配）");
        }

        String realMdc = TextUtil.hasText(drg.getMdcCode()) ? drg.getMdcCode() : mdc;
        String note = "主诊断 " + cat3
                + (TextUtil.hasText(in.mainOperCode()) ? " + 主手术 " + in.mainOperCode() : "")
                + " → ADRG " + adrg + " → DRG " + drg.getDrgCode()
                + (ccLevel == CcMccService.CcLevel.MCC ? "（伴MCC）"
                    : ccLevel == CcMccService.CcLevel.CC ? "（伴CC）" : "")
                + (preGroup ? "（先期特征）" : "");
        return new GroupResult(drg.getDrgCode(), drg.getDrgName(), realMdc, adrg,
                drg.getWeight(), drg.getPayStandard(), note, true);
    }

    private GroupResult qy(String reason) {
        return new GroupResult(QY_CODE, null, null, null, null, null, reason, false);
    }

    /**
     * 先期分组特征（纯算法；具体先期 DRG 码由官方方案表提供）。
     */
    private boolean isPreGroup(GroupInput in) {
        boolean vent = in.ventilatorHours() != null && in.ventilatorHours() >= 96
                && Integer.valueOf(1).equals(in.isSurgery());
        boolean nbw = in.birthWeight() != null && in.birthWeight() < 1500;
        boolean trauma = in.mainDiagCode() != null
                && (in.mainDiagCode().toUpperCase().startsWith("S")
                    || in.mainDiagCode().toUpperCase().startsWith("T"))
                && TextUtil.hasText(in.mainOperCode());
        return vent || nbw || trauma;
    }

    /**
     * ADRG 匹配：有主手术优先外科（oper_match 命中），无主手术走内科（oper_match 为空）。
     */
    private String matchAdrg(List<DrgGroupRowVO> groups, String cat3, String mainOper) {
        for (DrgGroupRowVO g : groups) {
            if (!containsPrefix(g.getDiagMatch(), cat3)) continue;
            boolean hasOper = TextUtil.hasText(mainOper);
            boolean operHit = containsPrefix(g.getOperMatch(), mainOper);
            if (hasOper) {
                if (operHit) return g.getAdrgCode();
            } else {
                if (!TextUtil.hasText(g.getOperMatch())) return g.getAdrgCode();
            }
        }
        return null;
    }

    /**
     * DRG 细分组：ADRG 下按 先期/性别/年龄/CC-MCC 维度全匹配，优先级 MCC > CC > 无。
     */
    private DrgGroupRowVO matchDrg(List<DrgGroupRowVO> groups, String adrg,
                                   CcMccService.CcLevel cc, GroupInput in, boolean preGroup) {
        DrgGroupRowVO mccHit = null, ccHit = null, noneHit = null;
        for (DrgGroupRowVO g : groups) {
            if (!adrg.equals(g.getAdrgCode())) continue;
            if (toBool(g.getPreGroupFlag()) != preGroup) continue;
            if (!genderMatch(g, in)) continue;
            if (!ageMatch(g, in)) continue;
            int flag = g.getCcMccFlag() == null ? 0 : g.getCcMccFlag();
            if (flag == 2 && cc == CcMccService.CcLevel.MCC) mccHit = g;
            else if (flag == 1 && (cc == CcMccService.CcLevel.CC || cc == CcMccService.CcLevel.MCC)) ccHit = g;
            else if (flag == 0 && cc == CcMccService.CcLevel.NONE) noneHit = g;
        }
        if (mccHit != null) return mccHit;
        if (ccHit != null) return ccHit;
        return noneHit;
    }

    private boolean genderMatch(DrgGroupRowVO g, GroupInput in) {
        if (g.getGenderLimit() == null || g.getGenderLimit() == 0) return true;
        return in.gender() != null && in.gender().equals(g.getGenderLimit());
    }

    private boolean ageMatch(DrgGroupRowVO g, GroupInput in) {
        if (g.getAgeTier() == null || g.getAgeTier() == 0) return true;
        int tier = g.getAgeTier();
        if (tier == 1) { // ≤6 岁
            if (Integer.valueOf(1).equals(in.ageUnit())) return in.age() != null && in.age() <= 6;
            return in.ageUnit() != null && in.ageUnit() != 1; // 月/天 视为婴儿
        }
        if (tier == 2) { // ≥70 岁
            return Integer.valueOf(1).equals(in.ageUnit()) && in.age() != null && in.age() >= 70;
        }
        if (tier == 3) { // 新生儿
            return in.birthWeight() != null
                    || (Integer.valueOf(3).equals(in.ageUnit()) && in.age() != null && in.age() < 28);
        }
        return true;
    }

    private Boolean toBool(Integer v) {
        return v != null && v == 1;
    }

    /**
     * 匹配键（逗号分隔前缀）是否包含 code（去点后前缀比较）。
     */
    private boolean containsPrefix(String matchStr, String code) {
        if (!TextUtil.hasText(matchStr) || !TextUtil.hasText(code)) return false;
        String c = icdPrefix3(code);
        for (String p : matchStr.split(",")) {
            String pp = icdPrefix3(p.trim());
            if (!pp.isEmpty() && c.startsWith(pp)) return true;
        }
        return false;
    }

    /**
     * ICD 编码取前 3 位（去点），如 J18.9 / J18 → J18。
     */
    public static String icdPrefix3(String code) {
        if (code == null) return "";
        String s = code.trim().replace(".", "");
        return s.length() > 3 ? s.substring(0, 3) : s;
    }

    /**
     * MDC 粗映射（展示用；真实 MDC 由查到的分组行携带）。
     */
    private String mdcOf(String cat) {
        if (cat == null || cat.isEmpty()) return null;
        return switch (cat.charAt(0)) {
            case 'A', 'B' -> "S";
            case 'C', 'D' -> "R";
            case 'E' -> "K";
            case 'F', 'G' -> "B";
            case 'H' -> "C";
            case 'I' -> "F";
            case 'J' -> "E";
            case 'K' -> "G";
            case 'L' -> "J";
            case 'M' -> "I";
            case 'N' -> "L";
            case 'O' -> "O";
            case 'P' -> "P";
            case 'Q' -> "P";
            case 'S', 'T' -> "T";
            default -> null;
        };
    }
}
