package com.his.medicaltech.service;

import com.his.medicaltech.dto.CdrQueryDTO;
import com.his.medicaltech.vo.CdrEventTypeSelectListVO;
import com.his.medicaltech.vo.CdrTimelineVO;

import java.util.List;

/**
 * 患者全景时间轴（CDR / P5.2）。
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
