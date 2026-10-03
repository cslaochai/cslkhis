package com.his.patient.service;

import com.his.patient.dto.InfusionActionDTO;
import com.his.patient.vo.InpatientOrderExecVO;
import com.his.patient.vo.InfusionRoundVO;

import java.util.List;

/**
 * 输液执行闭环（G14）：开始（滴速）→ N 次巡视 → 结束（不良反应）。
 *
 * <p>为什么挂在执行行而不是医嘱上：长期医嘱一天可能输多袋，每袋是执行行里独立的一次
 * 执行（各自计费），闭环的粒度必须是「这袋液」。
 */
public interface InpatientInfusionService {

    /** 开始输注：写执行行开始时间与滴速（仅已执行、静脉类、未开始的执行行） */
    InpatientOrderExecVO start(InfusionActionDTO dto);

    /** 巡视：插一条巡视记录（开始后、结束前） */
    InfusionRoundVO round(InfusionActionDTO dto);

    /** 结束输注：写执行行结束时间与不良反应（adverseFlag=1 时描述必填） */
    InpatientOrderExecVO finish(InfusionActionDTO dto);

    /** 某执行行的巡视记录（时间升序） */
    List<InfusionRoundVO> rounds(Long execId);
}
