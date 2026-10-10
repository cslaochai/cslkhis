package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.entity.BizFeeRecord;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.system.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.pharmacy.dto.StockDeductResultDTO;
import com.his.pharmacy.dto.WardDispenseActionDTO;
import com.his.pharmacy.dto.WardDispenseGenerateDTO;
import com.his.pharmacy.dto.WardDispenseQueryPageDTO;
import com.his.pharmacy.entity.BizWardDispense;
import com.his.pharmacy.entity.BizWardDispenseItem;
import com.his.pharmacy.mapper.BizWardDispenseItemMapper;
import com.his.pharmacy.mapper.BizWardDispenseMapper;
import com.his.pharmacy.service.PharmacyService;
import com.his.pharmacy.service.WardDispenseService;
import com.his.pharmacy.support.WardDispenseChargeInvoker;
import com.his.pharmacy.vo.WardDispenseCandidateVO;
import com.his.pharmacy.vo.WardDispenseStatsVO;
import com.his.pharmacy.vo.WardDispenseVO;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.DeptScopeService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 住院摆药服务实现（G13）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WardDispenseServiceImpl extends ServiceImpl<BizWardDispenseItemMapper, BizWardDispenseItem> implements WardDispenseService {

    private final BizWardDispenseMapper bizWardDispenseMapper;
    private final BizWardDispenseItemMapper bizWardDispenseItemMapper;
    private final PharmacyService pharmacyService;
    private final WardDispenseChargeInvoker chargeInvoker;
    private final RedisSequenceService redisSequenceService;
    private final DeptScopeService deptScopeService;

    @Override
    public List<WardDispenseCandidateVO> candidates(Long wardId, Long admissionId, LocalDate dispenseDate) {
        // C 类：本方法还被 generate 直调（非 web 入口），形参上的注解不会执行
        if (wardId == null) {
            throw new BusinessException("病区不能为空");
        }
        // 病区数据权限：前端选病区（B 类），越权病区直接拒绝
        assertWardAccessible(wardId);
        LocalDate day = dispenseDate != null ? dispenseDate : LocalDate.now();
        return bizWardDispenseItemMapper.selectCandidates(wardId, day, TimeUtil.dayStart(day), TimeUtil.dayEnd(day), admissionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WardDispenseVO generate(WardDispenseGenerateDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        LocalDate day = dto.getDispenseDate() != null ? dto.getDispenseDate() : LocalDate.now();
        if (day.isAfter(LocalDate.now())) {
            throw new BusinessException("摆药日期不能是未来日期");
        }
        List<WardDispenseCandidateVO> cands = candidates(dto.getWardId(), dto.getAdmissionId(), day);
        long unmatched = bizWardDispenseItemMapper.countUnmatchedCandidates(dto.getWardId(), day,
                TimeUtil.dayStart(day), TimeUtil.dayEnd(day), dto.getAdmissionId());

        // 幂等回退：重跑同一 generate 请求时（候选已被摆过、全部排除），返回既有主单而不是报错
        if (cands.isEmpty()) {
            BizWardDispense existing = findExistingDispense(dto, day);
            if (existing != null) {
                applyAggregatedStatus(existing.getId());
                WardDispenseVO vo = getDetailById(existing.getId());
                vo.setRemark("当日无可摆医嘱（候选已全部在单），返回既有摆药单");
                return vo;
            }
            throw new BusinessException("没有可摆药的医嘱"
                    + (unmatched > 0 ? "（另有 " + unmatched + " 条药品医嘱未能匹配药品档案，已跳过）" : ""));
        }

        String operator = operatorUser.getRealName();
        BizWardDispense dispense = null;
        for (WardDispenseCandidateVO c : cands) {
            if (dispense == null || !Objects.equals(dispense.getAdmissionId(), c.getAdmissionId())) {
                // 主单粒度「一次住院 × 一天」：换患者就换单；同入院同日复用、明细追加
                dispense = findOrCreateDispense(c, day, operator);
            }
            BigDecimal amount = c.getQuantity() != null && c.getPrice() != null
                    ? c.getQuantity().multiply(c.getPrice()).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BizWardDispenseItem item = new BizWardDispenseItem();
            item.setDispenseId(dispense.getId());
            item.setDispenseNo(dispense.getDispenseNo());
            item.setDispenseDate(day);
            // 重摆序号：同医嘱同日含已退药明细取 max+1（已退药不占坑，旧明细留痕不动）；
            // 并发同 seq 撞唯一键 uk_order_date_seq，catch 跳过即可
            item.setDispenseSeq(bizWardDispenseItemMapper.nextDispenseSeq(c.getOrderId(), day));
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
            item.setStatus(BizWardDispenseItem.STATUS_PENDING);
            try {
                bizWardDispenseItemMapper.insert(item);
            } catch (DuplicateKeyException e) {
                // 并发生成撞唯一索引 (order_id, dispense_date)：同医嘱同日已被别的请求摆过，跳过即可
                log.info("摆药明细已存在（医嘱 {} 摆药日 {}），跳过重复生成", c.getOrderNo(), day);
            }
        }
        if (dispense == null) {
            throw new BusinessException("没有可摆药的医嘱"
                    + (unmatched > 0 ? "（另有 " + unmatched + " 条药品医嘱未能匹配药品档案，已跳过）" : ""));
        }
        applyAggregatedStatus(dispense.getId());
        WardDispenseVO vo = getDetailById(dispense.getId());
        if (unmatched > 0) {
            vo.setRemark("另有 " + unmatched + " 条药品医嘱未能匹配药品档案，未进摆药单");
        }
        return vo;
    }

    @Override
    public PageResult<WardDispenseVO> listPage(WardDispenseQueryPageDTO dto) {
        Page<WardDispenseVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        List<Long> wardIds = scopedWardIds(dto.getWardId());
        List<WardDispenseVO> records = bizWardDispenseMapper.selectDispensePage(
                page, dto.getWardId(), wardIds, dto.getDispenseDate(), dto.getPatientName(), dto.getStatus());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public WardDispenseVO getDetailById(Long id) {
        WardDispenseVO vo = bizWardDispenseMapper.selectDispenseById(id);
        if (vo == null) {
            throw new BusinessException("摆药单不存在或已删除");
        }
        assertWardAccessible(vo.getWardId());
        vo.setItems(bizWardDispenseItemMapper.selectItemsByDispenseId(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WardDispenseVO dispenseItem(WardDispenseActionDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizWardDispenseItem item = requireItem(dto.getItemId());
        assertWardAccessible(item.getWardId());
        if (item.getStatus() == null || item.getStatus() != BizWardDispenseItem.STATUS_PENDING) {
            throw new BusinessException("仅待配药明细允许配药（当前状态码 " + item.getStatus() + "）");
        }
        String operatorName = operatorUser.getRealName();
        Long operatorId = operatorUser.getEmployeeId();

        // ① FEFO 扣库存：先过期先出、跨批次、逐批落药品库存流水；总量不足整单失败
        StockDeductResultDTO deduct = pharmacyService.deductStockFefo(item.getDrugId(), item.getQuantity(),
                "wardDispense", item.getId(), item.getDispenseNo(), operatorName);

        // ② 计费进 L1 记账行（独立事务，失败不回滚扣库存，但绝不静默当作已计费）
        BizFeeRecord charge = chargeInvoker.charge(item);

        item.setStockBefore(deduct.getQuantityBefore());
        item.setStockAfter(deduct.getQuantityAfter());
        item.setStatus(BizWardDispenseItem.STATUS_DISPENSED);
        item.setDispenserId(operatorId);
        item.setDispenserName(operatorName);
        item.setDispenseTime(TimeUtil.nowSeconds());
        if (charge != null) {
            // 记账行的 (id, feeNo)
            item.setFeeRecordId(charge.getId());
            item.setFeeNo(charge.getFeeNo());
        } else {
            log.warn("摆药明细 {} 配药成功但未计费（记账入参不全或被拒），fee_record_id 留空待补", item.getId());
        }
        if (bizWardDispenseItemMapper.updateById(item) <= 0) {
            throw new BusinessException("配药更新失败");
        }
        applyAggregatedStatus(item.getDispenseId());
        return getDetailById(item.getDispenseId());
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WardDispenseVO checkItem(WardDispenseActionDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizWardDispenseItem item = requireItem(dto.getItemId());
        assertWardAccessible(item.getWardId());
        if (item.getStatus() == null || item.getStatus() != BizWardDispenseItem.STATUS_DISPENSED) {
            throw new BusinessException("仅已配药明细允许核对（当前状态码 " + item.getStatus() + "）");
        }
        item.setStatus(BizWardDispenseItem.STATUS_CHECKED);
        item.setCheckerId(operatorUser.getEmployeeId());
        item.setCheckerName(operatorUser.getRealName());
        item.setCheckTime(TimeUtil.nowSeconds());
        if (TextUtil.hasText(dto.getRemark())) {
            item.setRemark(dto.getRemark());
        }
        if (bizWardDispenseItemMapper.updateById(item) <= 0) {
            throw new BusinessException("核对更新失败");
        }
        applyAggregatedStatus(item.getDispenseId());
        return getDetailById(item.getDispenseId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WardDispenseVO returnItem(WardDispenseActionDTO dto) {
        // B 类：reason 只在退药入口必填，同一 DTO 被配药/核对入口复用，加 @NotBlank 会挡死那两个接口
        if (!TextUtil.hasText(dto.getReason())) {
            throw new BusinessException("退药原因必填");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizWardDispenseItem item = requireItem(dto.getItemId());
        assertWardAccessible(item.getWardId());
        if (item.getStatus() == null
                || (item.getStatus() != BizWardDispenseItem.STATUS_DISPENSED
                && item.getStatus() != BizWardDispenseItem.STATUS_CHECKED)) {
            throw new BusinessException("仅已配药/已核对明细允许退药（当前状态码 " + item.getStatus() + "）");
        }
        String operatorName = operatorUser.getRealName();

        // ① 回库（落 type=3 流水；并入数量最大批次，无批次则按来源单建新批次）
        pharmacyService.restoreStock(item.getDrugId(), item.getQuantity(),
                "wardDispenseReturn", item.getId(), item.getDispenseNo(), operatorName);

        // ② 红冲那一笔记账行（独立事务，失败不回滚回库，留 warn 待人工核对账务）
        BizFeeRecord refund = chargeInvoker.refund(item, dto.getReason());

        // ③ 明细推进 → 4（终态不可逆）
        item.setStatus(BizWardDispenseItem.STATUS_RETURNED);
        item.setReturnBy(operatorName);
        item.setReturnTime(TimeUtil.nowSeconds());
        item.setReturnReason(dto.getReason());
        if (refund == null) {
            log.warn("摆药明细 {} 退药回库成功但红冲未落（当初未记账或红冲被拒），待人工核对账务", item.getId());
        }
        if (bizWardDispenseItemMapper.updateById(item) <= 0) {
            throw new BusinessException("退药更新失败");
        }
        applyAggregatedStatus(item.getDispenseId());
        return getDetailById(item.getDispenseId());
    }

    @Override
    public WardDispenseStatsVO stats(LocalDate dispenseDate, Long wardId) {
        LocalDate day = dispenseDate != null ? dispenseDate : LocalDate.now();
        List<Long> wardIds = scopedWardIds(wardId);
        WardDispenseStatsVO vo = new WardDispenseStatsVO();
        vo.setPending(countItems(day, wardIds, BizWardDispenseItem.STATUS_PENDING));
        vo.setDispensed(countItems(day, wardIds, BizWardDispenseItem.STATUS_DISPENSED));
        vo.setChecked(countItems(day, wardIds, BizWardDispenseItem.STATUS_CHECKED));
        vo.setReturned(countItems(day, wardIds, BizWardDispenseItem.STATUS_RETURNED));
        vo.setDispenseCount(bizWardDispenseMapper.selectCount(new LambdaQueryWrapper<BizWardDispense>()
                .eq(BizWardDispense::getDispenseDate, day)
                .in(wardIds != null, BizWardDispense::getWardId, wardIds)));
        return vo;
    }

    private long countItems(LocalDate day, List<Long> wardIds, int status) {
        return bizWardDispenseItemMapper.selectCount(new LambdaQueryWrapper<BizWardDispenseItem>()
                .eq(BizWardDispenseItem::getDispenseDate, day)
                .in(wardIds != null, BizWardDispenseItem::getWardId, wardIds)
                .eq(BizWardDispenseItem::getStatus, status));
    }

    /**
     * 病区数据权限（ward_id 是 sys_ward 主键不是科室ID，须经 sys_ward.dept_id 折算后再判）
     */
    private void assertWardAccessible(Long wardId) {
        if (wardId == null) {
            return;
        }
        deptScopeService.assertDeptAccessible(bizWardDispenseMapper.selectWardDeptId(wardId));
    }

    /** null=不限病区；非空=授权科室折算出的病区集合（空集合一律当配置缺失拒掉，IN () 是语法错误） */
    private List<Long> scopedWardIds(Long requestedWardId) {
        if (requestedWardId != null) {
            assertWardAccessible(requestedWardId);
            return List.of(requestedWardId);
        }
        Set<Long> allowed = deptScopeService.allowedDeptIds();
        if (allowed == null) {
            return null;
        }
        if (allowed.isEmpty()) {
            throw new BusinessException("当前岗位未绑定任何科室，无法查看相关数据");
        }
        List<Long> wardIds = bizWardDispenseMapper.selectWardIdsByDeptIds(List.copyOf(allowed));
        if (wardIds.isEmpty()) {
            throw new BusinessException("当前岗位授权科室下没有绑定的病区，无法查看相关数据");
        }
        return wardIds;
    }

    /**
     * 幂等回退用：按 generate 请求的 scope（传了 admissionId 按住院，否则按病区）找当日既有主单
     */
    private BizWardDispense findExistingDispense(WardDispenseGenerateDTO dto, LocalDate day) {
        LambdaQueryWrapper<BizWardDispense> w = new LambdaQueryWrapper<BizWardDispense>()
                .eq(BizWardDispense::getDispenseDate, day);
        if (dto.getAdmissionId() != null) {
            w.eq(BizWardDispense::getAdmissionId, dto.getAdmissionId());
        } else {
            w.eq(BizWardDispense::getWardId, dto.getWardId());
        }
        return bizWardDispenseMapper.selectOne(w.orderByDesc(BizWardDispense::getId).last("LIMIT 1"));
    }

    /**
     * 同入院同日找主单，没有才建一张（单号 WD+yyyyMMdd+4位）。
     * 并发窗口由明细唯一索引 (order_id, dispense_date, dispense_seq) 兜底。
     */
    private BizWardDispense findOrCreateDispense(WardDispenseCandidateVO first, LocalDate day, String operator) {
        BizWardDispense existing = bizWardDispenseMapper.selectOne(new LambdaQueryWrapper<BizWardDispense>()
                .eq(BizWardDispense::getAdmissionId, first.getAdmissionId())
                .eq(BizWardDispense::getDispenseDate, day)
                .orderByDesc(BizWardDispense::getId)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }
        BizWardDispense d = new BizWardDispense();
        d.setDispenseNo(redisSequenceService.generateWardDispenseNo());
        d.setDispenseDate(day);
        d.setAdmissionId(first.getAdmissionId());
        d.setPatientId(first.getPatientId());
        d.setPatientNo(first.getPatientNo());
        d.setPatientName(first.getPatientName());
        d.setWardId(first.getWardId());
        d.setWardName(bizWardDispenseMapper.selectWardName(first.getWardId()));
        d.setStatus(1);
        d.setGenerateBy(operator);
        d.setGenerateTime(TimeUtil.nowSeconds());
        bizWardDispenseMapper.insert(d);
        return d;
    }

    /**
     * 主单状态聚合回算：明细状态是事实源，主单 status 只是给人看的派生值。
     * 规则：有 1 → 全 1 则 1-待配药、否则 2-配药中；无 1 有 2 → 3-已配药；
     * 无 1 无 2 有 3 → 4-已核对；全 4 → 5-已退药。
     */
    private void applyAggregatedStatus(Long dispenseId) {
        List<BizWardDispenseItem> items = bizWardDispenseItemMapper.selectList(new LambdaQueryWrapper<BizWardDispenseItem>()
                .eq(BizWardDispenseItem::getDispenseId, dispenseId));
        if (items.isEmpty()) {
            return;
        }
        Map<Integer, Long> byStatus = items.stream()
                .collect(Collectors.groupingBy(BizWardDispenseItem::getStatus, Collectors.counting()));
        int agg;
        if (byStatus.containsKey(BizWardDispenseItem.STATUS_PENDING)) {
            agg = byStatus.size() == 1 ? 1 : 2;
        } else if (byStatus.containsKey(BizWardDispenseItem.STATUS_DISPENSED)) {
            agg = 3;
        } else if (byStatus.containsKey(BizWardDispenseItem.STATUS_CHECKED)) {
            agg = 4;
        } else {
            agg = 5;
        }
        BizWardDispense d = bizWardDispenseMapper.selectById(dispenseId);
        if (d != null && !Objects.equals(d.getStatus(), agg)) {
            d.setStatus(agg);
            bizWardDispenseMapper.updateById(d);
        }
    }

    private BizWardDispenseItem requireItem(Long itemId) {
        BizWardDispenseItem item = bizWardDispenseItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("摆药明细不存在或已删除");
        }
        return item;
    }

}
