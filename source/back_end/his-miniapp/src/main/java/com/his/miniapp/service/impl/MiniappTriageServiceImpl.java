package com.his.miniapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.appoint.dto.ScheduleSelectQueryDTO;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.miniapp.entity.BizTriageRule;
import com.his.miniapp.mapper.BizTriageRuleMapper;
import com.his.miniapp.service.MiniappDirectoryService;
import com.his.miniapp.service.MiniappTriageService;
import com.his.miniapp.vo.TriageDeptVO;
import com.his.miniapp.vo.TriageSymptomVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 患者端智能导诊实现。
 *
 * <p><b>刻意不做模型推理：</b>主诉 → 关键词 → 科室三步全由代码完成。模型若接入，
 * 只能用在「把口语归一成症状词」和「生成补充追问」两处，不得决定推荐结果 ——
 * 导诊推荐错了不会有任何报错，后果是患者挂错号白跑一趟。
 * <p><b>急症信号来自表的 urgent_flag 硬编码</b>，不是模型给的分数。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniappTriageServiceImpl extends ServiceImpl<BizTriageRuleMapper, BizTriageRule> implements MiniappTriageService {

    /**
     * 最多推荐几个科室（给太多等于没推荐）
     */
    private static final int MAX_RESULT = 3;

    private final BizTriageRuleMapper bizTriageRuleMapper;
    private final MiniappDirectoryService miniappDirectoryService;

    @Override
    public List<TriageDeptVO> recommend(String description) {
        if (!TextUtil.hasText(description)) {
            return List.of();
        }
        String text = description.trim();
        List<BizTriageRule> hit = enabledRules().stream()
                .filter(r -> matches(r, text))
                .toList();
        if (hit.isEmpty()) {
            // 不瞎推一个科：返回空，前端引导患者点常见症状标签或自行选科室
            log.info("[智能导诊] 未命中任何规则，主诉长度={}", text.length());
            return List.of();
        }
        // 同一科室可能被多个症状命中，保留排序最靠前的那条（它带最贴合的推荐理由）
        Map<Long, BizTriageRule> best = new LinkedHashMap<>();
        for (BizTriageRule r : hit) {
            BizTriageRule old = best.get(r.getDeptId());
            if (old == null || cmp(r, old) < 0) {
                best.put(r.getDeptId(), r);
            }
        }
        return best.values().stream()
                .sorted(this::cmp)
                .limit(MAX_RESULT)
                .map(this::toDeptVO)
                .toList();
    }

    @Override
    public List<TriageSymptomVO> hotSymptoms() {
        Map<String, TriageSymptomVO> map = new LinkedHashMap<>();
        enabledRules().stream()
                // 急症不让患者当普通症状点：它是命中后的提示，不是入口
                .filter(r -> !Integer.valueOf(1).equals(r.getUrgentFlag()))
                .sorted(Comparator.comparing(r -> NumUtil.orZero(r.getSortOrder())))
                .forEach(r -> map.putIfAbsent(r.getSymptomCode(), toSymptomVO(r)));
        return new ArrayList<>(map.values());
    }

    // 私有

    private List<BizTriageRule> enabledRules() {
        return bizTriageRuleMapper.selectList(new LambdaQueryWrapper<BizTriageRule>()
                .eq(BizTriageRule::getStatus, 1)
                .orderByAsc(BizTriageRule::getSortOrder)
                .orderByAsc(BizTriageRule::getId));
    }

    /**
     * 关键词命中：keywords 顿号分隔，任一命中即算中；未配关键词时退化为症状名包含。
     */
    private boolean matches(BizTriageRule rule, String text) {
        if (!TextUtil.hasText(rule.getKeywords())) {
            return TextUtil.hasText(rule.getSymptomName()) && text.contains(rule.getSymptomName().trim());
        }
        for (String kw : rule.getKeywords().split("、")) {
            if (TextUtil.hasText(kw) && text.contains(kw.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 排序口径：急症优先 → 权重降序 → 排序号升序
     */
    private int cmp(BizTriageRule a, BizTriageRule b) {
        int c = Integer.compare(NumUtil.orZero(b.getUrgentFlag()), NumUtil.orZero(a.getUrgentFlag()));
        if (c != 0) {
            return c;
        }
        c = Integer.compare(NumUtil.orZero(b.getWeight()), NumUtil.orZero(a.getWeight()));
        if (c != 0) {
            return c;
        }
        return Integer.compare(NumUtil.orZero(a.getSortOrder()), NumUtil.orZero(b.getSortOrder()));
    }

    private TriageDeptVO toDeptVO(BizTriageRule r) {
        TriageDeptVO vo = new TriageDeptVO();
        vo.setDeptId(String.valueOf(r.getDeptId()));
        vo.setDeptName(r.getDeptName());
        vo.setSymptomName(r.getSymptomName());
        vo.setUrgent(NumUtil.orZero(r.getUrgentFlag()));
        vo.setAdvice(r.getAdvice());
        vo.setWeight(NumUtil.orZero(r.getWeight()));
        vo.setBookableCount(countBookable(r.getDeptId()));
        return vo;
    }

    /**
     * 该科室可约号源数（可约 = 开了预约池且池内还有余号，口径与挂号页一致）。
     *
     * <p>查不到时返回 -1（前端不显示）而不是 0：把「没查到」说成「没号」会让患者以为这家医院看不了，
     * 与 {@code BizLabResultVO.abnormalFlagText} 里「未判定不能显示成正常」是同一个道理。
     */
    private int countBookable(Long deptId) {
        try {
            ScheduleSelectQueryDTO query = new ScheduleSelectQueryDTO();
            query.setDeptId(deptId);
            return (int) miniappDirectoryService.schedules(query).stream()
                    .filter(s -> Integer.valueOf(1).equals(s.getIsAppointment()))
                    .filter(s -> NumUtil.orZero(s.getAppointmentSource()) - NumUtil.orZero(s.getUsedAppointmentSource()) > 0)
                    .count();
        } catch (Exception e) {
            log.warn("[智能导诊] 号源余量查询失败 deptId={} err={}", deptId, e.getMessage());
            return -1;
        }
    }

    private TriageSymptomVO toSymptomVO(BizTriageRule r) {
        TriageSymptomVO vo = new TriageSymptomVO();
        vo.setSymptomCode(r.getSymptomCode());
        vo.setSymptomName(r.getSymptomName());
        return vo;
    }

}
