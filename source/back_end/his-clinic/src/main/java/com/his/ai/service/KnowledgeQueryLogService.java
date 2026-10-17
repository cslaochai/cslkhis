package com.his.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.ai.dto.KnowledgeQueryLogQueryPageDTO;
import com.his.ai.entity.SysKnowledgeQueryLog;
import com.his.ai.vo.KnowledgeQueryLogVO;

/**
 * 知识库问答观测日志服务（在线观测：留痕 + 反馈，为后续离线评测提供真实数据）。
 */
public interface KnowledgeQueryLogService {

    /**
     * 记录一次问答（在 web 线程调用，内部捕获提问人）。
     */
    void record(String question, String answer, String hitDocs, int hitCount,
                boolean degraded, String degradeReason, long latencyMs);

    /**
     * 分页查询（按提问时间倒序）。
     */
    IPage<KnowledgeQueryLogVO> listPage(KnowledgeQueryLogQueryPageDTO dto);

    /**
     * 提交/覆盖反馈（1-有用 2-无用）。
     */
    void feedback(Long id, Integer feedback);
}
