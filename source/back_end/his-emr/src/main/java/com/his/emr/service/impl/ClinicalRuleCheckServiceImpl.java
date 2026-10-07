package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.CheckResultEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.emr.entity.BizClinicalRuleCheck;
import com.his.emr.enums.RuleCheckStatusEnum;
import com.his.emr.mapper.BizClinicalRuleCheckMapper;
import com.his.emr.service.ClinicalRuleCheckService;
import com.his.emr.vo.BizClinicalRuleCheckVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 临床规则校验服务实现
 */
@Service
@RequiredArgsConstructor
public class ClinicalRuleCheckServiceImpl extends ServiceImpl<BizClinicalRuleCheckMapper, BizClinicalRuleCheck> implements ClinicalRuleCheckService {

    private static final AtomicInteger SEQ = new AtomicInteger(0);

    @Override
    public PageResult<BizClinicalRuleCheckVO> selectCheckPage(Long patientId, Integer ruleType,
                                                              Integer checkStatus, int pageNum, int pageSize) {
        LambdaQueryWrapper<BizClinicalRuleCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizClinicalRuleCheck::getPatientId, patientId)
                .eq(ruleType != null, BizClinicalRuleCheck::getRuleType, ruleType)
                .eq(checkStatus != null, BizClinicalRuleCheck::getCheckStatus, checkStatus)
                .orderByDesc(BizClinicalRuleCheck::getCreateTime);

        Page<BizClinicalRuleCheck> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<BizClinicalRuleCheckVO> voList = page.getRecords().stream()
                .map(this::toVo).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public BizClinicalRuleCheckVO getCheckDetail(Long checkId) {
        return toVo(this.getById(checkId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizClinicalRuleCheckVO executeCheck(Long recordId, Integer ruleType, String checkBy) {
        BizClinicalRuleCheck check = new BizClinicalRuleCheck();
        check.setCheckNo("RC" + LocalDateTime.now().format(DateFormats.COMPACT_DATETIME)
                + String.format("%04d", SEQ.incrementAndGet() % 10000));
        check.setRecordId(recordId);
        check.setRuleType(ruleType);
        check.setRuleName(ruleType == 1 ? "配伍禁忌检查" : ruleType == 2 ? "检验诊断关联性检查" : "用药合理性检查");
        check.setCheckResult(CheckResultEnum.PASS.getCode());
        check.setCheckStatus(RuleCheckStatusEnum.PENDING.getCode());
        check.setCheckBy(checkBy);
        check.setCheckTime(LocalDateTime.now());
        check.setCreateBy(checkBy);
        check.setCreateTime(LocalDateTime.now());
        this.save(check);
        return toVo(check);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleCheck(Long checkId, boolean ignore, String remark) {
        BizClinicalRuleCheck check = this.getById(checkId);
        if (check == null) {
            throw new BusinessException("校验记录不存在");
        }
        if (check.getCheckStatus() != 1) {
            throw new BusinessException("当前状态不允许处理");
        }

        check.setCheckStatus(ignore ? RuleCheckStatusEnum.IGNORED.getCode() : RuleCheckStatusEnum.HANDLED.getCode());
        check.setRemark(remark);
        return this.updateById(check);
    }

    private BizClinicalRuleCheckVO toVo(BizClinicalRuleCheck entity) {
        BizClinicalRuleCheckVO vo = new BizClinicalRuleCheckVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
