package com.his.miniapp.service;

import com.his.miniapp.vo.TriageDeptVO;
import com.his.miniapp.vo.TriageSymptomVO;

import java.util.List;

/**
 * 患者端智能导诊。
 *
 * <p>这里是<b>规则匹配</b>而不是模型推理：主诉 → 症状关键词 → 科室，三步全部由代码完成。
 * 模型（若将来接入）只做「把口语归一成症状词」和「生成补充追问」两件事，
 * <b>不得决定推荐哪个科室</b>，更不得判断是否急症 —— 急症信号是表里的硬编码，
 * 判定错了不会有任何报错，患者却可能因此在家等一夜。
 */
public interface MiniappTriageService {

    /**
     * 按主诉推荐科室。
     *
     * @param description 患者描述原文
     * @return 推荐科室（急症优先、权重降序），无命中时返回兜底科室而不是空列表
     */
    List<TriageDeptVO> recommend(String description);

    /** 常见症状快捷标签 */
    List<TriageSymptomVO> hotSymptoms();
}
