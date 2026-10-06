package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.service.RedisSequenceService;
import com.his.common.exception.BusinessException;
import com.his.pharmacy.dto.PivasActionDTO;
import com.his.pharmacy.dto.PivasAuditDTO;
import com.his.pharmacy.dto.PivasGenerateDTO;
import com.his.pharmacy.dto.PivasQueryPageDTO;
import com.his.pharmacy.entity.BizPivasBatch;
import com.his.pharmacy.entity.BizPivasItem;
import com.his.pharmacy.mapper.BizPivasBatchMapper;
import com.his.pharmacy.mapper.BizPivasItemMapper;
import com.his.pharmacy.service.PivasService;
import com.his.pharmacy.vo.PivasCandidateVO;
import com.his.pharmacy.vo.PivasStatsVO;
import com.his.pharmacy.vo.PivasVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 静配中心（PIVAS）服务实现。
 *
 * <p>链路口径：
 * <ol>
 *   <li><b>生成</b>：主单粒度「一次住院 × 一天」，复用主单、明细追加（同住院摆药）；
 *       候选 = 摆药口径 ∩ route 静脉关键词；新明细一律 1-待审方。
 *       审方退回（0）不挡重进候选，重排入 seq+1、旧行留痕不动。</li>
 *   <li><b>审方</b>：药师逐条 1→2；退回 1→0 原因必填（截到列宽，绝不裸拼长文本写库）。</li>
 *   <li><b>打标签排队</b>：整单一次性把已审方(2)明细推 3 并取排队号
 *       （调配日内全局递增，中心叫号口径）；标签打印预留——只取号盖时间，不接打印机。</li>
 *   <li><b>调配 / 核对发放</b>：3→4→5 顺序推进、不可跳级不可逆；操作人服务端取当前登录人。</li>
 *   <li>主单 status 与 item_count 是聚合派生值，每次明细动作后实时回算。
 *       计费不在本链——静脉药品费用仍走住院摆药链，避免双计。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PivasServiceImpl implements PivasService {

    private static final int REJECT_REASON_MAX = 200;

    private final BizPivasBatchMapper batchMapper;
    private final BizPivasItemMapper itemMapper;
    private final RedisSequenceService sequenceService;

    @Override
    public List<PivasCandidateVO> candidates(Long wardId, Long admissionId, LocalDate admixDate) {
        // C 类：本方法还被 generate 直调（非 web 入口），形参上的注解不会执行
        if (wardId == null) {
            throw new BusinessException("病区不能为空");
        }
        LocalDate day = admixDate != null ? admixDate : LocalDate.now();
        return itemMapper.selectCandidates(wardId, day, dayStart(day), dayEnd(day), admissionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PivasVO generate(PivasGenerateDTO dto) {
        LocalDate day = dto.getAdmixDate() != null ? dto.getAdmixDate() : LocalDate.now();
        if (day.isAfter(LocalDate.now())) {
            throw new BusinessException("调配日期不能是未来日期");
        }
        List<PivasCandidateVO> cands = candidates(dto.getWardId(), dto.getAdmissionId(), day);
        long unmatched = itemMapper.countUnmatchedCandidates(dto.getWardId(), day,
                dayStart(day), dayEnd(day), dto.getAdmissionId());

        // 幂等回退：重跑同一 generate 时（候选已入单被排除），返回既有主单而不是报错
        if (cands.isEmpty()) {
            BizPivasBatch existing = findExistingBatch(dto, day);
            if (existing != null) {
                applyAggregatedStatus(existing.getId());
                PivasVO vo = getDetailById(existing.getId());
                vo.setRemark("当日无可静配医嘱（候选已全部在单），返回既有静配单");
                return vo;
            }
            throw new BusinessException("没有可入静配单的静脉用药医嘱"
                    + (unmatched > 0 ? "（另有 " + unmatched + " 条静脉药未能匹配药品档案，已跳过）" : ""));
        }

        String operator = currentOperator();
        BizPivasBatch batch = null;
        for (PivasCandidateVO c : cands) {
            if (batch == null || !Objects.equals(batch.getAdmissionId(), c.getAdmissionId())) {
                // 主单粒度「一次住院 × 一天」：换患者就换单；同入院同日复用、明细追加
                batch = findOrCreateBatch(c, day, operator);
            }
            BigDecimal amount = c.getQuantity() != null && c.getPrice() != null
                    ? c.getQuantity().multiply(c.getPrice()).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BizPivasItem item = new BizPivasItem();
            item.setPivasId(batch.getId());
            item.setPivasNo(batch.getPivasNo());
            item.setAdmixDate(day);
            // 重排入序号：同医嘱同日含已拒配明细取 max+1（拒配不占坑，旧行留痕不动）；
            // 并发同 seq 撞唯一键 uk_pivas_order_date_seq，catch 跳过即可
            item.setPivasSeq(itemMapper.nextPivasSeq(c.getOrderId(), day));
            item.setOrderId(c.getOrderId());
            item.setOrderNo(c.getOrderNo());
            item.setAdmissionId(c.getAdmissionId());
            item.setPatientId(c.getPatientId());
            item.setPatientNo(c.getPatientNo());
            item.setPatientName(c.getPatientName());
            item.setWardId(c.getWardId());
            item.setDrugId(c.getDrugId());
            item.setDrugName(c.getDrugName());
            item.setItemCode(c.getItemCode());
            item.setItemName(c.getItemName());
            item.setSpec(c.getSpec());
            item.setUnit(c.getUnit());
            item.setQuantity(c.getQuantity() != null ? c.getQuantity() : BigDecimal.ONE);
            item.setPrice(c.getPrice() != null ? c.getPrice() : BigDecimal.ZERO);
            item.setAmount(amount);
            item.setRoute(c.getRoute());
            item.setFrequency(c.getFrequency());
            item.setStatus(BizPivasItem.STATUS_PENDING_AUDIT);
            try {
                itemMapper.insert(item);
            } catch (DuplicateKeyException e) {
                // 并发生成撞唯一索引 (order_id, admix_date, pivas_seq)：跳过即可
                log.info("静配明细已存在（医嘱 {} 调配日 {}），跳过重复生成", c.getOrderNo(), day);
            }
        }
        if (batch == null) {
            throw new BusinessException("没有可入静配单的静脉用药医嘱");
        }
        applyAggregatedStatus(batch.getId());
        PivasVO vo = getDetailById(batch.getId());
        if (unmatched > 0) {
            vo.setRemark("另有 " + unmatched + " 条静脉药未能匹配药品档案，未进静配单");
        }
        return vo;
    }

    @Override
    public PageResult<PivasVO> listPage(PivasQueryPageDTO dto) {
        Page<PivasVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        // mapper 返回 List 时结果只在返回值里，page.getRecords() 不会被 MP 回填
        List<PivasVO> records = batchMapper.selectBatchPage(page, dto.getWardId(), dto.getAdmixDate(),
                dto.getPatientName(), dto.getStatus());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public PivasVO getDetailById(Long id) {
        PivasVO vo = batchMapper.selectBatchById(id);
        if (vo == null) {
            throw new BusinessException("静配单不存在或已删除");
        }
        vo.setItems(itemMapper.selectItemsByBatchId(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PivasVO auditItem(PivasAuditDTO dto) {
        BizPivasItem item = requireItem(dto.getItemId());
        if (item.getStatus() == null || item.getStatus() != BizPivasItem.STATUS_PENDING_AUDIT) {
            throw new BusinessException("仅待审方明细允许审方（当前状态码 " + item.getStatus() + "）");
        }
        boolean pass = Boolean.TRUE.equals(dto.getPass());
        // B 类：仅退回（pass=false）才必填，条件必填不能下沉成 @NotBlank
        if (!pass && !StringUtils.hasText(dto.getReason())) {
            throw new BusinessException("审方退回必须填写原因");
        }
        item.setAuditorId(UserUtils.getCurrentUser().getEmployeeId());
        item.setAuditorName(currentOperator());
        item.setAuditTime(now());
        if (pass) {
            item.setStatus(BizPivasItem.STATUS_AUDITED);
        } else {
            item.setStatus(BizPivasItem.STATUS_REJECTED);
            // 原因截到列宽：超长会把"审方失败"升级成 Data too long 的 500
            item.setRejectReason(cut(dto.getReason().trim(), REJECT_REASON_MAX));
        }
        if (itemMapper.updateById(item) <= 0) {
            throw new BusinessException("审方更新失败");
        }
        applyAggregatedStatus(item.getPivasId());
        return getDetailById(item.getPivasId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PivasVO labelBatch(PivasActionDTO dto) {
        // B 类：入参 DTO 被调配/核对入口复用，那两个入口只传 itemId，batchId 加 @NotNull 会挡死它们
        if (dto.getBatchId() == null) {
            throw new BusinessException("静配单ID不能为空");
        }
        BizPivasBatch batch = requireBatch(dto.getBatchId());
        List<BizPivasItem> items = itemMapper.selectList(new LambdaQueryWrapper<BizPivasItem>()
                .eq(BizPivasItem::getPivasId, batch.getId())
                .orderByAsc(BizPivasItem::getId));
        boolean anyPendingAudit = items.stream()
                .anyMatch(i -> Objects.equals(i.getStatus(), BizPivasItem.STATUS_PENDING_AUDIT));
        if (anyPendingAudit) {
            throw new BusinessException("仍有待审方明细，请先完成全部审方再打标签排队");
        }
        List<BizPivasItem> audited = items.stream()
                .filter(i -> Objects.equals(i.getStatus(), BizPivasItem.STATUS_AUDITED))
                .toList();
        if (audited.isEmpty()) {
            throw new BusinessException("没有已审方待排队的明细");
        }
        int queue = itemMapper.maxQueueNo(batch.getAdmixDate());
        for (BizPivasItem item : audited) {
            item.setStatus(BizPivasItem.STATUS_QUEUED);
            // 排队号 = 调配日内全局递增（中心叫号口径，跨病区连续）
            item.setQueueNo(++queue);
            if (itemMapper.updateById(item) <= 0) {
                throw new BusinessException("排队取号更新失败");
            }
        }
        // 标签打印预留：这里只落操作人与时间，真实对接时在同一事务尾部出打印任务
        batch.setLabelBy(currentOperator());
        batch.setLabelTime(now());
        if (batchMapper.updateById(batch) <= 0) {
            throw new BusinessException("打标签更新主单失败");
        }
        applyAggregatedStatus(batch.getId());
        return getDetailById(batch.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PivasVO compoundItem(PivasActionDTO dto) {
        BizPivasItem item = requireItem(dto.getItemId());
        if (item.getStatus() == null || item.getStatus() != BizPivasItem.STATUS_QUEUED) {
            throw new BusinessException("仅已排队明细允许调配（当前状态码 " + item.getStatus() + "）");
        }
        item.setStatus(BizPivasItem.STATUS_COMPOUNDED);
        item.setCompounderId(UserUtils.getCurrentUser().getEmployeeId());
        item.setCompounderName(currentOperator());
        item.setCompoundTime(now());
        if (StringUtils.hasText(dto.getRemark())) {
            item.setRemark(dto.getRemark());
        }
        if (itemMapper.updateById(item) <= 0) {
            throw new BusinessException("调配更新失败");
        }
        applyAggregatedStatus(item.getPivasId());
        return getDetailById(item.getPivasId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PivasVO verifyItem(PivasActionDTO dto) {
        BizPivasItem item = requireItem(dto.getItemId());
        if (item.getStatus() == null || item.getStatus() != BizPivasItem.STATUS_COMPOUNDED) {
            throw new BusinessException("仅已调配明细允许核对发放（当前状态码 " + item.getStatus() + "）");
        }
        item.setStatus(BizPivasItem.STATUS_VERIFIED);
        item.setVerifierId(UserUtils.getCurrentUser().getEmployeeId());
        item.setVerifierName(currentOperator());
        item.setVerifyTime(now());
        if (StringUtils.hasText(dto.getRemark())) {
            item.setRemark(dto.getRemark());
        }
        if (itemMapper.updateById(item) <= 0) {
            throw new BusinessException("核对发放更新失败");
        }
        applyAggregatedStatus(item.getPivasId());
        return getDetailById(item.getPivasId());
    }

    @Override
    public PivasStatsVO stats(LocalDate admixDate, Long wardId) {
        LocalDate day = admixDate != null ? admixDate : LocalDate.now();
        PivasStatsVO vo = new PivasStatsVO();
        vo.setPendingAudit(countItems(day, wardId, BizPivasItem.STATUS_PENDING_AUDIT));
        vo.setAudited(countItems(day, wardId, BizPivasItem.STATUS_AUDITED));
        vo.setQueued(countItems(day, wardId, BizPivasItem.STATUS_QUEUED));
        vo.setCompounded(countItems(day, wardId, BizPivasItem.STATUS_COMPOUNDED));
        vo.setVerified(countItems(day, wardId, BizPivasItem.STATUS_VERIFIED));
        vo.setRejected(countItems(day, wardId, BizPivasItem.STATUS_REJECTED));
        vo.setBatchCount(batchMapper.selectCount(new LambdaQueryWrapper<BizPivasBatch>()
                .eq(BizPivasBatch::getAdmixDate, day)
                .eq(wardId != null, BizPivasBatch::getWardId, wardId)));
        return vo;
    }

    // 内部

    private long countItems(LocalDate day, Long wardId, int status) {
        return itemMapper.selectCount(new LambdaQueryWrapper<BizPivasItem>()
                .eq(BizPivasItem::getAdmixDate, day)
                .eq(wardId != null, BizPivasItem::getWardId, wardId)
                .eq(BizPivasItem::getStatus, status));
    }

    /**
     * 幂等回退用：按 generate 请求的 scope（传了 admissionId 按住院，否则按病区）找当日既有主单
     */
    private BizPivasBatch findExistingBatch(PivasGenerateDTO dto, LocalDate day) {
        LambdaQueryWrapper<BizPivasBatch> w = new LambdaQueryWrapper<BizPivasBatch>()
                .eq(BizPivasBatch::getAdmixDate, day);
        if (dto.getAdmissionId() != null) {
            w.eq(BizPivasBatch::getAdmissionId, dto.getAdmissionId());
        } else {
            w.eq(BizPivasBatch::getWardId, dto.getWardId());
        }
        return batchMapper.selectOne(w.orderByDesc(BizPivasBatch::getId).last("LIMIT 1"));
    }

    /**
     * 同入院同日找主单，没有才建一张（单号 PV+yyyyMMdd+4位）。
     * 并发窗口由明细唯一索引 (order_id, admix_date, pivas_seq) 兜底。
     */
    private BizPivasBatch findOrCreateBatch(PivasCandidateVO first, LocalDate day, String operator) {
        BizPivasBatch existing = batchMapper.selectOne(new LambdaQueryWrapper<BizPivasBatch>()
                .eq(BizPivasBatch::getAdmissionId, first.getAdmissionId())
                .eq(BizPivasBatch::getAdmixDate, day)
                .orderByDesc(BizPivasBatch::getId)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }
        BizPivasBatch b = new BizPivasBatch();
        b.setPivasNo(sequenceService.generatePivasNo());
        b.setAdmixDate(day);
        b.setAdmissionId(first.getAdmissionId());
        b.setPatientId(first.getPatientId());
        b.setPatientNo(first.getPatientNo());
        b.setPatientName(first.getPatientName());
        b.setWardId(first.getWardId());
        b.setWardName(batchMapper.selectWardName(first.getWardId()));
        b.setStatus(BizPivasBatch.STATUS_PENDING_AUDIT);
        b.setItemCount(0);
        b.setGenerateBy(operator);
        b.setGenerateTime(now());
        batchMapper.insert(b);
        return b;
    }

    /**
     * 主单状态聚合回算：明细状态是事实源，主单 status/item_count 只是给人看的派生值。
     * 规则（按推进度取"最落后档"）：无有效明细(全 0)→6 全拒配；有 1→1 待审方；
     * 有 2→2 待排队；有 3→3 待调配；有 4→4 待核对；否则→5 已完成。
     */
    private void applyAggregatedStatus(Long batchId) {
        List<BizPivasItem> items = itemMapper.selectList(new LambdaQueryWrapper<BizPivasItem>()
                .eq(BizPivasItem::getPivasId, batchId));
        if (items.isEmpty()) {
            return;
        }
        Map<Integer, Long> byStatus = items.stream()
                .collect(Collectors.groupingBy(BizPivasItem::getStatus, Collectors.counting()));
        int agg;
        if (byStatus.size() == 1 && byStatus.containsKey(BizPivasItem.STATUS_REJECTED)) {
            agg = BizPivasBatch.STATUS_ALL_REJECTED;
        } else if (byStatus.containsKey(BizPivasItem.STATUS_PENDING_AUDIT)) {
            agg = BizPivasBatch.STATUS_PENDING_AUDIT;
        } else if (byStatus.containsKey(BizPivasItem.STATUS_AUDITED)) {
            agg = BizPivasBatch.STATUS_PENDING_QUEUE;
        } else if (byStatus.containsKey(BizPivasItem.STATUS_QUEUED)) {
            agg = BizPivasBatch.STATUS_PENDING_COMPOUND;
        } else if (byStatus.containsKey(BizPivasItem.STATUS_COMPOUNDED)) {
            agg = BizPivasBatch.STATUS_PENDING_VERIFY;
        } else {
            agg = BizPivasBatch.STATUS_DONE;
        }
        BizPivasBatch b = batchMapper.selectById(batchId);
        if (b != null && (!Objects.equals(b.getStatus(), agg) || !Objects.equals(b.getItemCount(), items.size()))) {
            b.setStatus(agg);
            b.setItemCount(items.size());
            batchMapper.updateById(b);
        }
    }

    private BizPivasItem requireItem(Long itemId) {
        BizPivasItem item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("静配明细不存在或已删除");
        }
        return item;
    }

    private BizPivasBatch requireBatch(Long batchId) {
        BizPivasBatch batch = batchMapper.selectById(batchId);
        if (batch == null) {
            throw new BusinessException("静配单不存在或已删除");
        }
        return batch;
    }

    private static String cut(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max);
    }

    private static LocalDateTime dayStart(LocalDate day) {
        return day.atStartOfDay();
    }

    private static LocalDateTime dayEnd(LocalDate day) {
        return day.atTime(LocalTime.MAX).truncatedTo(ChronoUnit.SECONDS);
    }

    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private String currentOperator() {
        return UserUtils.getCurrentUser().getRealName();
    }
}
