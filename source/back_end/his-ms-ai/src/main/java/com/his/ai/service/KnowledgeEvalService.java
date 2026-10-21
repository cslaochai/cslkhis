package com.his.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.ai.dto.KnowledgeEvalCasePageQueryDTO;
import com.his.ai.dto.KnowledgeEvalCaseUpsertDTO;
import com.his.ai.vo.KnowledgeEvalCaseVO;
import com.his.ai.vo.KnowledgeEvalRunDetailVO;
import com.his.ai.vo.KnowledgeEvalRunVO;

import java.util.List;

/**
 * 知识库离线评测服务（第 2 层可观测）：
 * 评测集（问题 + 期望命中文档）→ 批量检索 → recall@k / MRR，运行留痕供回归对比。
 */
public interface KnowledgeEvalService {

    /**
     * 新增/修改评测用例（id 为空新增，非空修改）。
     */
    Long upsertCase(KnowledgeEvalCaseUpsertDTO dto);

    /**
     * 删除评测用例（软删）。
     */
    void deleteCase(Long id);

    /**
     * 评测用例分页。
     */
    IPage<KnowledgeEvalCaseVO> listCasePage(KnowledgeEvalCasePageQueryDTO dto);

    /**
     * 运行评测：对全部未删用例逐条检索，计算 recall@k / MRR，落运行汇总 + 明细。
     */
    KnowledgeEvalRunVO runEval();

    /**
     * 运行历史分页（按运行时间倒序）。
     */
    IPage<KnowledgeEvalRunVO> listRunPage(KnowledgeEvalCasePageQueryDTO dto);

    /**
     * 某次运行的明细列表。
     */
    List<KnowledgeEvalRunDetailVO> listRunDetail(Long runId);
}
