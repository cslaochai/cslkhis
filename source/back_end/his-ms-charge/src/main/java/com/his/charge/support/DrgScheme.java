package com.his.charge.support;

import com.his.common.enums.DrgCcLevelEnum;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 分组方案的一份只读快照：方案头 + 三级目录（MDC → ADRG → DRG）+ 规则引用的码集合 + 并发症合并症目录。
 *
 * <p>官方模型是「两跳」：规则里写的是集合编号，集合编号再展开成一批精确 ICD 码；
 * 目录行的规则原文是入组的唯一事实，本对象只负责把这两跳查出来，判定顺序留在 {@link DrgGrouper}。
 *
 * <p>官方 3.0 表里的行不是同一类：{@link #ROW_QY} 是该 MDC 的「其他手术歧义组」（合法结果码，
 * 如 BQY），{@link #ROW_PLACEHOLDER} 是该 MDC 的「未入组」占位（如 B00/B000，绝不能当真实病组返回）。
 * 行类型由库列 row_kind 显式给出，不再靠「名字为空」猜测。
 *
 * @param header      方案头（标准来源、规则可信度、官方预期规模）
 * @param mdcs        MDC 目录（按 group_step、sort_no 排序，先期分组在最前）
 * @param adrgByMdc   MDC 编码 → ADRG 目录（已按官方排序）
 * @param drgByAdrg   ADRG 编码 → DRG 细分组（已按官方排序）
 * @param sets        集合编号 → 归一后的精确码集
 * @param ccmcc       归一诊断编码 → 级别与该行挂的排除组
 * @param missingRefs 规则引用但库内不存在的集合编号（健康检查必须报出）
 */
public record DrgScheme(Header header,
                        List<MdcNode> mdcs,
                        Map<String, List<AdrgNode>> adrgByMdc,
                        Map<String, List<DrgNode>> drgByAdrg,
                        Map<String, Set<String>> sets,
                        Map<String, CcEntry> ccmcc,
                        Set<String> missingRefs) {

    /**
     * 伪集合：并发症合并症目录里级别为 MCC 的诊断全集，事实不在集合表里
     */
    public static final String REF_MCC = "MCC";
    /**
     * 伪集合：并发症合并症目录里级别为 CC 的诊断全集
     */
    public static final String REF_CC = "CC";

    /**
     * 正常入组行
     */
    public static final int ROW_NORMAL = 0;
    /**
     * QY 歧义行（官方 ?QY：本 MDC 其他手术歧义组）
     */
    public static final int ROW_QY = 1;
    /**
     * 未入组占位行（官方 ?00 / ?000：该 MDC 未入组，结果码统一为 0000）
     */
    public static final int ROW_PLACEHOLDER = 2;

    /**
     * @param ruleSource 规则来源（OFFICIAL/INFERRED/LOCAL）
     * @param verified   是否双人复核且与官方分组器回归一致
     */
    public record Header(String version, String schemeCode, String schemeName, String standardNo,
                         String ruleSource, boolean verified,
                         Integer expectedMdc, Integer expectedAdrg, Integer expectedDrg,
                         Integer status, boolean isDefault) {
    }

    public record MdcNode(String code, String name, DrgRule rule, Integer sortNo,
                          boolean preGroup, Integer groupStep, String ruleSource, boolean verified) {
    }

    public record AdrgNode(String code, String name, String mdcCode, DrgRule rule,
                           Integer sortNo, Integer rowKind) {
    }

    public record DrgNode(String code, String name, String adrgCode, DrgRule rule,
                          BigDecimal weight, BigDecimal payStandard, Integer sortNo, Integer rowKind) {
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
     * 真组数量（不含 QY / 占位伪行），用于和方案头的官方预期数量比对。
     */
    public int realAdrgCount() {
        return (int) adrgByMdc.values().stream()
                .flatMap(List::stream)
                .filter(a -> kindOf(a.rowKind()) == ROW_NORMAL)
                .count();
    }

    public int realDrgCount() {
        return (int) drgByAdrg.values().stream()
                .flatMap(List::stream)
                .filter(d -> kindOf(d.rowKind()) == ROW_NORMAL)
                .count();
    }

    /**
     * 规模是否与官方预期一致，且规则引用的集合全部存在。
     *
     * <p>注意「规模一致」不等于「规则可信」：2.0 的规则是院内推导，规模可以对上，
     * 但 {@link Header#verified()} 仍是 false，必须另看健康检查的 verified 字段。
     */
    public boolean complete() {
        if (header == null) {
            return false;
        }
        return missingRefs.isEmpty()
                && header.expectedMdc() != null && header.expectedMdc() == mdcs.size()
                && header.expectedAdrg() != null && header.expectedAdrg() == realAdrgCount()
                && header.expectedDrg() != null && header.expectedDrg() == realDrgCount();
    }

    private static int kindOf(Integer rowKind) {
        return rowKind == null ? ROW_NORMAL : rowKind;
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
}
