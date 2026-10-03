package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.system.dto.DrugRationalGroupDTO;
import com.his.system.dto.DrugRationalItemDTO;
import com.his.system.entity.SysDrugDoseLimit;
import com.his.system.entity.SysDrugInteraction;
import com.his.system.mapper.SysDrugDoseLimitMapper;
import com.his.system.mapper.SysDrugInteractionMapper;
import com.his.system.service.DrugRationalCheckService;
import com.his.system.support.DosageTextParser;
import com.his.system.vo.DrugRationalGroupVO;
import com.his.system.vo.DrugRationalHitVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 合理用药审查实现
 * <p>
 * 匹配走<b>成分关键字包含</b>：知识的两端各在「药品名称 + 通用名」里找一条单据行，
 * 且两行必须是<b>不同的行</b> —— 一张单据行自己命中一对的两个成分说明它是复方制剂
 * （本来就设计在一起用），报出来只会逼药师多点一次确认。
 * <p>
 * 每次调用读一遍知识表（启用行两百来条，两个小 SELECT），<b>不做缓存</b>：
 * 这张表的价值在于「改了立刻生效」，缓存会让药师改完规则后继续看到旧结论，
 * 而这类不一致是无声的。真出现性能问题再按「改一次刷一次」加失效钩子。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugRationalCheckServiceImpl implements DrugRationalCheckService {

    private static final String TYPE_INTERACTION = "INTERACTION";
    private static final String TYPE_DOSE_SINGLE = "DOSE_SINGLE";
    private static final String TYPE_DOSE_DAILY = "DOSE_DAILY";

    /** 只有禁忌级能拦（sql/130 文件头第三条） */
    private static final int SEVERITY_FORBIDDEN = 1;
    private static final int SEVERITY_CAUTION = 2;

    private static final BigDecimal THOUSAND = new BigDecimal("1000");

    private final SysDrugInteractionMapper interactionMapper;
    private final SysDrugDoseLimitMapper doseLimitMapper;

    @Override
    public List<DrugRationalHitVO> check(List<DrugRationalItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        Knowledge knowledge = loadKnowledge();
        return doCheck(knowledge, items);
    }

    @Override
    public List<DrugRationalGroupVO> checkGroups(List<DrugRationalGroupDTO> groups) {
        if (groups == null || groups.isEmpty()) {
            return List.of();
        }
        Knowledge knowledge = loadKnowledge();
        List<DrugRationalGroupVO> results = new ArrayList<>(groups.size());
        for (DrugRationalGroupDTO group : groups) {
            if (group == null) {
                continue;
            }
            List<DrugRationalHitVO> hits = group.getItems() == null
                    ? List.of() : doCheck(knowledge, group.getItems());
            DrugRationalGroupVO vo = new DrugRationalGroupVO();
            vo.setGroupId(group.getGroupId());
            vo.setHits(hits);
            vo.setBlocked(hits.stream().anyMatch(h -> Boolean.TRUE.equals(h.getBlocked())));
            vo.setBlockMessage(joinBlockMessage(hits));
            results.add(vo);
        }
        return results;
    }

    private List<DrugRationalHitVO> doCheck(Knowledge knowledge, List<DrugRationalItemDTO> items) {
        List<DrugRationalHitVO> hits = new ArrayList<>();
        collectInteractionHits(knowledge, items, hits);
        collectDoseHits(knowledge, items, hits);
        // 禁忌排最前：审方弹窗与列表标注都要让人先看到拦人的那条
        hits.sort(Comparator.comparingInt(h -> SEVERITY_FORBIDDEN == nullSafeInt(h.getSeverity()) ? 0 : 1));
        return hits;
    }

    // 相互作用

    private void collectInteractionHits(Knowledge knowledge, List<DrugRationalItemDTO> items,
                                       List<DrugRationalHitVO> hits) {
        for (SysDrugInteraction rule : knowledge.interactions()) {
            int indexA = -1;
            for (int i = 0; i < items.size(); i++) {
                if (matches(items.get(i), rule.getComponentA())) {
                    indexA = i;
                    break;
                }
            }
            if (indexA < 0) {
                continue;
            }
            // 另一味药必须在【另一行】：同一行命中两端 = 复方制剂，不算相互作用
            int indexB = -1;
            for (int i = 0; i < items.size(); i++) {
                if (i != indexA && matches(items.get(i), rule.getComponentB())) {
                    indexB = i;
                    break;
                }
            }
            if (indexB < 0) {
                continue;
            }
            hits.add(buildInteractionHit(rule, items.get(indexA), items.get(indexB)));
        }
    }

    private DrugRationalHitVO buildInteractionHit(SysDrugInteraction rule, DrugRationalItemDTO a,
                                                 DrugRationalItemDTO b) {
        DrugRationalHitVO hit = new DrugRationalHitVO();
        hit.setHitType(TYPE_INTERACTION);
        hit.setSeverity(rule.getSeverity());
        hit.setBlocked(SEVERITY_FORBIDDEN == nullSafeInt(rule.getSeverity()));
        hit.setKnowledgeId(rule.getId());
        hit.setComponentA(rule.getComponentA());
        hit.setComponentB(rule.getComponentB());
        hit.setDrugIdA(a.getDrugId());
        hit.setDrugNameA(a.getDrugName());
        hit.setDrugIdB(b.getDrugId());
        hit.setDrugNameB(b.getDrugName());
        // 后果正文逐字引用知识表，只在前面拼上「哪两味药」——药师要能对着表核对这句话从哪来
        hit.setMessage(a.getDrugName() + " × " + b.getDrugName() + "：" + rule.getInteractionDesc());
        hit.setSuggestion(rule.getSuggestion());
        return hit;
    }

    // 剂量上限

    private void collectDoseHits(Knowledge knowledge, List<DrugRationalItemDTO> items,
                                List<DrugRationalHitVO> hits) {
        for (DrugRationalItemDTO item : items) {
            BigDecimal singleMg = DosageTextParser.singleDoseMg(item.getSingleDosage(), item.getSpecification());
            if (singleMg == null) {
                // 单次量算不出来 → 日累计也算不出来，整条不判（见 DosageTextParser 类注释）
                continue;
            }
            for (SysDrugDoseLimit limit : knowledge.doseLimits()) {
                if (!matches(item, limit.getComponent())) {
                    continue;
                }
                BigDecimal factor = DosageTextParser.toMg(BigDecimal.ONE, limit.getDoseUnit());
                if (factor == null) {
                    // 库里存了非质量单位（历史数据或手工插库），拿它比较就是编数字
                    continue;
                }
                addIfExceeded(hits, item, limit, singleMg, factor, true);
                BigDecimal times = DosageTextParser.timesPerDay(item.getFrequency());
                if (times != null) {
                    addIfExceeded(hits, item, limit, singleMg.multiply(times), factor, false);
                }
            }
        }
    }

    /**
     * @param takenMg 待比较量（单次给药量或单次×每日次数）
     * @param single  true=比单次上限，false=比每日上限
     */
    private void addIfExceeded(List<DrugRationalHitVO> hits, DrugRationalItemDTO item, SysDrugDoseLimit limit,
                               BigDecimal takenMg, BigDecimal factor, boolean single) {
        BigDecimal limitRaw = single ? limit.getMaxSingleDose() : limit.getMaxDailyDose();
        if (limitRaw == null) {
            return;
        }
        BigDecimal limitMg = limitRaw.multiply(factor);
        // 严格大于才算超量：正好等于极量是方案内的最大合法剂量，报出来就是误报
        if (takenMg.compareTo(limitMg) <= 0) {
            return;
        }
        DrugRationalHitVO hit = new DrugRationalHitVO();
        hit.setHitType(single ? TYPE_DOSE_SINGLE : TYPE_DOSE_DAILY);
        hit.setSeverity(SEVERITY_CAUTION);
        hit.setBlocked(false);
        hit.setKnowledgeId(limit.getId());
        hit.setComponentA(limit.getComponent());
        hit.setDrugIdA(item.getDrugId());
        hit.setDrugNameA(item.getDrugName());
        hit.setMessage(item.getDrugName() + (single ? " 单次" : " 每日累计")
                + mgText(takenMg) + "，超过剂量上限 " + mgText(limitMg)
                + (StringUtils.hasText(limit.getNote()) ? "（口径：" + limit.getNote() + "）" : ""));
        hit.setSuggestion(limit.getNote());
        hits.add(hit);
    }

    // 公共件

    /** 药品名称或通用名包含成分关键字即命中（大小写无关，字典里拉丁字母写法不统一） */
    private boolean matches(DrugRationalItemDTO item, String component) {
        if (!StringUtils.hasText(component)) {
            return false;
        }
        String key = component.trim().toLowerCase(Locale.ROOT);
        return contains(item.getDrugName(), key) || contains(item.getGenericName(), key);
    }

    private boolean contains(String text, String lowerKey) {
        return StringUtils.hasText(text) && text.toLowerCase(Locale.ROOT).contains(lowerKey);
    }

    private String joinBlockMessage(List<DrugRationalHitVO> hits) {
        List<String> reasons = hits.stream()
                .filter(h -> Boolean.TRUE.equals(h.getBlocked()))
                .map(DrugRationalHitVO::getMessage)
                .toList();
        return reasons.isEmpty() ? null : String.join("；", reasons);
    }

    private Knowledge loadKnowledge() {
        LambdaQueryWrapper<SysDrugInteraction> iw = new LambdaQueryWrapper<>();
        iw.eq(SysDrugInteraction::getStatus, 1).orderByAsc(SysDrugInteraction::getSeverity);
        LambdaQueryWrapper<SysDrugDoseLimit> dw = new LambdaQueryWrapper<>();
        dw.eq(SysDrugDoseLimit::getStatus, 1);
        return new Knowledge(interactionMapper.selectList(iw), doseLimitMapper.selectList(dw));
    }

    /** 数值显示：整千克显示成 g，其余保持 mg，别在提示里写一串 0 */
    private String mgText(BigDecimal mg) {
        BigDecimal value = mg.stripTrailingZeros();
        if (value.compareTo(THOUSAND) >= 0 && value.remainder(THOUSAND).compareTo(BigDecimal.ZERO) == 0) {
            return value.divide(THOUSAND, 2, java.math.RoundingMode.HALF_UP)
                    .stripTrailingZeros().toPlainString() + "g";
        }
        return value.toPlainString() + "mg";
    }

    private int nullSafeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private record Knowledge(List<SysDrugInteraction> interactions, List<SysDrugDoseLimit> doseLimits) {

        Knowledge {
            interactions = Objects.requireNonNullElseGet(interactions, List::of);
            doseLimits = Objects.requireNonNullElseGet(doseLimits, List::of);
        }
    }
}
