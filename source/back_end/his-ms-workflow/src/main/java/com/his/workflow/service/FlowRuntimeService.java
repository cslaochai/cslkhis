package com.his.workflow.service;

import com.his.common.base.PageResult;
import com.his.workflow.dto.FlowDispatchDTO;
import com.his.workflow.dto.FlowStartDTO;
import com.his.workflow.dto.FlowTaskPageQueryDTO;
import com.his.workflow.dto.FlowTaskHandleDTO;
import com.his.workflow.vo.FlowDispatchResultVO;
import com.his.workflow.vo.FlowHistoryVO;
import com.his.workflow.vo.FlowInstanceVO;
import com.his.workflow.vo.FlowTaskVO;

import java.util.List;

/**
 * 流程运行时：启动、推进、审批、撤回、查询。
 */
public interface FlowRuntimeService {

    /**
     * 按流程编码启动。
     */
    FlowInstanceVO start(FlowStartDTO dto);

    /**
     * 统一入口：一段文本先过 AI 意图识别，再启动对应流程。
     */
    FlowDispatchResultVO dispatch(FlowDispatchDTO dto);

    PageResult<FlowTaskVO> listMyTasks(FlowTaskPageQueryDTO query);

    void approve(FlowTaskHandleDTO dto);

    void reject(FlowTaskHandleDTO dto);

    /**
     * 撤回：仅发起人本人、仅运行中实例。
     */
    void withdraw(Long instanceId);

    FlowInstanceVO getInstance(Long id);

    List<FlowHistoryVO> listHistory(Long instanceId);

    /**
     * 按业务单据查实例（业务模块回查自己单据的流程进度）。
     */
    List<FlowInstanceVO> listByBusinessId(String businessId);
}
