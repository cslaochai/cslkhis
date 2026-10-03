package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.system.dto.Icd10PredictDTO;
import com.his.system.entity.SysIcd10;
import com.his.system.mapper.SysIcd10Mapper;
import com.his.system.service.Icd10Service;
import com.his.system.vo.Icd10PredictVO;
import com.his.system.vo.SysIcd10SelectListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class Icd10ServiceImpl implements Icd10Service {

    /**
     * 精确/前缀命中池上限。这一池是"高置信"的（比如输入「肺炎」时名称以肺炎开头的 31 条），
     * 必须先捞进来，否则宽池按任意顺序截断会把正确编码挤掉。
     */
    private static final int PREFIX_POOL_LIMIT = 300;

    /**
     * 包含命中池上限。用来兜住"输入的是名称片段、但不在开头"的情况
     * （比如输入「并发肺炎」）。上限存在的意义是避免输入单个高频字（如「病」）
     * 时把几万行全拉进内存。
     */
    private static final int CONTAINS_POOL_LIMIT = 600;

    /**
     * 码表缓存时长。ICD 是低频变更的主数据，5 分钟足够，
     * 也避免每次智能预测都全表扫一遍。
     */
    private static final long CACHE_TTL_MS = 300_000L;

    private final SysIcd10Mapper icd10Mapper;

    private volatile List<SysIcd10> cachedCodes;

    private final AtomicLong cachedAt = new AtomicLong(0L);

    // 检索

    @Override
    public List<SysIcd10> search(String keyword, int limit) {
        int size = limit <= 0 ? 20 : limit;
        if (!StringUtils.hasText(keyword)) {
            // 空关键字：按维护顺序给前若干条（与历史行为一致，不按字典序乱跳）
            List<SysIcd10> all = activeCodes();
            return all.size() > size ? new ArrayList<>(all.subList(0, size)) : all;
        }

        String kw = keyword.trim();
        Map<Long, SysIcd10> pool = new LinkedHashMap<>();

        // ① 高置信池：编码/名称精确或以关键词开头
        LambdaQueryWrapper<SysIcd10> prefixWrapper = new LambdaQueryWrapper<>();
        prefixWrapper.eq(SysIcd10::getStatus, 1)
                .and(w -> w.eq(SysIcd10::getIcdCode, kw)
                        .or().likeRight(SysIcd10::getIcdCode, kw)
                        .or().eq(SysIcd10::getIcdName, kw)
                        .or().likeRight(SysIcd10::getIcdName, kw))
                .last("LIMIT " + PREFIX_POOL_LIMIT);
        for (SysIcd10 code : safeSelect(prefixWrapper)) {
            pool.put(code.getId(), code);
        }

        // ② 宽池：包含命中（补「并发肺炎」这类不在开头的片段）
        LambdaQueryWrapper<SysIcd10> containsWrapper = new LambdaQueryWrapper<>();
        containsWrapper.eq(SysIcd10::getStatus, 1)
                .and(w -> w.like(SysIcd10::getIcdCode, kw)
                        .or().like(SysIcd10::getIcdName, kw))
                .last("LIMIT " + CONTAINS_POOL_LIMIT);
        for (SysIcd10 code : safeSelect(containsWrapper)) {
            pool.putIfAbsent(code.getId(), code);
        }

        List<SysIcd10> ranked = new ArrayList<>(pool.values());
        ranked.sort(relevanceComparator(kw));
        return ranked.size() > size ? new ArrayList<>(ranked.subList(0, size)) : ranked;
    }

    @Override
    public List<SysIcd10SelectListVO> selectOptions(String keyword, int limit) {
        return search(keyword, limit).stream().map(code -> {
            SysIcd10SelectListVO vo = new SysIcd10SelectListVO();
            BeanUtils.copyProperties(code, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 相关性排序：命中位置越"确定"越靠前，其次名称越短越靠前（越贴近检索词本身），
     * 最后按维护顺序 sort_order、编码字典序兜底保证稳定。
     */
    private Comparator<SysIcd10> relevanceComparator(String keyword) {
        String kw = keyword.toLowerCase();
        return Comparator
                .comparingInt((SysIcd10 code) -> relevanceRank(code, kw))
                .thenComparingInt(code -> nameLength(code))
                .thenComparingInt(code -> code.getSortOrder() == null ? Integer.MAX_VALUE : code.getSortOrder())
                .thenComparing(code -> code.getIcdCode() == null ? "" : code.getIcdCode());
    }

    /**
     * 命中档位，数字越小越靠前：
     * 0 名称全等 &gt; 1 编码全等 &gt; 2 名称前缀 &gt; 3 编码前缀 &gt; 4 名称包含 &gt; 5 编码包含
     */
    private int relevanceRank(SysIcd10 code, String kwLower) {
        String name = code.getIcdName() == null ? "" : code.getIcdName().toLowerCase();
        String icdCode = code.getIcdCode() == null ? "" : code.getIcdCode().toLowerCase();
        if (name.equals(kwLower)) {
            return 0;
        }
        if (icdCode.equals(kwLower)) {
            return 1;
        }
        if (name.startsWith(kwLower)) {
            return 2;
        }
        if (icdCode.startsWith(kwLower)) {
            return 3;
        }
        if (name.contains(kwLower)) {
            return 4;
        }
        return 5;
    }

    private int nameLength(SysIcd10 code) {
        return code.getIcdName() == null ? Integer.MAX_VALUE : code.getIcdName().length();
    }

    private List<SysIcd10> safeSelect(LambdaQueryWrapper<SysIcd10> wrapper) {
        try {
            List<SysIcd10> list = icd10Mapper.selectList(wrapper);
            return list == null ? List.of() : list;
        } catch (Exception ex) {
            log.error("[ICD] 检索 sys_icd10 失败，本次返回空结果", ex);
            return List.of();
        }
    }

    // 缓存

    @Override
    public List<SysIcd10> activeCodes() {
        long now = System.currentTimeMillis();
        List<SysIcd10> local = cachedCodes;
        if (local != null && now - cachedAt.get() <= CACHE_TTL_MS) {
            return local;
        }
        synchronized (this) {
            if (cachedCodes == null || now - cachedAt.get() > CACHE_TTL_MS) {
                cachedCodes = load();
                cachedAt.set(now);
            }
            return cachedCodes;
        }
    }

    @Override
    public void refreshCache() {
        cachedAt.set(0L);
    }

    private List<SysIcd10> load() {
        try {
            List<SysIcd10> list = icd10Mapper.selectList(new LambdaQueryWrapper<SysIcd10>()
                    .eq(SysIcd10::getStatus, 1)
                    .orderByAsc(SysIcd10::getSortOrder)
                    .orderByAsc(SysIcd10::getIcdCode));
            return list == null ? List.of() : list;
        } catch (Exception ex) {
            log.error("[ICD] 加载 sys_icd10 失败，缓存返回空集合", ex);
            return List.of();
        }
    }

    // 规则版推荐（原 Icd10Controller 逻辑原样下沉，行为不变）

    @Override
    public List<Icd10PredictVO> predict(Icd10PredictDTO predictDTO) {
        String chiefComplaint = predictDTO.getChiefComplaint() != null ? predictDTO.getChiefComplaint() : "";
        String presentIllness = predictDTO.getPresentIllness() != null ? predictDTO.getPresentIllness() : "";
        String specialistExam = predictDTO.getSpecialistExam() != null ? predictDTO.getSpecialistExam() : "";
        String diagnosis = predictDTO.getDiagnosis() != null ? predictDTO.getDiagnosis() : "";
        String allText = chiefComplaint + " " + presentIllness + " " + specialistExam + " " + diagnosis;

        List<Icd10PredictVO> predictions = new ArrayList<>();
        for (SysIcd10 icd : activeCodes()) {
            int score = calculateMatchScore(allText, icd);
            if (score > 0) {
                Icd10PredictVO item = new Icd10PredictVO();
                item.setIcdCode(icd.getIcdCode());
                item.setIcdName(icd.getIcdName());
                item.setIcdCategory(icd.getIcdCategory());
                item.setScore(score);
                item.setDrgWeight(BigDecimal.valueOf(estimateDrgWeight(icd.getIcdCode())));
                item.setEstimatedCost(BigDecimal.valueOf(estimateCost(icd.getIcdCode())));
                predictions.add(item);
            }
        }

        predictions.sort((a, b) -> b.getScore() - a.getScore());
        return predictions.size() > 10 ? new ArrayList<>(predictions.subList(0, 10)) : predictions;
    }

    /**
     * 计算ICD编码与文本的匹配分数
     */
    private int calculateMatchScore(String text, SysIcd10 icd) {
        if (!StringUtils.hasText(icd.getIcdName())) {
            return 0;
        }
        int score = 0;
        String icdName = icd.getIcdName().toLowerCase();
        String icdCategory = icd.getIcdCategory() != null ? icd.getIcdCategory().toLowerCase() : "";
        String textLower = text.toLowerCase();

        if (textLower.contains(icdName)) {
            score += 100;
        }

        String[] nameParts = icdName.split("[\\s、/]+");
        for (String part : nameParts) {
            if (part.length() >= 2 && textLower.contains(part)) {
                score += 30;
            }
        }

        if (StringUtils.hasText(icdCategory) && textLower.contains(icdCategory)) {
            score += 20;
        }

        for (String[] entry : symptomKeywords()) {
            String category = entry[0];
            for (int i = 1; i < entry.length; i++) {
                String keyword = entry[i];
                if (textLower.contains(keyword) && icdCategory.contains(category)) {
                    score += 15;
                    break;
                }
            }
        }

        return score;
    }

    /**
     * 症状关键词到ICD分类的映射
     */
    private List<String[]> symptomKeywords() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"呼吸", "咳嗽", "咳痰", "气喘", "胸闷", "呼吸困难", "鼻塞", "咽痛"});
        list.add(new String[]{"消化", "腹痛", "腹泻", "恶心", "呕吐", "胃痛", "反酸", "便血", "便秘"});
        list.add(new String[]{"心血管", "胸痛", "心悸", "心慌", "高血压", "头晕", "水肿"});
        list.add(new String[]{"神经", "头痛", "偏瘫", "麻木", "抽搐", "意识障碍", "失眠"});
        list.add(new String[]{"骨", "骨折", "关节痛", "腰痛", "颈椎", "扭伤", "骨质疏松"});
        list.add(new String[]{"泌尿", "尿频", "尿急", "尿痛", "血尿", "肾结石"});
        list.add(new String[]{"皮肤", "皮疹", "瘙痒", "红肿", "过敏", "荨麻疹"});
        list.add(new String[]{"眼", "视力", "眼睛", "眼痛", "白内障", "近视"});
        list.add(new String[]{"耳鼻喉", "耳鸣", "听力", "鼻炎", "扁桃体"});
        list.add(new String[]{"内分泌", "糖尿病", "血糖", "甲状腺", "甲亢"});
        list.add(new String[]{"传染", "发热", "发烧", "感染", "炎症", "病毒"});
        return list;
    }

    /**
     * 根据ICD编码估算DRG权重（简化版）
     */
    private double estimateDrgWeight(String icdCode) {
        if (icdCode == null) {
            return 1.0;
        }
        String major = icdCode.length() >= 3 ? icdCode.substring(0, 3) : icdCode;
        return switch (major) {
            case "I21", "I63", "J18", "K80", "K85" -> 2.5;
            case "E11", "E10" -> 1.8;
            case "I10", "I11" -> 1.2;
            case "J20", "J06" -> 0.8;
            case "K29", "K35" -> 1.5;
            case "M54", "M17" -> 1.3;
            case "N20", "N39" -> 1.1;
            default -> 1.0;
        };
    }

    /**
     * 根据ICD编码估算费用（简化版，单位：元）
     */
    private double estimateCost(String icdCode) {
        double weight = estimateDrgWeight(icdCode);
        return Math.round(weight * 5000 * 100.0) / 100.0;
    }
}
