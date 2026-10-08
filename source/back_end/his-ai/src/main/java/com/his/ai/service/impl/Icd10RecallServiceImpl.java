package com.his.ai.service.impl;

import com.his.ai.config.AiConfigProvider;
import com.his.ai.dto.Icd10SelectListDTO;
import com.his.ai.service.Icd10RecallService;
import com.his.ai.vo.Icd10SelectListVO;
import com.his.common.util.TextUtil;
import com.his.system.entity.SysIcd10;
import com.his.system.service.Icd10Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ICD-10 候选编码召回层。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Icd10RecallServiceImpl implements Icd10RecallService {

    private static final int MIN_TERM_LENGTH = 2;

    /**
     * 下拉候选未传条数时的默认条数
     */
    private static final int DEFAULT_SELECT_LIMIT = 20;

    private static final int SCORE_FULL_NAME_HIT = 100;

    private static final int SCORE_CATEGORY_HIT = 10;

    /**
     * 全名命中的权重（按名称长度加成，名称越长命中越可信：「急性上呼吸道感染」比「发热」可信）
     */
    private static final int FULL_NAME_LENGTH_WEIGHT = 2;

    /**
     * 二元组重合度的满分权重
     */
    private static final double BIGRAM_WEIGHT = 60D;

    /**
     * 归一化时要剔除的噪声字符：空白、标点、括号、占位符
     */
    private static final Pattern NOISE = Pattern.compile(
            "[\\s　,，。、;；:：!！?？\"'“”‘’()（）\\[\\]【】{}<>《》\\-—_/\\\\|*#~`+=$%^&]+");

    private final Icd10Service icd10Service;

    private final AiConfigProvider aiConfigProvider;

    private static Icd10SelectListVO toOption(SysIcd10 entity) {
        Icd10SelectListVO vo = new Icd10SelectListVO();
        vo.setIcdCode(entity.getIcdCode());
        vo.setIcdName(entity.getIcdName());
        vo.setIcdCategory(entity.getIcdCategory());
        return vo;
    }

    /**
     * 二元组重合度：词条切成的所有相邻两字组中，出现在文本里的比例。
     * 用「比例」而非「个数」，避免长词条靠长度刷分。
     */
    private static double bigramOverlap(String term, String note) {
        if (term.length() < MIN_TERM_LENGTH) {
            return 0D;
        }
        Set<String> grams = new LinkedHashSet<>();
        for (int i = 0; i + MIN_TERM_LENGTH <= term.length(); i++) {
            grams.add(term.substring(i, i + MIN_TERM_LENGTH));
        }
        if (grams.isEmpty()) {
            return 0D;
        }
        int hit = 0;
        for (String gram : grams) {
            if (note.contains(gram)) {
                hit++;
            }
        }
        return (double) hit / grams.size();
    }

    private static String normalize(String text) {
        if (!TextUtil.hasText(text)) {
            return "";
        }
        return NOISE.matcher(text).replaceAll("").toLowerCase();
    }

    /**
     * 按病历文本召回候选编码。返回结果已按得分降序排列。
     *
     * @param noteText     病历文本（已拼接）
     * @param topNOverride 召回上限覆盖，null 时取配置值
     */
    public List<RecallHit> recall(String noteText, Integer topNOverride) {
        List<SysIcd10> all = activeCodes();
        if (all.isEmpty()) {
            log.warn("[AI-ICD] sys_icd10 无启用编码，推荐将返回空结果");
            return List.of();
        }

        String note = normalize(noteText);
        List<RecallHit> scored = new ArrayList<>(all.size());
        for (SysIcd10 code : all) {
            scored.add(new RecallHit(code, scoreCode(code, note)));
        }
        scored.sort(Comparator
                .comparingInt(RecallHit::score).reversed()
                .thenComparing(hit -> hit.code().getSortOrder() == null ? Integer.MAX_VALUE : hit.code().getSortOrder())
                .thenComparing(hit -> hit.code().getIcdCode()));

        int topN = resolveTopN(topNOverride);
        if (scored.size() <= topN) {
            // 字典足够小：全量下发，让模型自己判断，不因字面召回漏掉正确编码
            return scored;
        }

        List<RecallHit> hitList = new ArrayList<>(topN);
        for (RecallHit hit : scored) {
            if (hitList.size() >= topN) {
                break;
            }
            if (hit.score() > 0) {
                hitList.add(hit);
            }
        }
        if (hitList.isEmpty()) {
            // 全文与任何编码都没有字面关联（医生叙述太口语化）。
            // 此时不能下发空集合 —— 宁可退化成「按维护顺序取前 N 条」，
            // 让模型在一个偏窄但非空的集合里判断，也好过直接无结果。
            log.info("[AI-ICD] 病历文本与码表无字面关联，退化为按维护顺序下发前 {} 条", topN);
            return scored.subList(0, topN);
        }
        return hitList;
    }

    /**
     * 取全部启用编码。
     *
     * <p>实现委托给 his-system 的 {@link Icd10Service}：码表读取与缓存全系统只此一份，
     * 避免本类与 {@code /system/icd10/selectList} 各查一遍库、各给一套顺序。</p>
     */
    public List<SysIcd10> activeCodes() {
        return icd10Service.activeCodes();
    }

    /**
     * 让码表缓存立即失效（码表变更后调用）
     */
    public void refresh() {
        icd10Service.refreshCache();
    }

    /**
     * 按关键词检索编码，供下拉框使用（不产生模型调用）。
     *
     * <p>委托 {@link Icd10Service#search}，与 {@code /system/icd10/selectList} 同一套相关性排序。
     * 旧实现是「遍历全表、命中即收、收满 limit 就停」，在 4 万条码表下有两个问题：
     * ① 结果顺序由 sort_order 决定而非相关性，输入「肺炎」可能前 20 条全是无关的
     * 「XX并发肺炎」；② 首次缓存未命中要全表加载 + 线性扫描，实测首击 1.8s。</p>
     */
    public List<SysIcd10> search(String keyword, int limit) {
        return icd10Service.search(keyword, limit);
    }

    /**
     * 下拉候选出参：入参缺省条数在此兜底，映射也在这里一处完成。
     */
    public List<Icd10SelectListVO> selectOptions(Icd10SelectListDTO selectListDTO) {
        int limit = selectListDTO.getLimit() == null || selectListDTO.getLimit() <= 0
                ? DEFAULT_SELECT_LIMIT : selectListDTO.getLimit();
        return search(selectListDTO.getKeyword(), limit).stream()
                .map(Icd10RecallServiceImpl::toOption)
                .collect(Collectors.toList());
    }

    private int resolveTopN(Integer override) {
        if (override != null && override > 0) {
            return override;
        }
        int configured = aiConfigProvider.get().getRetrieveTopN();
        return configured > 0 ? configured : 50;
    }

    /**
     * 单个编码与病历文本的字面关联度
     */
    private int scoreCode(SysIcd10 code, String note) {
        if (!TextUtil.hasText(note)) {
            return 0;
        }
        int score = 0;
        String name = normalize(code.getIcdName());
        if (name.length() >= MIN_TERM_LENGTH) {
            if (note.contains(name)) {
                score += SCORE_FULL_NAME_HIT + name.length() * FULL_NAME_LENGTH_WEIGHT;
            } else {
                score += (int) Math.round(bigramOverlap(name, note) * BIGRAM_WEIGHT);
            }
        }
        String category = normalize(code.getIcdCategory());
        if (category.length() >= MIN_TERM_LENGTH && note.contains(category)) {
            score += SCORE_CATEGORY_HIT;
        }
        return score;
    }
}
