package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.dto.FeeBookDTO;
import com.his.charge.dto.FeeRecordQueryPageDTO;
import com.his.charge.dto.FeeReverseDTO;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.mapper.BizFeeRecordMapper;
import com.his.charge.service.FeeRecordService;
import com.his.charge.vo.BizFeeRecordDetailVO;
import com.his.charge.vo.BizFeeRecordVO;
import com.his.charge.vo.FeeTypeSumVO;
import com.his.common.base.PageResult;
import com.his.common.enums.EncounterTypeEnum;
import com.his.common.enums.FeeSourceTypeEnum;
import com.his.common.enums.FeeStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 费用记账实现（L1）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeeRecordServiceImpl extends ServiceImpl<BizFeeRecordMapper, BizFeeRecord> implements FeeRecordService {

    private static final int AMOUNT_SCALE = 2;

    /**
     * 列宽：写库文本一律先截，超长报 Data too long 会把"记不上账"升级成 500
     */
    private static final int W_FEE_NO = 32;
    private static final int W_PATIENT_NO = 32;
    private static final int W_PATIENT_NAME = 50;
    private static final int W_ENCOUNTER_NO = 32;
    private static final int W_DEPT_NAME = 100;
    private static final int W_DOCTOR_NAME = 50;
    private static final int W_ITEM_CODE = 32;
    private static final int W_ITEM_NAME = 200;
    private static final int W_SPEC = 100;
    private static final int W_UNIT = 20;
    private static final int W_SOURCE_NO = 64;
    private static final int W_BOOK_BY_NAME = 64;
    private static final int W_REMARK = 500;

    private final RedisSequenceService redisSequenceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizFeeRecord book(FeeBookDTO dto) {
        if (dto == null) {
            throw new BusinessException("缺少记账内容");
        }
        if (EncounterTypeEnum.fromCode(dto.getEncounterType()) == null) {
            throw new BusinessException("就诊类型不合法（1-门诊 2-住院）");
        }
        if (FeeSourceTypeEnum.fromCode(dto.getSourceType()) == null) {
            throw new BusinessException("费用来源不合法");
        }
        BigDecimal price = NumUtil.orZero(dto.getPrice());
        BigDecimal quantity = NumUtil.orZero(dto.getQuantity());
        if (price.signum() < 0) {
            throw new BusinessException("单价不能为负");
        }
        if (quantity.signum() <= 0) {
            // 传负数记账等于绕过红冲链：净额对了，但没人知道这笔是谁冲的
            throw new BusinessException("记账数量必须大于 0，冲减请走红冲接口");
        }

        BizFeeRecord booked = findBooked(dto);
        if (booked != null) {
            log.info("[记账幂等] 来源 {}/{} 项目 {} 已记过 {}，本次不再重复记账",
                    dto.getSourceType(), dto.getSourceId(), dto.getItemCode(), booked.getFeeNo());
            return booked;
        }

        BizFeeRecord row = new BizFeeRecord();
        row.setFeeNo(TextUtil.cut(redisSequenceService.generateFeeNo(), W_FEE_NO));
        applySnapshot(row, dto);
        // 金额由服务端现算：信调用方传来的金额等于把应收交给调用方定义
        row.setAmount(price.multiply(quantity).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP));
        row.setFeeStatus(FeeStatusEnum.PENDING.getCode());
        row.setBookTime(LocalDateTime.now());
        row.setBookById(UserUtils.getCurrentUser().getEmployeeId());
        row.setBookByName(TextUtil.cut(UserUtils.getCurrentUser().getRealName(), W_BOOK_BY_NAME));
        row.setRemark(TextUtil.cut(dto.getRemark(), W_REMARK));
        this.save(row);
        log.info("[记账] {} 患者 {} 项目 {} 数量 {} 金额 ¥{}", row.getFeeNo(), row.getPatientName(),
                row.getItemName(), quantity.toPlainString(), row.getAmount().toPlainString());
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BizFeeRecord> bookBatch(List<FeeBookDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException("没有要记账的项目");
        }
        List<BizFeeRecord> rows = new ArrayList<>();
        for (FeeBookDTO dto : list) {
            rows.add(book(dto));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizFeeRecordVO bookVO(FeeBookDTO dto) {
        return toVO(book(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizFeeRecordVO reverseVO(FeeReverseDTO dto) {
        return toVO(dto.getQuantity() == null
                ? reverse(dto.getFeeId(), dto.getReason())
                : reversePartial(dto.getFeeId(), dto.getQuantity(), dto.getReason()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizFeeRecord reverse(Long feeId, String reason) {
        BizFeeRecord orig = requireReversible(feeId, false);
        return doReverse(orig, remainingQuantity(orig), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizFeeRecord reversePartial(Long feeId, BigDecimal quantity, String reason) {
        BizFeeRecord orig = requireReversible(feeId, false);
        BigDecimal left = remainingQuantity(orig);
        if (quantity == null || quantity.signum() <= 0) {
            throw new BusinessException("冲减数量必须大于 0");
        }
        if (quantity.compareTo(left) > 0) {
            throw new BusinessException("本行剩余可冲 " + left.stripTrailingZeros().toPlainString()
                    + "，不能冲 " + quantity.stripTrailingZeros().toPlainString());
        }
        return doReverse(orig, quantity, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizFeeRecord reverseForRefund(Long feeId, String reason) {
        BizFeeRecord orig = requireReversible(feeId, true);
        return doReverse(orig, remainingQuantity(orig), reason);
    }

    @Override
    public List<BizFeeRecord> listPending(Integer encounterType, Long encounterId) {
        return this.list(new LambdaQueryWrapper<BizFeeRecord>()
                .eq(BizFeeRecord::getEncounterType, encounterType)
                .eq(BizFeeRecord::getEncounterId, encounterId)
                .eq(BizFeeRecord::getFeeStatus, FeeStatusEnum.PENDING.getCode())
                .orderByAsc(BizFeeRecord::getId));
    }

    @Override
    public BigDecimal sumPendingAmount(Integer encounterType, Long encounterId) {
        return NumUtil.orZero(baseMapper.sumPendingAmount(encounterType, encounterId));
    }

    @Override
    public List<FeeTypeSumVO> sumNetGroupByItemType(Integer encounterType, Long encounterId) {
        return baseMapper.sumNetGroupByItemType(encounterType, encounterId);
    }

    @Override
    public List<BizFeeRecord> listNetByEncounter(Integer encounterType, Long encounterId) {
        return baseMapper.selectNetByEncounter(encounterType, encounterId);
    }

    @Override
    public BigDecimal sumNetAmount(Integer encounterType, Long encounterId) {
        return NumUtil.orZero(baseMapper.sumNetByEncounter(encounterType, encounterId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockToBill(List<Long> feeIds, Long billId) {
        List<Long> ids = requireIds(feeIds);
        if (billId == null) {
            throw new BusinessException("缺少结算账单");
        }
        int changed = baseMapper.update(null, new LambdaUpdateWrapper<BizFeeRecord>()
                .set(BizFeeRecord::getFeeStatus, FeeStatusEnum.LOCKED.getCode())
                .set(BizFeeRecord::getBillId, billId)
                .in(BizFeeRecord::getId, ids)
                .eq(BizFeeRecord::getFeeStatus, FeeStatusEnum.PENDING.getCode())
                .isNull(BizFeeRecord::getBillId));
        // affected 不足说明有人和这次结算抢同一批行：整笔回滚，绝不能让一行费用进两张账单
        if (changed != ids.size()) {
            throw new BusinessException("有记账行已被其他结算占用或已结算，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markSettledByBill(Long billId) {
        if (billId == null) {
            throw new BusinessException("缺少结算账单");
        }
        // 只提「2-已锁定」：钱收齐才叫结算完成，已红冲的行永远不该跟着变成"已结算"
        return baseMapper.update(null, new LambdaUpdateWrapper<BizFeeRecord>()
                .set(BizFeeRecord::getFeeStatus, FeeStatusEnum.SETTLED.getCode())
                .eq(BizFeeRecord::getBillId, billId)
                .eq(BizFeeRecord::getFeeStatus, FeeStatusEnum.LOCKED.getCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int releaseFromBill(List<Long> feeIds) {
        List<Long> ids = requireIds(feeIds);
        // 「3-已结算」也在匹配范围内：整单撤销已经把收进来的钱全额退回，那些行的"已结算"
        // 事实上已被抹掉，留在原地只会让下一步的红冲被"已结算"门禁挡回来。
        return baseMapper.update(null, new LambdaUpdateWrapper<BizFeeRecord>()
                .set(BizFeeRecord::getFeeStatus, FeeStatusEnum.PENDING.getCode())
                .set(BizFeeRecord::getBillId, null)
                .in(BizFeeRecord::getId, ids)
                .in(BizFeeRecord::getFeeStatus, FeeStatusEnum.LOCKED.getCode(), FeeStatusEnum.SETTLED.getCode()));
        // 匹配不足是常态而非事故：已全额红冲的行（状态 4）本来就没有可解锁的账
    }

    @Override
    public List<BizFeeRecord> listByBill(Long billId) {
        if (billId == null) {
            return new ArrayList<>();
        }
        return this.list(new LambdaQueryWrapper<BizFeeRecord>()
                .eq(BizFeeRecord::getBillId, billId)
                .orderByAsc(BizFeeRecord::getId));
    }

    @Override
    public BizFeeRecord findBookedBySource(Integer sourceType, Long sourceId, String itemCode) {
        if (sourceType == null || sourceId == null) {
            return null;
        }
        for (BizFeeRecord row : baseMapper.selectBySource(sourceType, sourceId)) {
            boolean amountPositive = NumUtil.orZero(row.getAmount()).signum() > 0;
            boolean sameItem = itemCode == null || Objects.equals(TextUtil.cut(row.getItemCode(), W_ITEM_CODE), TextUtil.cut(itemCode, W_ITEM_CODE));
            if (sameItem && amountPositive && !FeeStatusEnum.REVERSED.getCode().equals(row.getFeeStatus())) {
                return row;
            }
        }
        return null;
    }

    @Override
    public PageResult<BizFeeRecordVO> selectPage(FeeRecordQueryPageDTO query) {
        Page<BizFeeRecord> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), buildWrapper(query));
        List<BizFeeRecordVO> records = new ArrayList<>();
        for (BizFeeRecord row : page.getRecords()) {
            records.add(toVO(row));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public BizFeeRecordDetailVO getDetailById(Long id) {
        BizFeeRecord row = this.getById(id);
        if (row == null) {
            throw new BusinessException("记账行不存在");
        }
        BizFeeRecordDetailVO vo = new BizFeeRecordDetailVO();
        BeanUtils.copyProperties(row, vo);
        BigDecimal reversed = NumUtil.orZero(baseMapper.sumReversalAmount(id));
        vo.setReversedAmount(reversed);
        vo.setRemainingAmount(NumUtil.orZero(row.getAmount()).add(reversed));
        if (row.getOrigFeeId() != null) {
            BizFeeRecord orig = this.getById(row.getOrigFeeId());
            vo.setReverseOf(orig == null ? null : toVO(orig));
        }
        List<BizFeeRecordVO> reversals = new ArrayList<>();
        for (BizFeeRecord neg : baseMapper.selectReversalRows(id)) {
            reversals.add(toVO(neg));
        }
        vo.setReverseRows(reversals);
        return vo;
    }

    /**
     * 幂等判重：同一来源单据 + 同一项目已有<b>有效</b>的正数记账行就不再记。
     *
     * <p>三个过滤条件各挡一类误判：负行是冲减不是新费用；已红冲的行意味着"这笔记错了重记"，
     * 必须允许再记；来源ID 为空则根本无从判重（手工补记账），每次都记。
     */
    private BizFeeRecord findBooked(FeeBookDTO dto) {
        if (dto.getSourceId() == null) {
            return null;
        }
        for (BizFeeRecord exist : baseMapper.selectBySource(dto.getSourceType(), dto.getSourceId())) {
            boolean sameItem = Objects.equals(TextUtil.cut(exist.getItemCode(), W_ITEM_CODE), TextUtil.cut(dto.getItemCode(), W_ITEM_CODE));
            if (sameItem && NumUtil.orZero(exist.getAmount()).signum() > 0
                    && !FeeStatusEnum.REVERSED.getCode().equals(exist.getFeeStatus())) {
                return exist;
            }
        }
        return null;
    }

    /**
     * 可红冲性检查。
     *
     * <p>「1-待结算」随时可冲；「2-已锁定」正被一张未付账单占着，要冲先作废账单，
     * 否则账单合计与记账行当场对不上；「3-已结算」只有退费流程（{@code forRefund}）能冲，
     * 因为钱已经收进来了，冲费用必须同时把钱退回去。
     */
    private BizFeeRecord requireReversible(Long feeId, boolean forRefund) {
        if (feeId == null) {
            throw new BusinessException("缺少记账行");
        }
        BizFeeRecord orig = this.getById(feeId);
        if (orig == null) {
            throw new BusinessException("记账行不存在");
        }
        if (NumUtil.orZero(orig.getAmount()).signum() < 0) {
            throw new BusinessException("红冲行不能再被红冲");
        }
        FeeStatusEnum status = FeeStatusEnum.fromCode(orig.getFeeStatus());
        if (status == FeeStatusEnum.REVERSED) {
            throw new BusinessException("该记账行已全额红冲");
        }
        if (status == FeeStatusEnum.LOCKED) {
            throw new BusinessException("该费用已进入结算账单，请先作废账单再红冲");
        }
        if (status == FeeStatusEnum.SETTLED && !forRefund) {
            throw new BusinessException("该费用已结算收款，请走退费，不要在记账层冲账");
        }
        return orig;
    }

    /**
     * 写一条负行。金额取负，快照照抄原行（改名的项目字典不能改写历史费用名称）。
     *
     * <p>末笔倒挤：冲完本行时金额直接取"原行金额 - 已冲金额"，而不是单价×数量，
     * 否则多次部分冲减各自四舍五入后，链上会留下几分钱永远冲不平。
     */
    private BizFeeRecord doReverse(BizFeeRecord orig, BigDecimal quantity, String reason) {
        if (!TextUtil.hasText(reason)) {
            throw new BusinessException("缺少红冲原因");
        }
        BigDecimal leftQty = remainingQuantity(orig);
        boolean full = quantity.compareTo(leftQty) >= 0;
        BigDecimal remainingAmount = NumUtil.orZero(orig.getAmount()).add(NumUtil.orZero(baseMapper.sumReversalAmount(orig.getId())));
        BigDecimal amount = full ? remainingAmount
                : NumUtil.orZero(orig.getPrice()).multiply(quantity).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);

        BizFeeRecord neg = new BizFeeRecord();
        neg.setFeeNo(TextUtil.cut(redisSequenceService.generateFeeNo(), W_FEE_NO));
        neg.setPatientId(orig.getPatientId());
        neg.setPatientNo(orig.getPatientNo());
        neg.setPatientName(TextUtil.cut(orig.getPatientName(), W_PATIENT_NAME));
        neg.setEncounterType(orig.getEncounterType());
        neg.setEncounterId(orig.getEncounterId());
        neg.setEncounterNo(orig.getEncounterNo());
        neg.setDeptId(orig.getDeptId());
        neg.setDeptName(orig.getDeptName());
        neg.setDoctorId(orig.getDoctorId());
        neg.setDoctorName(orig.getDoctorName());
        neg.setItemType(orig.getItemType());
        neg.setItemCode(orig.getItemCode());
        neg.setItemName(TextUtil.cut(orig.getItemName(), W_ITEM_NAME));
        neg.setSpecification(orig.getSpecification());
        neg.setUnit(orig.getUnit());
        neg.setCatalogType(orig.getCatalogType());
        neg.setPrice(orig.getPrice());
        neg.setQuantity(quantity.negate());
        neg.setAmount(amount.negate());
        neg.setFeeStatus(FeeStatusEnum.PENDING.getCode());
        // 来源照抄原行：sumNetBySource 才能把这张临床单据的净额算对，负行也不会被幂等判重新记一遍
        neg.setSourceType(orig.getSourceType());
        neg.setSourceId(orig.getSourceId());
        neg.setSourceNo(orig.getSourceNo());
        neg.setOrigFeeId(orig.getId());
        neg.setBookTime(LocalDateTime.now());
        neg.setBookById(UserUtils.getCurrentUser().getEmployeeId());
        neg.setBookByName(TextUtil.cut(UserUtils.getCurrentUser().getRealName(), W_BOOK_BY_NAME));
        neg.setRemark(TextUtil.cut(reason, W_REMARK));
        this.save(neg);

        if (full) {
            // 整行冲完：原行与它名下所有负行一起退出应收，否则负行单独留在待结算里，净额会算成负数
            baseMapper.update(null, new LambdaUpdateWrapper<BizFeeRecord>()
                    .set(BizFeeRecord::getFeeStatus, FeeStatusEnum.REVERSED.getCode())
                    .set(BizFeeRecord::getOrigFeeId, neg.getId())
                    .eq(BizFeeRecord::getId, orig.getId()));
            baseMapper.update(null, new LambdaUpdateWrapper<BizFeeRecord>()
                    .set(BizFeeRecord::getFeeStatus, FeeStatusEnum.REVERSED.getCode())
                    .eq(BizFeeRecord::getOrigFeeId, orig.getId()));
            // 同步更新原行的 refunded_amount = SUM(ABS(负行amount))
            BigDecimal totalRefunded = NumUtil.orZero(orig.getAmount()).abs();
            baseMapper.update(null, new LambdaUpdateWrapper<BizFeeRecord>()
                    .set(BizFeeRecord::getRefundedAmount, totalRefunded)
                    .eq(BizFeeRecord::getId, orig.getId()));
        } else {
            // 部分冲减：累加 refunded_amount
            BigDecimal currentRefunded = NumUtil.orZero(orig.getRefundedAmount());
            BigDecimal thisRefund = amount.abs();
            baseMapper.update(null, new LambdaUpdateWrapper<BizFeeRecord>()
                    .set(BizFeeRecord::getRefundedAmount, currentRefunded.add(thisRefund))
                    .eq(BizFeeRecord::getId, orig.getId()));
        }
        log.info("[红冲] {} 冲 {} 数量 {} 金额 ¥{} 全额={} 原因={}",
                neg.getFeeNo(), orig.getFeeNo(), neg.getQuantity().toPlainString(),
                neg.getAmount().toPlainString(), full, reason);
        return neg;
    }

    /**
     * 本行剩余可冲数量 = 原行数量 + Σ(负行数量)（负行数量为负）
     */
    private BigDecimal remainingQuantity(BizFeeRecord orig) {
        return NumUtil.orZero(orig.getQuantity()).add(NumUtil.orZero(baseMapper.sumReversalQuantity(orig.getId())));
    }

    private void applySnapshot(BizFeeRecord row, FeeBookDTO dto) {
        row.setPatientId(dto.getPatientId());
        row.setPatientNo(TextUtil.cut(dto.getPatientNo(), W_PATIENT_NO));
        row.setPatientName(TextUtil.cut(dto.getPatientName(), W_PATIENT_NAME));
        row.setEncounterType(dto.getEncounterType());
        row.setEncounterId(dto.getEncounterId());
        row.setEncounterNo(TextUtil.cut(dto.getEncounterNo(), W_ENCOUNTER_NO));
        row.setDeptId(dto.getDeptId());
        row.setDeptName(TextUtil.cut(dto.getDeptName(), W_DEPT_NAME));
        row.setDoctorId(dto.getDoctorId());
        row.setDoctorName(TextUtil.cut(dto.getDoctorName(), W_DOCTOR_NAME));
        row.setItemType(dto.getItemType());
        row.setItemCode(TextUtil.cut(dto.getItemCode(), W_ITEM_CODE));
        row.setItemName(TextUtil.cut(dto.getItemName(), W_ITEM_NAME));
        row.setSpecification(TextUtil.cut(dto.getSpecification(), W_SPEC));
        row.setUnit(TextUtil.cut(dto.getUnit(), W_UNIT));
        row.setCatalogType(dto.getCatalogType() == null ? 0 : dto.getCatalogType());
        row.setPrice(dto.getPrice());
        row.setQuantity(dto.getQuantity());
        row.setSourceType(dto.getSourceType());
        row.setSourceId(dto.getSourceId());
        row.setSourceNo(TextUtil.cut(dto.getSourceNo(), W_SOURCE_NO));
    }

    private List<Long> requireIds(List<Long> feeIds) {
        if (CollectionUtils.isEmpty(feeIds)) {
            throw new BusinessException("缺少记账行");
        }
        List<Long> ids = new ArrayList<>();
        for (Long id : feeIds) {
            if (id != null && !ids.contains(id)) {
                ids.add(id);
            }
        }
        if (ids.isEmpty()) {
            throw new BusinessException("缺少记账行");
        }
        return ids;
    }

    private LambdaQueryWrapper<BizFeeRecord> buildWrapper(FeeRecordQueryPageDTO query) {
        String keyword = query.getKeyword();
        return new LambdaQueryWrapper<BizFeeRecord>()
                .eq(query.getEncounterType() != null, BizFeeRecord::getEncounterType, query.getEncounterType())
                .eq(query.getEncounterId() != null, BizFeeRecord::getEncounterId, query.getEncounterId())
                .eq(query.getPatientId() != null, BizFeeRecord::getPatientId, query.getPatientId())
                .eq(query.getFeeStatus() != null, BizFeeRecord::getFeeStatus, query.getFeeStatus())
                .eq(query.getItemType() != null, BizFeeRecord::getItemType, query.getItemType())
                .eq(query.getSourceType() != null, BizFeeRecord::getSourceType, query.getSourceType())
                .eq(query.getDeptId() != null, BizFeeRecord::getDeptId, query.getDeptId())
                .eq(query.getBillId() != null, BizFeeRecord::getBillId, query.getBillId())
                .and(TextUtil.hasText(keyword), w -> w.like(BizFeeRecord::getFeeNo, keyword)
                        .or().like(BizFeeRecord::getItemName, keyword)
                        .or().like(BizFeeRecord::getPatientName, keyword)
                        .or().like(BizFeeRecord::getSourceNo, keyword))
                // 同一时刻记多行时只按 book_time 排会翻页重复/漏行，补 id 作二级键
                .orderByDesc(BizFeeRecord::getBookTime)
                .orderByDesc(BizFeeRecord::getId);
    }

    private BizFeeRecordVO toVO(BizFeeRecord row) {
        BizFeeRecordVO vo = new BizFeeRecordVO();
        BeanUtils.copyProperties(row, vo);
        return vo;
    }
}
