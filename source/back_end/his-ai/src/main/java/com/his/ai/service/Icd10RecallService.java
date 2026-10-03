package com.his.ai.service;

import com.his.ai.dto.Icd10SelectListDTO;
import com.his.ai.vo.Icd10SelectListVO;
import com.his.system.entity.SysIcd10;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public interface Icd10RecallService {

    /**
     * 供能力层构造「编码 → 实体」索引，用于输出校验
     */
    public static Map<String, SysIcd10> indexByCode(List<RecallHit> hits) {
        Map<String, SysIcd10> index = new ConcurrentHashMap<>();
        for (RecallHit hit : hits) {
            index.put(hit.code().getIcdCode(), hit.code());
        }
        return index;
    }

    /**
     * 一次召回命中
     *
     * @param code  ICD 编码
     * @param score 字面重合得分，0 表示与本次病历文本无字面关联
     */
    public record RecallHit(SysIcd10 code, int score) {
    }

    List<RecallHit> recall(String noteText, Integer topNOverride);

    List<SysIcd10> activeCodes();

    void refresh();

    List<SysIcd10> search(String keyword, int limit);

    List<Icd10SelectListVO> selectOptions(Icd10SelectListDTO selectListDTO);
}
