package com.his.drg.support;

import com.his.common.util.TextUtil;
import com.his.drg.config.DrgProperties;
import com.his.drg.config.DrgSchemeConfig;
import com.his.drg.enums.DrgCcLevelEnum;
import com.his.drg.mapper.DrgSimMapper;
import com.his.drg.vo.CcMccRowVO;
import com.his.drg.vo.DrgAdrgRowVO;
import com.his.drg.vo.DrgGroupRowVO;
import com.his.drg.vo.DrgMdcRowVO;
import com.his.drg.vo.DrgSetRowVO;
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
 *
 * <p>为什么按版本分桶：2.0/3.0 共用同一套 CHS-DRG 编码（3.0 在 2.0 上增组），码值同名，
 * 必须按 version 隔离装载，否则两版规则会混装互相污染模拟结果。默认启用版本由
 * {@link DrgSchemeConfig} 决定（解析自 sys_config / Redis / yml，见 config/domain/his-drg.yml），
 * 启动日志会打印实际取值。
 *
 * <p>为什么必须缓存：一份官方方案是 27 条 MDC、537 条 ADRG、870 条 DRG、12.8 万条集合码、
 * 7900 余条并发症目录。批量模拟原本每条首页都要把这几张表整个捞一遍并逐条比前缀。
 *
 * <p><b>改了方案数据（例如统筹区下发权重后重灌）需重启进程才生效</b>：这些是官方下发的配置表，
 * 重灌是运维动作，不是运行期编辑，所以不做刷新入口，只保留「首次用到才装载」这一条惰性路径。
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
        return get(drgSchemeConfig.getDefaultVersion());
    }

    /**
     * 取指定版本的方案快照，首次访问该版本时惰性装载并缓存。
     */
    public DrgScheme get(String version) {
        String v = version == null || version.isBlank() ? drgSchemeConfig.getDefaultVersion() : version;
        return schemes.computeIfAbsent(v, this::load);
    }

    /**
     * 当前默认启用的库内 version 字符串（如 chs-drg-2.0 / 3.0）。
     */
    public String getDefaultVersion() {
        return drgSchemeConfig.getDefaultVersion();
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
