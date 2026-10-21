package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.LisQcDTO;
import com.his.medicaltech.vo.LisQcVO;

/**
 * LIS 室内质控服务：质控计划维护、质控结果录入与 Westgard 判定、失控处理与复核的业务收口。
 */
public interface LisQcService {

    /**
     * 质控计划分页：按项目、仪器、质控品与状态筛出已登记的质控计划。
     * 用于室内质控页「质控计划」标签的查询表格。
     */
    PageResult<LisQcVO.PlanVO> planPage(LisQcDTO.PlanQuery q);

    /**
     * 新增或修改质控计划：登记检验项目 × 仪器 × 质控品的靶值、SD 与效期，返回计划编号。
     * 用于室内质控页质控计划的新增与修改。
     */
    String planUpsert(LisQcDTO.PlanUpsert dto);

    /**
     * 启用或停用一条质控计划。
     * 用于室内质控页质控计划行的状态开关。
     */
    void planToggle(Long planId, Integer status);

    /**
     * 录入一次质控测定值，由服务端按 Westgard 规则判定在控或失控并生出质控记录。
     * 用于室内质控页每日质控的结果登记。
     */
    LisQcVO.RecordVO inputResult(LisQcDTO.ResultInput dto);

    /**
     * 质控记录分页：含在控/失控判定与失控处理进度。
     * 用于室内质控页「质控记录」标签的查询表格。
     */
    PageResult<LisQcVO.RecordVO> recordPage(LisQcDTO.RecordQuery q);

    /**
     * 登记失控处理：填写失控原因与纠正措施，处理完成后记录进入待复核。
     * 用于室内质控页失控记录的「处理」动作。
     */
    void handle(LisQcDTO.Handle dto);

    /**
     * 复核失控处理并闭环，复核人不得是处理人本人。
     * 用于室内质控页已处理失控记录的「复核」动作。
     */
    void review(LisQcDTO.Review dto);

    /**
     * 质控概览统计：今日在控率与待处理失控数。
     * 用于室内质控页顶部的统计卡。
     */
    LisQcVO.StatsVO stats();
}
