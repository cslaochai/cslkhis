package com.his.charge.drgdip.support;

import com.his.common.util.TextUtil;
import com.his.charge.drgdip.config.DrgSchemeConfig;
import com.his.charge.drgdip.enums.DrgCcLevelEnum;
import com.his.charge.drgdip.mapper.DrgSimMapper;
import com.his.charge.drgdip.vo.CcMccRowVO;
import com.his.charge.drgdip.vo.DrgAdrgRowVO;
import com.his.charge.drgdip.vo.DrgGroupRowVO;
import com.his.charge.drgdip.vo.DrgMdcRowVO;
import com.his.charge.drgdip.vo.DrgSetRowVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分组方案快照缓存：按版本各装载一份三级目录 + 码集合 + 并发症合并症目录，供入组反复判定。
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

    private DrgScheme load(String version) {
        Map<String, Set<String>> sets = loadSets(version);
        Map<String, DrgScheme.CcEntry> ccmcc = loadCcMcc(version);
        Map<String, List<DrgScheme.DrgNode>> drgByAdrg = new LinkedHashMap<>();
        for (DrgGroupRowVO row : drgSimMapper.groupList(version)) {
            DrgRule rule = parseOrDrop(row.getDrgRule(), "DRG", row.getDrgCode());
            drgByAdrg.computeIfAbsent(row.getAdrgCode(), k -> new ArrayList<>())
                    .add(new DrgScheme.DrgNode(row.getDrgCode(), row.getDrgName(), row.getAdrgCode(), rule,
                            row.getWeight(), row.getPayStandard(), row.getSortNo()));
        }

        Map<String, List<DrgScheme.AdrgNode>> adrgByMdc = new LinkedHashMap<>();
        for (DrgAdrgRowVO row : drgSimMapper.adrgList(version)) {
            DrgRule rule = parseOrDrop(row.getAdrgRule(), "ADRG", row.getAdrgCode());
            adrgByMdc.computeIfAbsent(row.getMdcCode(), k -> new ArrayList<>())
                    .add(new DrgScheme.AdrgNode(row.getAdrgCode(), row.getAdrgName(), row.getMdcCode(), rule,
                            row.getSortNo()));
        }

        List<DrgScheme.MdcNode> mdcs = new ArrayList<>();
        for (DrgMdcRowVO row : drgSimMapper.mdcList(version)) {
            // 名字为空的 MDC 行是目录里的分隔行，它又不带规则，收下就等于「任何病例都先进这个 MDC」
            if (!DrgScheme.named(row.getMdcName())) {
                continue;
            }
            DrgRule rule = parseOrDrop(row.getMdcRule(), "MDC", row.getMdcCode());
            mdcs.add(new DrgScheme.MdcNode(row.getMdcCode(), row.getMdcName(), rule, row.getSortNo()));
        }

        log.info("DRG 分组方案[{}]装载完成：MDC {}，ADRG {}，DRG 分档 {}，码集合 {} 组，并发症目录 {} 条",
                version, mdcs.size(), adrgByMdc.values().stream().mapToLong(List::size).sum(),
                drgByAdrg.values().stream().mapToLong(List::size).sum(), sets.size(), ccmcc.size());
        return new DrgScheme(mdcs, adrgByMdc, drgByAdrg, sets, ccmcc);
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
     * ADRG 层的空规则行是该 MDC 的末组，DRG 层的空规则行是该 ADRG 的兜底档），解析失败则丢掉这一行并留警告，
     * 绝不能把它当兜底档——那会把整组病例错入到一个规则根本没读懂的组里。
     */
    private DrgRule parseOrDrop(String text, String level, String code) {
        if (!TextUtil.hasText(text)) {
            return null;
        }
        try {
            return DrgRuleParser.parse(text, code);
        } catch (IllegalArgumentException e) {
            log.warn("分组规则解析失败，该行本次启动不参与入组：{} {}，原因：{}", level, code, e.getMessage());
            return null;
        }
    }
}
