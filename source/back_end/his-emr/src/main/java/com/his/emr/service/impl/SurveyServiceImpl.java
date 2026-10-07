package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.BizCodeConstants;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.*;
import com.his.common.util.TextUtil;
import com.his.emr.dto.*;
import com.his.emr.entity.*;
import com.his.emr.enums.*;
import com.his.emr.mapper.*;
import com.his.emr.service.DisputeService;
import com.his.emr.service.SurveyService;
import com.his.emr.service.SurveyTemplateService;
import com.his.emr.support.FollowupTaskSnapshot;
import com.his.emr.vo.*;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.DeptScopeProvider;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * 满意度评价发放/回收与看板实现。
 *
 * <p>口径：
 * <ol>
 *   <li>发放先行：回收率的分母是满意度发放台账的行数，未回收也必须成行
 *       （过期靠时间现算，不靠定时任务翻状态）。</li>
 *   <li>答卷只认一次发放：{@code uk_survey_answer_dispatch(dispatch_id)}，重填覆盖明细
 *       （物理删旧答案再写），不新建第二张卷 —— 否则同一个人的意见被算两遍。</li>
 *   <li>题目一律快照进满意度逐题答案：模板改题干/换维度不能改写历史。</li>
 *   <li>低分自动转投诉：百分制 &lt; 60 或某维度均分 &le; 2 → 写医疗纠纷投诉主单，
 *       {@code dispute_case_id} 是幂等锚，也是「这张卷不许再改分」的锁。</li>
 *   <li>手机号：库内存明文供外呼拨号，列表出参只给 {@code phoneMasked}，
 *       明文只在 {@code dispatchGetById}（编辑回显）返回。</li>
 *   <li>科室范围：所有列表与看板由登录岗位收口，越权科室直接报错，不静默改写。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyServiceImpl extends ServiceImpl<BizSurveyAnswerMapper, BizSurveyAnswer> implements SurveyService {
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private final DeptScopeProvider deptScopeProvider;
    private final BizSurveyDispatchMapper bizSurveyDispatchMapper;
    private final BizSurveyAnswerMapper bizSurveyAnswerMapper;
    private final BizSurveyAnswerItemMapper bizSurveyAnswerItemMapper;
    private final BizSurveyItemMapper bizSurveyItemMapper;
    private final BizFollowupTaskMapper bizFollowupTaskMapper;
    private final SurveyTemplateService surveyTemplateService;
    private final DisputeService disputeService;
    private final RedisSequenceService redisSequenceService;

    // 发放与回收

    private static BigDecimal rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator).multiply(HUNDRED)
                .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP);
    }

    private static int maxScoreOf(BizSurveyItem item) {
        return item.getMaxScore() == null ? 5 : item.getMaxScore();
    }

    // 答卷

    @Override
    public PageResult<SurveyDispatchVO> dispatchListPage(SurveyDispatchQueryPageDTO dto) {
        Page<SurveyDispatchVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        List<SurveyDispatchVO> records = bizSurveyDispatchMapper.selectDispatchPage(page,
                TextUtil.trimToNull(dto.getKeyword()), dto.getPatientId(), dto.getSourceType(),
                dto.getDispatchStatus(), dto.getChannel(), dto.getOverdueOnly(),
                TextUtil.trimToNull(dto.getDateFrom()), TextUtil.trimToNull(dto.getDateTo()),
                scopedDeptIds(dto.getDeptId()));
        // 列表一律脱敏并清空明文：抓包拿到全量手机号等于没做脱敏
        records.forEach(vo -> decorateDispatch(vo, false));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    // 看板

    @Override
    public SurveyDispatchVO dispatchGetById(Long id) {
        BizSurveyDispatch entity = requireDispatch(id);
        assertDeptAccessible(entity.getDeptId());
        SurveyDispatchVO vo = new SurveyDispatchVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setOverdue(isOverdue(entity));
        decorateDispatch(vo, true);
        return vo;
    }

    // 内部：校验、算分、转投诉

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SurveyDispatchVO issueFromFollowup(SurveyDispatchIssueDTO dto) {
        BizFollowupTask task = bizFollowupTaskMapper.selectById(dto.getFollowupTaskId());
        if (task == null) {
            throw new BusinessException("随访任务不存在或已删除");
        }
        if (Objects.equals(task.getFollowupStatus(), FollowupTaskStatusEnum.CANCELLED.getCode())) {
            throw new BusinessException("已取消的随访任务不再发放评价（人都没联系上，问谁去）");
        }
        assertDeptAccessible(task.getDeptId());
        SurveyDispatchVO issued = issueForFollowup(FollowupTaskSnapshot.of(task),
                SurveySourceEnum.MANUAL.getCode(), dto.getChannel(), dto.getExpireDays());
        if (issued == null) {
            // 自动发放时「没配卷」只是留痕，人工补发时它是必须说清楚的失败原因
            throw new BusinessException("出院随访场景还没有启用中的问卷，请先在「问卷模板」里配置并启用");
        }
        return issued;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SurveyDispatchVO issueForFollowup(FollowupTaskSnapshot task, Integer sourceType,
                                             Integer channel, Integer expireDays) {
        if (task == null || task.getTaskId() == null) {
            throw new BusinessException("随访任务不存在，无法发放评价");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        int src = sourceType == null ? SurveySourceEnum.FOLLOWUP.getCode() : sourceType;
        SurveyTemplateVO template = surveyTemplateService.findEnabledForScene(SurveySceneEnum.DISCHARGE_FOLLOWUP.getCode());
        // 没配卷就不发：评价域是随访的旁路，绝不能反过来把随访卡死
        if (template == null || template.getItems() == null || template.getItems().isEmpty()) {
            log.warn("[满意度] 随访任务 {} 未发放评价：场景 {} 没有启用中的问卷", task.getTaskId(),
                    SurveySceneEnum.DISCHARGE_FOLLOWUP.getCode());
            return null;
        }
        BizSurveyDispatch existed = bizSurveyDispatchMapper.selectBySource(src, task.getTaskId(), template.getId());
        if (existed != null) {
            return dispatchGetById(existed.getId());
        }
        int ch = channel == null ? SurveyChannelEnum.PHONE.getCode() : channel;
        int days = expireDays == null || expireDays <= 0 ? BizSurveyDispatch.DEFAULT_EXPIRE_DAYS : expireDays;

        BizSurveyDispatch entity = new BizSurveyDispatch();
        entity.setDispatchNo(nextNo(BizCodeConstants.SURVEY_DISPATCH_NO_PREFIX, BizCodeConstants.SURVEY_DISPATCH_NO_KEY_PREFIX));
        entity.setTemplateId(template.getId());
        entity.setTemplateName(TextUtil.cut(template.getTemplateName(), 128));
        entity.setScene(template.getScene());
        entity.setSourceType(src);
        entity.setSourceId(task.getTaskId());
        entity.setPatientId(task.getPatientId());
        entity.setPatientNo(TextUtil.cut(task.getPatientNo(), 64));
        entity.setPatientName(TextUtil.cut(task.getPatientName(), 128));
        entity.setPhone(TextUtil.cut(task.getPhone(), 20));
        entity.setDeptId(task.getDeptId());
        entity.setDeptName(TextUtil.cut(task.getDeptName(), 128));
        entity.setChannel(ch);
        // 短信/微信没有真实网关，只能落「待推送」等人工外呼 —— 状态诚实比好看重要
        entity.setDispatchStatus(SurveyDispatchStatusEnum.PENDING_PUSH.getCode());
        entity.setExpireTime(TimeUtil.nowSeconds().plusDays(days));
        entity.setCreateBy(operatorUser.getRealName());
        bizSurveyDispatchMapper.insert(entity);
        return dispatchGetById(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SurveyDispatchVO markDispatch(SurveyDispatchActionDTO dto) {
        BizSurveyDispatch entity = requireDispatch(dto.getId());
        assertDeptAccessible(entity.getDeptId());
        String remark = TextUtil.cut(dto.getRemark(), 512);
        if (Objects.equals(dto.getAction(), 1)) {
            if (!Objects.equals(entity.getDispatchStatus(), SurveyDispatchStatusEnum.PENDING_PUSH.getCode())) {
                throw new BusinessException("仅「待推送」的发放单可标记已推送（当前："
                        + SurveyDispatchStatusEnum.getText(entity.getDispatchStatus()) + "）");
            }
            entity.setDispatchStatus(SurveyDispatchStatusEnum.PUSHED.getCode());
            entity.setPushTime(TimeUtil.nowSeconds());
        } else if (Objects.equals(dto.getAction(), 2)) {
            if (Objects.equals(entity.getDispatchStatus(), SurveyDispatchStatusEnum.RECYCLED.getCode())) {
                throw new BusinessException("已回收的发放单不能标记拒答（要更正请先作废答卷）");
            }
            if (!Objects.equals(entity.getDispatchStatus(), SurveyDispatchStatusEnum.PENDING_PUSH.getCode())
                    && !Objects.equals(entity.getDispatchStatus(), SurveyDispatchStatusEnum.PUSHED.getCode())) {
                throw new BusinessException("仅未回收的发放单可标记拒答（当前："
                        + SurveyDispatchStatusEnum.getText(entity.getDispatchStatus()) + "）");
            }
            entity.setDispatchStatus(SurveyDispatchStatusEnum.REFUSED.getCode());
        } else {
            throw new BusinessException("动作不合法：1-标记已推送 2-标记已拒答（「已回收」只能由答卷写入）");
        }
        entity.setRemark(remark);
        bizSurveyDispatchMapper.updateById(entity);
        return dispatchGetById(entity.getId());
    }

    @Override
    public PageResult<SurveyAnswerVO> answerListPage(SurveyAnswerQueryPageDTO dto) {
        Page<SurveyAnswerVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        List<SurveyAnswerVO> records = bizSurveyAnswerMapper.selectAnswerPage(page,
                TextUtil.trimToNull(dto.getKeyword()), dto.getPatientId(), dto.getTemplateId(), dto.getScene(),
                dto.getAnswerStatus(), dto.getFillSource(), dto.getLowScoreOnly(),
                TextUtil.trimToNull(dto.getDateFrom()), TextUtil.trimToNull(dto.getDateTo()), scopedDeptIds(dto.getDeptId()));
        records.forEach(this::decorateAnswer);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public SurveyAnswerVO answerGetById(Long id) {
        SurveyAnswerVO vo = bizSurveyAnswerMapper.selectAnswerById(id);
        if (vo == null) {
            throw new BusinessException("答卷不存在或已删除");
        }
        assertDeptAccessible(vo.getDeptId());
        decorateAnswer(vo);
        vo.setItems(bizSurveyAnswerItemMapper.selectByAnswer(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SurveyAnswerVO submitAnswer(SurveyAnswerUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizSurveyDispatch dispatch = requireDispatch(dto.getDispatchId());
        assertDeptAccessible(dispatch.getDeptId());
        if (Objects.equals(dispatch.getDispatchStatus(), SurveyDispatchStatusEnum.EXPIRED.getCode())) {
            throw new BusinessException("该发放单已过期");
        }

        BizSurveyAnswer answer = dispatch.getAnswerId() == null ? null : bizSurveyAnswerMapper.selectById(dispatch.getAnswerId());
        if (answer != null && Objects.equals(answer.getAnswerStatus(), AnswerStatusEnum.VALID.getCode())
                && answer.getDisputeCaseId() != null) {
            throw new BusinessException("该答卷已转出投诉单，禁止重填（改分数等于改掉投诉的由来）");
        }

        Map<Long, BizSurveyItem> paper = paperOf(dispatch.getTemplateId());
        List<SurveyAnswerItemVO> submitted = validateAndBuild(dto.getItems(), paper);
        boolean isNew = answer == null;
        if (isNew) {
            answer = new BizSurveyAnswer();
            answer.setAnswerNo(nextNo(BizCodeConstants.SURVEY_ANSWER_NO_PREFIX, BizCodeConstants.SURVEY_ANSWER_NO_KEY_PREFIX));
            answer.setDispatchId(dispatch.getId());
            answer.setTemplateId(dispatch.getTemplateId());
            answer.setScene(dispatch.getScene());
            answer.setPatientId(dispatch.getPatientId());
            answer.setPatientNo(dispatch.getPatientNo());
            answer.setPatientName(dispatch.getPatientName());
            answer.setDeptId(dispatch.getDeptId());
            answer.setDeptName(dispatch.getDeptName());
            answer.setCreateBy(operatorUser.getRealName());
        }
        // 患者身份与科室一律取自发放单，不接收前端传值：否则改个 patientId 就能把意见挂到别人头上
        answer.setAnswerStatus(AnswerStatusEnum.VALID.getCode());
        answer.setFillSource(dto.getFillSource() == null ? FillSourceEnum.AGENT.getCode() : dto.getFillSource());
        answer.setAnonymousFlag(Objects.equals(dto.getAnonymousFlag(), 1) ? 1 : 0);
        answer.setFillEmployeeId(operatorUser.getEmployeeId());
        answer.setFillEmployeeName(TextUtil.cut(operatorUser.getRealName(), 64));
        answer.setFillTime(TimeUtil.nowSeconds());
        answer.setCommentText(TextUtil.cut(dto.getCommentText(), 1000));
        answer.setRemark(TextUtil.cut(dto.getRemark(), 512));
        scoreOf(submitted, paper, answer);

        if (isNew) {
            bizSurveyAnswerMapper.insert(answer);
        } else {
            bizSurveyAnswerMapper.updateById(answer);
        }
        // 重填覆盖明细：uk_survey_answer_item(answer_id,item_id) 不含 del_flag，软删必撞键
        bizSurveyAnswerItemMapper.purgeByAnswer(answer.getId());
        for (SurveyAnswerItemVO item : submitted) {
            bizSurveyAnswerItemMapper.insert(toAnswerItem(answer.getId(), dispatch.getTemplateId(), item));
        }

        dispatch.setDispatchStatus(SurveyDispatchStatusEnum.RECYCLED.getCode());
        dispatch.setAnswerId(answer.getId());
        // 电话代填说明已触达患者，补记推送时点，台账上不会出现「已回收却从没推送」
        if (dispatch.getPushTime() == null) {
            dispatch.setPushTime(answer.getFillTime());
        }
        bizSurveyDispatchMapper.updateById(dispatch);

        // 低分转投诉：同一事务内完成，转单失败就整体回滚 ——
        // 「打了低分但投诉台账里没有」是最难发现的漏，宁可让录入员看到报错重来
        if (answer.getDisputeCaseId() == null && isLowScore(answer, submitted)) {
            Long caseId = disputeService.caseUpsert(buildDispute(dispatch, answer, submitted)).getId();
            answer.setDisputeCaseId(caseId);
            bizSurveyAnswerMapper.updateById(answer);
        }
        return answerGetById(answer.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SurveyAnswerVO voidAnswer(SurveyAnswerVoidDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizSurveyAnswer answer = bizSurveyAnswerMapper.selectById(dto.getId());
        if (answer == null) {
            throw new BusinessException("答卷不存在或已删除");
        }
        assertDeptAccessible(answer.getDeptId());
        if (!Objects.equals(answer.getAnswerStatus(), AnswerStatusEnum.VALID.getCode())) {
            throw new BusinessException("答卷已是作废状态");
        }
        String reason = TextUtil.cut(dto.getReason(), 400);
        answer.setAnswerStatus(AnswerStatusEnum.VOID.getCode());
        answer.setRemark(TextUtil.cut((answer.getRemark() == null ? "" : answer.getRemark() + "；")
                + "作废：" + reason + "（" + operatorUser.getRealName() + " " + LocalDate.now() + "）", 512));
        bizSurveyAnswerMapper.updateById(answer);

        // 发放单退回未回收：作废不等于「没问过」，回收率的分母不能跟着缩
        BizSurveyDispatch dispatch = bizSurveyDispatchMapper.selectById(answer.getDispatchId());
        if (dispatch != null && Objects.equals(dispatch.getAnswerId(), answer.getId())) {
            dispatch.setDispatchStatus(dispatch.getPushTime() == null
                    ? SurveyDispatchStatusEnum.PENDING_PUSH.getCode() : SurveyDispatchStatusEnum.PUSHED.getCode());
            bizSurveyDispatchMapper.updateById(dispatch);
        }
        if (answer.getDisputeCaseId() != null) {
            String withTrace = TextUtil.cut((answer.getRemark() == null ? "" : answer.getRemark() + "；")
                    + "原转投诉单 " + answer.getDisputeCaseId() + " 继续有效（本答卷已作废重填）", 512);
            bizSurveyAnswerMapper.update(null, new LambdaUpdateWrapper<BizSurveyAnswer>()
                    .eq(BizSurveyAnswer::getId, answer.getId())
                    .set(BizSurveyAnswer::getRemark, withTrace)
                    .set(BizSurveyAnswer::getDisputeCaseId, null));
            answer.setDisputeCaseId(null);
        }
        return answerGetById(answer.getId());
    }

    @Override
    public SurveyStatVO stat(Long templateId, Integer scene, String dateFrom, String dateTo) {
        String from = TextUtil.trimToNull(dateFrom);
        String to = TextUtil.trimToNull(dateTo);
        List<Long> scope = scopedDeptIds(null);
        SurveyStatVO vo = new SurveyStatVO();

        long pendingPush = 0, pushed = 0, recycled = 0, expired = 0, refused = 0;
        for (SurveyDispatchCountVO row : bizSurveyDispatchMapper.countByStatus(templateId, null, from, to, scope)) {
            int k = NumUtil.orZero(row.getK());
            long c = NumUtil.orZero(row.getC());
            if (k == SurveyDispatchStatusEnum.PENDING_PUSH.getCode()) {
                pendingPush = c;
            } else if (k == SurveyDispatchStatusEnum.PUSHED.getCode()) {
                pushed = c;
            } else if (k == SurveyDispatchStatusEnum.RECYCLED.getCode()) {
                recycled = c;
            } else if (k == SurveyDispatchStatusEnum.EXPIRED.getCode()) {
                expired = c;
            } else if (k == SurveyDispatchStatusEnum.REFUSED.getCode()) {
                refused = c;
            }
        }
        long dispatchTotal = pendingPush + pushed + recycled + expired + refused;
        vo.setDispatchTotal(dispatchTotal);
        vo.setPendingPushCount(pendingPush);
        vo.setWaitingCount(pushed);
        vo.setRecycledCount(recycled);
        vo.setRefusedCount(refused);
        // 过期是派生态：状态列里没有 4 也照样可能已过截止，看板必须现算
        vo.setOverdueCount(NumUtil.orZero(bizSurveyDispatchMapper.countOverdue(templateId, null, scope)));
        vo.setRecycleRate(rate(recycled, dispatchTotal));

        SurveyOverallStatVO overall = bizSurveyAnswerMapper.statOverall(templateId, scene, from, to, scope);
        if (overall != null) {
            long total = NumUtil.orZero(overall.getTotal());
            vo.setAnswerTotal(total);
            vo.setVoidCount(NumUtil.orZero(overall.getVoided()));
            vo.setAvgScore(overall.getAvgScore());
            vo.setAvgScore100(overall.getAvgScore100());
            vo.setLowScoreCount(NumUtil.orZero(overall.getLowScore()));
            vo.setDisputedCount(NumUtil.orZero(overall.getDisputed()));
            vo.setSatisfiedRate(rate(NumUtil.orZero(overall.getSatisfied()), total));
        } else {
            vo.setAnswerTotal(0L);
            vo.setSatisfiedRate(BigDecimal.ZERO);
        }

        SurveyNpsStatVO nps = bizSurveyAnswerMapper.statNps(templateId, from, to, scope);
        if (nps != null) {
            long rated = NumUtil.orZero(nps.getRated());
            vo.setNps(rated == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(NumUtil.orZero(nps.getPromoter()) - NumUtil.orZero(nps.getDetractor()))
                    .multiply(HUNDRED).divide(BigDecimal.valueOf(rated), 1, RoundingMode.HALF_UP));
        } else {
            vo.setNps(BigDecimal.ZERO);
        }

        List<SurveyStatItemVO> byDimension = new ArrayList<>();
        for (SurveyDimensionStatVO row : bizSurveyAnswerMapper.statByDimension(templateId, from, to, scope)) {
            int k = NumUtil.orZero(row.getDimension());
            SurveyStatItemVO item = new SurveyStatItemVO(String.valueOf(k),
                    SurveyDimensionEnum.getText(k), NumUtil.orZero(row.getCnt()));
            item.setAvgScore(row.getAvgScore());
            byDimension.add(item);
        }
        vo.setByDimension(byDimension);

        List<SurveyStatItemVO> byDept = new ArrayList<>();
        for (SurveyDeptScoreVO row : bizSurveyAnswerMapper.statByDeptBottom(templateId, from, to, scope)) {
            SurveyStatItemVO item = new SurveyStatItemVO(String.valueOf(row.getDeptId()),
                    row.getDeptName(), NumUtil.orZero(row.getCnt()));
            item.setAvgScore(row.getAvgScore100());
            byDept.add(item);
        }
        vo.setByDeptBottom(byDept);

        List<SurveyStatItemVO> byChannel = new ArrayList<>();
        for (SurveyChannelStatVO row : bizSurveyDispatchMapper.countByChannel(templateId, from, to, scope)) {
            int k = NumUtil.orZero(row.getK());
            long total = NumUtil.orZero(row.getTotal());
            SurveyStatItemVO item = new SurveyStatItemVO(String.valueOf(k),
                    SurveyChannelEnum.getText(k), total);
            item.setRate(rate(NumUtil.orZero(row.getRecycled()), total));
            byChannel.add(item);
        }
        vo.setByChannel(byChannel);

        List<SurveyStatItemVO> byDay = new ArrayList<>();
        for (SurveyDayTrendVO row : bizSurveyAnswerMapper.statByDay(templateId, scope)) {
            SurveyStatItemVO item = new SurveyStatItemVO(row.getStatDate(),
                    row.getStatDate(), NumUtil.orZero(row.getCnt()));
            item.setAvgScore(row.getAvgScore100());
            byDay.add(item);
        }
        vo.setByDay(byDay);
        return vo;
    }

    // 内部：出参与收口

    /**
     * 卷面题目（按题目ID索引）—— 回收必须落在这张卷真实存在的题上
     */
    private Map<Long, BizSurveyItem> paperOf(Long templateId) {
        Map<Long, BizSurveyItem> paper = new HashMap<>();
        for (BizSurveyItem item : bizSurveyItemMapper.selectByTemplate(templateId)) {
            paper.put(item.getId(), item);
        }
        if (paper.isEmpty()) {
            throw new BusinessException("该问卷没有题目，请先在问卷模板里配置题目");
        }
        return paper;
    }

    /**
     * 逐题校验并回填题目快照。
     *
     * <p>答了不属于本卷的题（换卷、或前端缓存了旧题ID）必须报错而不是忽略：
     * 静默丢题会让「必答 8 题只收到 6 题」这种残缺卷进统计。
     */
    private List<SurveyAnswerItemVO> validateAndBuild(List<SurveyAnswerUpsertDTO.Item> answers,
                                                      Map<Long, BizSurveyItem> paper) {
        Map<Long, SurveyAnswerItemVO> built = new LinkedHashMap<>();
        for (SurveyAnswerUpsertDTO.Item src : answers) {
            BizSurveyItem item = paper.get(src.getItemId());
            if (item == null) {
                throw new BusinessException("题目 " + src.getItemId() + " 不属于本次发放的问卷");
            }
            if (built.containsKey(item.getId())) {
                throw new BusinessException("第 " + item.getSeqNo() + " 题重复作答");
            }
            checkAnswer(item, src);
            SurveyAnswerItemVO vo = new SurveyAnswerItemVO();
            vo.setItemId(item.getId());
            vo.setDimension(item.getDimension());
            vo.setSeqNo(item.getSeqNo());
            vo.setTitle(item.getTitle());
            vo.setQuestionType(item.getQuestionType());
            vo.setScore(src.getScore());
            vo.setOptionLabel(TextUtil.cut(src.getOptionLabel(), 128));
            vo.setTextValue(TextUtil.cut(src.getTextValue(), 1000));
            built.put(item.getId(), vo);
        }
        for (BizSurveyItem item : paper.values()) {
            if (Objects.equals(item.getRequired(), 1) && !built.containsKey(item.getId())) {
                throw new BusinessException("第 " + item.getSeqNo() + " 题（" + item.getTitle() + "）为必答题");
            }
        }
        return new ArrayList<>(built.values());
    }

    private void checkAnswer(BizSurveyItem item, SurveyAnswerUpsertDTO.Item src) {
        int type = item.getQuestionType();
        if (type == SurveyQuestionTypeEnum.SCALE.getCode()) {
            if (src.getScore() == null) {
                throw new BusinessException("第 " + item.getSeqNo() + " 题需要打分（1-" + maxScoreOf(item) + "）");
            }
            if (src.getScore() < 1 || src.getScore() > maxScoreOf(item)) {
                throw new BusinessException("第 " + item.getSeqNo() + " 题打分超出范围（1-" + maxScoreOf(item) + "）");
            }
        } else if (type == SurveyQuestionTypeEnum.NPS.getCode()) {
            if (src.getScore() == null) {
                throw new BusinessException("第 " + item.getSeqNo() + " 题需要打推荐度（0-10）");
            }
            if (src.getScore() < 0 || src.getScore() > 10) {
                throw new BusinessException("第 " + item.getSeqNo() + " 题推荐度超出范围（0-10）");
            }
        } else if (type == SurveyQuestionTypeEnum.TEXT.getCode()) {
            if (!TextUtil.hasText(src.getTextValue()) && !TextUtil.hasText(src.getOptionLabel())) {
                throw new BusinessException("第 " + item.getSeqNo() + " 题请填写文字内容");
            }
        } else if (!TextUtil.hasText(src.getOptionLabel())) {
            throw new BusinessException("第 " + item.getSeqNo() + " 题请选择选项");
        }
    }

    /**
     * 计分：李克特加权均分 → 百分制 → NPS 单列。
     *
     * <p>NPS 不并入百分制：0-10 的推荐度和 1-5 的满意度不同量纲，混算出来的数既不是满意度也不是 NPS。
     */
    private void scoreOf(List<SurveyAnswerItemVO> items, Map<Long, BizSurveyItem> paper, BizSurveyAnswer answer) {
        BigDecimal weightSum = BigDecimal.ZERO;
        BigDecimal scoreSum = BigDecimal.ZERO;
        Integer nps = null;
        for (SurveyAnswerItemVO item : items) {
            if (Objects.equals(item.getQuestionType(), SurveyQuestionTypeEnum.SCALE.getCode()) && item.getScore() != null) {
                BigDecimal weight = paper.get(item.getItemId()).getWeight();
                if (weight == null || weight.signum() <= 0) {
                    continue;
                }
                weightSum = weightSum.add(weight);
                scoreSum = scoreSum.add(weight.multiply(BigDecimal.valueOf(item.getScore())));
            } else if (Objects.equals(item.getQuestionType(), SurveyQuestionTypeEnum.NPS.getCode()) && nps == null) {
                nps = item.getScore();
            }
        }
        if (weightSum.signum() > 0) {
            BigDecimal avg = scoreSum.divide(weightSum, 2, RoundingMode.HALF_UP);
            answer.setAvgScore(avg);
            answer.setScore100(avg.subtract(BigDecimal.ONE)
                    .divide(new BigDecimal("4"), 4, RoundingMode.HALF_UP)
                    .multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP));
        } else {
            answer.setAvgScore(null);
            answer.setScore100(null);
        }
        answer.setNps(nps);
    }

    /**
     * 低分判定：百分制 &lt; 60，或任一维度均分 &le; 2。
     *
     * <p>只看总分会漏掉「总体还行但护士服务被打 1 分」—— 那正是评审最想要的那类线索。
     */
    private boolean isLowScore(BizSurveyAnswer answer, List<SurveyAnswerItemVO> items) {
        if (answer.getScore100() != null && answer.getScore100().compareTo(BizSurveyAnswer.LOW_SCORE_100) < 0) {
            return true;
        }
        Map<Integer, BigDecimal> sum = new HashMap<>();
        Map<Integer, Long> cnt = new HashMap<>();
        for (SurveyAnswerItemVO item : items) {
            if (!Objects.equals(item.getQuestionType(), SurveyQuestionTypeEnum.SCALE.getCode()) || item.getScore() == null
                    || item.getDimension() == null) {
                continue;
            }
            sum.merge(item.getDimension(), BigDecimal.valueOf(item.getScore()), BigDecimal::add);
            cnt.merge(item.getDimension(), 1L, Long::sum);
        }
        for (Map.Entry<Integer, BigDecimal> entry : sum.entrySet()) {
            BigDecimal avg = entry.getValue().divide(BigDecimal.valueOf(cnt.get(entry.getKey())), 2, RoundingMode.HALF_UP);
            if (avg.compareTo(BizSurveyAnswer.LOW_DIMENSION_AVG) <= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 低分卷转成投诉单（走 DisputeService，绝不直接写它的表：登记要生成单号、患者快照、状态初始化）
     */
    private DisputeCaseUpsertDTO buildDispute(BizSurveyDispatch dispatch, BizSurveyAnswer answer,
                                              List<SurveyAnswerItemVO> items) {
        DisputeCaseUpsertDTO dto = new DisputeCaseUpsertDTO();
        dto.setCaseType(DisputeCategoryEnum.SERVICE_COMPLAINT.getCode());
        dto.setSourceType(DisputeSourceEnum.INTERNAL.getCode());
        // 百分制低于 40 视为较大：整体被打到「很不满」通常不是单点瑕疵
        dto.setLevel(answer.getScore100() != null
                && answer.getScore100().compareTo(new BigDecimal("40")) < 0 ? 2 : 1);
        dto.setPatientId(answer.getPatientId());
        dto.setDeptId(answer.getDeptId());
        dto.setComplainant(answer.getPatientName());
        dto.setComplainantRel(ComplainantRelEnum.SELF.getCode());
        dto.setComplainantTel(dispatch.getPhone());
        dto.setOccurTime(answer.getFillTime() == null ? null : answer.getFillTime().format(DateFormats.DATETIME));
        dto.setOccurPlace("出院随访满意度回访");

        StringBuilder content = new StringBuilder();
        content.append("满意度回访低分自动登记：问卷《").append(dispatch.getTemplateName()).append("》");
        content.append(answer.getScore100() == null ? "未计百分制分" : "百分制 " + answer.getScore100() + " 分");
        String worst = worstDimension(items);
        if (worst != null) {
            content.append("；最低维度：").append(worst);
        }
        if (TextUtil.hasText(answer.getCommentText())) {
            content.append("；患者原话：").append(answer.getCommentText());
        }
        content.append("（答卷号 ").append(answer.getAnswerNo()).append("）");
        dto.setContent(content.toString());
        dto.setDemand("请科室核实整改，并由投诉管理部门向患者答复");
        dto.setRemark("由满意度答卷自动登记，来源发放单 " + dispatch.getDispatchNo());
        return dto;
    }

    /**
     * 最短板维度描述（均分最低的那个维度）
     */
    private String worstDimension(List<SurveyAnswerItemVO> items) {
        Map<Integer, BigDecimal> sum = new HashMap<>();
        Map<Integer, Long> cnt = new HashMap<>();
        for (SurveyAnswerItemVO item : items) {
            if (!Objects.equals(item.getQuestionType(), SurveyQuestionTypeEnum.SCALE.getCode()) || item.getScore() == null
                    || item.getDimension() == null) {
                continue;
            }
            sum.merge(item.getDimension(), BigDecimal.valueOf(item.getScore()), BigDecimal::add);
            cnt.merge(item.getDimension(), 1L, Long::sum);
        }
        Integer worstKey = null;
        BigDecimal worstAvg = null;
        for (Map.Entry<Integer, BigDecimal> entry : sum.entrySet()) {
            BigDecimal avg = entry.getValue().divide(BigDecimal.valueOf(cnt.get(entry.getKey())), 2, RoundingMode.HALF_UP);
            if (worstAvg == null || avg.compareTo(worstAvg) < 0) {
                worstAvg = avg;
                worstKey = entry.getKey();
            }
        }
        // 落进纠纷投诉单正文（可追责的书面记录），脏码值要用 labelOrUnknown 保留原始码值便于排查
        return worstKey == null ? null : SurveyDimensionEnum.labelOrUnknown(worstKey) + " " + worstAvg + " 分";
    }

    private BizSurveyAnswerItem toAnswerItem(Long answerId, Long templateId, SurveyAnswerItemVO src) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizSurveyAnswerItem item = new BizSurveyAnswerItem();
        item.setAnswerId(answerId);
        item.setItemId(src.getItemId());
        item.setTemplateId(templateId);
        item.setDimension(src.getDimension());
        item.setSeqNo(src.getSeqNo());
        item.setTitle(src.getTitle());
        item.setQuestionType(src.getQuestionType());
        item.setScore(src.getScore());
        item.setOptionLabel(src.getOptionLabel());
        item.setTextValue(src.getTextValue());
        item.setCreateBy(operatorUser.getRealName());
        return item;
    }

    /**
     * 过期是派生态：状态列没有 4 也算（不靠定时任务翻状态，避免「日期已过、状态还没刷」的漂移窗口）
     */
    private boolean isOverdue(BizSurveyDispatch entity) {
        return !Objects.equals(entity.getDispatchStatus(), SurveyDispatchStatusEnum.RECYCLED.getCode())
                && entity.getExpireTime() != null && entity.getExpireTime().isBefore(TimeUtil.nowSeconds());
    }

    /**
     * @param plainPhone 仅编辑回显（外呼拨号）给明文，列表一律脱敏
     */
    private void decorateDispatch(SurveyDispatchVO vo, boolean plainPhone) {
        Integer st = vo.getDispatchStatus();
        boolean recycled = Objects.equals(st, SurveyDispatchStatusEnum.RECYCLED.getCode());
        vo.setCanFill(!recycled);
        vo.setCanPush(Objects.equals(st, SurveyDispatchStatusEnum.PENDING_PUSH.getCode()));
        vo.setCanRefuse(Objects.equals(st, SurveyDispatchStatusEnum.PENDING_PUSH.getCode())
                || Objects.equals(st, SurveyDispatchStatusEnum.PUSHED.getCode())
                || Objects.equals(st, SurveyDispatchStatusEnum.EXPIRED.getCode()));
        vo.setPhoneMasked(SensitiveMaskUtil.maskPhone(vo.getPhone()));
        if (!plainPhone) {
            vo.setPhone(null);
        }
    }

    private void decorateAnswer(SurveyAnswerVO vo) {
        boolean valid = Objects.equals(vo.getAnswerStatus(), AnswerStatusEnum.VALID.getCode());
        vo.setCanEdit(valid && vo.getDisputeCaseId() == null);
        vo.setCanVoid(valid);
    }

    private BizSurveyDispatch requireDispatch(Long id) {
        BizSurveyDispatch entity = id == null ? null : bizSurveyDispatchMapper.selectDispatchById(id);
        if (entity == null) {
            throw new BusinessException("发放单不存在或已删除");
        }
        return entity;
    }

    private void assertDeptAccessible(Long deptId) {
        if (!deptScopeProvider.canAccessDept(deptId)) {
            throw new BusinessException("该数据所属科室不在当前岗位的数据范围内");
        }
    }

    /**
     * null=不限科室；非空=收口集合（显式 deptId 越权时由 DeptScopeProvider 抛错；空集合按配置缺失拒掉，绝不放行成全院）
     */
    private List<Long> scopedDeptIds(Long requestedDeptId) {
        Long resolved = deptScopeProvider.resolveDeptId(requestedDeptId);
        if (resolved != null) {
            return List.of(resolved);
        }
        Set<Long> allowed = deptScopeProvider.allowedDeptIds();
        if (allowed == null) {
            return null;
        }
        if (allowed.isEmpty()) {
            throw new BusinessException("当前岗位未绑定任何科室，无法查看评价数据（请在系统管理为岗位分配科室）");
        }
        return List.copyOf(allowed);
    }

    private String nextNo(String prefix, String module) {
        return prefix + LocalDate.now().format(DateFormats.COMPACT_DATE)
                + String.format("%04d", redisSequenceService.next(module));
    }
}
