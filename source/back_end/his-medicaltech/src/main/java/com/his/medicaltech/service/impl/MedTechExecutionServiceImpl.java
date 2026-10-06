package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.entity.BizMedTechExecution;
import com.his.medicaltech.mapper.BizMedTechExecutionMapper;
import com.his.medicaltech.service.MedTechExecutionService;
import com.his.medicaltech.vo.BizMedTechExecutionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 医技执行服务实现
 */
@Service
@RequiredArgsConstructor
public class MedTechExecutionServiceImpl extends ServiceImpl<BizMedTechExecutionMapper, BizMedTechExecution> implements MedTechExecutionService {

    @Override
    public PageResult<BizMedTechExecution> listPage(Long patientId, Integer applyType,
                                                    Integer executionStatus, int pageNum, int pageSize) {
        LambdaQueryWrapper<BizMedTechExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizMedTechExecution::getPatientId, patientId)
                .eq(applyType != null, BizMedTechExecution::getApplyType, applyType)
                .eq(executionStatus != null, BizMedTechExecution::getExecutionStatus, executionStatus)
                .orderByDesc(BizMedTechExecution::getCreateTime);

        Page<BizMedTechExecution> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public PageResult<BizMedTechExecutionVO> listPageVO(Long patientId, Integer applyType,
                                                        Integer executionStatus, int pageNum, int pageSize) {
        PageResult<BizMedTechExecution> result = listPage(patientId, applyType, executionStatus, pageNum, pageSize);
        List<BizMedTechExecutionVO> voList = result.getRecords().stream().map(entity -> {
            BizMedTechExecutionVO vo = new BizMedTechExecutionVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getPageNum(), result.getPageSize(), result.getPages(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startExecution(Long executionId, Long executorId, String executorName) {
        BizMedTechExecution execution = this.getById(executionId);
        if (execution == null) {
            throw new BusinessException("执行记录不存在");
        }
        if (execution.getExecutionStatus() != 1) {
            throw new BusinessException("当前状态不允许开始执行");
        }

        execution.setExecutionStatus(2); // 执行中
        execution.setExecutorId(executorId);
        execution.setExecutorName(executorName);
        execution.setExecuteTime(LocalDateTime.now());
        return this.updateById(execution);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeExecution(Long executionId) {
        BizMedTechExecution execution = this.getById(executionId);
        if (execution == null) {
            throw new BusinessException("执行记录不存在");
        }
        if (execution.getExecutionStatus() != 2) {
            throw new BusinessException("当前状态不允许完成");
        }

        execution.setExecutionStatus(3); // 已完成
        execution.setCompleteTime(LocalDateTime.now());
        return this.updateById(execution);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewExecution(Long executionId, Long reviewerId, String reviewerName) {
        BizMedTechExecution execution = this.getById(executionId);
        if (execution == null) {
            throw new BusinessException("执行记录不存在");
        }
        if (execution.getExecutionStatus() != 3) {
            throw new BusinessException("当前状态不允许审核");
        }

        execution.setExecutionStatus(4); // 已审核
        execution.setReviewerId(reviewerId);
        execution.setReviewerName(reviewerName);
        execution.setReviewTime(LocalDateTime.now());
        return this.updateById(execution);
    }
}
