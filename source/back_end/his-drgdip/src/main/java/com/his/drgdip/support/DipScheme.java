package com.his.drgdip.support;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DIP 病种库的一份只读快照：核心病种（主诊断 → 手术操作组合）+ 综合病种（主诊断前缀归组）+ 辅助目录系数。
 *
 * <p>与 DRG 的层级两跳模型不同，DIP 是「主诊断 + 手术操作直接组合成病种」的平铺匹配：
 * 核心病种按目录行的主诊断整码 + 手术操作整码精确匹配；进不了核心病种的病例按主诊断前缀落入综合病种。
 *
 * @param coreByDiag     归一主诊断编码 → 核心病种行（含手术操作，可空=内科/保守治疗病种）
 * @param comprehensive  综合病种行（已按前缀码长度降序，最长前缀优先归组）
 * @param auxByDisease   病种编码 → 辅助目录规则（key 为空串=适用全部病种的全局规则）
 */
public record DipScheme(Map<String, List<DiseaseNode>> coreByDiag,
                        List<DiseaseNode> comprehensive,
                        Map<String, List<AuxRule>> auxByDisease) {

    /**
     * 病种目录一行（核心病种或综合病种）。
     *
     * @param diagMatch 主诊断匹配方式（1-整码 2-前缀；综合病种一般为前缀归组）
     */
    public record DiseaseNode(String diseaseCode, String diseaseName, Integer diseaseType,
                              String mainDiagCode, String operCode, Integer diagMatch,
                              BigDecimal baseScore, String source, String version, Integer status) {
    }

    /**
     * 辅助目录规则一行（命中后结算分值 = 基准分值 × 系数）。
     *
     * @param diseaseCode 绑定病种编码（null=适用全部病种的全局规则）
     * @param auxType     1-年龄 2-合并症并发症 3-肿瘤严重程度/恶性程度 4-其他病例属性
     * @param auxCode     属性条件 ICD 码（合并症/肿瘤用；年龄型为空）
     */
    public record AuxRule(String diseaseCode, Integer auxType, String auxCode,
                          Integer ageMin, Integer ageMax, BigDecimal coeff) {
    }

    public boolean empty() {
        return coreByDiag.isEmpty() && comprehensive.isEmpty();
    }

    /**
     * 主诊断下的核心病种行（含手术操作组合；无手术操作的病种行也在此列表里，operCode 为 null）
     */
    public List<DiseaseNode> coreOf(String diag) {
        return coreByDiag.getOrDefault(diag, List.of());
    }

    /**
     * 综合病种最长前缀匹配：前缀码越长越具体越优先（装载时已按前缀码长度降序）。
     */
    public DiseaseNode comprehensiveMatch(String diag) {
        for (DiseaseNode node : comprehensive) {
            if (diag.startsWith(node.mainDiagCode())) {
                return node;
            }
        }
        return null;
    }

    /**
     * 某病种可用的辅助目录规则（先病种专属，再全局规则）。
     */
    public List<AuxRule> auxOf(String diseaseCode) {
        if (diseaseCode == null) {
            return auxByDisease.getOrDefault("", List.of());
        }
        List<AuxRule> own = auxByDisease.getOrDefault(diseaseCode, List.of());
        List<AuxRule> global = auxByDisease.getOrDefault("", List.of());
        if (own.isEmpty()) {
            return global;
        }
        if (global.isEmpty()) {
            return own;
        }
        return java.util.stream.Stream.concat(own.stream(), global.stream()).toList();
    }
}
