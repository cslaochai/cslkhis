package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.system.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.emr.dto.RxFlowActionDTO;
import com.his.emr.dto.RxFlowQueryPageDTO;
import com.his.emr.dto.RxFlowUpsertDTO;
import com.his.emr.entity.BizPrescription;
import com.his.emr.entity.BizRxFlow;
import com.his.emr.mapper.BizPrescriptionMapper;
import com.his.emr.mapper.BizRxFlowMapper;
import com.his.emr.service.RxFlowService;
import com.his.emr.vo.RxFlowListVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 处方流转单
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RxFlowServiceImpl extends ServiceImpl<BizRxFlowMapper, BizRxFlow> implements RxFlowService {

    private final BizRxFlowMapper bizRxFlowMapper;
    private final BizPrescriptionMapper bizPrescriptionMapper;
    private final RedisSequenceService redisSequenceService;

    @Override
    public RxFlowListVO createFlow(RxFlowUpsertDTO dto) {
        BizPrescription prescription = bizPrescriptionMapper.selectById(dto.getPrescriptionId());
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }
        // 同一处方至多一张有效流转单（1-已流转）
        Long active = bizRxFlowMapper.selectCount(new LambdaQueryWrapper<BizRxFlow>()
                .eq(BizRxFlow::getPrescriptionId, dto.getPrescriptionId())
                .eq(BizRxFlow::getFlowStatus, 1));
        if (active != null && active > 0) {
            throw new BusinessException("该处方已存在流转中的单据，不可重复流转");
        }
        var current = UserUtils.getCurrentUser();
        BizRxFlow flow = new BizRxFlow();
        flow.setFlowNo(redisSequenceService.generateRxFlowNo());
        flow.setPrescriptionId(prescription.getId());
        flow.setPrescriptionNo(prescription.getPrescriptionNo());
        flow.setPatientId(prescription.getPatientId());
        flow.setPatientNo(prescription.getPatientNo());
        flow.setPatientName(prescription.getPatientName());
        flow.setOrgName(dto.getOrgName());
        flow.setOrgType(dto.getOrgType() == null ? 1 : dto.getOrgType());
        flow.setFlowStatus(1);
        flow.setFlowTime(LocalDateTime.now());
        flow.setTotalAmount(prescription.getTotalAmount());
        flow.setRemark(dto.getRemark());
        bizRxFlowMapper.insert(flow);
        log.info("[处方流转口子] ===== 打印：处方流转单已创建（外发外联渠道，占位不真发） ===== flowNo={} prescriptionNo={} org={}",
                flow.getFlowNo(), flow.getPrescriptionNo(), flow.getOrgName());
        return toVO(flow);
    }

    @Override
    public void finish(RxFlowActionDTO dto) {
        BizRxFlow flow = requireActive(dto.getFlowId());
        flow.setFlowStatus(2);
        flow.setFinishTime(LocalDateTime.now());
        bizRxFlowMapper.updateById(flow);
        log.info("[处方流转口子] ===== 打印：院外取药回执 ===== flowNo={} prescriptionNo={} org={}",
                flow.getFlowNo(), flow.getPrescriptionNo(), flow.getOrgName());
    }

    @Override
    public void cancel(RxFlowActionDTO dto) {
        BizRxFlow flow = requireActive(dto.getFlowId());
        flow.setFlowStatus(3);
        flow.setFinishTime(LocalDateTime.now());
        if (TextUtil.hasText(dto.getReason())) {
            flow.setRemark(flow.getRemark() == null ? dto.getReason() : flow.getRemark() + "；取消原因：" + dto.getReason());
        }
        bizRxFlowMapper.updateById(flow);
    }

    @Override
    public PageResult<RxFlowListVO> listPage(RxFlowQueryPageDTO dto) {
        LambdaQueryWrapper<BizRxFlow> wrapper = new LambdaQueryWrapper<>();
        if (dto.getPatientId() != null) {
            wrapper.eq(BizRxFlow::getPatientId, dto.getPatientId());
        }
        if (dto.getPrescriptionId() != null) {
            wrapper.eq(BizRxFlow::getPrescriptionId, dto.getPrescriptionId());
        }
        if (dto.getFlowStatus() != null) {
            wrapper.eq(BizRxFlow::getFlowStatus, dto.getFlowStatus());
        }
        wrapper.orderByDesc(BizRxFlow::getId);
        Page<BizRxFlow> page = bizRxFlowMapper.selectPage(
                Page.of(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<RxFlowListVO> vos = new ArrayList<>(page.getRecords().size());
        for (BizRxFlow row : page.getRecords()) {
            vos.add(toVO(row));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(),
                page.getSize(), page.getPages(), vos);
    }

    private RxFlowListVO toVO(BizRxFlow flow) {
        RxFlowListVO vo = new RxFlowListVO();
        BeanUtils.copyProperties(flow, vo);
        return vo;
    }

    private BizRxFlow requireActive(Long flowId) {
        BizRxFlow flow = bizRxFlowMapper.selectById(flowId);
        if (flow == null) {
            throw new BusinessException("流转单不存在");
        }
        if (flow.getFlowStatus() == null || flow.getFlowStatus() != 1) {
            throw new BusinessException("仅「已流转」状态可执行该操作（终态不可逆）");
        }
        return flow;
    }
}
