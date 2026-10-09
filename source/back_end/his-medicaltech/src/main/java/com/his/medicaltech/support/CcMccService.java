package com.his.medicaltech.support;

import com.his.common.util.TextUtil;
import com.his.medicaltech.mapper.DrgSimMapper;
import com.his.medicaltech.vo.CcMccRowVO;
import com.his.medicaltech.vo.DrgExclusionRowVO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CC/MCC 判定服务：读官方目录（sys_drg_ccmcc）+ 排除表（sys_drg_exclusion），
 * 给其他诊断定 CC/MCC 级（直接决定 DRG 细分组与高编高套风险）。
 *
 * <p>目录为空时所有其他诊断视为 NONE——没有目录就不知道谁是 CC/MCC，绝不含糊判级。
 */
@Service
public class CcMccService {

    /**
     * 并发症合并症级别（序数越高越严重）。
     */
    public enum CcLevel { NONE, CC, MCC }

    private final DrgSimMapper drgSimMapper;

    public CcMccService(DrgSimMapper drgSimMapper) {
        this.drgSimMapper = drgSimMapper;
    }

    public CcLevel assess(String mainDiagCode, List<String> otherDiagCodes) {
        if (otherDiagCodes == null || otherDiagCodes.isEmpty()) {
            return CcLevel.NONE;
        }
        List<CcMccRowVO> ccmcc = drgSimMapper.selectCcMccList();
        if (ccmcc.isEmpty()) {
            return CcLevel.NONE; // 目录缺失：诚信返回 NONE，不编造
        }

        Map<String, Set<String>> exclMap = buildExclusion(drgSimMapper.selectExclusionList());
        String mainNorm = mainDiagCode == null ? null : DrgGrouper.icdPrefix3(mainDiagCode);

        CcLevel max = CcLevel.NONE;
        for (String code : otherDiagCodes) {
            if (!TextUtil.hasText(code)) {
                continue;
            }
            String norm = DrgGrouper.icdPrefix3(code);
            if (mainNorm != null && exclMap.containsKey(mainNorm) && exclMap.get(mainNorm).contains(norm)) {
                continue; // 排除表：该诊断在主诊断下丧失 CC/MCC 资格
            }
            CcLevel lvl = levelOf(ccmcc, norm);
            if (lvl.ordinal() > max.ordinal()) {
                max = lvl;
            }
        }
        return max;
    }

    private CcLevel levelOf(List<CcMccRowVO> ccmcc, String norm) {
        for (CcMccRowVO r : ccmcc) {
            if (norm.equals(DrgGrouper.icdPrefix3(r.getIcdCode()))) {
                if ("MCC".equalsIgnoreCase(r.getCcLevel())) {
                    return CcLevel.MCC;
                }
                if ("CC".equalsIgnoreCase(r.getCcLevel())) {
                    return CcLevel.CC;
                }
            }
        }
        return CcLevel.NONE;
    }

    private Map<String, Set<String>> buildExclusion(List<DrgExclusionRowVO> list) {
        Map<String, Set<String>> map = new HashMap<>();
        if (list == null) {
            return map;
        }
        for (DrgExclusionRowVO r : list) {
            if (!TextUtil.hasText(r.getMainDiagCode()) || !TextUtil.hasText(r.getExcludedCode())) {
                continue;
            }
            map.computeIfAbsent(DrgGrouper.icdPrefix3(r.getMainDiagCode()), k -> new HashSet<>())
                    .add(DrgGrouper.icdPrefix3(r.getExcludedCode()));
        }
        return map;
    }
}
