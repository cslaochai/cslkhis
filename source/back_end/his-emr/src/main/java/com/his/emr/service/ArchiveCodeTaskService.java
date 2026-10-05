package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.CodeTaskAssignUpsertDTO;
import com.his.emr.dto.CodeTaskAuditDTO;
import com.his.emr.dto.CodeTaskQueryPageDTO;
import com.his.emr.dto.CodeTaskSubmitDTO;
import com.his.emr.vo.ArchiveCodeTaskStatsVO;
import com.his.emr.vo.ArchiveCodeTaskVO;

/**
 * 病案编码任务服务
 */
public interface ArchiveCodeTaskService {

    /**
     * 分页查询
     */
    PageResult<ArchiveCodeTaskVO> page(CodeTaskQueryPageDTO query);

    /**
     * 详情
     */
    ArchiveCodeTaskVO getDetailById(Long id);

    /**
     * 工作台统计（待编码/已提交/已完成/已退修）
     */
    ArchiveCodeTaskStatsVO stats();

    /**
     * 同步任务池：为尚无任务的归档记录（待归档/已归档）各建一条待编码任务，返回新建条数（幂等）
     */
    int syncTasks();

    /**
     * 分配编码员
     */
    void assign(CodeTaskAssignUpsertDTO dto);

    /**
     * 提交编码（待编码/已退修 → 已提交；未分配的任务提交时自动认领当前编码员）
     */
    void submit(CodeTaskSubmitDTO dto);

    /**
     * 审核（通过 → 已完成；退修 → 已退修 + 计数 + 站内信提醒编码员）
     */
    void audit(CodeTaskAuditDTO dto);
}
