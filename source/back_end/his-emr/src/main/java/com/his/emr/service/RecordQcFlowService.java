package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.*;
import com.his.emr.vo.RecordQcFlowActionVO;
import com.his.emr.vo.RecordQcFlowVO;

import java.util.List;

/**
 * 病历三级质控流转服务
 * <p>
 * 状态机：1 科级待审 → 2 病案室待审 → 3 医务处待审 → 4 终审通过；
 * 任一审核级可退回 → 5 整改中（return_level 记退回级），整改提交 → 回到 return_level 待审。
 */
public interface RecordQcFlowService {

    /**
     * 发起流转（同一病历同时只允许一条在途）
     */
    RecordQcFlowVO start(RecordQcFlowStartDTO dto);

    /**
     * 当前级审核通过 → 送下一级（医务处通过请走 finalApprove，这里直接拦截）
     */
    void approve(RecordQcFlowOpinionDTO dto);

    /**
     * 当前级退回整改（缺陷明细 + 整改要求必填）
     */
    void returnForRework(RecordQcFlowReturnDTO dto);

    /**
     * 科室整改提交 → 回到退回发生级待审
     */
    void resubmit(RecordQcFlowOpinionDTO dto);

    /**
     * 医务处终审（定级必填，通过后流转终态）
     */
    void finalApprove(RecordQcFlowFinalDTO dto);

    /**
     * 分页
     */
    PageResult<RecordQcFlowVO> page(RecordQcFlowQueryPageDTO q);

    /**
     * 详情（不含时间线）
     */
    RecordQcFlowVO getDetailById(Long id);

    /**
     * 流转时间线（action_time 升序）
     */
    List<RecordQcFlowActionVO> listActions(Long flowId);
}
