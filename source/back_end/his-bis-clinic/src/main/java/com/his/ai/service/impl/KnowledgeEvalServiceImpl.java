package com.his.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.ai.config.AiProperties;
import com.his.ai.dto.KnowledgeEvalCaseQueryPageDTO;
import com.his.ai.dto.KnowledgeEvalCaseUpsertDTO;
import com.his.ai.entity.SysKnowledgeEvalCase;
import com.his.ai.entity.SysKnowledgeEvalRun;
import com.his.ai.entity.SysKnowledgeEvalRunDetail;
import com.his.ai.mapper.SysKnowledgeEvalCaseMapper;
import com.his.ai.mapper.SysKnowledgeEvalRunDetailMapper;
import com.his.ai.mapper.SysKnowledgeEvalRunMapper;
import com.his.ai.rag.embedding.EmbeddingProviderSelector;
import com.his.ai.rag.store.RetrievedChunk;
import com.his.ai.rag.store.VectorStore;
import com.his.ai.service.KnowledgeEvalService;
import com.his.ai.service.KnowledgeStoreService;
import com.his.ai.vo.KnowledgeEvalCaseVO;
import com.his.ai.vo.KnowledgeEvalRunDetailVO;
import com.his.ai.vo.KnowledgeEvalRunVO;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 知识库离线评测服务实现。
 * <p>评测集规模小（几十条），运行评测同步执行；汇总/明细只追加不改。
 * recall@k 口径：期望命中文档（至少一个）出现在 top-k 检索结果里的用例占比；
 * MRR 口径：期望文档最高命中位次的倒数均值（未命中记 0）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeEvalServiceImpl implements KnowledgeEvalService {

    /** 运行状态（同步执行，成功即完成） */
    private static final int STATUS_DONE = 2;
    private static final int STATUS_FAILED = 3;
    /** 明细里实际命中文档标题最多记录 5 个 */
    private static final int HIT_TITLES_MAX = 5;

    private final SysKnowledgeEvalCaseMapper evalCaseMapper;
    private final SysKnowledgeEvalRunMapper evalRunMapper;
    private final SysKnowledgeEvalRunDetailMapper evalRunDetailMapper;
    private final EmbeddingProviderSelector embeddingSelector;
    private final VectorStore vectorStore;
    private final AiProperties aiProperties;
    private final KnowledgeStoreService knowledgeStoreService;

    @Override
    @Transactional
    public Long upsertCase(KnowledgeEvalCaseUpsertDTO dto) {
        SysKnowledgeEvalCase entity = new SysKnowledgeEvalCase();
        entity.setQuestion(dto.getQuestion().trim());
        entity.setExpectedDocIds(dto.getExpectedDocIds().trim());
        entity.setExpectedDocTitles(dto.getExpectedDocTitles());
        entity.setRemark(dto.getRemark());
        if (dto.getId() == null) {
            entity.setDelFlag(0);
            evalCaseMapper.insert(entity);
            return entity.getId();
        }
        evalCaseMapper.updateById(entity);
        return dto.getId();
    }

    @Override
    public void deleteCase(Long id) {
        SysKnowledgeEvalCase exists = evalCaseMapper.selectById(id);
        if (exists == null) {
            throw new BusinessException("评测用例不存在");
        }
        SysKnowledgeEvalCase upd = new SysKnowledgeEvalCase();
        upd.setId(id);
        upd.setDelFlag(1);
        evalCaseMapper.updateById(upd);
    }

    @Override
    public IPage<KnowledgeEvalCaseVO> listCasePage(KnowledgeEvalCaseQueryPageDTO dto) {
        QueryWrapper<SysKnowledgeEvalCase> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0).orderByDesc("create_time");
        if (TextUtil.hasText(dto.getQuestion())) {
            qw.like("question", dto.getQuestion().trim());
        }
        IPage<SysKnowledgeEvalCase> page = evalCaseMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), qw);
        return page.convert(this::toCaseVo);
    }

    @Override
    @Transactional
    public KnowledgeEvalRunVO runEval() {
        List<SysKnowledgeEvalCase> cases = evalCaseMapper.selectList(
                new QueryWrapper<SysKnowledgeEvalCase>().eq("del_flag", 0).orderByAsc("id"));
        if (cases.isEmpty()) {
            throw new BusinessException("评测集为空，请先添加评测用例");
        }
        // 懒加载：索引未构建时先全量载入，保证评测可检索
        knowledgeStoreService.ensureIndexLoaded();

        SysKnowledgeEvalRun run = new SysKnowledgeEvalRun();
        run.setStatus(STATUS_DONE);
        run.setCaseCount(cases.size());
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        run.setOperatorId(user.getEmployeeId());
        run.setOperatorName(user.getRealName());
        int ragChunkSize = aiProperties.getRag().getChunkSize();
        int ragOverlap = aiProperties.getRag().getChunkOverlap();
        run.setConfigSnapshot("chunkSize=" + ragChunkSize + ",overlap=" + ragOverlap
                + ",embedding=" + embeddingSelector.select().name());
        evalRunMapper.insert(run);

        int hitCaseCount = 0;
        double mrrSum = 0;
        long latencySum = 0;
        List<SysKnowledgeEvalRunDetail> details = new ArrayList<>();
        try {
            for (SysKnowledgeEvalCase c : cases) {
                long t0 = System.currentTimeMillis();
                float[] queryVector = embeddingSelector.select().embed(c.getQuestion());
                List<RetrievedChunk> hits = vectorStore.search(queryVector, aiProperties.getRag().getTopK());
                latencySum += System.currentTimeMillis() - t0;

                Set<Long> expected = parseDocIds(c.getExpectedDocIds());
                int hitRank = 0;
                for (int rank = 1; rank <= hits.size(); rank++) {
                    if (expected.contains(hits.get(rank - 1).docId())) {
                        hitRank = rank;
                        break;
                    }
                }
                boolean hit = hitRank > 0;
                if (hit) {
                    hitCaseCount++;
                    mrrSum += 1.0 / hitRank;
                }

                SysKnowledgeEvalRunDetail detail = new SysKnowledgeEvalRunDetail();
                detail.setRunId(run.getId());
                detail.setCaseId(c.getId());
                detail.setQuestion(c.getQuestion());
                detail.setExpectedDocIds(c.getExpectedDocIds());
                detail.setExpectedDocTitles(c.getExpectedDocTitles());
                detail.setHitDocTitles(hits.stream()
                        .map(RetrievedChunk::docTitle)
                        .filter(TextUtil::hasText)
                        .limit(HIT_TITLES_MAX)
                        .collect(Collectors.joining(";")));
                detail.setHitCount(hits.size());
                detail.setHitRank(hitRank);
                detail.setHitFlag(hit ? 1 : 0);
                details.add(detail);
            }
            details.forEach(evalRunDetailMapper::insert);

            run.setHitCaseCount(hitCaseCount);
            run.setRecallAtK(BigDecimal.valueOf(hitCaseCount * 100.0 / cases.size())
                    .setScale(2, RoundingMode.HALF_UP));
            run.setMrr(BigDecimal.valueOf(mrrSum / cases.size())
                    .setScale(2, RoundingMode.HALF_UP));
            run.setAvgLatencyMs((int) (latencySum / cases.size()));
            evalRunMapper.updateById(run);
        } catch (Exception ex) {
            // 失败不重抛：保留 FAILED 运行记录供观测（明细可能半截，以 status=3 为准）
            run.setStatus(STATUS_FAILED);
            run.setErrorMsg(TextUtil.ellipsis(String.valueOf(ex.getMessage()), 500));
            evalRunMapper.updateById(run);
            log.error("[RAG] 离线评测运行失败：{}", ex.getMessage());
            return toRunVo(run);
        }
        return toRunVo(run);
    }

    @Override
    public IPage<KnowledgeEvalRunVO> listRunPage(KnowledgeEvalCaseQueryPageDTO dto) {
        QueryWrapper<SysKnowledgeEvalRun> qw = new QueryWrapper<>();
        qw.orderByDesc("create_time");
        IPage<SysKnowledgeEvalRun> page = evalRunMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), qw);
        return page.convert(this::toRunVo);
    }

    @Override
    public List<KnowledgeEvalRunDetailVO> listRunDetail(Long runId) {
        return evalRunDetailMapper.selectList(
                        new QueryWrapper<SysKnowledgeEvalRunDetail>()
                                .eq("run_id", runId).orderByAsc("id"))
                .stream().map(d -> {
                    KnowledgeEvalRunDetailVO vo = new KnowledgeEvalRunDetailVO();
                    BeanUtils.copyProperties(d, vo);
                    return vo;
                }).toList();
    }

    private Set<Long> parseDocIds(String s) {
        if (!TextUtil.hasText(s)) {
            return Set.of();
        }
        Set<Long> ids = new HashSet<>();
        for (String p : s.split(";")) {
            String v = p.trim();
            if (v.isEmpty()) {
                continue;
            }
            try {
                ids.add(Long.parseLong(v));
            } catch (NumberFormatException ignored) {
                // 忽略非法 ID，评测继续
            }
        }
        return ids;
    }

    private KnowledgeEvalCaseVO toCaseVo(SysKnowledgeEvalCase c) {
        KnowledgeEvalCaseVO vo = new KnowledgeEvalCaseVO();
        BeanUtils.copyProperties(c, vo);
        return vo;
    }

    private KnowledgeEvalRunVO toRunVo(SysKnowledgeEvalRun r) {
        KnowledgeEvalRunVO vo = new KnowledgeEvalRunVO();
        BeanUtils.copyProperties(r, vo);
        return vo;
    }
}
