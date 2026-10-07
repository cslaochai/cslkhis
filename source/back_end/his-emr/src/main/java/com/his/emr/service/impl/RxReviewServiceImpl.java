package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.emr.dto.*;
import com.his.emr.entity.*;
import com.his.emr.enums.*;
import com.his.emr.mapper.*;
import com.his.emr.service.RxReviewService;
import com.his.emr.vo.*;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 处方点评实现。
 *
 * <p>关键口径（写在 sql/160 头注释与各方法上，这里不复述）：
 * 公示只增不可撤、已公示禁改、约谈医师确认后禁改禁删、
 * 一张处方只进一个批次、抽样对象为已审核/已发药处方。
 */
@Service
@RequiredArgsConstructor
public class RxReviewServiceImpl extends ServiceImpl<BizRxReviewBatchMapper, BizRxReviewBatch> implements RxReviewService {

    private static final int EXPORT_MAX = 5000;

    /**
     * 结论 → 允许的问题码分组（11~15 不规范 / 21~27 不适宜 / 31~34 超常）
     */
    private static final Map<Integer, Set<String>> RESULT_PROBLEM_CODES = Map.of(
            RxReviewResultEnum.IRREGULAR.getCode(), Set.of("11", "12", "13", "14", "15"),
            RxReviewResultEnum.UNSUITABLE.getCode(), Set.of("21", "22", "23", "24", "25", "26", "27"),
            RxReviewResultEnum.ABNORMAL.getCode(), Set.of("31", "32", "33", "34"));

    /**
     * 规范：超常处方 3 次以上且无正当理由 → 警告并限制处方权
     */
    private static final long NEED_TALK_ABNORMAL_THRESHOLD = 3;

    private final BizRxReviewBatchMapper bizRxReviewBatchMapper;
    private final BizRxReviewItemMapper bizRxReviewItemMapper;
    private final BizRxDoctorTalkMapper bizRxDoctorTalkMapper;
    private final BizPrescriptionMapper bizPrescriptionMapper;
    private final BizPrescriptionDetailMapper bizPrescriptionDetailMapper;

    // 批次

    /**
     * 百分比（分母 0 记 0.00），scale 2
     */
    private static BigDecimal ratePercent(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private static String resultText(Integer result) {
        if (result == null) {
            return "未点评";
        }
        // 文案差异：本报表把「合理」显示为「合理处方」，其余沿用枚举 label，兜底走枚举 getText
        return Objects.equals(RxReviewResultEnum.REASONABLE.getCode(), result)
                ? "合理处方" : RxReviewResultEnum.getText(result);
    }

    private static String csv(String v) {
        if (v == null) {
            return "";
        }
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return '"' + v.replace("\"", "\"\"") + '"';
        }
        return v;
    }

    @Override
    public PageResult<RxReviewBatchVO> batchListPage(RxReviewBatchQueryPageDTO query) {
        LambdaQueryWrapper<BizRxReviewBatch> wrapper = new LambdaQueryWrapper<>();
        String keyword = query.getKeyword() == null ? null : query.getKeyword().trim();
        wrapper.and(TextUtil.hasText(keyword), w -> w
                        .like(BizRxReviewBatch::getBatchName, keyword)
                        .or().like(BizRxReviewBatch::getBatchNo, keyword))
                .eq(query.getStatus() != null, BizRxReviewBatch::getStatus, query.getStatus())
                .eq(query.getReviewType() != null, BizRxReviewBatch::getReviewType, query.getReviewType())
                .orderByDesc(BizRxReviewBatch::getId);

        Page<BizRxReviewBatch> page = bizRxReviewBatchMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), Collections.emptyList());
        }
        List<RxReviewBatchVO> vos = page.getRecords().stream().map(this::toBatchVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    // 明细 / 点评

    @Override
    @Transactional
    public RxReviewBatchVO batchUpsert(RxReviewBatchUpsertDTO dto) {
        if (dto.getId() != null) {
            return updateBatch(dto);
        }
        return createBatch(dto);
    }

    private RxReviewBatchVO createBatch(RxReviewBatchUpsertDTO dto) {
        if (dto.getDateStart().isAfter(dto.getDateEnd())) {
            throw new BusinessException("就诊日期起不能晚于就诊日期止");
        }
        boolean special = dto.getReviewType() != null && dto.getReviewType() == RxReviewTypeEnum.SPECIAL.getCode();
        // B 类保留：条件必填——仅专项点评要求专项主题，普通/抽样点评不传
        if (special && !TextUtil.hasText(dto.getSpecialty())) {
            throw new BusinessException("专项点评必须填写专项主题");
        }
        if (dto.getSampleCount() == null || dto.getSampleCount() < 1) {
            throw new BusinessException("抽样数至少 1 张");
        }

        List<BizPrescription> sampled = bizRxReviewBatchMapper.samplePrescriptions(
                dto.getDateStart(), dto.getDateEnd(), dto.getSampleCount());
        if (CollectionUtils.isEmpty(sampled)) {
            throw new BusinessException("该日期范围内没有可点评的处方（已审核/已发药且未被其他批次收录）");
        }

        BizRxReviewBatch batch = new BizRxReviewBatch();
        batch.setBatchNo(nextBatchNo());
        batch.setBatchName(dto.getBatchName().trim());
        batch.setReviewType(dto.getReviewType());
        batch.setSpecialty(TextUtil.hasText(dto.getSpecialty()) ? dto.getSpecialty().trim() : null);
        batch.setDateStart(dto.getDateStart());
        batch.setDateEnd(dto.getDateEnd());
        batch.setSampleCount(sampled.size());
        batch.setReviewedCount(0);
        batch.setStatus(RxReviewBatchStatusEnum.RUNNING.getCode());
        batch.setReviewerId(UserUtils.getCurrentUser().getEmployeeId());
        batch.setReviewerName(UserUtils.getCurrentUser().getRealName());
        batch.setCreateBy(UserUtils.getCurrentUser().getRealName());
        batch.setRemark(TextUtil.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
        bizRxReviewBatchMapper.insert(batch);

        for (BizPrescription p : sampled) {
            bizRxReviewItemMapper.insert(toItemEntity(batch, p));
        }
        return toBatchVO(batch);
    }

    private RxReviewBatchVO updateBatch(RxReviewBatchUpsertDTO dto) {
        BizRxReviewBatch batch = bizRxReviewBatchMapper.selectById(dto.getId());
        if (batch == null) {
            throw new BusinessException("点评批次不存在");
        }
        if (batch.getStatus() == RxReviewBatchStatusEnum.DONE.getCode()) {
            throw new BusinessException("已完成的批次不可修改");
        }
        batch.setBatchName(dto.getBatchName().trim());
        if (TextUtil.hasText(dto.getSpecialty())) {
            batch.setSpecialty(dto.getSpecialty().trim());
        }
        if (TextUtil.hasText(dto.getRemark())) {
            batch.setRemark(dto.getRemark().trim());
        }
        bizRxReviewBatchMapper.updateById(batch);
        return toBatchVO(batch);
    }

    @Override
    public void completeBatch(Long id) {
        BizRxReviewBatch batch = bizRxReviewBatchMapper.selectById(id);
        if (batch == null) {
            throw new BusinessException("点评批次不存在");
        }
        if (batch.getStatus() == RxReviewBatchStatusEnum.DONE.getCode()) {
            return;
        }
        batch.setStatus(RxReviewBatchStatusEnum.DONE.getCode());
        bizRxReviewBatchMapper.updateById(batch);
    }

    @Override
    @Transactional
    public void batchDeleteById(Long id) {
        BizRxReviewBatch batch = bizRxReviewBatchMapper.selectById(id);
        if (batch == null) {
            return;
        }
        Long reviewed = bizRxReviewItemMapper.selectCount(new LambdaQueryWrapper<BizRxReviewItem>()
                .eq(BizRxReviewItem::getBatchId, id)
                .eq(BizRxReviewItem::getReviewStatus, RxReviewItemStatusEnum.DONE.getCode()));
        if (reviewed != null && reviewed > 0) {
            throw new BusinessException("批次下已有 " + reviewed + " 张已点评处方，点评结论是评审台账，禁止删除");
        }
        bizRxReviewItemMapper.purgeByBatchId(id);
        bizRxReviewBatchMapper.deleteById(id);
    }

    // 公示

    @Override
    public PageResult<RxReviewItemVO> itemListPage(RxReviewItemPageDTO query) {
        Page<BizRxReviewItem> page = bizRxReviewItemMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), itemWrapper(query));
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), Collections.emptyList());
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                toItemVOs(page.getRecords()));
    }

    /**
     * 明细查询条件（列表/导出同形）
     */
    private LambdaQueryWrapper<BizRxReviewItem> itemWrapper(RxReviewItemPageDTO query) {
        LambdaQueryWrapper<BizRxReviewItem> wrapper = new LambdaQueryWrapper<>();
        String prescriptionNo = query.getPrescriptionNo() == null ? null : query.getPrescriptionNo().trim();
        String doctorName = query.getDoctorName() == null ? null : query.getDoctorName().trim();
        wrapper.eq(query.getBatchId() != null, BizRxReviewItem::getBatchId, query.getBatchId())
                .like(TextUtil.hasText(prescriptionNo), BizRxReviewItem::getPrescriptionNo, prescriptionNo)
                .like(TextUtil.hasText(doctorName), BizRxReviewItem::getDoctorName, doctorName)
                .eq(query.getReviewStatus() != null, BizRxReviewItem::getReviewStatus, query.getReviewStatus())
                .eq(query.getReviewResult() != null, BizRxReviewItem::getReviewResult, query.getReviewResult())
                .eq(query.getPublicityStatus() != null, BizRxReviewItem::getPublicityStatus, query.getPublicityStatus());
        return wrapper;
    }

    @Override
    @Transactional
    public void itemUpsert(RxReviewItemUpsertDTO dto) {
        BizRxReviewItem item = bizRxReviewItemMapper.selectById(dto.getId());
        if (item == null) {
            throw new BusinessException("点评明细不存在");
        }
        if (item.getPublicityStatus() != null && item.getPublicityStatus() == 1) {
            throw new BusinessException("该点评已公示，公示结论不可修改");
        }

        int result = dto.getReviewResult();
        List<String> codes = dto.getProblemTypes() == null ? List.of()
                : dto.getProblemTypes().stream()
                .filter(TextUtil::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        String opinion = dto.getReviewOpinion() == null ? null : dto.getReviewOpinion().trim();

        if (result == RxReviewResultEnum.REASONABLE.getCode()) {
            codes = List.of();
            opinion = null;
        } else {
            if (codes.isEmpty()) {
                throw new BusinessException("不合理处方（不规范/不适宜/超常）必须勾选问题项");
            }
            Set<String> allowed = RESULT_PROBLEM_CODES.get(result);
            for (String code : codes) {
                if (!allowed.contains(code)) {
                    throw new BusinessException("问题项 " + code + " 与点评结论分组不一致");
                }
            }
            // B 类保留：条件必填——仅结论为不合理处方时才要求点评意见
            if (!TextUtil.hasText(opinion)) {
                throw new BusinessException("不合理处方必须填写点评意见");
            }
        }

        boolean firstReview = item.getReviewStatus() == null
                || item.getReviewStatus() == RxReviewItemStatusEnum.PENDING.getCode();

        item.setReviewStatus(RxReviewItemStatusEnum.DONE.getCode());
        item.setReviewResult(result);
        item.setProblemTypes(codes.isEmpty() ? null : String.join(",", codes));
        item.setReviewOpinion(opinion);
        item.setReviewerId(UserUtils.getCurrentUser().getEmployeeId());
        item.setReviewerName(UserUtils.getCurrentUser().getRealName());
        item.setReviewTime(TimeUtil.nowSeconds());
        bizRxReviewItemMapper.updateById(item);

        if (firstReview) {
            bizRxReviewBatchMapper.increaseReviewed(item.getBatchId());
        }
        autoFinishBatchIfDone(item.getBatchId());
    }

    /**
     * 批次下明细全部点评 → 批次自动置已完成（保留手动关闭入口，抽样数没点完也能归档）
     */
    private void autoFinishBatchIfDone(Long batchId) {
        BizRxReviewBatch batch = bizRxReviewBatchMapper.selectById(batchId);
        if (batch == null || batch.getStatus() == RxReviewBatchStatusEnum.DONE.getCode()) {
            return;
        }
        Long total = bizRxReviewItemMapper.selectCount(new LambdaQueryWrapper<BizRxReviewItem>()
                .eq(BizRxReviewItem::getBatchId, batchId));
        Long reviewed = bizRxReviewItemMapper.selectCount(new LambdaQueryWrapper<BizRxReviewItem>()
                .eq(BizRxReviewItem::getBatchId, batchId)
                .eq(BizRxReviewItem::getReviewStatus, RxReviewItemStatusEnum.DONE.getCode()));
        if (total != null && total > 0 && total.equals(reviewed)) {
            batch.setStatus(RxReviewBatchStatusEnum.DONE.getCode());
            bizRxReviewBatchMapper.updateById(batch);
        }
    }

    @Override
    @Transactional
    public void itemAddByNo(Long batchId, String prescriptionNo) {
        BizRxReviewBatch batch = bizRxReviewBatchMapper.selectById(batchId);
        if (batch == null) {
            throw new BusinessException("点评批次不存在");
        }
        if (batch.getStatus() == RxReviewBatchStatusEnum.DONE.getCode()) {
            throw new BusinessException("已完成的批次不可补录");
        }
        String no = prescriptionNo == null ? "" : prescriptionNo.trim();
        // C 类保留：入参是拆开直传的 String（Controller 解 DTO 后调用），Bean Validation 不经过这一层
        if (!TextUtil.hasText(no)) {
            throw new BusinessException("处方号不能为空");
        }
        BizPrescription p = bizPrescriptionMapper.selectOne(new LambdaQueryWrapper<BizPrescription>()
                .eq(BizPrescription::getPrescriptionNo, no));
        if (p == null) {
            throw new BusinessException("处方 " + no + " 不存在");
        }
        Integer status = p.getPrescriptionStatus();
        if (status == null || (status != 3 && status != 4)) {
            throw new BusinessException("处方 " + no + " 未完成审核/发药，不在点评范围");
        }
        BizRxReviewItem existed = bizRxReviewItemMapper.selectOne(new LambdaQueryWrapper<BizRxReviewItem>()
                .eq(BizRxReviewItem::getPrescriptionId, p.getId())
                .last("LIMIT 1"));
        if (existed != null) {
            throw new BusinessException("处方 " + no + " 已收录于批次 " + existed.getBatchNo() + "，一处方只点评一次");
        }
        bizRxReviewItemMapper.insert(toItemEntity(batch, p));
    }

    @Override
    @Transactional
    public int publicity(RxReviewPublicityDTO dto) {
        List<Long> ids = dto.getItemIds().stream().distinct().toList();
        List<BizRxReviewItem> items = bizRxReviewItemMapper.selectList(new LambdaQueryWrapper<BizRxReviewItem>()
                .in(BizRxReviewItem::getId, ids));
        if (items.size() != ids.size()) {
            throw new BusinessException("存在无效的点评明细");
        }
        String operator = UserUtils.getCurrentUser().getRealName();
        LocalDateTime now = TimeUtil.nowSeconds();
        for (BizRxReviewItem item : items) {
            if (item.getReviewStatus() == null || item.getReviewStatus() != RxReviewItemStatusEnum.DONE.getCode()) {
                throw new BusinessException("处方 " + item.getPrescriptionNo() + " 尚未点评，不能公示");
            }
            if (item.getReviewResult() == null || item.getReviewResult() == RxReviewResultEnum.REASONABLE.getCode()) {
                throw new BusinessException("处方 " + item.getPrescriptionNo() + " 结论为合理处方，无需公示");
            }
            if (item.getPublicityStatus() != null && item.getPublicityStatus() == 1) {
                throw new BusinessException("处方 " + item.getPrescriptionNo() + " 已公示（公示只增不可撤）");
            }
        }
        for (BizRxReviewItem item : items) {
            item.setPublicityStatus(PublicityStatusEnum.PUBLISHED.getCode());
            item.setPublicityBy(operator);
            item.setPublicityTime(now);
            bizRxReviewItemMapper.updateById(item);
        }
        return items.size();
    }

    // 约谈

    @Override
    public PageResult<RxReviewItemVO> publicityListPage(RxReviewItemPageDTO query) {
        query.setPublicityStatus(PublicityStatusEnum.PUBLISHED.getCode());
        Page<BizRxReviewItem> page = bizRxReviewItemMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), itemWrapper(query));
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), Collections.emptyList());
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                toItemVOs(page.getRecords()));
    }

    @Override
    public List<RxPublicityDoctorVO> publicityStats(LocalDate dateStart, LocalDate dateEnd) {
        List<RxPublicityDoctorVO> list = bizRxReviewItemMapper.selectPublicityDoctorStats(dateStart, dateEnd);
        for (RxPublicityDoctorVO vo : list) {
            vo.setNeedTalk(vo.getAbnormalCount() != null && vo.getAbnormalCount() >= NEED_TALK_ABNORMAL_THRESHOLD);
        }
        return list;
    }

    @Override
    public RxReviewStatsVO stats(String month) {
        YearMonth ym;
        try {
            ym = TextUtil.hasText(month) ? YearMonth.parse(month.trim()) : YearMonth.now();
        } catch (Exception e) {
            throw new BusinessException("月份格式应为 yyyy-MM");
        }
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        RxReviewMonthStatVO itemStats = bizRxReviewItemMapper.selectMonthlyStats(start, end);
        long totalPrescriptions = bizRxReviewItemMapper.countPrescriptions(start, end);

        long reviewed = NumUtil.orZero(itemStats.getReviewed());
        long unreasonable = NumUtil.orZero(itemStats.getUnreasonable());
        long abnormal = NumUtil.orZero(itemStats.getAbnormal());
        long publicized = NumUtil.orZero(itemStats.getPublicized());
        long pending = NumUtil.orZero(itemStats.getPending());

        RxReviewStatsVO vo = new RxReviewStatsVO();
        vo.setMonth(ym.toString());
        vo.setTotalPrescriptions(totalPrescriptions);
        vo.setReviewedCount(reviewed);
        vo.setReviewRate(ratePercent(reviewed, totalPrescriptions));
        vo.setUnreasonableCount(unreasonable);
        vo.setUnreasonableRate(ratePercent(unreasonable, reviewed));
        vo.setAbnormalCount(abnormal);
        vo.setPublicityCount(publicized);
        vo.setPendingCount(pending);
        return vo;
    }

    @Override
    public PageResult<RxReviewTalkVO> talkListPage(RxReviewTalkQueryPageDTO query) {
        LambdaQueryWrapper<BizRxDoctorTalk> wrapper = new LambdaQueryWrapper<>();
        String keyword = query.getKeyword() == null ? null : query.getKeyword().trim();
        wrapper.and(TextUtil.hasText(keyword), w -> w
                        .like(BizRxDoctorTalk::getDoctorName, keyword)
                        .or().like(BizRxDoctorTalk::getTalkNo, keyword))
                .eq(query.getTalkType() != null, BizRxDoctorTalk::getTalkType, query.getTalkType())
                .eq(query.getRectifyStatus() != null, BizRxDoctorTalk::getRectifyStatus, query.getRectifyStatus())
                .ge(query.getDateStart() != null, BizRxDoctorTalk::getTalkTime,
                        TimeUtil.dayStart(query.getDateStart()))
                .le(query.getDateEnd() != null, BizRxDoctorTalk::getTalkTime,
                        TimeUtil.dayEnd(query.getDateEnd()))
                .orderByDesc(BizRxDoctorTalk::getTalkTime)
                .orderByDesc(BizRxDoctorTalk::getId);

        Page<BizRxDoctorTalk> page = bizRxDoctorTalkMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), Collections.emptyList());
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                toTalkVOs(page.getRecords()));
    }

    @Override
    @Transactional
    public RxReviewTalkVO talkUpsert(RxReviewTalkUpsertDTO dto) {
        RelatedContext ctx = resolveRelatedItems(dto.getRelatedReviewIds(), dto.getDoctorId(), dto.getDoctorName());

        BizRxDoctorTalk talk;
        if (dto.getId() == null) {
            talk = new BizRxDoctorTalk();
            talk.setTalkNo(nextTalkNo());
            talk.setCreateBy(UserUtils.getCurrentUser().getRealName());
            talk.setDoctorConfirm(YesOrNoEnum.NO.getCode());
            talk.setRectifyStatus(dto.getRectifyStatus() == null
                    ? RectifyStatusEnum.PENDING.getCode() : dto.getRectifyStatus());
        } else {
            talk = bizRxDoctorTalkMapper.selectById(dto.getId());
            if (talk == null) {
                throw new BusinessException("约谈记录不存在");
            }
            if (talk.getDoctorConfirm() != null && talk.getDoctorConfirm() == 1) {
                throw new BusinessException("医师已确认签字的约谈记录不可修改");
            }
            talk.setRectifyStatus(dto.getRectifyStatus() == null
                    ? talk.getRectifyStatus() : dto.getRectifyStatus());
        }

        talk.setDoctorId(ctx.doctorId());
        talk.setDoctorName(dto.getDoctorName().trim());
        talk.setDeptName(TextUtil.hasText(dto.getDeptName()) ? dto.getDeptName().trim() : ctx.deptName());
        talk.setTalkType(dto.getTalkType());
        talk.setTalkTime(dto.getTalkTime());
        talk.setTalkerName(TextUtil.hasText(dto.getTalkerName())
                ? dto.getTalkerName().trim() : UserUtils.getCurrentUser().getRealName());
        talk.setTalkerOrg(TextUtil.hasText(dto.getTalkerOrg()) ? dto.getTalkerOrg().trim() : null);
        talk.setRelatedCount(ctx.items().size());
        talk.setRelatedReviewIds(ctx.idsText());
        talk.setProblemSummary(TextUtil.cut(dto.getProblemSummary(), 500));
        talk.setTalkContent(TextUtil.cut(dto.getTalkContent(), 1000));
        talk.setRectifyRequire(TextUtil.cut(dto.getRectifyRequire(), 500));
        talk.setRectifyRemark(TextUtil.cut(dto.getRectifyRemark(), 500));
        talk.setRemark(TextUtil.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);

        if (dto.getId() == null) {
            bizRxDoctorTalkMapper.insert(talk);
        } else {
            bizRxDoctorTalkMapper.updateById(talk);
        }
        return toTalkVO(talk);
    }

    /**
     * 约谈依据（关联点评明细）校验：必须存在、均为不合理处方、且同属入参医师
     */
    private RelatedContext resolveRelatedItems(List<Long> relatedIds, Long doctorId, String doctorName) {
        List<Long> ids = relatedIds == null ? List.of()
                : relatedIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return new RelatedContext(doctorId, null, List.of(), null);
        }
        List<BizRxReviewItem> items = bizRxReviewItemMapper.selectList(new LambdaQueryWrapper<BizRxReviewItem>()
                .in(BizRxReviewItem::getId, ids));
        if (items.size() != ids.size()) {
            throw new BusinessException("存在无效的点评明细引用");
        }
        Long ctxDoctorId = null;
        String ctxDeptName = null;
        for (BizRxReviewItem item : items) {
            if (item.getReviewResult() == null || item.getReviewResult() == RxReviewResultEnum.REASONABLE.getCode()) {
                throw new BusinessException("约谈依据必须是不合理处方（处方 " + item.getPrescriptionNo() + " 不是）");
            }
            if (ctxDoctorId == null) {
                ctxDoctorId = item.getDoctorId();
                ctxDeptName = item.getDeptName();
            } else if (!ctxDoctorId.equals(item.getDoctorId())) {
                throw new BusinessException("关联的点评明细必须同属一位医师");
            }
        }
        if (doctorId != null && !doctorId.equals(ctxDoctorId)) {
            throw new BusinessException("被约谈医师与关联点评明细的开方医师不一致");
        }
        if (TextUtil.hasText(doctorName) && items.get(0).getDoctorName() != null
                && !doctorName.trim().equals(items.get(0).getDoctorName())) {
            throw new BusinessException("被约谈医师姓名与关联点评明细的开方医师不一致");
        }
        return new RelatedContext(ctxDoctorId, ctxDeptName, items,
                ids.stream().map(String::valueOf).collect(Collectors.joining(",")));
    }

    // 导出

    @Override
    public void talkConfirm(Long id, String confirmBy) {
        // C 类保留：入参是拆开直传的 String（Controller 解 DTO 后调用），Bean Validation 不经过这一层
        if (!TextUtil.hasText(confirmBy)) {
            throw new BusinessException("请填写医师确认人（签字）");
        }
        BizRxDoctorTalk talk = bizRxDoctorTalkMapper.selectById(id);
        if (talk == null) {
            throw new BusinessException("约谈记录不存在");
        }
        if (talk.getDoctorConfirm() != null && talk.getDoctorConfirm() == 1) {
            return;
        }
        talk.setDoctorConfirm(YesOrNoEnum.YES.getCode());
        talk.setDoctorConfirmBy(confirmBy.trim());
        talk.setDoctorConfirmTime(TimeUtil.nowSeconds());
        bizRxDoctorTalkMapper.updateById(talk);
    }

    @Override
    public void talkDeleteById(Long id) {
        BizRxDoctorTalk talk = bizRxDoctorTalkMapper.selectById(id);
        if (talk == null) {
            return;
        }
        if (talk.getDoctorConfirm() != null && talk.getDoctorConfirm() == 1) {
            throw new BusinessException("医师已确认签字的约谈记录不可删除");
        }
        bizRxDoctorTalkMapper.deleteById(id);
    }

    @Override
    public String itemExportCsv(RxReviewItemPageDTO query) {
        query.forExport(EXPORT_MAX);
        List<RxReviewItemVO> rows = itemListPage(query).getRecords();
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("批次号,处方号,患者,科室,医生,就诊日期,诊断,药品种数,金额,点评结论,问题码,点评意见,点评人,点评时间,公示状态,公示时间\n");
        for (RxReviewItemVO r : rows) {
            sb.append(csv(r.getBatchNo())).append(',')
                    .append(csv(r.getPrescriptionNo())).append(',')
                    .append(csv(r.getPatientName())).append(',')
                    .append(csv(r.getDeptName())).append(',')
                    .append(csv(r.getDoctorName())).append(',')
                    .append(r.getVisitDate() == null ? "" : r.getVisitDate()).append(',')
                    .append(csv(r.getDiagnosis())).append(',')
                    .append(r.getDrugCount() == null ? "" : r.getDrugCount()).append(',')
                    .append(r.getTotalAmount() == null ? "" : r.getTotalAmount()).append(',')
                    .append(csv(resultText(r.getReviewResult()))).append(',')
                    .append(csv(r.getProblemTypes())).append(',')
                    .append(csv(r.getReviewOpinion())).append(',')
                    .append(csv(r.getReviewerName())).append(',')
                    .append(r.getReviewTime() == null ? "" : r.getReviewTime().format(DateFormats.DATETIME)).append(',')
                    .append(r.getPublicityStatus() != null && r.getPublicityStatus() == 1 ? "已公示" : "未公示").append(',')
                    .append(r.getPublicityTime() == null ? "" : r.getPublicityTime().format(DateFormats.DATETIME)).append('\n');
        }
        return sb.toString();
    }

    private String nextBatchNo() {
        String day = LocalDate.now().format(DateFormats.COMPACT_DATE);
        String max = bizRxReviewBatchMapper.selectMaxBatchNo(day);
        int seq = max == null ? 0 : Integer.parseInt(max.substring(max.length() - 4));
        return "RXRB" + day + String.format("%04d", seq + 1);
    }

    // 组装

    private String nextTalkNo() {
        String day = LocalDate.now().format(DateFormats.COMPACT_DATE);
        String max = bizRxDoctorTalkMapper.selectMaxTalkNo(day);
        int seq = max == null ? 0 : Integer.parseInt(max.substring(max.length() - 4));
        return "YT" + day + String.format("%04d", seq + 1);
    }

    /**
     * 抽样/补录共用：处方 → 点照明细（快照冻结）
     */
    private BizRxReviewItem toItemEntity(BizRxReviewBatch batch, BizPrescription p) {
        BizRxReviewItem item = new BizRxReviewItem();
        item.setBatchId(batch.getId());
        item.setBatchNo(batch.getBatchNo());
        item.setPrescriptionId(p.getId());
        item.setPrescriptionNo(p.getPrescriptionNo());
        item.setPatientName(p.getPatientName());
        item.setDeptName(p.getDeptName());
        item.setDoctorId(p.getDoctorId());
        item.setDoctorName(p.getDoctorName());
        item.setVisitDate(p.getVisitDate());
        item.setDiagnosis(TextUtil.cut(p.getDiagnosis(), 500));
        item.setDrugCount(p.getDrugCount());
        item.setTotalAmount(p.getTotalAmount());
        item.setPrescriptionType(p.getPrescriptionType());
        item.setPrescriptionSource(p.getPrescriptionSource());
        item.setReviewStatus(RxReviewItemStatusEnum.PENDING.getCode());
        item.setPublicityStatus(PublicityStatusEnum.NOT_PUBLISHED.getCode());
        return item;
    }

    private RxReviewBatchVO toBatchVO(BizRxReviewBatch b) {
        RxReviewBatchVO vo = new RxReviewBatchVO();
        vo.setId(b.getId());
        vo.setBatchNo(b.getBatchNo());
        vo.setBatchName(b.getBatchName());
        vo.setReviewType(b.getReviewType());
        vo.setSpecialty(b.getSpecialty());
        vo.setDateStart(b.getDateStart());
        vo.setDateEnd(b.getDateEnd());
        vo.setSampleCount(b.getSampleCount());
        vo.setReviewedCount(b.getReviewedCount());
        vo.setStatus(b.getStatus());
        vo.setReviewerId(b.getReviewerId());
        vo.setReviewerName(b.getReviewerName());
        vo.setCreateTime(b.getCreateTime());
        vo.setRemark(b.getRemark());
        return vo;
    }

    /**
     * 明细 VO + 处方药品明细文本（一次 IN 查全，内存分组，避免 N+1）
     */
    private List<RxReviewItemVO> toItemVOs(List<BizRxReviewItem> records) {
        List<Long> prescriptionIds = records.stream()
                .map(BizRxReviewItem::getPrescriptionId).distinct().toList();
        Map<Long, List<BizPrescriptionDetail>> detailMap = bizPrescriptionDetailMapper.selectList(
                        new LambdaQueryWrapper<BizPrescriptionDetail>()
                                .in(BizPrescriptionDetail::getPrescriptionId, prescriptionIds)
                                .orderByAsc(BizPrescriptionDetail::getId))
                .stream()
                .collect(Collectors.groupingBy(BizPrescriptionDetail::getPrescriptionId, LinkedHashMap::new, Collectors.toList()));

        List<RxReviewItemVO> vos = new ArrayList<>(records.size());
        for (BizRxReviewItem item : records) {
            RxReviewItemVO vo = new RxReviewItemVO();
            vo.setId(item.getId());
            vo.setBatchId(item.getBatchId());
            vo.setBatchNo(item.getBatchNo());
            vo.setPrescriptionId(item.getPrescriptionId());
            vo.setPrescriptionNo(item.getPrescriptionNo());
            vo.setPatientName(item.getPatientName());
            vo.setDeptName(item.getDeptName());
            vo.setDoctorId(item.getDoctorId());
            vo.setDoctorName(item.getDoctorName());
            vo.setVisitDate(item.getVisitDate());
            vo.setDiagnosis(item.getDiagnosis());
            vo.setDrugCount(item.getDrugCount());
            vo.setTotalAmount(item.getTotalAmount());
            vo.setPrescriptionType(item.getPrescriptionType());
            vo.setPrescriptionSource(item.getPrescriptionSource());
            vo.setReviewStatus(item.getReviewStatus());
            vo.setReviewResult(item.getReviewResult());
            vo.setProblemTypes(item.getProblemTypes());
            vo.setReviewOpinion(item.getReviewOpinion());
            vo.setReviewerName(item.getReviewerName());
            vo.setReviewTime(item.getReviewTime());
            vo.setPublicityStatus(item.getPublicityStatus());
            vo.setPublicityBy(item.getPublicityBy());
            vo.setPublicityTime(item.getPublicityTime());
            vo.setCreateTime(item.getCreateTime());

            List<BizPrescriptionDetail> details = detailMap.get(item.getPrescriptionId());
            List<String> texts = new ArrayList<>();
            if (!CollectionUtils.isEmpty(details)) {
                for (BizPrescriptionDetail d : details) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(d.getDrugName());
                    if (TextUtil.hasText(d.getSpecification())) {
                        sb.append(' ').append(d.getSpecification());
                    }
                    sb.append('，').append(d.getQuantity().stripTrailingZeros().toPlainString()).append(d.getUnit());
                    if (TextUtil.hasText(d.getRoute()) || TextUtil.hasText(d.getUsageDosage())) {
                        sb.append('，');
                        if (TextUtil.hasText(d.getUsageDosage())) {
                            sb.append(d.getUsageDosage());
                        }
                        if (TextUtil.hasText(d.getFrequency())) {
                            if (TextUtil.hasText(d.getUsageDosage())) {
                                sb.append(' ');
                            }
                            sb.append(d.getFrequency());
                        }
                    }
                    texts.add(sb.toString());
                }
            }
            vo.setDrugDetails(texts);
            vos.add(vo);
        }
        return vos;
    }

    private List<RxReviewTalkVO> toTalkVOs(List<BizRxDoctorTalk> records) {
        // 关联明细结构化回带：一次 IN 查全，按 talk 聚合
        List<Long> allIds = records.stream()
                .filter(t -> TextUtil.hasText(t.getRelatedReviewIds()))
                .flatMap(t -> java.util.Arrays.stream(t.getRelatedReviewIds().split(",")))
                .map(Long::valueOf)
                .distinct()
                .toList();
        Map<Long, RxReviewItemVO> itemMap = allIds.isEmpty() ? Map.of()
                : toItemVOs(bizRxReviewItemMapper.selectList(new LambdaQueryWrapper<BizRxReviewItem>()
                .in(BizRxReviewItem::getId, allIds))).stream()
                .collect(Collectors.toMap(RxReviewItemVO::getId, v -> v, (a, b) -> a));

        List<RxReviewTalkVO> vos = new ArrayList<>(records.size());
        for (BizRxDoctorTalk t : records) {
            RxReviewTalkVO vo = toTalkVO(t);
            if (TextUtil.hasText(t.getRelatedReviewIds())) {
                List<RxReviewItemVO> related = new ArrayList<>();
                for (String idStr : t.getRelatedReviewIds().split(",")) {
                    RxReviewItemVO iv = itemMap.get(Long.valueOf(idStr.trim()));
                    if (iv != null) {
                        related.add(iv);
                    }
                }
                vo.setRelatedItems(related);
            }
            vos.add(vo);
        }
        return vos;
    }

    private RxReviewTalkVO toTalkVO(BizRxDoctorTalk t) {
        RxReviewTalkVO vo = new RxReviewTalkVO();
        vo.setId(t.getId());
        vo.setTalkNo(t.getTalkNo());
        vo.setDoctorId(t.getDoctorId());
        vo.setDoctorName(t.getDoctorName());
        vo.setDeptName(t.getDeptName());
        vo.setTalkType(t.getTalkType());
        vo.setTalkTime(t.getTalkTime());
        vo.setTalkerName(t.getTalkerName());
        vo.setTalkerOrg(t.getTalkerOrg());
        vo.setRelatedCount(t.getRelatedCount());
        vo.setRelatedReviewIds(t.getRelatedReviewIds());
        vo.setProblemSummary(t.getProblemSummary());
        vo.setTalkContent(t.getTalkContent());
        vo.setRectifyRequire(t.getRectifyRequire());
        vo.setRectifyStatus(t.getRectifyStatus());
        vo.setRectifyRemark(t.getRectifyRemark());
        vo.setDoctorConfirm(t.getDoctorConfirm());
        vo.setDoctorConfirmBy(t.getDoctorConfirmBy());
        vo.setDoctorConfirmTime(t.getDoctorConfirmTime());
        vo.setCreateTime(t.getCreateTime());
        vo.setRemark(t.getRemark());
        return vo;
    }

    /**
     * 关联明细上下文（doctorId / deptName / 明细 / id 文本）
     */
    private record RelatedContext(Long doctorId, String deptName,
                                  List<BizRxReviewItem> items, String idsText) {
    }
}
