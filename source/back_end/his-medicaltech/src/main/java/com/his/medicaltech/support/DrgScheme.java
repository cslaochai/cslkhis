package com.his.medicaltech.support;

import com.his.common.util.TextUtil;
import com.his.medicaltech.enums.DrgCcLevelEnum;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 分组方案的一份只读快照：三级目录（MDC → ADRG → DRG）+ 规则引用的码集合 + 并发症合并症目录。
 *
 * <p>官方模型是「两跳」：规则里写的是集合编号，集合编号再展开成一批精确 ICD 码；
 * 目录行的规则原文是入组的唯一事实，本对象只负责把这两跳查出来，判定顺序留在 {@link DrgGrouper}。
 *
 * @param mdcs      MDC 目录（已按官方排序，先期分组在最前）
 * @param adrgByMdc MDC 编码 → ADRG 目录（已按官方排序）
 * @param drgByAdrg ADRG 编码 → DRG 细分组（已按官方排序）
 * @param sets      集合编号 → 归一后的精确码集
 * @param ccmcc     归一诊断编码 → 级别与该行挂的排除组
 */
public record DrgScheme(List<MdcNode> mdcs,
                        Map<String, List<AdrgNode>> adrgByMdc,
                        Map<String, List<DrgNode>> drgByAdrg,
                        Map<String, Set<String>> sets,
                        Map<String, CcEntry> ccmcc) {

    /**
     * 伪集合：并发症合并症目录里级别为 MCC 的诊断全集，事实不在集合表里
     */
    public static final String REF_MCC = "MCC";
    /**
     * 伪集合：并发症合并症目录里级别为 CC 的诊断全集
     */
    public static final String REF_CC = "CC";

    public record MdcNode(String code, String name, DrgRule rule, Integer sortNo) {
    }

    public record AdrgNode(String code, String name, String mdcCode, DrgRule rule, Integer sortNo) {
    }

    public record DrgNode(String code, String name, String adrgCode, DrgRule rule,
                          BigDecimal weight, BigDecimal payStandard, Integer sortNo) {
    }

    /**
     * @param level     该诊断的并发症合并症级别
     * @param exclGroup 排除组编号：主诊断落进这个组时，该诊断不再享受本级（同一条 CC 行可挂不同排除组）
     */
    public record CcEntry(DrgCcLevelEnum level, String exclGroup) {
    }

    public boolean empty() {
        return mdcs.isEmpty();
    }

    public List<AdrgNode> adrgsOf(String mdcCode) {
        return adrgByMdc.getOrDefault(mdcCode, List.of());
    }

    public List<DrgNode> drgsOf(String adrgCode) {
        return drgByAdrg.getOrDefault(adrgCode, List.of());
    }

    public Set<String> members(String setCode) {
        return sets.getOrDefault(setCode, Set.of());
    }

    /**
     * 引用命中判定：MCC/CC 两个伪集合按目录级别判，其余按集合表的精确码判。
     *
     * <p>码为空串（该维度首页没填）一律不命中——没数据不等于落在集合里。
     */
    public boolean matchRef(String ref, String code, String mainDiag) {
        if (code.isEmpty()) {
            return false;
        }
        if (REF_MCC.equals(ref)) {
            return ccLevel(code, mainDiag) == DrgCcLevelEnum.MCC;
        }
        if (REF_CC.equals(ref)) {
            return ccLevel(code, mainDiag) == DrgCcLevelEnum.CC;
        }
        return members(ref).contains(code);
    }

    /**
     * 某诊断在该主诊断下的并发症合并症级别；目录没这条码就是无级别。
     */
    public DrgCcLevelEnum ccLevel(String icdCode, String mainDiag) {
        CcEntry entry = ccmcc.get(icdCode);
        if (entry == null) {
            return DrgCcLevelEnum.NONE;
        }
        if (entry.exclGroup() != null && members(entry.exclGroup()).contains(mainDiag)) {
            return DrgCcLevelEnum.NONE;
        }
        return entry.level();
    }

    /**
     * 官方表只在 MDC 层用「名字为空」表达分隔行（编码是一串 0，不带任何规则），装载时按这个判掉。
     * ADRG/DRG 层的空名字行不是占位：它们是每组的「其他手术」歧义档与末组，
     * 规则原文照走，只是官方没给名字。
     */
    public static boolean named(String name) {
        return TextUtil.hasText(name);
    }
}
