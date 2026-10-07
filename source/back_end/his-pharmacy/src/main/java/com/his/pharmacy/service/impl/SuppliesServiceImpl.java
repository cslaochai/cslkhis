package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TimeUtil;
import com.his.pharmacy.dto.ConsumableUpsertDTO;
import com.his.pharmacy.entity.BizConsumableConsume;
import com.his.pharmacy.entity.BizConsumableStock;
import com.his.pharmacy.entity.BizConsumableStockLog;
import com.his.pharmacy.entity.SysConsumable;
import com.his.pharmacy.mapper.BizConsumableConsumeMapper;
import com.his.pharmacy.mapper.BizConsumableStockLogMapper;
import com.his.pharmacy.mapper.BizConsumableStockMapper;
import com.his.pharmacy.mapper.SysConsumableMapper;
import com.his.pharmacy.service.SuppliesService;
import com.his.pharmacy.vo.*;
import com.his.system.provider.DeptScopeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 物资耗材服务实现
 * 口径：库存一切增减落流水；领用即扣库存（FEFO）并落台账；缺字典/缺批号/超库存一律 BusinessException。
 */
@Service
@RequiredArgsConstructor
public class SuppliesServiceImpl extends ServiceImpl<BizConsumableStockMapper, BizConsumableStock> implements SuppliesService {
    private final DeptScopeProvider deptScopeProvider;

    private final SysConsumableMapper sysConsumableMapper;
    private final BizConsumableStockLogMapper bizConsumableStockLogMapper;
    private final BizConsumableConsumeMapper bizConsumableConsumeMapper;

    // 字典

    @Override
    public PageResult<SysConsumableVO> selectConsumablePage(String keyword, Integer category, Integer status,
                                                            int pageNum, int pageSize) {
        Page<SysConsumableVO> page = sysConsumableMapper.selectConsumablePage(
                new Page<>(pageNum, pageSize), keyword, category, status);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public SysConsumableVO getConsumableById(Long id) {
        SysConsumable entity = sysConsumableMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        SysConsumableVO vo = new SysConsumableVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consumableUpsert(ConsumableUpsertDTO dto) {
        if (sysConsumableMapper.countByCode(dto.getConsumableCode(), dto.getId()) > 0) {
            throw new BusinessException("耗材编码已存在：" + dto.getConsumableCode());
        }
        SysConsumable entity = new SysConsumable();
        entity.setId(dto.getId());
        entity.setConsumableCode(dto.getConsumableCode());
        entity.setConsumableName(dto.getConsumableName());
        entity.setCategory(dto.getCategory() == null ? 5 : dto.getCategory());
        entity.setSpecification(dto.getSpecification());
        entity.setUnit(dto.getUnit());
        entity.setManufacturer(dto.getManufacturer());
        entity.setRetailPrice(dto.getRetailPrice() == null ? BigDecimal.ZERO : dto.getRetailPrice());
        entity.setIsHighValue(dto.getIsHighValue() == null ? 0 : dto.getIsHighValue());
        entity.setUdiDi(StringUtils.hasText(dto.getUdiDi()) ? dto.getUdiDi().trim() : null);
        entity.setRegCertNo(dto.getRegCertNo());
        entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        entity.setRemark(dto.getRemark());
        if (dto.getId() == null) {
            return sysConsumableMapper.insert(entity) > 0;
        }
        if (sysConsumableMapper.selectById(dto.getId()) == null) {
            throw new BusinessException("待修改的耗材不存在");
        }
        return sysConsumableMapper.updateById(entity) > 0;
    }

    @Override
    public List<ConsumableSelectListVO> selectEnabledConsumables() {
        return sysConsumableMapper.selectEnabledList();
    }

    // 库存

    @Override
    public PageResult<BizConsumableStockVO> selectStockPage(String keyword, Integer category, Integer stockStatus,
                                                            int pageNum, int pageSize) {
        Page<BizConsumableStockVO> page = baseMapper.selectStockPage(
                new Page<>(pageNum, pageSize), keyword, category, stockStatus);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public BizConsumableStockVO getStockDetailById(Long stockId) {
        return baseMapper.selectStockDetail(stockId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addStock(Long consumableId, String batchNo, LocalDate productionDate, LocalDate expiryDate,
                            BigDecimal quantity, BigDecimal costPrice, String location, String supplier,
                            String operatorName) {
        if (baseMapper.countConsumableById(consumableId) == 0) {
            throw new BusinessException("耗材不存在或已停用，请从耗材字典重新选择");
        }
        // ③ 业务规则（取值范围）：非「字段填没填」，DTO 注解放不下
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("库存数量必须大于0");
        }
        // ③ 业务规则（取值范围）
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("成本价不能为负数");
        }
        if (baseMapper.countSameBatch(consumableId, batchNo) > 0) {
            throw new BusinessException("该耗材此批号已存在库存，请走补货入库");
        }
        BizConsumableStock stock = new BizConsumableStock();
        stock.setConsumableId(consumableId);
        stock.setBatchNo(batchNo);
        stock.setProductionDate(productionDate);
        stock.setExpiryDate(expiryDate);
        stock.setQuantity(quantity);
        stock.setCostPrice(costPrice);
        stock.setTotalAmount(quantity.multiply(costPrice));
        stock.setLocation(location);
        stock.setSupplier(supplier);
        stock.setStockStatus(1);
        stock.setRemark("建批入库");
        boolean saved = this.save(stock);
        if (saved) {
            insertLog(stock, 1, quantity, BigDecimal.ZERO, quantity, "manual", null, null, operatorName);
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean inboundStock(Long stockId, BigDecimal quantity, String operatorName) {
        BizConsumableStock stock = requireStock(stockId);
        requirePositive(quantity, "入库数量必须大于0");
        BigDecimal before = stock.getQuantity();
        stock.setQuantity(before.add(quantity));
        stock.setTotalAmount(stock.getQuantity().multiply(stock.getCostPrice()));
        recalcStockStatus(stock);
        boolean result = this.updateById(stock);
        if (result) {
            insertLog(stock, 1, quantity, before, stock.getQuantity(), "manual", null, null, operatorName);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean outboundStock(Long stockId, BigDecimal quantity, String operatorName) {
        BizConsumableStock stock = requireStock(stockId);
        requirePositive(quantity, "出库数量必须大于0");
        if (stock.getQuantity().compareTo(quantity) < 0) {
            throw new BusinessException("库存不足：现有 " + stock.getQuantity());
        }
        BigDecimal before = stock.getQuantity();
        stock.setQuantity(before.subtract(quantity));
        stock.setTotalAmount(stock.getQuantity().multiply(stock.getCostPrice()));
        recalcStockStatus(stock);
        boolean result = this.updateById(stock);
        if (result) {
            insertLog(stock, 4, quantity.negate(), before, stock.getQuantity(), "manual", null, null, operatorName);
        }
        return result;
    }

    // 科室领用

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consume(Long consumableId, BigDecimal quantity, Long deptId, String purpose, String operatorName) {
        requirePositive(quantity, "领用数量必须大于0");
        String consumableName = baseMapper.selectConsumableNameById(consumableId);
        if (consumableName == null) {
            throw new BusinessException("耗材不存在或已停用");
        }

        // FEFO 扣减（行锁，任一批不足整体失败）
        List<BizConsumableStock> batches = baseMapper.selectFefoBatchesForUpdate(consumableId);
        BigDecimal totalBefore = BigDecimal.ZERO;
        for (BizConsumableStock b : batches) {
            totalBefore = totalBefore.add(b.getQuantity());
        }
        if (totalBefore.compareTo(quantity) < 0) {
            throw new BusinessException("耗材库存不足：" + consumableName + " 需要领用 " + quantity + "，现有 " + totalBefore);
        }
        String consumeNo = nextConsumeNo();
        BizConsumableConsume record = new BizConsumableConsume();
        record.setConsumeNo(consumeNo);
        record.setConsumableId(consumableId);
        record.setConsumableName(consumableName);
        record.setQuantity(quantity);
        record.setDeptId(deptId);
        if (deptId != null) {
            record.setDeptName(baseMapper.selectDeptNameById(deptId));
        }
        record.setPurpose(purpose);
        record.setConsumeTime(TimeUtil.nowSeconds());
        record.setOperatorName(operatorName);
        record.setStockBefore(totalBefore);
        record.setStockAfter(totalBefore.subtract(quantity));
        bizConsumableConsumeMapper.insert(record);

        BigDecimal remain = quantity;
        for (BizConsumableStock batch : batches) {
            if (remain.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal take = batch.getQuantity().min(remain);
            BigDecimal before = batch.getQuantity();
            batch.setQuantity(before.subtract(take));
            batch.setTotalAmount(batch.getQuantity().multiply(batch.getCostPrice()));
            recalcStockStatus(batch);
            this.updateById(batch);
            insertLog(batch, 2, take.negate(), before, batch.getQuantity(), "consume",
                    record.getId(), consumeNo, operatorName);
            remain = remain.subtract(take);
        }
        return true;
    }

    @Override
    public PageResult<BizConsumableConsumeVO> selectConsumePage(String keyword, Long deptId,
                                                                int pageNum, int pageSize) {
        // 科室数据权限收口（M6）：领用台账归属领用科室；传了 deptId 先越权校验，
        // 没传且受限则收敛到授权科室集合（不再等于看全院台账）。
        Long scopedDeptId = deptScopeProvider.resolveDeptId(deptId);
        if (scopedDeptId != null) {
            deptId = scopedDeptId;
        }
        List<Long> scopeDeptIds = (deptScopeProvider.isScoped() && scopedDeptId == null)
                ? List.copyOf(deptScopeProvider.allowedDeptIds()) : null;
        Page<BizConsumableConsumeVO> page = bizConsumableConsumeMapper.selectConsumePage(
                new Page<>(pageNum, pageSize), keyword, deptId, scopeDeptIds);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public PageResult<BizConsumableStockLogVO> selectStockLogPage(String keyword, Integer changeType,
                                                                  int pageNum, int pageSize) {
        Page<BizConsumableStockLogVO> page = bizConsumableStockLogMapper.selectLogPage(
                new Page<>(pageNum, pageSize), keyword, changeType);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    // 私有

    private BizConsumableStock requireStock(Long stockId) {
        BizConsumableStock stock = this.getById(stockId);
        if (stock == null) {
            throw new BusinessException("库存批次不存在");
        }
        return stock;
    }

    private void requirePositive(BigDecimal quantity, String message) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(message);
        }
    }

    private void recalcStockStatus(BizConsumableStock stock) {
        if (stock.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            stock.setStockStatus(3); // 缺货
        } else if (stock.getQuantity().compareTo(new BigDecimal("50")) <= 0) {
            stock.setStockStatus(2); // 低库存预警
        }
    }

    private void insertLog(BizConsumableStock batch, int changeType, BigDecimal changeQuantity,
                           BigDecimal before, BigDecimal after, String sourceType,
                           Long sourceId, String sourceNo, String operatorName) {
        BizConsumableStockLog log = new BizConsumableStockLog();
        log.setStockId(batch.getId());
        log.setConsumableId(batch.getConsumableId());
        log.setBatchNo(batch.getBatchNo());
        log.setChangeType(changeType);
        log.setChangeQuantity(changeQuantity);
        log.setQuantityBefore(before);
        log.setQuantityAfter(after);
        log.setSourceType(sourceType);
        log.setSourceId(sourceId);
        log.setSourceNo(sourceNo);
        log.setOperatorName(operatorName);
        bizConsumableStockLogMapper.insert(log);
    }

    private String nextConsumeNo() {
        return "LC" + LocalDateTime.now().format(DateFormats.COMPACT_DATETIME)
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
