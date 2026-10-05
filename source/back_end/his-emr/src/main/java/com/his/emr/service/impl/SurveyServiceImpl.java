package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.Constants;
import com.his.common.base.PageResult;
import com.his.common.base.RedisSequenceService;
import com.his.common.exception.BusinessException;
import com.his.common.support.SensitiveMaskUtils;
import com.his.emr.dto.*;
import com.his.emr.entity.*;
import com.his.emr.enums.*;
import com.his.emr.mapper.*;
import com.his.emr.service.DisputeService;
import com.his.emr.service.SurveyService;
import com.his.emr.service.SurveyTemplateService;
import com.his.emr.support.FollowupTaskSnapshot;
import com.his.emr.vo.*;
import com.his.security.DeptScopeGuard;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
public class SurveyServiceImpl implements SurveyService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 评价维度名（字典 his_survey_dimension 的 Java 侧镜像，看板直接出中文名）
     */
    private static final Map<Integer, String> DIMENSION_NAMES = Map.of(
            1, "挂号便捷", 2, "医生服务", 3, "护士服务", 4, "环境与流程",
            5, "费用透明", 6, "疗效与安全感", 7, "总体印象");
    /**
     * 回收渠道名（字典 his_survey_channel）
     */
    private static final Map<Integer, String> CHANNEL_NAMES = Map.of(
            1, "电话代填", 2, "短信", 3, "微信/互联网", 4, "现场扫码");
    /**
     * 回收状态名（字典 his_survey_dispatch_status）
     */
    private static final Map<Integer, String> DISPATCH_STATUS_NAMES = Map.of(
            1, "待推送", 2, "已推送待回收", 3, "已回收", 4, "已过期", 5, "已拒答");

    private final BizSurveyDispatchMapper dispatchMapper;
    private final BizSurveyAnswerMapper answerMapper;
    private final BizSurveyAnswerItemMapper answerItemMapper;
    private final BizSurveyItemMapper itemMapper;
    private final BizFollowupTaskMapper followupTaskMapper;
    private final SurveyTemplateService templateService;
    private final DisputeService disputeService;
    private final RedisSequenceService sequenceService;

    // 发放与回收

    private static BigDecimal rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator).multiply(HUNDRED)
                .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP);
    }

    private static BigDecimal decimal(Object v) {
        return v == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(v));
    }

    private static long toLong(Object v) {
        return v == null ? 0L : new BigDecimal(String.valueOf(v)).longValue();
    }

    private static long nz(Long v) {
        return v == null ? 0L : v;
    }

    private static int maxScoreOf(BizSurveyItem item) {
        return item.getMaxScore() == null ? 5 : item.getMaxScore();
    }

    // 答卷

    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private static String cut(String v, int max) {
        if (v == null) {
            return null;
        }
        String s = v.trim();
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static String trimToNull(String v) {
        return StringUtils.hasText(v) ? v.trim() : null;
    }

    @Override
    public PageResult<SurveyDispatchVO> dispatchListPage(SurveyDispatchQueryPageDTO dto) {
        Page<SurveyDispatchVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        List<SurveyDispatchVO> records = dispatchMapper.selectDispatchPage(page,
                trimToNull(dto.getKeyword()), dto.getPatientId(), dto.getSourceType(),
                dto.getDispatchStatus(), dto.getChannel(), dto.getOverdueOnly(),
                trimToNull(dto.getDateFrom()), trimToNull(dto.getDateTo()),
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
        BizFollowupTask task = followupTaskMapper.selectById(dto.getFollowupTaskId());
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
        int src = sourceType == null ? SurveySourceEnum.FOLLOWUP.getCode() : sourceType;
        SurveyTemplateVO template = templateService.findEnabledForScene(SurveySceneEnum.DISCHARGE_FOLLOWUP.getCode());
        // 没配卷就不发：评价域是随访的旁路，绝不能反过来把随访卡死
        if (template == null || template.getItems() == null || template.getItems().isEmpty()) {
            log.warn("[满意度] 随访任务 {} 未发放评价：场景 {} 没有启用中的问卷", task.getTaskId(),
                    SurveySceneEnum.DISCHARGE_FOLLOWUP.getCode());
            return null;
        }
        BizSurveyDispatch existed = dispatchMapper.selectBySource(src, task.getTaskId(), template.getId());
        if (existed != null) {
            return dispatchGetById(existed.getId());
        }
        int ch = channel == null ? SurveyChannelEnum.PHONE.getCode() : channel;
        int days = expireDays == null || expireDays <= 0 ? BizSurveyDispatch.DEFAULT_EXPIRE_DAYS : expireDays;

        BizSurveyDispatch entity = new BizSurveyDispatch();
        entity.setDispatchNo(nextNo(Constants.SURVEY_DISPATCH_NO_PREFIX, Constants.SURVEY_DISPATCH_NO_KEY_PREFIX));
        entity.setTemplateId(template.getId());
        entity.setTemplateName(cut(template.getTemplateName(), 128));
        entity.setScene(template.getScene());
        entity.setSourceType(src);
        entity.setSourceId(task.getTaskId());
        entity.setPatientId(task.getPatientId());
        entity.setPatientNo(cut(task.getPatientNo(), 64));
        entity.setPatientName(cut(task.getPatientName(), 128));
        entity.setPhone(cut(task.getPhone(), 20));
        entity.setDeptId(task.getDeptId());
        entity.setDeptName(cut(task.getDeptName(), 128));
        entity.setChannel(ch);
        // 短信/微信没有真实网关，只能落「待推送」等人工外呼 —— 状态诚实比好看重要
        entity.setDispatchStatus(SurveyDispatchStatusEnum.PENDING_PUSH.getCode());
        entity.setExpireTime(now().plusDays(days));
        entity.setCreateBy(currentOperator());
        dispatchMapper.insert(entity);
        return dispatchGetById(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SurveyDispatchVO markDispatch(SurveyDispatchActionDTO dto) {
        BizSurveyDispatch entity = requireDispatch(dto.getId());
        assertDeptAccessible(entity.getDeptId());
        String remark = cut(dto.getRemark(), 512);
        if (Objects.equals(dto.getAction(), 1)) {
            if (!Objects.equals(entity.getDispatchStatus(), SurveyDispatchStatusEnum.PENDING_PUSH.getCode())) {
                throw new BusinessException("仅「待推送」的发放单可标记已推送（当前："
                        + DISPATCH_STATUS_NAMES.get(entity.getDispatchStatus()) + "）");
            }
            entity.setDispatchStatus(SurveyDispatchStatusEnum.PUSHED.getCode());
            entity.setPushTime(now());
        } else if (Objects.equals(dto.getAction(), 2)) {
            if (Objects.equals(entity.getDispatchStatus(), SurveyDispatchStatusEnum.RECYCLED.getCode())) {
                throw new BusinessException("已回收的发放单不能标记拒答（要更正请先作废答卷）");
            }
            if (!Objects.equals(entity.getDispatchStatus(), SurveyDispatchStatusEnum.PENDING_PUSH.getCode())
                    && !Objects.equals(entity.getDispatchStatus(), SurveyDispatchStatusEnum.PUSHED.getCode())) {
                throw new BusinessException("仅未回收的发放单可标记拒答（当前："
                        + DISPATCH_STATUS_NAMES.get(entity.getDispatchStatus()) + "）");
            }
            entity.setDispatchStatus(SurveyDispatchStatusEnum.REFUSED.getCode());
        } else {
            throw new BusinessException("动作不合法：1-标记已推送 2-标记已拒答（「已回收」只能由答卷写入）");
        }
        entity.setRemark(remark);
        dispatchMapper.updateById(entity);
        return dispatchGetById(entity.getId());
    }

    @Override
    public PageResult<SurveyAnswerVO> answerListPage(SurveyAnswerQueryPageDTO dto) {
        Page<SurveyAnswerVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        List<SurveyAnswerVO> records = answerMapper.selectAnswerPage(page,
                trimToNull(dto.getKeyword()), dto.getPatientId(), dto.getTemplateId(), dto.getScene(),
                dto.getAnswerStatus(), dto.getFillSource(), dto.getLowScoreOnly(),
                trimToNull(dto.getDateFrom()), trimToNull(dto.getDateTo()), scopedDeptIds(dto.getDeptId()));
        records.forEach(this::decorateAnswer);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public SurveyAnswerVO answerGetById(Long id) {
        SurveyAnswerVO vo = answerMapper.selectAnswerById(id);
        if (vo == null) {
            throw new BusinessException("答卷不存在或已删除");
        }
        assertDeptAccessible(vo.getDeptId());
        decorateAnswer(vo);
        vo.setItems(answerItemMapper.selectByAnswer(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SurveyAnswerVO submitAnswer(SurveyAnswerUpsertDTO dto) {
        BizSurveyDispatch dispatch = requireDispatch(dto.getDispatchId());
        assertDeptAccessible(dispatch.getDeptId());
        if (Objects.equals(dispatch.getDispatchStatus(), SurveyDispatchStatusEnum.EXPIRED.getCode())) {
            throw new BusinessException("该发放单已过期");
        }

        BizSurveyAnswer answer = dispatch.getAnswerId() == null ? null : answerMapper.selectById(dispatch.getAnswerId());
        if (answer != null && Objects.equals(answer.getAnswerStatus(), AnswerStatusEnum.VALID.getCode())
                && answer.getDisputeCaseId() != null) {
            throw new BusinessException("该答卷已转出投诉单，禁止重填（改分数等于改掉投诉的由来）");
        }

        Map<Long, BizSurveyItem> paper = paperOf(dispatch.getTemplateId());
        List<SurveyAnswerItemVO> submitted = validateAndBuild(dto.getItems(), paper);
        boolean isNew = answer == null;
        if (isNew) {
            answer = new BizSurveyAnswer();
            answer.setAnswerNo(nextNo(Constants.SURVEY_ANSWER_NO_PREFIX, Constants.SURVEY_ANSWER_NO_KEY_PREFIX));
            answer.setDispatchId(dispatch.getId());
            answer.setTemplateId(dispatch.getTemplateId());
            answer.setScene(dispatch.getScene());
            answer.setPatientId(dispatch.getPatientId());
            answer.setPatientNo(dispatch.getPatientNo());
            answer.setPatientName(dispatch.getPatientName());
            answer.setDeptId(dispatch.getDeptId());
            answer.setDeptName(dispatch.getDeptName());
            answer.setCreateBy(currentOperator());
        }
        // 患者身份与科室一律取自发放单，不接收前端传值：否则改个 patientId 就能把意见挂到别人头上
        answer.setAnswerStatus(AnswerStatusEnum.VALID.getCode());
        answer.setFillSource(dto.getFillSource() == null ? FillSourceEnum.AGENT.getCode() : dto.getFillSource());
        answer.setAnonymousFlag(Objects.equals(dto.getAnonymousFlag(), 1) ? 1 : 0);
        answer.setFillEmployeeId(UserUtils.getCurrentEmployeeId());
        answer.setFillEmployeeName(cut(currentOperator(), 64));
        answer.setFillTime(now());
        answer.setCommentText(cut(dto.getCommentText(), 1000));
        answer.setRemark(cut(dto.getRemark(), 512));
        scoreOf(submitted, paper, answer);

        if (isNew) {
            answerMapper.insert(answer);
        } else {
            answerMapper.updateById(answer);
        }
        // 重填覆盖明细：uk_survey_answer_item(answer_id,item_id) 不含 del_flag，软删必撞键
        answerItemMapper.purgeByAnswer(answer.getId());
        for (SurveyAnswerItemVO item : submitted) {
            answerItemMapper.insert(toAnswerItem(answer.getId(), dispatch.getTemplateId(), item));
        }

        dispatch.setDispatchStatus(SurveyDispatchStatusEnum.RECYCLED.getCode());
        dispatch.setAnswerId(answer.getId());
        // 电话代填说明已触达患者，补记推送时点，台账上不会出现「已回收却从没推送」
        if (dispatch.getPushTime() == null) {
            dispatch.setPushTime(answer.getFillTime());
        }
        dispatchMapper.updateById(dispatch);

        // 低分转投诉：同一事务内完成，转单失败就整体回滚 ——
        // 「打了低分但投诉台账里没有」是最难发现的漏，宁可让录入员看到报错重来
        if (answer.getDisputeCaseId() == null && isLowScore(answer, submitted)) {
            Long caseId = disputeService.caseUpsert(buildDispute(dispatch, answer, submitted)).getId();
            answer.setDisputeCaseId(caseId);
            answerMapper.updateById(answer);
        }
        return answerGetById(answer.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SurveyAnswerVO voidAnswer(SurveyAnswerVoidDTO dto) {
        BizSurveyAnswer answer = answerMapper.selectById(dto.getId());
        if (answer == null) {
            throw new BusinessException("答卷不存在或已删除");
        }
        assertDeptAccessible(answer.getDeptId());
        if (!Objects.equals(answer.getAnswerStatus(), AnswerStatusEnum.VALID.getCode())) {
            throw new BusinessException("答卷已是作废状态");
        }
        String reason = cut(dto.getReason(), 400);
        answer.setAnswerStatus(AnswerStatusEnum.VOID.getCode());
        answer.setRemark(cut((answer.getRemark() == null ? "" : answer.getRemark() + "；")
                + "作废：" + reason + "（" + currentOperator() + " " + LocalDate.now() + "）", 512));
        answerMapper.updateById(answer);

        // 发放单退回未回收：作废不等于「没问过」，回收率的分母不能跟着缩
        BizSurveyDispatch dispatch = dispatchMapper.selectById(answer.getDispatchId());
        if (dispatch != null && Objects.equals(dispatch.getAnswerId(), answer.getId())) {
            dispatch.setDispatchStatus(dispatch.getPushTime() == null
                    ? SurveyDispatchStatusEnum.PENDING_PUSH.getCode() : SurveyDispatchStatusEnum.PUSHED.getCode());
            dispatchMapper.updateById(dispatch);
        }
        // 投诉单本身不撤（登记时患者确实在电话里表达了不满，作废的是分数不是这条事实），
        // 但要解除「已转投诉禁止重填」这道锁 —— 留着指针会让这张卷子永远卡在「已回收 + 不许重填」。
        // 「答过什么 → 转了哪张投诉单」的追溯改由 remark 与投诉单 content 里的答卷号自证。
        if (answer.getDisputeCaseId() != null) {
            String withTrace = cut((answer.getRemark() == null ? "" : answer.getRemark() + "；")
                    + "原转投诉单 " + answer.getDisputeCaseId() + " 继续有效（本答卷已作废重填）", 512);
            // updateById 的字段策略默认 NOT_NULL，setDisputeCaseId(null) 会被静默忽略 ——
            // 必须走 UpdateWrapper 显式 SET NULL，否则锁解不掉，这张卷子再也收不回来
            answerMapper.update(null, new LambdaUpdateWrapper<BizSurveyAnswer>()
                    .eq(BizSurveyAnswer::getId, answer.getId())
                    .set(BizSurveyAnswer::getRemark, withTrace)
                    .set(BizSurveyAnswer::getDisputeCaseId, null));
            answer.setDisputeCaseId(null);
        }
        return answerGetById(answer.getId());
    }

    @Override
    public SurveyStatVO stat(Long templateId, Integer scene, String dateFrom, String dateTo) {
        String from = trimToNull(dateFrom);
        String to = trimToNull(dateTo);
        List<Long> scope = scopedDeptIds(null);
        SurveyStatVO vo = new SurveyStatVO();

        long pendingPush = 0, pushed = 0, recycled = 0, expired = 0, refused = 0;
        for (Map<String, Object> row : dispatchMapper.countByStatus(templateId, null, from, to, scope)) {
            int k = (int) toLong(row.get("k"));
            long c = toLong(row.get("c"));
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
        vo.setOverdueCount(nz(dispatchMapper.countOverdue(templateId, null, scope)));
        vo.setRecycleRate(rate(recycled, dispatchTotal));

        Map<String, Object> overall = answerMapper.statOverall(templateId, scene, from, to, scope);
        if (overall != null) {
            long total = toLong(overall.get("total"));
            vo.setAnswerTotal(total);
            vo.setVoidCount(toLong(overall.get("voided")));
            vo.setAvgScore(decimal(overall.get("avg_score")));
            vo.setAvgScore100(decimal(overall.get("avg_score100")));
            vo.setLowScoreCount(toLong(overall.get("low_score")));
            vo.setDisputedCount(toLong(overall.get("disputed")));
            vo.setSatisfiedRate(rate(toLong(overall.get("satisfied")), total));
        } else {
            vo.setAnswerTotal(0L);
            vo.setSatisfiedRate(BigDecimal.ZERO);
        }

        Map<String, Object> nps = answerMapper.statNps(templateId, from, to, scope);
        if (nps != null) {
            long rated = toLong(nps.get("rated"));
            vo.setNps(rated == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(toLong(nps.get("promoter")) - toLong(nps.get("detractor")))
                    .multiply(HUNDRED).divide(BigDecimal.valueOf(rated), 1, RoundingMode.HALF_UP));
        } else {
            vo.setNps(BigDecimal.ZERO);
        }

        List<SurveyStatItemVO> byDimension = new ArrayList<>();
        for (Map<String, Object> row : answerMapper.statByDimension(templateId, from, to, scope)) {
            int k = (int) toLong(row.get("k"));
            SurveyStatItemVO item = new SurveyStatItemVO(String.valueOf(k),
                    DIMENSION_NAMES.getOrDefault(k, "维度" + k), toLong(row.get("c")));
            item.setAvgScore(decimal(row.get("avg_score")));
            byDimension.add(item);
        }
        vo.setByDimension(byDimension);

        List<SurveyStatItemVO> byDept = new ArrayList<>();
        for (Map<String, Object> row : answerMapper.statByDeptBottom(templateId, from, to, scope)) {
            SurveyStatItemVO item = new SurveyStatItemVO(String.valueOf(toLong(row.get("d"))),
                    String.valueOf(row.get("n")), toLong(row.get("c")));
            item.setAvgScore(decimal(row.get("avg_score100")));
            byDept.add(item);
        }
        vo.setByDeptBottom(byDept);

        List<SurveyStatItemVO> byChannel = new ArrayList<>();
        for (Map<String, Object> row : dispatchMapper.countByChannel(templateId, from, to, scope)) {
            int k = (int) toLong(row.get("k"));
            long total = toLong(row.get("total"));
            SurveyStatItemVO item = new SurveyStatItemVO(String.valueOf(k),
                    CHANNEL_NAMES.getOrDefault(k, "渠道" + k), total);
            item.setRate(rate(toLong(row.get("recycled")), total));
            byChannel.add(item);
        }
        vo.setByChannel(byChannel);

        List<SurveyStatItemVO> byDay = new ArrayList<>();
        for (Map<String, Object> row : answerMapper.statByDay(templateId, scope)) {
            SurveyStatItemVO item = new SurveyStatItemVO(String.valueOf(row.get("d")),
                    String.valueOf(row.get("d")), toLong(row.get("c")));
            item.setAvgScore(decimal(row.get("avg_score100")));
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
        for (BizSurveyItem item : itemMapper.selectByTemplate(templateId)) {
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
            vo.setOptionLabel(cut(src.getOptionLabel(), 128));
            vo.setTextValue(cut(src.getTextValue(), 1000));
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
            if (!StringUtils.hasText(src.getTextValue()) && !StringUtils.hasText(src.getOptionLabel())) {
                throw new BusinessException("第 " + item.getSeqNo() + " 题请填写文字内容");
            }
        } else if (!StringUtils.hasText(src.getOptionLabel())) {
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
        dto.setOccurTime(answer.getFillTime() == null ? null : answer.getFillTime().format(TIME_FMT));
        dto.setOccurPlace("出院随访满意度回访");

        StringBuilder content = new StringBuilder();
        content.append("满意度回访低分自动登记：问卷《").append(dispatch.getTemplateName()).append("》");
        content.append(answer.getScore100() == null ? "未计百分制分" : "百分制 " + answer.getScore100() + " 分");
        String worst = worstDimension(items);
        if (worst != null) {
            content.append("；最低维度：").append(worst);
        }
        if (StringUtils.hasText(answer.getCommentText())) {
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
        return worstKey == null ? null : DIMENSION_NAMES.getOrDefault(worstKey, "维度" + worstKey) + " " + worstAvg + " 分";
    }

    private BizSurveyAnswerItem toAnswerItem(Long answerId, Long templateId, SurveyAnswerItemVO src) {
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
        item.setCreateBy(currentOperator());
        return item;
    }

    /**
     * 过期是派生态：状态列没有 4 也算（不靠定时任务翻状态，避免「日期已过、状态还没刷」的漂移窗口）
     */
    private boolean isOverdue(BizSurveyDispatch entity) {
        return !Objects.equals(entity.getDispatchStatus(), SurveyDispatchStatusEnum.RECYCLED.getCode())
                && entity.getExpireTime() != null && entity.getExpireTime().isBefore(now());
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
        vo.setPhoneMasked(SensitiveMaskUtils.maskPhone(vo.getPhone()));
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
        BizSurveyDispatch entity = id == null ? null : dispatchMapper.selectDispatchById(id);
        if (entity == null) {
            throw new BusinessException("发放单不存在或已删除");
        }
        return entity;
    }

    private void assertDeptAccessible(Long deptId) {
        if (!DeptScopeGuard.canAccessDept(deptId)) {
            throw new BusinessException("该数据所属科室不在当前岗位的数据范围内");
        }
    }

    /**
     * null=不限科室；非空=收口集合（显式 deptId 越权时由 DeptScopeGuard 抛错；空集合按配置缺失拒掉，绝不放行成全院）
     */
    private List<Long> scopedDeptIds(Long requestedDeptId) {
        Long resolved = DeptScopeGuard.resolveDeptId(requestedDeptId);
        if (resolved != null) {
            return List.of(resolved);
        }
        Set<Long> allowed = DeptScopeGuard.allowedDeptIds();
        if (allowed == null) {
            return null;
        }
        if (allowed.isEmpty()) {
            throw new BusinessException("当前岗位未绑定任何科室，无法查看评价数据（请在系统管理为岗位分配科室）");
        }
        return List.copyOf(allowed);
    }

    private String nextNo(String prefix, String module) {
        return prefix + LocalDate.now().format(NO_DATE)
                + String.format("%04d", sequenceService.next(module));
    }

    private String currentOperator() {
        String name = UserUtils.getCurrentEmployeeName();
        return StringUtils.hasText(name) ? name : "系统";
    }
}
