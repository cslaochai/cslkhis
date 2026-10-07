package com.his.appoint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.appoint.dto.RevisitFeePolicyQueryPageDTO;
import com.his.appoint.dto.RevisitFeePolicyUpsertDTO;
import com.his.appoint.entity.BizRevisitFeePolicy;
import com.his.appoint.enums.RevisitChargeModeEnum;
import com.his.appoint.mapper.BizRevisitFeePolicyMapper;
import com.his.appoint.service.RevisitFeePolicyService;
import com.his.appoint.vo.RevisitFeePolicyVO;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 复诊收费策略服务实现
 */
@Slf4j
@Service
public class RevisitFeePolicyServiceImpl
        extends ServiceImpl<BizRevisitFeePolicyMapper, BizRevisitFeePolicy>
        implements RevisitFeePolicyService {

    /**
     * 三态条件：不限
     */
    private static final int ANY = 0;
    /**
     * 三态条件：要求相同
     */
    private static final int MUST_SAME = 1;
    /**
     * 三态条件：要求不同
     */
    private static final int MUST_DIFF = 2;

    @Override
    public RevisitFeeDecision decide(RevisitFeeContext context) {
        RevisitFeeDecision decision = new RevisitFeeDecision();
        BigDecimal registFee = NumUtil.orZero(context == null ? null : context.getRegistFee());
        BigDecimal diagnosisFee = NumUtil.orZero(context == null ? null : context.getDiagnosisFee());
        // 默认全额收费：没有策略、来源未知、判不出比对结果，都走这里。
        // 方向性很关键 —— 兜底若是「免」，等于任何异常路径都在替医院漏收挂号费。
        decision.setRegistFee(registFee);
        decision.setDiagnosisFee(diagnosisFee);
        decision.setChargeMode(RevisitChargeModeEnum.FULL.getCode());
        decision.setWaived(registFee.add(diagnosisFee).compareTo(BigDecimal.ZERO) <= 0);
        if (decision.isWaived()) {
            decision.setReason("号源原价为 0，免收");
        }

        if (context == null) {
            return decision;
        }
        BizRevisitFeePolicy policy = matchPolicy(context);
        if (policy == null) {
            return decision;
        }
        int mode = policy.getChargeMode() == null
                ? RevisitChargeModeEnum.FULL.getCode() : policy.getChargeMode();
        decision.setPolicyId(policy.getId());
        decision.setPolicyName(policy.getPolicyName());
        decision.setChargeMode(mode);
        if (mode == RevisitChargeModeEnum.FREE_REGIST.getCode()) {
            decision.setRegistFee(BigDecimal.ZERO);
            decision.setReason("复诊收费策略「" + policy.getPolicyName() + "」免收：挂号费");
        } else if (mode == RevisitChargeModeEnum.FREE_ALL.getCode()) {
            decision.setRegistFee(BigDecimal.ZERO);
            decision.setDiagnosisFee(BigDecimal.ZERO);
            decision.setReason("复诊收费策略「" + policy.getPolicyName() + "」免收：挂号费、诊查费");
        } else {
            decision.setReason("复诊收费策略「" + policy.getPolicyName() + "」：全额收费");
        }
        decision.setWaived(decision.getRegistFee().add(decision.getDiagnosisFee()).compareTo(BigDecimal.ZERO) <= 0);
        return decision;
    }

    /**
     * 取启用中的候选策略，在内存里按 priority 升序（同优先级按 id 升序）找第一条全条件命中的。
     *
     * <p>为什么不在 SQL 里拼完：三态条件（不限/要求同/要求不同）与「判不出来就不命中」
     * 这套兜底口径，用 SQL 表达要写成一堆 OR IS NULL，读的人看不出哪条是收口方向。
     * 策略表只有个位数行，内存匹配更贵不了多少，却能让规则一眼对上。
     */
    private BizRevisitFeePolicy matchPolicy(RevisitFeeContext context) {
        if (context.getRevisitSource() == null || context.getRevisitSource() <= 0) {
            return null;
        }
        LambdaQueryWrapper<BizRevisitFeePolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizRevisitFeePolicy::getStatus, 1)
                .in(BizRevisitFeePolicy::getRevisitSource, 0, context.getRevisitSource());
        List<BizRevisitFeePolicy> candidates = this.list(wrapper);
        if (CollectionUtils.isEmpty(candidates)) {
            return null;
        }
        return candidates.stream()
                .filter(p -> triStateHit(p.getSameDoctor(), context.getSameDoctor()))
                .filter(p -> triStateHit(p.getSameDept(), context.getSameDept()))
                .filter(p -> withinDaysHit(p.getWithinDays(), context.getDaysSinceOrigin()))
                .min(Comparator.comparing((BizRevisitFeePolicy p) -> p.getPriority() == null ? Integer.MAX_VALUE : p.getPriority())
                        .thenComparing(BizRevisitFeePolicy::getId))
                .orElse(null);
    }

    /**
     * 三态匹配：0 不限；1 要求相同；2 要求不同。
     * 实际值判不出来（null）时，凡带 1/2 条件的策略一律<b>不命中</b> —— 判不准就按更保守的收费走。
     */
    private boolean triStateHit(Integer condition, Boolean actual) {
        int cond = condition == null ? ANY : condition;
        if (cond == ANY) {
            return true;
        }
        if (actual == null) {
            return false;
        }
        return cond == MUST_SAME ? actual : !actual;
    }

    /**
     * 间隔天数匹配：策略未限制天数则恒命中；限制了但算不出间隔（缺原就诊日）则不命中。
     */
    private boolean withinDaysHit(Integer limit, Long actualDays) {
        if (limit == null) {
            return true;
        }
        return actualDays != null && actualDays <= limit;
    }

    @Override
    public PageResult<RevisitFeePolicyVO> listPage(RevisitFeePolicyQueryPageDTO queryDTO) {
        LambdaQueryWrapper<BizRevisitFeePolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getPolicyName()),
                        BizRevisitFeePolicy::getPolicyName, queryDTO.getPolicyName())
                .eq(queryDTO.getRevisitSource() != null,
                        BizRevisitFeePolicy::getRevisitSource, queryDTO.getRevisitSource())
                .eq(queryDTO.getChargeMode() != null,
                        BizRevisitFeePolicy::getChargeMode, queryDTO.getChargeMode())
                .eq(queryDTO.getStatus() != null, BizRevisitFeePolicy::getStatus, queryDTO.getStatus())
                // 必须带 id 兜底二级键：priority 并列时翻页会重复/丢行
                .orderByAsc(BizRevisitFeePolicy::getPriority)
                .orderByAsc(BizRevisitFeePolicy::getId);

        Page<BizRevisitFeePolicy> page = this.page(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PageResult<>();
        }
        List<RevisitFeePolicyVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public RevisitFeePolicyVO detail(Long id) {
        BizRevisitFeePolicy policy = super.getById(id);
        if (policy == null) {
            throw new BusinessException("复诊收费策略不存在");
        }
        return toVO(policy);
    }

    @Override
    public void upsert(RevisitFeePolicyUpsertDTO upsertDTO) {
        BizRevisitFeePolicy entity = new BizRevisitFeePolicy();
        BeanUtils.copyProperties(upsertDTO, entity);
        if (entity.getSameDoctor() == null) {
            entity.setSameDoctor(ANY);
        }
        if (entity.getSameDept() == null) {
            entity.setSameDept(ANY);
        }
        if (entity.getPriority() == null) {
            entity.setPriority(100);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (upsertDTO.getId() == null) {
            this.save(entity);
            return;
        }
        if (super.getById(upsertDTO.getId()) == null) {
            throw new BusinessException("复诊收费策略不存在，无法修改");
        }
        this.updateById(entity);
    }

    /**
     * 删除（软删，BaseEntity 的 del_flag 带 @TableLogic）。
     * <p>本表没有唯一键，软删留下的历史行不会再占住任何键，也不会被匹配到
     * （匹配一律带 status=1，且 MP 自动追加 del_flag=0）。
     */
    @Override
    public void deleteById(Long id) {
        if (super.getById(id) == null) {
            throw new BusinessException("复诊收费策略不存在");
        }
        this.removeById(id);
    }

    private RevisitFeePolicyVO toVO(BizRevisitFeePolicy entity) {
        RevisitFeePolicyVO vo = new RevisitFeePolicyVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

}
