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
    boolean reviewExecution(Long executionId, Long reviewerId, String reviewerName);
}
