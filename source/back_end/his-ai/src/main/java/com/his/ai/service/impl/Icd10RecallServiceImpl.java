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
 * ICD-10 候选编码<b>召回层</b>。
 * <p>
 * 这是「反幻觉」的第一道闸门：模型只能从本层给出的封闭集合里挑编码。
 * 集合之外一律不接受，从机制上消灭「编造编码」这类最危险的错误。
 * <p>
 * <b>召回策略（两档）</b>：
 * <ol>
 *   <li>字典规模 ≤ 召回上限（{@code ai.retrieve-topn}，默认 50）→ <b>全量下发</b>。</li>
 *   <li>字典规模 &gt; 上限 → 按「编码名/分类与病历文本的字面重合度」粗排取前 N。
 *       三级计分：全名命中 &gt; 二元组重合 &gt; 分类命中。</li>
 * </ol>
 *
 * <p><b>⚠ 2026-09-23 实测：上面写的「已知局限」已经变成事实，第 2 档正在生效。</b>
 * ICD-10 诊断编码字典已从演示期的 35 条扩到 <b>40477 条</b>（批量导入真实码表），
 * 于是「字典小 → 全量下发」这条兜底路径<b>永远走不到了</b>，召回集固定只有 50 条。</p>
 *
 * <p>后果不是「推荐不准」，而是<b>推荐方向性错误</b>。实测（本机 2026-09-23）：
 * 病历文本「咳嗽咳痰伴发热3天…双肺可闻及湿性啰音」，正确编码是
 * J18.9 肺炎，但 top-50 候选集被字面命中文本的症状类编码占满 —
 * 返回的是 R05.9 咳嗽 / R50.9 发热 / R04.2 咯血 / R05 咳嗽，
 * 连病历里明确否认的「咯血」都进来了，而<b>任何一个「肺炎」编码都没进候选集</b>。
 * 病因是根本性的：病历正文里写的是体征描述（湿啰音），而码表名是疾病名（肺炎），
 * 字面检索无法从前者推出后者 —— 模型再强也没用，它看不到正确答案的选项。</p>
 *
 * <p>修法只需要换第 2 档，不用动第 1 档和第 3 档重排：可选的路径是
 * ①向量召回（引入 embedding）②结构化症状/体征/药品 → 疾病候选的同义词表
 * ③先让模型做一次「病历文本 → 标准疾病名」归一化，再走字面检索。
 * 这就是为什么召回与重排要拆成两个类 —— <b>换召回不碰重排</b>。</p>
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
