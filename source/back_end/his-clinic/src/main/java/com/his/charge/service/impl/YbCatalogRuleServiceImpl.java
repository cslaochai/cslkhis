package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.charge.dto.YbCatalogRuleImportDTO;
import com.his.charge.entity.BizInsuranceCatalogRule;
import com.his.charge.mapper.BizInsuranceCatalogRuleMapper;
import com.his.charge.service.YbCatalogRuleService;
import com.his.charge.vo.YbImportResultVO;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 医保目录报销规则服务实现。
 */
@Service
@RequiredArgsConstructor
public class YbCatalogRuleServiceImpl implements YbCatalogRuleService {

    private static final int W_ITEM_CODE = 32;
    private static final int W_ITEM_NAME = 200;
    private static final int W_REMARK = 500;
    private static final int W_INSURANCE_TYPE = 32;

    private final BizInsuranceCatalogRuleMapper ruleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YbImportResultVO importBatch(List<YbCatalogRuleImportDTO> items) {
        YbImportResultVO result = new YbImportResultVO();
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        for (YbCatalogRuleImportDTO item : items == null ? List.<YbCatalogRuleImportDTO>of() : items) {
            // rule_no 非空由 DTO @NotBlank 收口（缺项直接 400，不必逐行判断后再跳过 —— 编号是幂等键，缺了无从判断是新增还是更新）
            if (item == null || !TextUtil.hasText(item.getItemCode()) || !TextUtil.hasText(item.getRuleNo())) {
                skipped++;
                continue;
            }
            String ruleNo = item.getRuleNo().trim();
            BizInsuranceCatalogRule existed = ruleNo == null ? null
                    : ruleMapper.selectOne(new LambdaQueryWrapper<BizInsuranceCatalogRule>()
                    .eq(BizInsuranceCatalogRule::getRuleNo, ruleNo)
                    .last("LIMIT 1"));
            BizInsuranceCatalogRule entity = existed == null ? new BizInsuranceCatalogRule() : existed;
            apply(entity, item, ruleNo);
            if (existed == null) {
                entity.setStatus(1);
                entity.setDelFlag(0);
                entity.setCreateBy(UserUtils.getCurrentUser().getRealName());
                // id 走 @TableId(ASSIGN_ID) 雪花策略，这里必须留空
                ruleMapper.insert(entity);
                inserted++;
            } else {
                entity.setUpdateBy(UserUtils.getCurrentUser().getRealName());
                ruleMapper.updateById(entity);
                updated++;
            }
        }
        result.setInserted(inserted);
        result.setUpdated(updated);
        result.setSkipped(skipped);
        return result;
    }

    private void apply(BizInsuranceCatalogRule entity, YbCatalogRuleImportDTO item, String ruleNo) {
        LocalDate eff = parseDate(item.getEffectiveDate());
        LocalDate exp = parseDate(item.getExpireDate());
        if (eff != null && exp != null && exp.isBefore(eff)) {
            throw new BusinessException("规则失效日期早于生效日期：" + ruleNo);
        }
        entity.setRuleNo(ruleNo);
        entity.setItemCode(TextUtil.cut(item.getItemCode().trim(), W_ITEM_CODE));
        entity.setItemName(TextUtil.cut(item.getItemName(), W_ITEM_NAME));
        entity.setCatalogType(item.getCatalogType());
        entity.setEncounterType(item.getEncounterType());
        // 险种存主档原文（结算时 resolveInsurance 传的就是原文）；留空=通用规则
        entity.setInsuranceType(TextUtil.hasText(item.getInsuranceType())
                ? TextUtil.cut(item.getInsuranceType().trim(), W_INSURANCE_TYPE) : null);
        entity.setSelfPayRatio(item.getSelfPayRatio());
        entity.setDeductible(zeroIfNull(item.getDeductible()));
        entity.setCeiling(zeroIfNull(item.getCeiling()));
        entity.setPoolRatio(item.getPoolRatio());
        entity.setLimitFlags(0);
        entity.setEffectiveDate(eff == null ? LocalDate.now() : eff);
        entity.setExpireDate(exp);
        entity.setPriority(item.getPriority() == null ? 0 : item.getPriority());
        entity.setRemark(TextUtil.cut(item.getRemark(), W_REMARK));
    }

    private java.math.BigDecimal zeroIfNull(java.math.BigDecimal value) {
        return value == null ? java.math.BigDecimal.ZERO : value;
    }

    private LocalDate parseDate(String text) {
        return !TextUtil.hasText(text) ? null : LocalDate.parse(text.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
    }
}