package com.his.medicaltech.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.medicaltech.entity.BizMedTechExecution;
import com.his.medicaltech.vo.BizMedTechExecutionVO;

/**
 * 医技执行服务接口
 */
public interface MedTechExecutionService extends IService<BizMedTechExecution> {

    /**
     * 查询待执行列表
     */
    PageResult<BizMedTechExecution> listPage(Long patientId, Integer applyType,
                                             Integer executionStatus, int pageNum, int pageSize);

    /**
     * 待执行分页（出参为渲染行）：按患者、申请类型与执行状态筛出医技执行单。
     * 供医技科室接单环节筛出本人待办检查、检验执行任务时取数。
     */
    PageResult<BizMedTechExecutionVO> listPageVO(Long patientId, Integer applyType,
                                                 Integer executionStatus, int pageNum, int pageSize);

    /**
     * 开始执行
     */
    boolean startExecution(Long executionId, Long executorId, String executorName);

    /**
     * 完成执行
     */
    boolean completeExecution(Long executionId);

    /**
     * 审核执行
     */
    boolean reviewExecution(Long executionId);
}
