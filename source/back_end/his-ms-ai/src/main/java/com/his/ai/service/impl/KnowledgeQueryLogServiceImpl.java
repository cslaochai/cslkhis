package com.his.ai.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.ai.dto.KnowledgeQueryLogPageQueryDTO;
import com.his.ai.entity.SysKnowledgeQueryLog;
import com.his.ai.mapper.SysKnowledgeQueryLogMapper;
import com.his.ai.service.KnowledgeQueryLogService;
import com.his.ai.vo.KnowledgeQueryLogVO;
import com.his.common.entity.CurrentUser;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 知识库问答观测日志服务实现。
 * <p>日志只追加不改（feedback 列例外，允许顶/踩覆盖）；
 * 提问人从 SecurityContext 捕获，写库失败不打断问答主流程（降级为静默丢日志）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeQueryLogServiceImpl extends ServiceImpl<SysKnowledgeQueryLogMapper, SysKnowledgeQueryLog> implements KnowledgeQueryLogService {

    /**
     * 命中文档标题列表最多记录 5 个
     */
    private static final int HIT_DOCS_MAX = 5;

    private final SysKnowledgeQueryLogMapper queryLogMapper;

    @Override
    public void record(String question, String answer, String hitDocs, int hitCount,
                       boolean degraded, String degradeReason, long latencyMs) {
        try {
            SysKnowledgeQueryLog log = new SysKnowledgeQueryLog();
            log.setQuestion(TextUtil.ellipsis(question, 500));
            log.setAnswer(answer);
            log.setHitDocs(hitDocs);
            log.setHitCount(hitCount);
            log.setDegraded(degraded ? 1 : 0);
            log.setDegradeReason(degradeReason);
            log.setLatencyMs((int) latencyMs);
            log.setFeedback(0);
            CurrentUser user = UserUtils.getCurrentUser();
            if (user != null) {
                log.setOperatorId(user.getEmployeeId());
                log.setOperatorName(user.getRealName());
            }
            queryLogMapper.insert(log);
        } catch (Exception ex) {
            // 观测日志写失败不影响问答主链路
            log.warn("[RAG] 问答观测日志写入失败：{}", ex.getMessage());
        }
    }

    @Override
    public IPage<KnowledgeQueryLogVO> listPage(KnowledgeQueryLogPageQueryDTO dto) {
        QueryWrapper<SysKnowledgeQueryLog> qw = new QueryWrapper<>();
        qw.orderByDesc("create_time");
        if (TextUtil.hasText(dto.getQuestion())) {
            qw.like("question", dto.getQuestion().trim());
        }
        if (dto.getDegraded() != null && (dto.getDegraded() == 0 || dto.getDegraded() == 1)) {
            qw.eq("degraded", dto.getDegraded());
        }
        if (dto.getFeedback() != null) {
            qw.eq("feedback", dto.getFeedback());
        }
        IPage<SysKnowledgeQueryLog> page = queryLogMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), qw);
        return page.convert(this::toVo);
    }

    @Override
    public void feedback(Long id, Integer feedback) {
        SysKnowledgeQueryLog exists = queryLogMapper.selectById(id);
        if (exists == null) {
            throw new BusinessException("观测日志不存在");
        }
        SysKnowledgeQueryLog upd = new SysKnowledgeQueryLog();
        upd.setId(id);
        upd.setFeedback(feedback);
        queryLogMapper.updateById(upd);
    }

    private KnowledgeQueryLogVO toVo(SysKnowledgeQueryLog log) {
        KnowledgeQueryLogVO vo = new KnowledgeQueryLogVO();
        BeanUtils.copyProperties(log, vo);
        return vo;
    }
}
