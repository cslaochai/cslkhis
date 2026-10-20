package com.his.drgdip.support;

import com.his.common.enums.DipDiseaseTypeEnum;
import com.his.common.util.TextUtil;
import com.his.drgdip.config.DipSchemeConfig;
import com.his.drgdip.mapper.DipSimMapper;
import com.his.drgdip.vo.DipAuxRowVO;
import com.his.drgdip.vo.DipDiseaseRowVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DIP 病种库快照缓存：按版本各装载一份病种目录 + 辅助目录，供入组反复判定。
 *
 * <p>装载口径与 DRG 一致：只取 del_flag=0 AND status=1 的启用行；
 * 病种库版本经 {@link DipSchemeConfig} 切换，各版本独立缓存。
 */
@Slf4j
@Component
public class DipSchemeCache {

    private final DipSimMapper dipSimMapper;
    private final DipSchemeConfig dipSchemeConfig;

    public DipSchemeCache(DipSimMapper dipSimMapper, DipSchemeConfig dipSchemeConfig) {
        this.dipSimMapper = dipSimMapper;
        this.dipSchemeConfig = dipSchemeConfig;
    }

    private final Map<String, DipScheme> schemes = new ConcurrentHashMap<>();

    public DipScheme get() {
        return get(dipSchemeConfig.getCurrentVersion());
    }

    /**
     * 取指定版本的病种库快照，首次访问该版本时惰性装载并缓存。
     */
    public DipScheme get(String version) {
        String v = TextUtil.hasText(version) ? version.trim() : dipSchemeConfig.getCurrentVersion();
        return schemes.computeIfAbsent(v, this::load);
    }

    public String getDefaultVersion() {
        return dipSchemeConfig.getCurrentVersion();
    }

    private DipScheme load(String version) {
        Map<String, List<DipScheme.DiseaseNode>> coreByDiag = new HashMap<>();
        List<DipScheme.DiseaseNode> comprehensive = new ArrayList<>();
        for (DipDiseaseRowVO row : dipSimMapper.diseaseList(version)) {
            String diag = DrgCodes.norm(row.getMainDiagCode());
            if (diag.isEmpty()) {
                // 主诊断编码为空的病种行无法参与任何匹配，装载时丢掉并留警告（目录数据质量问题，不该静默）
                log.warn("DIP 病种行缺少主诊断编码，本次启动不参与入组：病种 {} [版本 {}]", row.getDiseaseCode(), version);
                continue;
            }
            DipScheme.DiseaseNode node = new DipScheme.DiseaseNode(row.getDiseaseCode(), row.getDiseaseName(),
                    row.getDiseaseType(), diag, DrgCodes.norm(row.getOperCode()),
                    row.getDiagMatch(), row.getBaseScore(), row.getSource(), row.getVersion(), row.getStatus());
            if (DipDiseaseTypeEnum.COMPREHENSIVE.is(row.getDiseaseType())) {
                comprehensive.add(node);
            } else {
                coreByDiag.computeIfAbsent(diag, k -> new ArrayList<>()).add(node);
            }
        }
        // 综合病种按前缀码长度降序：最长前缀最具体，归组时优先
        comprehensive.sort(Comparator.comparing((DipScheme.DiseaseNode n) -> n.mainDiagCode().length())
                .reversed()
                .thenComparing(DipScheme.DiseaseNode::diseaseCode));

        Map<String, List<DipScheme.AuxRule>> auxByDisease = new HashMap<>();
        for (DipAuxRowVO row : dipSimMapper.auxList(version)) {
            String key = TextUtil.trimToNull(row.getDiseaseCode()) == null ? "" : row.getDiseaseCode().trim();
            auxByDisease.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(new DipScheme.AuxRule(key.isEmpty() ? null : key, row.getAuxType(), DrgCodes.norm(row.getAuxCode()),
                            row.getAgeMin(), row.getAgeMax(), row.getCoeff()));
        }

        log.info("DIP 病种库[{}]装载完成：核心病种 {} 条，综合病种 {} 条，辅助目录 {} 条",
                version,
                coreByDiag.values().stream().mapToLong(List::size).sum(),
                comprehensive.size(),
                auxByDisease.values().stream().mapToLong(List::size).sum());
        return new DipScheme(coreByDiag, comprehensive, auxByDisease);
    }
}
