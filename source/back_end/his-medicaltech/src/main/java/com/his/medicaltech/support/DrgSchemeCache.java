package com.his.medicaltech.support;

import com.his.common.util.TextUtil;
import com.his.medicaltech.enums.DrgCcLevelEnum;
import com.his.medicaltech.mapper.DrgSimMapper;
import com.his.medicaltech.vo.CcMccRowVO;
import com.his.medicaltech.vo.DrgAdrgRowVO;
import com.his.medicaltech.vo.DrgGroupRowVO;
import com.his.medicaltech.vo.DrgMdcRowVO;
import com.his.medicaltech.vo.DrgSetRowVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 分组方案快照缓存：一次装载三级目录 + 码集合 + 并发症合并症目录，供入组反复判定。
 *
 * <p>为什么必须缓存：一份官方方案是 27 条 MDC、537 条 ADRG、870 条 DRG、12.8 万条集合码、
 * 7900 余条并发症目录。批量模拟原本每条首页都要把这几张表整个捞一遍并逐条比前缀。
 *
 * <p><b>改了方案数据（例如统筹区下发权重后重灌）需重启进程才生效</b>：这些是官方下发的配置表，
 * 重灌是运维动作，不是运行期编辑，所以不做刷新入口，只保留「首次用到才装载」这一条惰性路径。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DrgSchemeCache {

    private final DrgSimMapper drgSimMapper;

    private volatile DrgScheme scheme;

    public DrgScheme get() {
        DrgScheme current = scheme;
        if (current == null) {
            synchronized (this) {
                current = scheme;
                if (current == null) {
                    current = load();
                    scheme = current;
                }
            }
        }
        return current;
    }

    private DrgScheme load() {
        Map<String, Set<String>> sets = loadSets();
        Map<String, DrgScheme.CcEntry> ccmcc = loadCcMcc();
        Map<String, List<DrgScheme.DrgNode>> drgByAdrg = new LinkedHashMap<>();
        for (DrgGroupRowVO row : drgSimMapper.groupList()) {
            DrgRule rule = parseOrDrop(row.getDrgRule(), "DRG", row.getDrgCode());
            drgByAdrg.computeIfAbsent(row.getAdrgCode(), k -> new ArrayList<>())
                    .add(new DrgScheme.DrgNode(row.getDrgCode(), row.getDrgName(), row.getAdrgCode(), rule,
                            row.getWeight(), row.getPayStandard(), row.getSortNo()));
        }

        Map<String, List<DrgScheme.AdrgNode>> adrgByMdc = new LinkedHashMap<>();
        for (DrgAdrgRowVO row : drgSimMapper.adrgList()) {
            DrgRule rule = parseOrDrop(row.getAdrgRule(), "ADRG", row.getAdrgCode());
            adrgByMdc.computeIfAbsent(row.getMdcCode(), k -> new ArrayList<>())
                    .add(new DrgScheme.AdrgNode(row.getAdrgCode(), row.getAdrgName(), row.getMdcCode(), rule,
                            row.getSortNo()));
        }

        List<DrgScheme.MdcNode> mdcs = new ArrayList<>();
        for (DrgMdcRowVO row : drgSimMapper.mdcList()) {
            // 名字为空的 MDC 行是目录里的分隔行，它又不带规则，收下就等于「任何病例都先进这个 MDC」
            if (!DrgScheme.named(row.getMdcName())) {
                continue;
            }
            DrgRule rule = parseOrDrop(row.getMdcRule(), "MDC", row.getMdcCode());
            mdcs.add(new DrgScheme.MdcNode(row.getMdcCode(), row.getMdcName(), rule, row.getSortNo()));
        }

        log.info("DRG 分组方案装载完成：MDC {}，ADRG {}，DRG 分档 {}，码集合 {} 组，并发症目录 {} 条",
                mdcs.size(), adrgByMdc.values().stream().mapToLong(List::size).sum(),
                drgByAdrg.values().stream().mapToLong(List::size).sum(), sets.size(), ccmcc.size());
        return new DrgScheme(mdcs, adrgByMdc, drgByAdrg, sets, ccmcc);
    }

    private Map<String, Set<String>> loadSets() {
        Map<String, Set<String>> sets = new HashMap<>();
        for (DrgSetRowVO row : drgSimMapper.setList()) {
            String setCode = TextUtil.trimToNull(row.getSetCode());
            String code = DrgCodes.norm(row.getIcdCode());
            if (setCode == null || code.isEmpty()) {
                continue;
            }
            sets.computeIfAbsent(setCode, k -> new HashSet<>()).add(code);
        }
        return sets;
    }

    private Map<String, DrgScheme.CcEntry> loadCcMcc() {
        Map<String, DrgScheme.CcEntry> ccmcc = new HashMap<>();
        for (CcMccRowVO row : drgSimMapper.ccmccList()) {
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
