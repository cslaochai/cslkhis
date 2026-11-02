package com.his.charge.support;

import com.his.charge.config.DrgSchemeConfig;
import com.his.common.enums.DrgCcLevelEnum;
import com.his.charge.mapper.DrgSimMapper;
import com.his.charge.vo.CcMccRowVO;
import com.his.charge.vo.DrgAdrgRowVO;
import com.his.charge.vo.DrgGroupRowVO;
import com.his.charge.vo.DrgMdcRowVO;
import com.his.charge.vo.DrgSchemeRowVO;
import com.his.charge.vo.DrgSetRowVO;
import com.his.common.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分组方案快照缓存：按版本各装载一份「方案头 + 三级目录 + 码集合 + 并发症合并症目录」，供入组反复判定。
 *
 * <p>装载阶段同时做两件体检：规则引用的集合是否都在库内、真组数量是否与方案头的官方预期一致。
 * 体检不过不阻止启动（2.0 本来就缺码表），但会把问题写进 {@link DrgScheme#missingRefs()} 并在日志里点名，
 * 由健康检查接口/页面显式暴露，避免「残缺方案看起来能跑」。
 */
@Slf4j
@Component
public class DrgSchemeCache {

    private final DrgSimMapper drgSimMapper;
    private final DrgSchemeConfig drgSchemeConfig;

    public DrgSchemeCache(DrgSimMapper drgSimMapper, DrgSchemeConfig drgSchemeConfig) {
        this.drgSimMapper = drgSimMapper;
        this.drgSchemeConfig = drgSchemeConfig;
    }

    private final Map<String, DrgScheme> schemes = new ConcurrentHashMap<>();

    public DrgScheme get() {
        return get(drgSchemeConfig.getCurrenttVersion());
    }

    /**
     * 取指定版本的方案快照，首次访问该版本时惰性装载并缓存。
     */
    public DrgScheme get(String version) {
        String v = version == null || version.isBlank() ? drgSchemeConfig.getCurrenttVersion() : version;
        return schemes.computeIfAbsent(v, this::load);
    }

    /**
     * 当前默认启用的库内 version 字符串
     */
    public String getDefaultVersion() {
        return drgSchemeConfig.getCurrenttVersion();
    }

    /**
     * 丢弃某版本快照（方案头或目录数据被改后调用，下次访问重新装载）
     */
    public void evict(String version) {
        if (version == null || version.isBlank()) {
            schemes.clear();
        } else {
            schemes.remove(version);
        }
    }

    private DrgScheme load(String version) {
        DrgSchemeRowVO h = drgSimMapper.scheme(version);
        DrgScheme.Header header = headerOf(version, h);

        Map<String, Set<String>> sets = loadSets(version);
        Map<String, DrgScheme.CcEntry> ccmcc = loadCcMcc(version);

        Set<String> refs = new HashSet<>();
        Map<String, List<DrgScheme.DrgNode>> drgByAdrg = new LinkedHashMap<>();
        for (DrgGroupRowVO row : drgSimMapper.groupList(version)) {
            DrgRule rule = parseOrDrop(row.getDrgRule(), "DRG", row.getDrgCode(), refs);
            drgByAdrg.computeIfAbsent(row.getAdrgCode(), k -> new ArrayList<>())
                    .add(new DrgScheme.DrgNode(row.getDrgCode(), row.getDrgName(), row.getAdrgCode(), rule,
                            row.getWeight(), row.getPayStandard(), row.getSortNo(), row.getRowKind()));
        }

        Map<String, List<DrgScheme.AdrgNode>> adrgByMdc = new LinkedHashMap<>();
        for (DrgAdrgRowVO row : drgSimMapper.adrgList(version)) {
            DrgRule rule = parseOrDrop(row.getAdrgRule(), "ADRG", row.getAdrgCode(), refs);
            adrgByMdc.computeIfAbsent(row.getMdcCode(), k -> new ArrayList<>())
                    .add(new DrgScheme.AdrgNode(row.getAdrgCode(), row.getAdrgName(), row.getMdcCode(), rule,
                            row.getSortNo(), row.getRowKind()));
        }

        List<DrgScheme.MdcNode> mdcs = new ArrayList<>();
        for (DrgMdcRowVO row : drgSimMapper.mdcList(version)) {
            DrgRule rule = parseOrDrop(row.getMdcRule(), "MDC", row.getMdcCode(), refs);
            mdcs.add(new DrgScheme.MdcNode(row.getMdcCode(), row.getMdcName(), rule, row.getSortNo(),
                    row.getPreGroupFlag() != null && row.getPreGroupFlag() == 1,
                    row.getGroupStep() == null ? 5 : row.getGroupStep(),
                    row.getRuleSource(), row.getVerified() != null && row.getVerified() == 1));
        }

        Set<String> missingRefs = new TreeSet<>();
        for (String ref : refs) {
            if (!sets.containsKey(ref)) {
                missingRefs.add(ref);
            }
        }

        DrgScheme scheme = new DrgScheme(header, mdcs, adrgByMdc, drgByAdrg, sets, ccmcc, missingRefs);
        report(version, scheme);
        return scheme;
    }

    private DrgScheme.Header headerOf(String version, DrgSchemeRowVO h) {
        if (h == null) {
            log.warn("DRG 方案头缺失：version={}，按未复核方案装载（请执行 321 增量 SQL 补 biz_drg_scheme）", version);
            return new DrgScheme.Header(version, version, version, null, "UNKNOWN", false,
                    null, null, null, null, false);
        }
        return new DrgScheme.Header(version, h.getSchemeCode(), h.getSchemeName(), h.getStandardNo(),
                h.getRuleSource(), h.getVerified() != null && h.getVerified() == 1,
                h.getExpectedMdc(), h.getExpectedAdrg(), h.getExpectedDrg(),
                h.getStatus(), h.getIsDefault() != null && h.getIsDefault() == 1);
    }

    private Map<String, Set<String>> loadSets(String version) {
        Map<String, Set<String>> sets = new HashMap<>();
        for (DrgSetRowVO row : drgSimMapper.setList(version)) {
            String setCode = TextUtil.trimToNull(row.getSetCode());
            String code = DrgCodes.norm(row.getIcdCode());
            if (setCode == null || code.isEmpty()) {
                continue;
            }
            sets.computeIfAbsent(setCode, k -> new HashSet<>()).add(code);
        }
        return sets;
    }

    private Map<String, DrgScheme.CcEntry> loadCcMcc(String version) {
        Map<String, DrgScheme.CcEntry> ccmcc = new HashMap<>();
        for (CcMccRowVO row : drgSimMapper.ccmccList(version)) {
            String code = DrgCodes.norm(row.getIcdCode());
            DrgCcLevelEnum level = DrgCcLevelEnum.fromCode(row.getCcLevel());
            if (code.isEmpty() || level == null || level == DrgCcLevelEnum.NONE) {
                continue;
            }
            ccmcc.put(code, new DrgScheme.CcEntry(level, TextUtil.trimToNull(row.getExclGroup())));
        }
        return ccmcc;
    }

    /**
     * 规则原文编译：空白是「该行不带条件」的合法形状（MDC 层的先期分组靠子 ADRG 判定，
     * ADRG 层的空规则行是该 MDC 的未入组占位，DRG 层的空规则行是该 ADRG 的兜底档），
     * 解析失败则丢掉这一行并留警告，绝不能把它当兜底档——那会把整组病例错入到一个规则根本没读懂的组里。
     */
    private DrgRule parseOrDrop(String text, String level, String code, Set<String> refs) {
        if (!TextUtil.hasText(text)) {
            return null;
        }
        try {
            DrgRule rule = DrgRuleParser.parse(text, code);
            if (rule != null) {
                refs.addAll(rule.refs());
            }
            return rule;
        } catch (IllegalArgumentException e) {
            log.warn("分组规则解析失败，该行本次启动不参与入组：{} {}，原因：{}", level, code, e.getMessage());
            return null;
        }
    }

    private void report(String version, DrgScheme scheme) {
        DrgScheme.Header h = scheme.header();
        log.info("DRG 分组方案[{}]装载完成：MDC {}，ADRG 共 {}（真组 {}），DRG 共 {}（真组 {}），码集合 {} 组，并发症目录 {} 条",
                version, scheme.mdcs().size(),
                scheme.adrgByMdc().values().stream().mapToLong(List::size).sum(), scheme.realAdrgCount(),
                scheme.drgByAdrg().values().stream().mapToLong(List::size).sum(), scheme.realDrgCount(),
                scheme.sets().size(), scheme.ccmcc().size());
        if (!scheme.missingRefs().isEmpty()) {
            log.warn("DRG 分组方案[{}]规则引用了库内不存在的集合 {}，相关 ADRG/DRG 不会命中", version, scheme.missingRefs());
        }
        if (h.expectedMdc() != null && h.expectedMdc() != scheme.mdcs().size()) {
            log.warn("DRG 分组方案[{}] MDC 数量与方案头不一致：库内 {}，官方 {}", version, scheme.mdcs().size(), h.expectedMdc());
        }
        if (h.expectedAdrg() != null && h.expectedAdrg() != scheme.realAdrgCount()) {
            log.warn("DRG 分组方案[{}] ADRG 真组数量与方案头不一致：库内 {}，官方 {}", version, scheme.realAdrgCount(), h.expectedAdrg());
        }
        if (h.expectedDrg() != null && h.expectedDrg() != scheme.realDrgCount()) {
            log.warn("DRG 分组方案[{}] DRG 真组数量与方案头不一致：库内 {}，官方 {}", version, scheme.realDrgCount(), h.expectedDrg());
        }
        if (!h.verified() || "INFERRED".equalsIgnoreCase(h.ruleSource())) {
            log.warn("DRG 分组方案[{}]规则来源={}、verified={}：规则可信度不足，不得用于正式结算",
                    version, h.ruleSource(), h.verified());
        }
    }
}
