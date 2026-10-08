package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.dto.FeeBookDTO;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.support.FeeCatalogResolver;
import com.his.common.base.PageResult;
import com.his.common.enums.EncounterTypeEnum;
import com.his.common.enums.FeeSourceTypeEnum;
import com.his.common.enums.PaymentItemTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.pharmacy.dto.ConsumableTraceQueryPageDTO;
import com.his.pharmacy.dto.HighValueUseDTO;
import com.his.pharmacy.entity.BizConsumableStock;
import com.his.pharmacy.entity.BizConsumableStockLog;
import com.his.pharmacy.entity.BizConsumableTrace;
import com.his.pharmacy.entity.SysConsumable;
import com.his.pharmacy.mapper.BizConsumableStockLogMapper;
import com.his.pharmacy.mapper.BizConsumableStockMapper;
import com.his.pharmacy.mapper.BizConsumableTraceMapper;
import com.his.pharmacy.mapper.SysConsumableMapper;
import com.his.pharmacy.service.HighValueTraceService;
import com.his.pharmacy.support.TraceChargeInvoker;
import com.his.pharmacy.support.UdiParser;
import com.his.pharmacy.vo.BizConsumableTraceVO;
import com.his.pharmacy.vo.ConsumableTraceDetailVO;
import com.his.pharmacy.vo.TracePatientSnapshotVO;
import com.his.pharmacy.vo.UdiScanVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 高值耗材 UDI 扫码溯源实现。
 * 铁律：台账一行=一件耗材=批次±1件；计费只在独立事务里尝试，任何失败都写进 charge_fail_reason，
 * 登记不因记账失败而回滚（这件东西用在谁身上是事实，钱没到账是账务问题）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HighValueTraceServiceImpl extends ServiceImpl<BizConsumableTraceMapper, BizConsumableTrace> implements HighValueTraceService {

    private static final BigDecimal ONE = BigDecimal.ONE;
    /**
     * charge_fail_reason 列宽 VARCHAR(500)：超长会把"补记"升级成 500，先截断
     */
    private static final int FAIL_REASON_MAX = 500;
    private static final int VOID_REASON_MAX = 200;

    private final BizConsumableTraceMapper bizConsumableTraceMapper;
    private final SysConsumableMapper sysConsumableMapper;
    private final BizConsumableStockMapper bizConsumableStockMapper;
    private final BizConsumableStockLogMapper bizConsumableStockLogMapper;
    private final TraceChargeInvoker chargeInvoker;

    private static String blankToNull(String v) {
        return TextUtil.hasText(v) ? v.trim() : null;
    }

    @Override
    public UdiScanVO scanUdi(String udiCode) {
        UdiScanVO vo = new UdiScanVO();
        vo.setUdiCode(udiCode == null ? null : udiCode.trim());
        UdiParser.UdiParts parts = UdiParser.parse(vo.getUdiCode());
        vo.setUdiDi(parts.getDi());
        vo.setUdiSerial(parts.getSerial());
        vo.setUdiBatch(parts.getBatch());
        vo.setUdiExpiryDate(parts.getExpiryDate());
        vo.setParsed(parts.isParsed());
        if (!parts.isParsed()) {
            vo.setTip("未能从码串解析出 UDI-DI（GS1 (01) 段），请人工选择耗材后继续登记");
            return vo;
        }
        SysConsumable dict = bizConsumableTraceMapper.selectByUdiDi(parts.getDi());
        if (dict == null) {
            vo.setTip("UDI-DI " + parts.getDi() + " 未命中耗材字典，请先到字典录入该 DI，或人工选择耗材");
            return vo;
        }
        vo.setMatched(true);
        vo.setConsumableId(dict.getId());
        vo.setConsumableCode(dict.getConsumableCode());
        vo.setConsumableName(dict.getConsumableName());
        vo.setSpecification(dict.getSpecification());
        vo.setUnit(dict.getUnit());
        vo.setManufacturer(dict.getManufacturer());
        vo.setRegCertNo(dict.getRegCertNo());
        vo.setRetailPrice(dict.getRetailPrice());
        vo.setIsHighValue(dict.getIsHighValue());
        vo.setConsumableStatus(dict.getStatus());
        if (dict.getIsHighValue() == null || dict.getIsHighValue() != 1) {
            vo.setTip("命中的耗材「" + dict.getConsumableName() + "」不是高值耗材，无需走扫码溯源登记");
        } else if (dict.getStatus() != null && dict.getStatus() == 0) {
            vo.setTip("命中的耗材「" + dict.getConsumableName() + "」已停用，不能登记使用");
        }
        if (TextUtil.hasText(vo.getTip())) {
            return vo;
        }
        vo.setBatches(bizConsumableStockMapper.selectInStockBatches(dict.getId()).stream().map(b -> {
            UdiScanVO.BatchOption opt = new UdiScanVO.BatchOption();
            opt.setStockId(b.getId());
            opt.setBatchNo(b.getBatchNo());
            opt.setExpiryDate(b.getExpiryDate());
            opt.setQuantity(b.getQuantity());
            opt.setLocation(b.getLocation());
            opt.setSupplier(b.getSupplier());
            return opt;
        }).toList());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizConsumableTraceVO traceUse(HighValueUseDTO dto, String operatorName) {
        String udiCode = dto.getUdiCode().trim();
        SysConsumable dict = sysConsumableMapper.selectById(dto.getConsumableId());
        if (dict == null) {
            throw new BusinessException("耗材字典中不存在该耗材");
        }
        if (dict.getStatus() != null && dict.getStatus() == 0) {
            throw new BusinessException("耗材「" + dict.getConsumableName() + "」已停用，不能登记使用");
        }
        if (dict.getIsHighValue() == null || dict.getIsHighValue() != 1) {
            throw new BusinessException("仅高值耗材走扫码溯源登记，普通耗材请走科室领用");
        }
        if (bizConsumableTraceMapper.countActiveByUdi(udiCode) > 0) {
            throw new BusinessException("该 UDI 已存在使用中的登记记录，同一件耗材不允许重复登记");
        }
        BizConsumableStock stock = bizConsumableStockMapper.selectBatchForUpdate(dto.getStockId());
        if (stock == null) {
            throw new BusinessException("出库批次不存在");
        }
        if (!stock.getConsumableId().equals(dto.getConsumableId())) {
            throw new BusinessException("出库批次与所选耗材不一致");
        }
        if (stock.getQuantity().compareTo(ONE) < 0) {
            throw new BusinessException("该批次剩余不足 1 件，无法登记使用");
        }
        if (stock.getExpiryDate() != null && stock.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BusinessException("该批次已过有效期（" + stock.getExpiryDate() + "），禁止使用");
        }
        String patientNo = dto.getPatientNo();
        String patientName = dto.getPatientName();
        if (!TextUtil.hasText(patientNo) || !TextUtil.hasText(patientName)) {
            TracePatientSnapshotVO snap = bizConsumableTraceMapper.selectPatientSnapshot(dto.getPatientId());
            if (snap == null) {
                throw new BusinessException("患者不存在，请重新选择");
            }
            patientNo = TextUtil.hasText(patientNo) ? patientNo : snap.getPatientNo();
            patientName = TextUtil.hasText(patientName) ? patientName : snap.getPatientName();
        }

        UdiParser.UdiParts parts = UdiParser.parse(udiCode);
        BizConsumableTrace trace = new BizConsumableTrace();
        trace.setTraceNo(nextTraceNo());
        trace.setUdiCode(udiCode);
        trace.setUdiDi(parts.getDi());
        trace.setUdiSerial(parts.getSerial());
        trace.setUdiBatch(parts.getBatch());
        trace.setUdiExpiryDate(parts.getExpiryDate());
        trace.setConsumableId(dict.getId());
        trace.setConsumableCode(dict.getConsumableCode());
        trace.setConsumableName(dict.getConsumableName());
        trace.setSpecification(dict.getSpecification());
        trace.setUnit(dict.getUnit());
        trace.setRegCertNo(dict.getRegCertNo());
        trace.setRetailPrice(NumUtil.orZero(dict.getRetailPrice()));
        trace.setStockId(stock.getId());
        trace.setBatchNo(stock.getBatchNo());
        trace.setSupplier(stock.getSupplier());
        trace.setPatientId(dto.getPatientId());
        trace.setPatientNo(patientNo);
        trace.setPatientName(patientName);
        trace.setVisitType(dto.getVisitType());
        trace.setRegistId(dto.getRegistId());
        trace.setAdmissionId(dto.getAdmissionId());
        trace.setDeptId(dto.getDeptId());
        if (dto.getDeptId() != null) {
            trace.setDeptName(bizConsumableStockMapper.selectDeptNameById(dto.getDeptId()));
        }
        trace.setUsageTime(TimeUtil.nowSeconds());
        trace.setOperatorName(operatorName);
        trace.setChargeStatus(0);
        trace.setStatus(1);
        trace.setRemark(dto.getPurpose());
        bizConsumableTraceMapper.insert(trace);

        deductBatch(stock, trace, operatorName);
        tryCharge(trace);
        bizConsumableTraceMapper.updateById(trace);
        return toVo(bizConsumableTraceMapper.selectById(trace.getId()));
    }

    // 私有

    @Override
    public PageResult<BizConsumableTraceVO> selectTracePage(ConsumableTraceQueryPageDTO q) {
        Page<BizConsumableTraceVO> page = bizConsumableTraceMapper.selectTracePage(
                new Page<>(q.getPageNum(), q.getPageSize()),
                blankToNull(q.getKeyword()), q.getConsumableId(), q.getPatientId(),
                q.getChargeStatus(), q.getStatus());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public ConsumableTraceDetailVO getTraceDetailById(Long traceId) {
        ConsumableTraceDetailVO vo = bizConsumableTraceMapper.selectTraceDetail(traceId);
        if (vo == null) {
            throw new BusinessException("溯源记录不存在");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizConsumableTraceVO traceVoid(Long traceId, String reason, String operatorName) {
        BizConsumableTrace trace = bizConsumableTraceMapper.selectById(traceId);
        if (trace == null) {
            throw new BusinessException("溯源记录不存在");
        }
        if (trace.getStatus() != null && trace.getStatus() == 2) {
            throw new BusinessException("该记录已作废，请勿重复操作");
        }
        if (trace.getChargeStatus() != null && trace.getChargeStatus() == 1) {
            throw new BusinessException("该件耗材已计费，请先在收费侧完成退费再作废");
        }
        BizConsumableStock stock = bizConsumableStockMapper.selectBatchForUpdate(trace.getStockId());
        if (stock == null) {
            throw new BusinessException("原出库批次已不存在，无法退库；请核实实物后手工调整库存");
        }
        BigDecimal before = stock.getQuantity();
        stock.setQuantity(before.add(ONE));
        stock.setTotalAmount(stock.getQuantity().multiply(NumUtil.orZero(stock.getCostPrice())));
        if (stock.getStockStatus() != null && stock.getStockStatus() == 3) {
            stock.setStockStatus(stock.getQuantity().compareTo(new BigDecimal("50")) <= 0 ? 2 : 1);
        }
        bizConsumableStockMapper.updateById(stock);

        BizConsumableStockLog back = new BizConsumableStockLog();
        back.setStockId(stock.getId());
        back.setConsumableId(stock.getConsumableId());
        back.setBatchNo(stock.getBatchNo());
        back.setChangeType(3);
        back.setChangeQuantity(ONE);
        back.setQuantityBefore(before);
        back.setQuantityAfter(stock.getQuantity());
        back.setSourceType("trace");
        back.setSourceId(trace.getId());
        back.setSourceNo(trace.getTraceNo());
        back.setOperatorName(operatorName);
        back.setRemark("高值耗材作废退库");
        bizConsumableStockLogMapper.insert(back);

        trace.setStatus(2);
        trace.setVoidTime(TimeUtil.nowSeconds());
        trace.setVoidReason(TextUtil.cut(reason, VOID_REASON_MAX));
        bizConsumableTraceMapper.updateById(trace);
        return toVo(bizConsumableTraceMapper.selectById(traceId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizConsumableTraceVO traceRecharge(Long traceId, String operatorName) {
        BizConsumableTrace trace = bizConsumableTraceMapper.selectById(traceId);
        if (trace == null) {
            throw new BusinessException("溯源记录不存在");
        }
        if (trace.getStatus() != null && trace.getStatus() == 2) {
            throw new BusinessException("已作废的记录不能补记计费");
        }
        if (trace.getChargeStatus() != null && trace.getChargeStatus() == 1) {
            throw new BusinessException("该记录已计费，无需补记");
        }
        if (trace.getRetailPrice() == null || trace.getRetailPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("登记时单价快照为 0，请先核对耗材字典零售价后重新登记");
        }
        tryCharge(trace);
        bizConsumableTraceMapper.updateById(trace);
        return toVo(bizConsumableTraceMapper.selectById(traceId));
    }

    private void deductBatch(BizConsumableStock stock, BizConsumableTrace trace, String operatorName) {
        BigDecimal before = stock.getQuantity();
        stock.setQuantity(before.subtract(ONE));
        stock.setTotalAmount(stock.getQuantity().multiply(NumUtil.orZero(stock.getCostPrice())));
        if (stock.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            stock.setStockStatus(3);
        } else if (stock.getQuantity().compareTo(new BigDecimal("50")) <= 0) {
            stock.setStockStatus(2);
        }
        bizConsumableStockMapper.updateById(stock);

        BizConsumableStockLog out = new BizConsumableStockLog();
        out.setStockId(stock.getId());
        out.setConsumableId(stock.getConsumableId());
        out.setBatchNo(stock.getBatchNo());
        out.setChangeType(7);
        out.setChangeQuantity(ONE.negate());
        out.setQuantityBefore(before);
        out.setQuantityAfter(stock.getQuantity());
        out.setSourceType("trace");
        out.setSourceId(trace.getId());
        out.setSourceNo(trace.getTraceNo());
        out.setOperatorName(operatorName);
        out.setRemark("高值耗材使用出库（" + trace.getPatientName() + "）");
        bizConsumableStockLogMapper.insert(out);
    }

    /**
     * 计费尝试（不抛异常；结果与原因一律落到台账上）。
     * 独立事务由 TraceChargeInvoker 保证：收费侧炸了只回滚收费，登记事实保留。
     */
    private void tryCharge(BizConsumableTrace trace) {
        Integer visitType = trace.getVisitType();
        boolean anchored = visitType != null
                && ((visitType == 1 && trace.getRegistId() != null) || (visitType == 2 && trace.getAdmissionId() != null));
        if (!anchored) {
            trace.setChargeStatus(0);
            trace.setChargeFailReason(TextUtil.cut("未关联就诊（缺门诊挂号或住院锚点），请在收费窗口手工计费", FAIL_REASON_MAX));
            return;
        }
        if (trace.getPatientId() == null || !TextUtil.hasText(trace.getPatientName())) {
            trace.setChargeStatus(2);
            trace.setChargeFailReason(TextUtil.cut("缺少患者快照，这笔费用落不到人，本次未记账，请补全后点补记", FAIL_REASON_MAX));
            return;
        }
        EncounterTypeEnum encounter = EncounterTypeEnum.fromCode(visitType);

        // 一件耗材 = 一条记账行，落库即「1-待结算」：门诊的等收费窗口锁进本次就诊的账单，住院的等出院结算
        FeeBookDTO fee = new FeeBookDTO();
        fee.setPatientId(trace.getPatientId());
        fee.setPatientNo(trace.getPatientNo());
        fee.setPatientName(trace.getPatientName());
        fee.setEncounterType(encounter.getCode());
        // 门诊挂挂号、住院挂入院：记账行没有就诊ID 就是一笔无主费用，日清单查不到它
        fee.setEncounterId(encounter == EncounterTypeEnum.INPATIENT ? trace.getAdmissionId() : trace.getRegistId());
        // 科室用使用科室快照直填：溯源台账没有可反查的临床单据，让反查去猜必然「无归属」
        fee.setDeptId(trace.getDeptId());
        fee.setDeptName(trace.getDeptName());
        fee.setItemType(PaymentItemTypeEnum.CONSUMABLE.getCode());
        fee.setItemCode(trace.getConsumableCode());
        fee.setItemName(trace.getConsumableName());
        fee.setSpecification(trace.getSpecification());
        fee.setUnit(TextUtil.hasText(trace.getUnit()) ? trace.getUnit() : "件");
        fee.setPrice(NumUtil.orZero(trace.getRetailPrice()));
        fee.setQuantity(ONE);
        fee.setSourceType(FeeSourceTypeEnum.CONSUMABLE.getCode());
        // ★ 幂等锚点 = 溯源台账行：一行台账 = 一件耗材 = 一条记账行，补记重入时记账层按它挡重
        fee.setSourceId(trace.getId());
        fee.setSourceNo(trace.getTraceNo());
        // 耗材按实价挂账；目录类别走项目类型推定（本系统无院内医保目录表）
        fee.setCatalogType(FeeCatalogResolver.byItemType(fee.getItemType()));
        fee.setRemark("高值耗材使用计费（" + trace.getConsumableName() + " " + trace.getTraceNo() + "）");
        try {
            BizFeeRecord booked = chargeInvoker.book(fee);
            trace.setChargeStatus(1);
            // fee_no / fee_record_id：四层改造后存的是 L1 记账行的 (fee_no, fee_id)
            trace.setFeeNo(booked.getFeeNo());
            trace.setFeeRecordId(booked.getId());
            // MyBatis-Plus updateById 不写 null：清空旧失败原因用空串覆盖
            trace.setChargeFailReason("");
        } catch (Exception e) {
            log.error("高值耗材计费异常 traceNo={}", trace.getTraceNo(), e);
            trace.setChargeStatus(2);
            trace.setChargeFailReason(TextUtil.cut("计费异常：" + e.getMessage(), FAIL_REASON_MAX));
        }
    }

    private BizConsumableTraceVO toVo(BizConsumableTrace t) {
        BizConsumableTraceVO vo = new BizConsumableTraceVO();
        org.springframework.beans.BeanUtils.copyProperties(t, vo);
        return vo;
    }

    private String nextTraceNo() {
        return "HV" + LocalDateTime.now().format(DateFormats.COMPACT_DATETIME)
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
