package com.his.medicaltech.service;

import com.his.medicaltech.dto.CdrQueryDTO;
import com.his.medicaltech.vo.CdrEventTypeSelectListVO;
import com.his.medicaltech.vo.CdrTimelineVO;

import java.util.List;

/**
 * 患者全景时间轴（CDR / P5.2）。
 *
 * <p>一句话职责：**把一个患者散在 11 个模块里的记录，按"他来过医院几次"重新讲一遍。**
 *
 * <p>它必须同时做到三件容易被做漏的事：
 * <ol>
 *   <li><b>经过 EMPI 归并</b>：合并过的档案，历史数据要一起出现，否则合并等于丢数据；</li>
 *   <li><b>报出缺口</b>：住院没有入院记录、出院没有出院记录，这些必须显式列出来；</li>
 *   <li><b>不藏悬空数据</b>：归属不到就诊次的单据单列一区，绝不静默丢弃。</li>
 * </ol>
 */
public interface CdrService {

    /**
     * 患者全景时间轴
     */
    CdrTimelineVO getTimeline(CdrQueryDTO dto);

    /**
     * 事件类型字典（前端做筛选下拉用；含节点类型归类）
     */
    List<CdrEventTypeSelectListVO> eventDict();
}
