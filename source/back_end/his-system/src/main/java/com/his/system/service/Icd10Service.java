package com.his.system.service;

import com.his.system.dto.Icd10PredictDTO;
import com.his.system.entity.SysIcd10;
import com.his.system.vo.Icd10PredictVO;
import com.his.system.vo.SysIcd10SelectListVO;

import java.util.List;

/**
 * ICD-10 编码服务（his-system 侧的唯一实现）
 */
public interface Icd10Service {

    /**
     * 按关键词检索编码，按相关性排序后返回前 limit 条。
     *
     * @param keyword 关键词（编码或名称的片段），空串/空白时返回维护顺序靠前的若干条
     * @param limit   返回条数上限
     */
    List<SysIcd10> search(String keyword, int limit);

    /**
     * 下拉候选出参：{@link #search} 的结果映射为选项 VO。
     */
    List<SysIcd10SelectListVO> selectOptions(String keyword, int limit);

    /**
     * 全部启用编码，带 TTL 缓存。
     * 供智能预测打分复用，避免每次调用都全表 4 万行。
     */
    List<SysIcd10> activeCodes();

    /**
     * 关键词打分式 ICD 推荐（规则版，不调用模型）。
     *
     * <p>注意：这是 {@code /system/icd10/predict} 的实现，属<b>刻意保留的规则版</b>。
     * 升级版是 his-ai 的 {@code /ai/icd10/predict}（码表封闭集合内由模型重排），
     * 两者并存不是重复建设：his-system 不能反向依赖 his-ai，
     * 且规则版正是 AI 版的降级路径。</p>
     */
    List<Icd10PredictVO> predict(Icd10PredictDTO predictDTO);

    /**
     * 码表变更后让缓存立即失效
     */
    void refreshCache();
}
