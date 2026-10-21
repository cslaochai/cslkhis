package com.his.ai.service;

import com.his.ai.dto.Icd10SelectListDTO;
import com.his.ai.vo.Icd10SelectListVO;
import com.his.spi.vo.Icd10BriefVO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface Icd10RecallService {

    /**
     * 供能力层构造「编码 → 实体」索引，用于输出校验
     */
    public static Map<String, Icd10BriefVO> indexByCode(List<RecallHit> hits) {
        Map<String, Icd10BriefVO> index = new ConcurrentHashMap<>();
        for (RecallHit hit : hits) {
            index.put(hit.code().getIcdCode(), hit.code());
        }
        return index;
    }

    List<RecallHit> recall(String noteText, Integer topNOverride);

    List<Icd10BriefVO> activeCodes();

    void refresh();

    List<Icd10BriefVO> search(String keyword, int limit);

    List<Icd10SelectListVO> selectOptions(Icd10SelectListDTO selectListDTO);

    /**
     * 一次召回命中
     */
    public record RecallHit(Icd10BriefVO code, int score) {
    }
}
