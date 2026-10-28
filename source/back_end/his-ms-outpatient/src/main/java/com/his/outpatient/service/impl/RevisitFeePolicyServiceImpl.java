package com.his.outpatient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.outpatient.dto.RevisitFeePolicyPageQueryDTO;
import com.his.outpatient.dto.RevisitFeePolicyUpsertDTO;
import com.his.outpatient.entity.RevisitFeePolicy;
import com.his.common.enums.ClinicDiagnosisTierEnum;
import com.his.common.enums.RevisitChargeModeEnum;
import com.his.common.enums.RevisitFeeConditionEnum;
import com.his.outpatient.mapper.RevisitFeePolicyMapper;
import com.his.outpatient.service.RevisitFeePolicyService;
import com.his.outpatient.vo.RevisitFeePolicyVO;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.support.PediatricSurchargePolicy;
import com.his.spi.service.ChargeFacadeService;
import com.his.spi.vo.PolicyPriceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 复诊收费策略服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RevisitFeePolicyServiceImpl extends ServiceImpl<RevisitFeePolicyMapper, RevisitFeePolicy> implements RevisitFeePolicyService {

    private final ChargeFacadeService chargeFacadeService;

    @Override
    public RevisitFeeDecision decide(RevisitFeeContext context) {
        RevisitFeeDecision decision = new RevisitFeeDecision();
        // 🔑 只读诊查费：湘医保发〔2025〕58号已废止挂号费合并进门诊诊查费，门诊现在只有一项费用。
        // context.registFee 保留字段仅作历史兼容（恒 0），不参与判定。
        BigDecimal baseFee = NumUtil.orZero(context == null ? null : context.getDiagnosisFee());
        decision.setRegistFee(BigDecimal.ZERO);
        decision.setBaseDiagnosisFee(baseFee);

        // 🔑 顺序：先按儿童政策算出应收价，再让复诊减免策略去减。
        //   反过来（先减免再加收）会让「免收的复诊号」因为是儿童反而多收 30% —— 减免是对
        //   「这项服务」的免收，不是对「基础价」的免收，加收项属于同一项服务，理应一起免。
        //   政策依据见 PediatricSurchargePolicy（湘医保发〔2025〕58号附件1 + 省局解读第4期第6问）。
        boolean child = context != null
                && PediatricSurchargePolicy.isChild(context.getBirthDate(), context.getVisitDate());
        BigDecimal diagnosisFee = child
                ? baseFee.add(PediatricSurchargePolicy.surchargeOf(baseFee))
                : baseFee;
        decision.setChildSurcharged(child);
        decision.setChildSurchargeAmount(child
                ? PediatricSurchargePolicy.surchargeOf(baseFee) : BigDecimal.ZERO);
        decision.setDiagnosisFee(diagnosisFee);
        // 🔑 拆 base + 加收项（湘医保发〔2025〕58号价表结构：主项目 + 加收项子码）：
        //   普通档只出主项门诊诊查费(011102020010000)，副主任/主任/知名专家在主项之上加收对应子码(001/002/003)。
        //   base = 普通档全额；加收 = 本档加收额；儿童加收 30% 对两行各自乘，
        //   保证 base + 加收 = 儿童总价（与 decision.diagnosisFee 完全一致，不破坏历史金额口径）。
        //   🔑 金额权威在价表（sys_charge_policy_price，政策 OPD_CONSULT，价格管理页「政策价」可调），
        //   实时查价表；价表缺行/停用回退枚举兜底价并留 error 痕——挂号是主流程不能因价表空挂死，
        //   但必须能从日志发现「价表没配」。
        ClinicDiagnosisTierEnum tier = ClinicDiagnosisTierEnum.ofCode(
                context == null ? null : context.getDiagnosisTier());
        BigDecimal baseUnit = policyAmountOrFallback(ClinicDiagnosisTierEnum.GENERAL);
        BigDecimal surchargeUnit = BigDecimal.ZERO;
        if (tier != null && tier != ClinicDiagnosisTierEnum.GENERAL) {
            BigDecimal fromPrice = policySurchargeOrFallback(tier);
            if (fromPrice != null) {
                surchargeUnit = fromPrice;
            }
        }
        // 主项目恒为普通门诊主项价；加收项=本档加收额。两行各自乘儿童系数。
        // 变量名 mainItemFee 区别于上文 baseFee（号源快照总价，含加收），避免同一方法内重复声明。
        BigDecimal mainItemFee = child ? baseUnit.add(PediatricSurchargePolicy.surchargeOf(baseUnit)) : baseUnit;
        BigDecimal surchargeFee = child
                ? surchargeUnit.add(PediatricSurchargePolicy.surchargeOf(surchargeUnit)) : surchargeUnit;
        decision.setDiagnosisTier(context == null ? null : context.getDiagnosisTier());
        decision.setBaseFee(mainItemFee);
        decision.setSurchargeFee(surchargeFee);
        decision.setChargeMode(RevisitChargeModeEnum.FULL.getCode());
        decision.setWaived(diagnosisFee.compareTo(BigDecimal.ZERO) <= 0);
        if (decision.isWaived()) {
            decision.setReason("号源原价为 0，免收");
        }
        if (child) {
            decision.setReason(appendReason(decision.getReason(),
                    PediatricSurchargePolicy.reasonText(context.getBirthDate(), context.getVisitDate())));
        }

        if (context == null) {
            return decision;
        }
        RevisitFeePolicy policy = matchPolicy(context);
        if (policy == null) {
            return decision;
        }
        int mode = policy.getChargeMode() == null
                ? RevisitChargeModeEnum.FULL.getCode() : policy.getChargeMode();
        decision.setPolicyId(policy.getId());
        decision.setPolicyName(policy.getPolicyName());
        decision.setChargeMode(mode);
        if (mode == RevisitChargeModeEnum.FREE_REGIST.getCode()) {
            // 🔑 FREE_REGIST「免挂号费」在政策合并后已无对应项：门诊唯一费用是门诊诊查费。
            //   这里映射成「全免」而不是「照收」——反过来会让原来免费的复诊突然开始收费，
            //   是本轮整改里最容易踩的静默坑（患者投诉 + 结算口径追溯都很难解释）。
            //   依据：湘医保发〔2025〕58号废止挂号费，挂号费职能已并入门诊诊查费。
            decision.setDiagnosisFee(BigDecimal.ZERO);
            // 免收把儿童加收一并免掉，但加收依据要留在备注里：否则事后看到「按儿童价却没收」
            // 会以为是漏收，而不是策略免收。
            decision.setReason(appendReason(decision.getReason(),
                    "复诊收费策略「" + policy.getPolicyName() + "」免收：门诊诊查费"
                            + "（原「免挂号费」口径，挂号费已按湘医保发〔2025〕58号废止并入诊查费）"));
        } else if (mode == RevisitChargeModeEnum.FREE_ALL.getCode()) {
            decision.setDiagnosisFee(BigDecimal.ZERO);
            decision.setReason(appendReason(decision.getReason(),
                    "复诊收费策略「" + policy.getPolicyName() + "」免收：门诊诊查费"));
        } else {
            decision.setReason(appendReason(decision.getReason(),
                    "复诊收费策略「" + policy.getPolicyName() + "」：全额收费"));
        }
        // 免收后 childSurchargeAmount 仍保留实算值：它是「本该加收多少」的凭据，
        // 与 diagnosisFee（实收0）分开存，审计要能核对减免幅度对不对。
        decision.setWaived(decision.getDiagnosisFee().compareTo(BigDecimal.ZERO) <= 0);
        return decision;
    }

    /**
     * 拼接收费单备注：多处判定理由（原价0免收 / 儿童加收 / 复诊策略）可能同时成立，
     * 后写的不能覆盖先写的 —— 备注是事后追溯「这笔为什么是这个数」的唯一出处，丢了就查不回来。
     */
    private static String appendReason(String base, String extra) {
        if (!TextUtil.hasText(base)) {
            return extra;
        }
        if (!TextUtil.hasText(extra)) {
            return base;
        }
        return base + "；" + extra;
    }

    /**
     * 取启用中的候选策略，在内存里按 priority 升序（同优先级按 id 升序）找第一条全条件命中的。
     *
     * <p>为什么不在 SQL 里拼完：三态条件（不限/要求同/要求不同）与「判不出来就不命中」
     * 这套兜底口径，用 SQL 表达要写成一堆 OR IS NULL，读的人看不出哪条是收口方向。
     * 策略表只有个位数行，内存匹配更贵不了多少，却能让规则一眼对上。
     */
    private RevisitFeePolicy matchPolicy(RevisitFeeContext context) {
        if (context.getRevisitSource() == null || context.getRevisitSource() <= 0) {
            return null;
        }
        LambdaQueryWrapper<RevisitFeePolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RevisitFeePolicy::getStatus, 1)
                .in(RevisitFeePolicy::getRevisitSource, 0, context.getRevisitSource());
        List<RevisitFeePolicy> candidates = this.list(wrapper);
        if (CollectionUtils.isEmpty(candidates)) {
            return null;
        }
        return candidates.stream()
                .filter(p -> triStateHit(p.getSameDoctor(), context.getSameDoctor()))
                .filter(p -> triStateHit(p.getSameDept(), context.getSameDept()))
                .filter(p -> withinDaysHit(p.getWithinDays(), context.getDaysSinceOrigin()))
                .min(Comparator.comparing((RevisitFeePolicy p) -> p.getPriority() == null ? Integer.MAX_VALUE : p.getPriority())
                        .thenComparing(RevisitFeePolicy::getId))
                .orElse(null);
    }

    /**
     * 三态匹配：0 不限；1 要求相同；2 要求不同。
     * 实际值判不出来（null）时，凡带 1/2 条件的策略一律<b>不命中</b> —— 判不准就按更保守的收费走。
     */
    private boolean triStateHit(Integer condition, Boolean actual) {
        int cond = condition == null ? RevisitFeeConditionEnum.ANY.getCode() : condition;
        if (cond == RevisitFeeConditionEnum.ANY.getCode()) {
            return true;
        }
        if (actual == null) {
            return false;
        }
        return cond == RevisitFeeConditionEnum.SAME.getCode() ? actual : !actual;
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
    public PageResult<RevisitFeePolicyVO> listPage(RevisitFeePolicyPageQueryDTO queryDTO) {
        LambdaQueryWrapper<RevisitFeePolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(queryDTO.getPolicyName()),
                        RevisitFeePolicy::getPolicyName, queryDTO.getPolicyName())
                .eq(queryDTO.getRevisitSource() != null,
                        RevisitFeePolicy::getRevisitSource, queryDTO.getRevisitSource())
                .eq(queryDTO.getChargeMode() != null,
                        RevisitFeePolicy::getChargeMode, queryDTO.getChargeMode())
                .eq(queryDTO.getStatus() != null, RevisitFeePolicy::getStatus, queryDTO.getStatus())
                // 必须带 id 兜底二级键：priority 并列时翻页会重复/丢行
                .orderByAsc(RevisitFeePolicy::getPriority)
                .orderByAsc(RevisitFeePolicy::getId);

        Page<RevisitFeePolicy> page = this.page(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PageResult<>();
        }
        List<RevisitFeePolicyVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public RevisitFeePolicyVO detail(Long id) {
        RevisitFeePolicy policy = super.getById(id);
        if (policy == null) {
            throw new BusinessException("复诊收费策略不存在");
        }
        return toVO(policy);
    }

    @Override
    public void upsert(RevisitFeePolicyUpsertDTO upsertDTO) {
        RevisitFeePolicy entity = new RevisitFeePolicy();
        BeanUtils.copyProperties(upsertDTO, entity);
        if (entity.getSameDoctor() == null) {
            entity.setSameDoctor(RevisitFeeConditionEnum.ANY.getCode());
        }
        if (entity.getSameDept() == null) {
            entity.setSameDept(RevisitFeeConditionEnum.ANY.getCode());
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

    private RevisitFeePolicyVO toVO(RevisitFeePolicy entity) {
        RevisitFeePolicyVO vo = new RevisitFeePolicyVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * 价表取门诊诊查费档位全额（政策 OPD_CONSULT），缺行/停用回退枚举兜底价并留 error 痕。
     */
    private BigDecimal policyAmountOrFallback(ClinicDiagnosisTierEnum tier) {
        PolicyPriceVO price = chargeFacadeService.policyPrice(
                ChargeFacadeService.POLICY_OPD_CONSULT, tier.getCode());
        if (price != null && price.getAmount() != null) {
            return price.getAmount();
        }
        log.error("[门诊诊查费] 价表缺行或停用 policy=OPD_CONSULT tier={}({})，回退枚举兜底价 {} 元，"
                + "请在价格管理页「政策价」补配置", tier.getCode(), tier.getLabel(), tier.getAmount());
        return new BigDecimal(tier.getAmount());
    }

    /**
     * 价表取门诊诊查费档位加收额，缺行/停用回退枚举兜底加收额并留 error 痕；无加收结构返回 null。
     */
    private BigDecimal policySurchargeOrFallback(ClinicDiagnosisTierEnum tier) {
        PolicyPriceVO price = chargeFacadeService.policyPrice(
                ChargeFacadeService.POLICY_OPD_CONSULT, tier.getCode());
        if (price != null) {
            return price.getSurcharge();
        }
        log.error("[门诊诊查费] 价表缺行或停用 policy=OPD_CONSULT tier={}({})，加收额回退枚举兜底 {} 元，"
                + "请在价格管理页「政策价」补配置", tier.getCode(), tier.getLabel(), tier.getSurcharge());
        return tier.getSurcharge() == null ? null : new BigDecimal(tier.getSurcharge());
    }

}
