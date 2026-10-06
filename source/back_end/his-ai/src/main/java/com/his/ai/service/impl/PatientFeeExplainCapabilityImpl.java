package com.his.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.PaymentItemTypeEnum;
import com.his.ai.dto.PatientFeeExplainDTO;
import com.his.ai.service.PatientFeeExplainCapability;
import com.his.ai.vo.FeeCatalogGroupVO;
import com.his.ai.vo.PatientFeeExplainVO;
import com.his.charge.entity.BizInsuranceSettlement;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.entity.BizSettlementBillItem;
import com.his.charge.mapper.BizInsuranceSettlementMapper;
import com.his.charge.mapper.BizSettlementBillItemMapper;
import com.his.charge.mapper.BizSettlementBillMapper;
import com.his.common.exception.BusinessException;
import com.his.patient.service.PatientGuardianService;
import com.his.system.utils.UserUtils;
import com.his.system.entity.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 患者端费用解释。
 * <p>
 * <b>患者真正在问什么：</b>「医保不是报 85% 吗，为什么我掏了这么多？」
 * 这个问题的答案是确定性计算，不需要模型：把账单明细按医保目录类别拆开 ——
 * 甲类全额纳入报销、乙类个人先负担一部分、丙类和自费医保一分不承担。
 * 自付占比高，几乎总是因为账里有自费项目，而不是算错了。
 * 不说清楚这一层，患者只会认定医院多收钱 —— 这是门诊最常见的一类投诉。
 * <p>
 * <b>为什么不解释医保政策：</b>起付线、封顶线、各险种报销比例会变、各地不同，
 * 而且政策文本的解释权在医保经办。系统只陈述<b>这张账单上实际发生了什么</b>，
 * 政策细节一律引导到窗口 —— 与 FAQ 语料「涉及比例一律引导式」是同一条纪律。
 * 唯一例外是账单上已落库的 {@code coverage_ratio}（结算时真实使用的比例），
 * 那是事实不是政策解释。
 * <p>
 * <b>不走模型</b>（施工手册纪律 9）：拆分是确定性计算、逐类说明是固定文案，
 * 输入与答案都能穷举成一张表；且解释的是钱，模型幻觉不可接受。
 * {@code summary} 由规则拼接，本能力不产生模型调用与审计行。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientFeeExplainCapabilityImpl implements PatientFeeExplainCapability {

    /**
     * 目录类别：0-自费 1-甲类 2-乙类 3-丙类
     */
    private static final int CATALOG_SELF = 0;
    private static final int CATALOG_A = 1;
    private static final int CATALOG_B = 2;
    private static final int CATALOG_C = 3;

    /**
     * 每个分组最多列出的项目名数量：列太长患者反而不看
     */
    private static final int MAX_ITEM_NAMES = 8;

    /**
     * 自付 Top 明细条数
     */
    private static final int TOP_SELF_LIMIT = 5;

    private static final String ADVICE =
            "以上是本账单的费用构成说明，实际报销以医保经办和收费窗口的解释为准；"
                    + "对某项收费有疑问，可在收费窗口打印明细清单核对。";

    private final BizSettlementBillMapper billMapper;

    private final BizSettlementBillItemMapper billItemMapper;

    private final BizInsuranceSettlementMapper insuranceSettlementMapper;

    private final PatientGuardianService patientGuardianService;

    private static String catalogText(Integer catalogType) {
        if (catalogType == null) {
            return "自费";
        }
        return switch (catalogType) {
            case CATALOG_A -> "甲类";
            case CATALOG_B -> "乙类";
            case CATALOG_C -> "丙类";
            default -> "自费";
        };
    }

    // ---------------------------------------------------------------- 规则层

    /**
     * 各类别的医保口径说明。
     * <p>
     * 只说「医保承不承担」这件事本身，不写任何比例数字 ——
     * 乙类先自付多少、甲类报多少，各地各险种不同，写死就是错的。
     */
    private static String ruleText(Integer catalogType) {
        if (catalogType == null) {
            return "医保不承担，全额由个人支付";
        }
        return switch (catalogType) {
            case CATALOG_A -> "全额纳入医保报销范围";
            case CATALOG_B -> "个人先负担一部分，剩余部分纳入报销";
            case CATALOG_C -> "医保不承担，全额由个人支付";
            default -> "医保不承担，全额由个人支付";
        };
    }

    /**
     * 单项的个人负担金额。
     * <p>
     * 优先用落库的 selfAmount；它为 0 时按「金额 - 统筹 - 个账」倒推 ——
     * 部分场景结算时只写了统筹与个账，selfAmount 留空，此时倒推比显示 0 更接近事实。
     */
    private static BigDecimal selfPartOf(BizSettlementBillItem item) {
        BigDecimal self = nz(item.getSelfAmount());
        if (self.compareTo(BigDecimal.ZERO) > 0) {
            return self;
        }
        return nz(item.getAmount()).subtract(nz(item.getPoolAmount())).subtract(nz(item.getAccountAmount()));
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 占比（百分数，保留 1 位）。分母为 0 时返回 0，不做除零。
     */
    private static BigDecimal ratio(BigDecimal part, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return nz(part).multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP);
    }

    public PatientFeeExplainVO execute(PatientFeeExplainDTO dto) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getPatientId() == null) {
            throw new BusinessException("未获取到就诊人身份，请重新登录");
        }

        BizSettlementBill bill = billMapper.selectById(dto.getBillId());
        if (bill == null) {
            throw new BusinessException("账单不存在：" + dto.getBillId());
        }
        if (patientGuardianService.patientScopeViolated(bill.getPatientId())) {
            throw new BusinessException("账单不存在或无权查看：" + dto.getBillId());
        }

        List<BizSettlementBillItem> items = billItemMapper.selectList(
                new LambdaQueryWrapper<BizSettlementBillItem>()
                        .eq(BizSettlementBillItem::getBillId, bill.getId()));

        BizInsuranceSettlement insurance = loadInsurance(bill.getId());

        PatientFeeExplainVO vo = new PatientFeeExplainVO();
        vo.setBillId(String.valueOf(bill.getId()));
        vo.setBillNo(bill.getBillNo());
        vo.setPatientName(bill.getPatientName());
        vo.setBillDate(bill.getBillDate());
        vo.setTotalAmount(nz(bill.getTotalAmount()));
        vo.setPoolAmount(nz(bill.getPoolAmount()));
        vo.setAccountAmount(nz(bill.getAccountAmount()));
        vo.setSelfAmount(nz(bill.getSelfAmount()));
        vo.setSelfRatio(ratio(vo.getSelfAmount(), vo.getTotalAmount()));

        if (insurance != null) {
            vo.setInsuranceType(insurance.getInsuranceType());
            vo.setCoverageRatio(insurance.getCoverageRatio());
        } else if (StringUtils.hasText(bill.getInsuranceType())) {
            vo.setInsuranceType(bill.getInsuranceType());
        }

        // 规则层：按目录类别 / 项目类型分组，再取自付 Top
        List<FeeCatalogGroupVO> catalogGroups = buildCatalogGroups(items, vo.getTotalAmount());
        vo.setCatalogGroups(catalogGroups);
        vo.setItemGroups(buildItemGroups(items));
        vo.setTopSelfItems(buildTopSelfItems(items));
        vo.setReasonText(buildReasonText(catalogGroups, vo.getSelfRatio(), vo.getSelfAmount()));

        String ruleSummary = buildRuleSummary(vo, catalogGroups);
        vo.setSummary(ruleSummary);

        vo.setAdvice(ADVICE);
        return vo;
    }

    private BizInsuranceSettlement loadInsurance(Long billId) {
        List<BizInsuranceSettlement> list = insuranceSettlementMapper.selectList(
                new LambdaQueryWrapper<BizInsuranceSettlement>()
                        .eq(BizInsuranceSettlement::getBillId, billId)
                        .orderByDesc(BizInsuranceSettlement::getId)
                        .last("LIMIT 1"));
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    // ---------------------------------------------------------------- 工具

    /**
     * 按医保目录类别分组 —— 这是「为什么自付这么多」的答案所在。
     */
    private List<FeeCatalogGroupVO> buildCatalogGroups(List<BizSettlementBillItem> items, BigDecimal total) {
        Map<Integer, List<BizSettlementBillItem>> byCatalog = new LinkedHashMap<>();
        if (items != null) {
            for (BizSettlementBillItem item : items) {
                // null 目录类别按自费处理：医保目录里查不到的项目，结算时就是按自费走的
                Integer type = item.getCatalogType() == null ? CATALOG_SELF : item.getCatalogType();
                byCatalog.computeIfAbsent(type, k -> new ArrayList<>()).add(item);
            }
        }
        List<FeeCatalogGroupVO> groups = new ArrayList<>();
        for (Map.Entry<Integer, List<BizSettlementBillItem>> entry : byCatalog.entrySet()) {
            List<BizSettlementBillItem> group = entry.getValue();
            BigDecimal amount = group.stream()
                    .map(item -> nz(item.getAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            FeeCatalogGroupVO vo = new FeeCatalogGroupVO();
            vo.setCatalogType(entry.getKey());
            vo.setCatalogText(catalogText(entry.getKey()));
            vo.setAmount(amount);
            vo.setRatio(ratio(amount, total));
            vo.setRuleText(ruleText(entry.getKey()));
            vo.setItemCount(group.size());
            List<String> names = new ArrayList<>();
            for (BizSettlementBillItem item : group) {
                if (names.size() >= MAX_ITEM_NAMES) {
                    break;
                }
                if (StringUtils.hasText(item.getItemName()) && !names.contains(item.getItemName())) {
                    names.add(item.getItemName());
                }
            }
            vo.setItemNames(names);
            groups.add(vo);
        }
        groups.sort(Comparator.comparing(FeeCatalogGroupVO::getAmount).reversed());
        return groups;
    }

    private List<PatientFeeExplainVO.FeeItemGroupVO> buildItemGroups(List<BizSettlementBillItem> items) {
        Map<Integer, List<BizSettlementBillItem>> byType = new LinkedHashMap<>();
        if (items != null) {
            for (BizSettlementBillItem item : items) {
                Integer type = item.getItemType() == null ? 0 : item.getItemType();
                byType.computeIfAbsent(type, k -> new ArrayList<>()).add(item);
            }
        }
        List<PatientFeeExplainVO.FeeItemGroupVO> groups = new ArrayList<>();
        for (Map.Entry<Integer, List<BizSettlementBillItem>> entry : byType.entrySet()) {
            BigDecimal amount = entry.getValue().stream()
                    .map(item -> nz(item.getAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            PatientFeeExplainVO.FeeItemGroupVO vo = new PatientFeeExplainVO.FeeItemGroupVO();
            vo.setItemType(entry.getKey());
            vo.setItemTypeText(PaymentItemTypeEnum.getText(entry.getKey()));
            vo.setAmount(amount);
            vo.setItemCount(entry.getValue().size());
            groups.add(vo);
        }
        groups.sort(Comparator.comparing(PatientFeeExplainVO.FeeItemGroupVO::getAmount).reversed());
        return groups;
    }

    /**
     * 自付金额最高的几项 —— 患者最常提出疑问的就是这几项。
     */
    private List<PatientFeeExplainVO.FeeTopItemVO> buildTopSelfItems(List<BizSettlementBillItem> items) {
        List<BizSettlementBillItem> sorted = new ArrayList<>(items == null ? List.of() : items);
        sorted.sort((a, b) -> selfPartOf(b).compareTo(selfPartOf(a)));
        List<PatientFeeExplainVO.FeeTopItemVO> result = new ArrayList<>();
        for (BizSettlementBillItem item : sorted) {
            if (result.size() >= TOP_SELF_LIMIT) {
                break;
            }
            if (nz(item.getAmount()).compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            PatientFeeExplainVO.FeeTopItemVO vo = new PatientFeeExplainVO.FeeTopItemVO();
            vo.setItemName(item.getItemName());
            vo.setAmount(nz(item.getAmount()));
            vo.setCatalogText(catalogText(item.getCatalogType() == null ? CATALOG_SELF : item.getCatalogType()));
            vo.setSelfAmount(nz(item.getSelfAmount()));
            result.add(vo);
        }
        return result;
    }

    /**
     * 「为什么自付这么多」的核心答案。
     * <p>
     * 只陈述账单上的事实：自费/丙类占了多少。不提政策，不替医保做解释。
     */
    private String buildReasonText(List<FeeCatalogGroupVO> groups, BigDecimal selfRatio, BigDecimal selfAmount) {
        BigDecimal selfPayPart = BigDecimal.ZERO;
        for (FeeCatalogGroupVO group : groups) {
            if (group.getCatalogType() != null
                    && (group.getCatalogType() == CATALOG_SELF || group.getCatalogType() == CATALOG_C)) {
                selfPayPart = selfPayPart.add(nz(group.getAmount()));
            }
        }
        BigDecimal total = groups.stream()
                .map(group -> nz(group.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return "本账单暂无费用明细，可在收费窗口打印明细清单核对。";
        }
        BigDecimal selfPartRatio = ratio(selfPayPart, total);

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("本账单共 %.2f 元，其中自付 %.2f 元（占 %.1f%%）。",
                total, selfAmount, selfRatio));
        if (selfPayPart.compareTo(BigDecimal.ZERO) > 0) {
            builder.append(String.format("账里有 %.2f 元属于医保不承担的自费/丙类项目（占 %.1f%%），"
                            + "这部分不进报销计算，是自付占比高的主要原因。",
                    selfPayPart, selfPartRatio));
        } else {
            builder.append("账内项目均属于医保目录内，自付部分来自报销比例之外的个人负担部分。");
        }
        return builder.toString();
    }

    private String buildRuleSummary(PatientFeeExplainVO vo, List<FeeCatalogGroupVO> groups) {
        if (groups.isEmpty()) {
            return "本账单暂无明细，可在收费窗口打印明细清单核对。";
        }
        FeeCatalogGroupVO top = groups.get(0);
        return String.format("这笔费用共 %.2f 元，医保统筹支付 %.2f 元，个人自付 %.2f 元；"
                        + "其中%s %.2f 元，占比最高。",
                nz(vo.getTotalAmount()), nz(vo.getPoolAmount()), nz(vo.getSelfAmount()),
                top.getCatalogText(), nz(top.getAmount()));
    }
}