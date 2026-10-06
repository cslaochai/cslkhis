package com.his.medicaltech.support;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DRG 简化分组器（院内模拟，单点收口）。
 *
 * <p>规则（可解释、可复核，正式 CHS-DRG 分组方案接入后整体替换为分组器服务）：
 * 1) 主诊断 ICD 取前 3 位类目，映射到「手术组 / 非手术组」候选对（内置 RULES）；
 * 2) 非手术组内按住院天数/死亡标志细分「伴/不伴严重并发症」档（>=10 天或死亡 → 伴并发症档）；
 * 3) 未命中的类目 → QY 未入组（sim_status=2）。
 */
@Component
public class DrgGrouper {

    public static final String QY_CODE = "QY";
    /**
     * ICD 类目 → 非手术组编码（普通档 / 重症档，双档组用数组）
     */
    private static final Map<String, String[]> NON_SURGERY = new LinkedHashMap<>();
    /**
     * ICD 类目 → 手术组编码
     */
    private static final Map<String, String> SURGERY = new LinkedHashMap<>();

    static {
        NON_SURGERY.put("J18", new String[]{"ES35", "ES31"}); // 肺炎
        NON_SURGERY.put("J15", new String[]{"ES33", "ES31"}); // 细菌性肺炎
        NON_SURGERY.put("J11", new String[]{"ES35", "ES31"}); // 病毒性肺炎
        NON_SURGERY.put("J44", new String[]{"ET41", "ET41"}); // COPD
        NON_SURGERY.put("J45", new String[]{"ET41", "ET41"}); // 哮喘
        NON_SURGERY.put("I50", new String[]{"FS23", "FS21"}); // 心衰
        NON_SURGERY.put("K29", new String[]{"GT21", "GT21"}); // 胃炎
        NON_SURGERY.put("K80", new String[]{"GE21", "GE21"}); // 胆石症（非手术）
        NON_SURGERY.put("E11", new String[]{"IE19", "IE19"}); // 2 型糖尿病
        NON_SURGERY.put("E78", new String[]{"IE19", "IE19"}); // 脂代谢紊乱
        NON_SURGERY.put("I63", new String[]{"NB13", "NB13"}); // 脑梗死
        NON_SURGERY.put("N39", new String[]{"RE19", "RE19"}); // 泌尿系感染
        SURGERY.put("K80", "GC19");   // 胆囊切除
        SURGERY.put("J18", "JJ29");   // 呼吸系统其他手术
        SURGERY.put("K29", "KE19");   // 消化系统其他手术
    }

    /**
     * 分组。icdCode 允许带小数点（J18.9 → J18）；空码直接 QY。
     */
    public GroupResult group(String icdCode, boolean surgery, Integer inpatientDays, boolean death) {
        if (!StringUtils.hasText(icdCode)) {
            return new GroupResult(QY_CODE, null, null, "主诊断编码为空，无法入组");
        }
        String cat = icdCode.trim().split("\\.")[0];
        if (cat.length() > 3) {
            cat = cat.substring(0, 3);
        }
        String mdc = mdcOf(cat);
        if (surgery) {
            String code = SURGERY.get(cat);
            if (code != null) {
                return new GroupResult(code, null, mdc, "主诊断 " + cat + " + 手术 → 手术组");
            }
            return new GroupResult(QY_CODE, null, mdc, "手术但类目 " + cat + " 无对应手术组，未入组");
        }
        String[] pair = NON_SURGERY.get(cat);
        if (pair == null) {
            return new GroupResult(QY_CODE, null, mdc, "类目 " + cat + " 未配置分组规则，未入组");
        }
        String normal = pair[0];
        String severe = pair.length > 1 ? pair[1] : pair[0];
        boolean heavy = death || (inpatientDays != null && inpatientDays >= 10);
        String picked = heavy ? severe : normal;
        String note = heavy
                ? "主诊断 " + cat + " + 住院天数/死亡达重症档 → " + picked
                : "主诊断 " + cat + " 常规档 → " + picked;
        return new GroupResult(picked, null, mdc, note);
    }

    /**
     * 类目首字母 → MDC 大类（粗映射，仅用于展示）
     */
    private String mdcOf(String cat) {
        if (cat == null || cat.isEmpty()) {
            return null;
        }
        return switch (cat.charAt(0)) {
            case 'A', 'B' -> "I";
            case 'C', 'D' -> "M";
            case 'E' -> "I";
            case 'F', 'G' -> "N";
            case 'H' -> "H";
            case 'I' -> "F";
            case 'J' -> "E";
            case 'K' -> "G";
            case 'N' -> "R";
            case 'O' -> "O";
            case 'P' -> "P";
            case 'Q' -> "Q";
            default -> null;
        };
    }

    /**
     * 单条分组结果（纯值对象，避免组表耦合）
     */
    public record GroupResult(String drgCode, String drgName, String mdc, String ruleNote) {
    }
}
