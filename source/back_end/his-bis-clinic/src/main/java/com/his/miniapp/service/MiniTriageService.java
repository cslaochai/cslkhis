package com.his.miniapp.service;

import com.his.miniapp.vo.MiniTriageDeptVO;
import com.his.miniapp.vo.TriageSymptomVO;

import java.util.List;

/**
 * 患者端智能导诊。
 */
public interface MiniTriageService {

    /**
     * 按主诉推荐科室。
     *
     * @param description 患者描述原文
     * @return 推荐科室（急症优先、权重降序），无命中时返回兜底科室而不是空列表
     */
    List<MiniTriageDeptVO> recommend(String description);

    /**
     * 常见症状快捷标签
     */
    List<TriageSymptomVO> hotSymptoms();
}
