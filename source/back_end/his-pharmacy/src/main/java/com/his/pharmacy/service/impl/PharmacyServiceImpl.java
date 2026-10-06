package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.service.RedisSequenceService;
import com.his.common.enums.DrugStockChangeTypeEnum;
import com.his.common.enums.StockRoomEnum;
import com.his.common.enums.StockStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.pharmacy.dto.BizDrugStockUpsertDTO;
import com.his.pharmacy.dto.StockBatchMoveDTO;
import com.his.pharmacy.dto.StockDeductResultDTO;
import com.his.pharmacy.entity.BizDrugStock;
import com.his.pharmacy.entity.BizDrugStockLog;
import com.his.pharmacy.mapper.BizDrugStockLogMapper;
import com.his.pharmacy.mapper.BizDrugStockMapper;
import com.his.pharmacy.service.DrugStockCacheService;
import com.his.pharmacy.service.PharmacyService;
import com.his.pharmacy.vo.BizDrugStockLogVO;
import com.his.pharmacy.vo.BizDrugStockVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 药房服务实现
 * 口径：库存的一切增减都落药品库存流水；发药出库走 FEFO（先过期先出）。
 */
@Service
@RequiredArgsConstructor
public class PharmacyServiceImpl extends ServiceImpl<BizDrugStockMapper, BizDrugStock> implements PharmacyService {

    private final DrugStockCacheService drugStockCacheService;
    private final RedisSequenceService redisSequenceService;
    private final BizDrugStockLogMapper stockLogMapper;

    @Override
    public PageResult<BizDrugStockVO> selectStockPage(String drugName, Integer stockStatus, Integer stockRoom,
                                                      int pageNum, int pageSize) {
        Page<BizDrugStockVO> page = baseMapper.selectStockPageWithDrug(
                new Page<>(pageNum, pageSize), drugName, stockStatus, stockRoom);
        page.getRecords().forEach(this::fillRoomText);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public List<BizDrugStockVO> selectBatchCandidates(Integer stockRoom, String keyword, boolean onlyWithSupplier) {
        List<BizDrugStockVO> rows = baseMapper.selectBatchCandidates(stockRoom, trimToNull(keyword), onlyWithSupplier);
        rows.forEach(this::fillRoomText);
        return rows;
    }

    /** 库位文字在服务端算：前端再抄一份 1/2→药库/药房的映射迟早和枚举漂移 */
    private void fillRoomText(BizDrugStockVO vo) {
        vo.setStockRoomText(StockRoomEnum.getText(vo.getStockRoom()));
    }

    @Override
    public List<BizDrugStockVO> selectStockWarningList(Integer stockStatus) {
        List<BizDrugStockVO> rows = baseMapper.selectStockWithDrugInfo(stockStatus);
        rows.forEach(this::fillRoomText);
        return rows;
    }

    @Override
    public BizDrugStockVO getStockDetailById(Long stockId) {
        BizDrugStockVO vo = baseMapper.selectStockDetailWithDrug(stockId);
        if (vo != null) {
            fillRoomText(vo);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addStock(BizDrugStockUpsertDTO upsertDTO) {
        BizDrugStock stock = new BizDrugStock();
        BeanUtils.copyProperties(upsertDTO, stock);
        if (baseMapper.countSysDrugById(stock.getDrugId()) == 0) {
            throw new BusinessException("药品不存在或已停用，请从药品字典重新选择");
        }
        if (stock.getQuantity() == null || stock.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("库存数量必须大于0");
        }
        if (stock.getCostPrice() == null || stock.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("成本价不能为负数");
        }
        if (stock.getStockRoom() == null) {
            // 手工建批默认落在药房：历史上所有库存都在"药房架上"这一层，不传就当沿用旧口径
            stock.setStockRoom(StockRoomEnum.PHARMACY.getCode());
        }
        if (baseMapper.countSameBatch(stock.getDrugId(), stock.getBatchNo(), stock.getStockRoom()) > 0) {
            throw new BusinessException("该药品此批号在同一个库存地点已有库存，请改为入库操作");
        }
        stock.setId(null);
        stock.setLockedQuantity(BigDecimal.ZERO);
        stock.setAvailableQuantity(stock.getQuantity());
        stock.setTotalAmount(stock.getQuantity().multiply(stock.getCostPrice()));
        stock.setStockStatus(StockStatusEnum.NORMAL.getCode());
        boolean saved = this.save(stock);
        if (saved) {
            // 入库流水（type=1）
            insertLog(stock, 1, stock.getQuantity(), BigDecimal.ZERO, stock.getQuantity(),
                    "manual", null, null, null);
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean inboundStock(Long stockId, BigDecimal quantity) {
        BizDrugStock stock = this.getById(stockId);
        if (stock == null) {
            throw new BusinessException("库存记录不存在");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("入库数量必须大于0");
        }
        BigDecimal before = stock.getQuantity();
        stock.setQuantity(before.add(quantity));
        stock.setAvailableQuantity(stock.getAvailableQuantity().add(quantity));
        stock.setTotalAmount(stock.getQuantity().multiply(stock.getCostPrice()));
        recalcStockStatus(stock);
        boolean result = this.updateById(stock);
        if (result) {
            drugStockCacheService.evictCache(stock.getDrugId());
            insertLog(stock, 1, quantity, before, stock.getQuantity(),
                    "manual", null, null, null);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean outboundStock(Long stockId, BigDecimal quantity) {
        BizDrugStock stock = this.getById(stockId);
        if (stock == null) {
            throw new BusinessException("库存记录不存在");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("出库数量必须大于0");
        }
        if (stock.getAvailableQuantity().compareTo(quantity) < 0) {
            throw new BusinessException("库存不足");
        }
        BigDecimal before = stock.getQuantity();
        stock.setQuantity(before.subtract(quantity));
        stock.setAvailableQuantity(stock.getAvailableQuantity().subtract(quantity));
        stock.setTotalAmount(stock.getQuantity().multiply(stock.getCostPrice()));
        recalcStockStatus(stock);
        boolean result = this.updateById(stock);
        if (result) {
            drugStockCacheService.evictCache(stock.getDrugId());
            insertLog(stock, 4, quantity.negate(), before, stock.getQuantity(),
                    "manual", null, null, null);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockStockByDrug(Long drugId, BigDecimal quantity) {
        if (drugId == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        List<BizDrugStock> batches = baseMapper.selectBatchesByDrugForUpdate(drugId, StockRoomEnum.PHARMACY.getCode());
        // 一个批次都没有 = 这个药还没建批（铺底数据常态）。开方不该被"库房没建档"挡住，
        // 真正的闸门在发药扣库（deductStockFefo 会拒），这里放过不会凭空造药。
        if (batches.isEmpty()) {
            return;
        }
        BigDecimal available = BigDecimal.ZERO;
        for (BizDrugStock b : batches) {
            available = available.add(nvl(b.getAvailableQuantity()));
        }
        if (available.compareTo(quantity) < 0) {
            String drugName = baseMapper.selectDrugNameById(drugId);
            throw new BusinessException("药品库存不足：" + (drugName != null ? drugName : "药品#" + drugId)
                    + " 开方需要 " + quantity + "，现有可用 " + available + "（发药前请先入库）");
        }
        BigDecimal remain = quantity;
        for (BizDrugStock batch : batches) {
            if (remain.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal take = nvl(batch.getAvailableQuantity()).min(remain);
            if (take.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            batch.setAvailableQuantity(nvl(batch.getAvailableQuantity()).subtract(take));
            batch.setLockedQuantity(nvl(batch.getLockedQuantity()).add(take));
            this.updateById(batch);
            remain = remain.subtract(take);
        }
        drugStockCacheService.evictCache(drugId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockStockByDrug(Long drugId, BigDecimal quantity) {
        if (drugId == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        List<BizDrugStock> batches = baseMapper.selectBatchesByDrugForUpdate(drugId, StockRoomEnum.PHARMACY.getCode());
        BigDecimal remain = quantity;
        for (BizDrugStock batch : batches) {
            if (remain.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            // 只退实际锁着的量：发药时锁定量已随扣库放掉，此后再撤方/删病历不能把可用量凭空加回来
            BigDecimal release = nvl(batch.getLockedQuantity()).min(remain);
            if (release.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            batch.setLockedQuantity(nvl(batch.getLockedQuantity()).subtract(release));
            batch.setAvailableQuantity(nvl(batch.getAvailableQuantity()).add(release));
            this.updateById(batch);
            remain = remain.subtract(release);
        }
        drugStockCacheService.evictCache(drugId);
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    @Override
    public BizDrugStock getStockByDrugId(Long drugId) {
        return drugStockCacheService.getStockByDrugId(drugId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockDeductResultDTO deductStockFefo(Long drugId, BigDecimal quantity, String sourceType,
                                             Long sourceId, String sourceNo, String operatorName) {
        // C 类：发药扣库是跨模块内部指令（形参入参，不经 HTTP 绑定），注解够不到
        if (drugId == null) {
            throw new BusinessException("药品ID不能为空");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("扣减数量必须大于0");
        }
        // FEFO 取批次（行锁防并发超扣）。⚠ 只扣药房在架量：药库的整件货没拆零上架就不能发给患者，
        // 想让药进药房只有一条路——调拨单（sql/154），那样中途才看得见"在途"。
        List<BizDrugStock> batches = baseMapper.selectFefoBatchesForUpdate(drugId, StockRoomEnum.PHARMACY.getCode());
        BigDecimal totalBefore = BigDecimal.ZERO;
        for (BizDrugStock b : batches) {
            totalBefore = totalBefore.add(b.getQuantity());
        }
        if (totalBefore.compareTo(quantity) < 0) {
            String drugName = baseMapper.selectDrugNameById(drugId);
            throw new BusinessException("药品库存不足：" + (drugName != null ? drugName : "药品#" + drugId)
                    + " 需要 " + quantity + "，现有 " + totalBefore);
        }
        BigDecimal remain = quantity;
        for (BizDrugStock batch : batches) {
            if (remain.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal take = batch.getQuantity().min(remain);
            BigDecimal before = batch.getQuantity();
            // 开方时锁过的量在发药这一刻被"用掉"：锁定量要一起放掉，否则 available 会被扣两次
            // （锁的时候已经减过一次），账面变成 quantity 与 available-locked 永久对不上。
            BigDecimal locked = batch.getLockedQuantity() == null ? BigDecimal.ZERO : batch.getLockedQuantity();
            BigDecimal release = locked.min(take);
            batch.setQuantity(before.subtract(take));
            batch.setLockedQuantity(locked.subtract(release));
            batch.setAvailableQuantity(batch.getAvailableQuantity().subtract(take).add(release));
            batch.setTotalAmount(batch.getQuantity().multiply(batch.getCostPrice()));
            recalcStockStatus(batch);
            this.updateById(batch);
            drugStockCacheService.evictCache(batch.getDrugId());
            // 发药出库流水（type=2）
            insertLog(batch, 2, take.negate(), before, batch.getQuantity(),
                    sourceType, sourceId, sourceNo, operatorName);
            remain = remain.subtract(take);
        }
        return new StockDeductResultDTO(totalBefore, totalBefore.subtract(quantity));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreStock(Long drugId, BigDecimal quantity, String sourceType,
                             Long sourceId, String sourceNo, String operatorName) {
        // C 类：退药回库由摆药/发药 service 直调，不经 HTTP 绑定
        if (drugId == null) {
            throw new BusinessException("药品ID不能为空");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("回库数量必须大于0");
        }
        // 患者退药回到药房（发出去的就是药房的货，退回来的也必须在药房；退到药库会让
        // 「药房架上有多少」这个数永远说不清）
        BizDrugStock target = baseMapper.selectRestoreTargetForUpdate(drugId, StockRoomEnum.PHARMACY.getCode());
        if (target == null) {
            // 该药品在药房已无任何批次：以来源单号建新批次承接退药
            target = new BizDrugStock();
            target.setDrugId(drugId);
            target.setBatchNo(sourceNo != null ? sourceNo : "RETURN");
            target.setStockRoom(StockRoomEnum.PHARMACY.getCode());
            target.setQuantity(BigDecimal.ZERO);
            target.setLockedQuantity(BigDecimal.ZERO);
            target.setAvailableQuantity(BigDecimal.ZERO);
            target.setCostPrice(BigDecimal.ZERO);
            target.setTotalAmount(BigDecimal.ZERO);
            target.setStockStatus(StockStatusEnum.NORMAL.getCode());
            target.setRemark("退药回库自动建批");
            this.save(target);
        }
        BigDecimal before = target.getQuantity();
        target.setQuantity(before.add(quantity));
        target.setAvailableQuantity(target.getAvailableQuantity().add(quantity));
        target.setTotalAmount(target.getQuantity().multiply(target.getCostPrice()));
        recalcStockStatus(target);
        this.updateById(target);
        drugStockCacheService.evictCache(target.getDrugId());
        // 退药回库流水（type=3）
        insertLog(target, 3, quantity, before, target.getQuantity(),
                sourceType, sourceId, sourceNo, operatorName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizDrugStock inboundByBatch(BizDrugStock batchInfo, BigDecimal quantity,
                                       String sourceType, Long sourceId, String sourceNo, String operatorName) {
        // C 类：入参是调用方（入库单入账）现造的批次实体，不是 HTTP 入参 DTO，注解不生效
        if (batchInfo == null || batchInfo.getDrugId() == null) {
            throw new BusinessException("药品ID不能为空，请从药品字典选择药品");
        }
        if (!StringUtils.hasText(batchInfo.getBatchNo())) {
            throw new BusinessException("批号不能为空");
        }
        if (batchInfo.getExpiryDate() == null) {
            throw new BusinessException("有效期不能为空（没有效期建不了批次）");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("入库数量必须大于0");
        }
        if (batchInfo.getCostPrice() == null || batchInfo.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("进货单价不能为负数");
        }
        if (baseMapper.countSysDrugById(batchInfo.getDrugId()) == 0) {
            throw new BusinessException("药品不存在或已停用，请从药品字典重新选择");
        }
        // 入库落在哪个库位由调用方决定，不传=药房（历史口径：本系统的入库单一直是直接进药房发药的）。
        // 采购收货要改成进药库再下拨，是入库单那一层的改造，不在这里偷偷换默认值。
        Integer room = batchInfo.getStockRoom() == null ? StockRoomEnum.PHARMACY.getCode() : batchInfo.getStockRoom();
        if (StockRoomEnum.fromCode(room) == null) {
            throw new BusinessException("库存地点只能是 1-药库 / 2-药房");
        }

        // 同批号同库位命中：加量。成本按实际进货金额累加、成本价回算均价（不能用旧成本价重算总额，否则两次进价的差额被抹掉）
        BizDrugStock exist = baseMapper.selectByDrugAndBatchForUpdate(batchInfo.getDrugId(), batchInfo.getBatchNo(), room);
        if (exist != null) {
            BigDecimal before = exist.getQuantity();
            BigDecimal oldTotal = exist.getTotalAmount() == null ? BigDecimal.ZERO : exist.getTotalAmount();
            BigDecimal addAmount = quantity.multiply(batchInfo.getCostPrice());
            exist.setQuantity(before.add(quantity));
            exist.setAvailableQuantity(exist.getAvailableQuantity().add(quantity));
            exist.setTotalAmount(oldTotal.add(addAmount));
            if (exist.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                exist.setCostPrice(exist.getTotalAmount().divide(exist.getQuantity(), 2, RoundingMode.HALF_UP));
            }
            recalcStockStatus(exist);
            this.updateById(exist);
            drugStockCacheService.evictCache(exist.getDrugId());
            insertLog(exist, 1, quantity, before, exist.getQuantity(),
                    sourceType, sourceId, sourceNo, operatorName);
            return exist;
        }

        // 同批号未命中：建新批次
        BizDrugStock stock = new BizDrugStock();
        stock.setDrugId(batchInfo.getDrugId());
        stock.setBatchNo(batchInfo.getBatchNo());
        stock.setProductionDate(batchInfo.getProductionDate());
        stock.setExpiryDate(batchInfo.getExpiryDate());
        stock.setCostPrice(batchInfo.getCostPrice());
        stock.setSupplier(batchInfo.getSupplier());
        stock.setSupplierId(batchInfo.getSupplierId());
        stock.setStockRoom(room);
        stock.setLocation(batchInfo.getLocation());
        stock.setQuantity(quantity);
        stock.setLockedQuantity(BigDecimal.ZERO);
        stock.setAvailableQuantity(quantity);
        stock.setTotalAmount(quantity.multiply(batchInfo.getCostPrice()));
        stock.setStockStatus(StockStatusEnum.NORMAL.getCode());
        stock.setCreateBy(operatorName);
        stock.setUpdateBy(operatorName);
        stock.setRemark(batchInfo.getRemark());
        this.save(stock);
        insertLog(stock, 1, quantity, BigDecimal.ZERO, quantity,
                sourceType, sourceId, sourceNo, operatorName);
        return stock;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postStocktakeDiff(Long stockId, BigDecimal diffQuantity, Long stocktakeId,
                                  String stocktakeNo, String operatorName, String reason) {
        // C 类：盘点过账由盘点 service 逐条直调，形参不经 HTTP 绑定
        if (stockId == null) {
            throw new BusinessException("库存批次ID不能为空");
        }
        if (diffQuantity == null || diffQuantity.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("无差异的批次不需要过账");
        }
        BizDrugStock stock = baseMapper.selectByIdForUpdate(stockId);
        if (stock == null) {
            throw new BusinessException("库存批次已不存在（盘点快照对应的批次被删过），请先核对该药品的出入库记录再重开盘点单");
        }
        String drugName = baseMapper.selectDrugNameById(stock.getDrugId());
        String label = (drugName == null ? "药品#" + stock.getDrugId() : drugName)
                + (StringUtils.hasText(stock.getBatchNo()) ? "（批号 " + stock.getBatchNo() + "）" : "");
        BigDecimal before = stock.getQuantity() == null ? BigDecimal.ZERO : stock.getQuantity();
        BigDecimal locked = stock.getLockedQuantity() == null ? BigDecimal.ZERO : stock.getLockedQuantity();
        // 差量叠加、不覆盖余额：快照之后的发药/入库都有自己的流水，覆盖等于把它们抹掉、凭空造药
        BigDecimal after = before.add(diffQuantity);
        if (after.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(label + "：当前库存 " + before + " 承受不了 " + diffQuantity
                    + " 的盘亏（过账后为负），请重新核对实盘数");
        }
        if (after.compareTo(locked) < 0) {
            throw new BusinessException(label + "：过账后只剩 " + after + "，小于已锁定（已开方未发药）的 "
                    + locked + "，请先处理完这些发药再盘");
        }
        BigDecimal costPrice = stock.getCostPrice() == null ? BigDecimal.ZERO : stock.getCostPrice();
        stock.setQuantity(after);
        stock.setAvailableQuantity(after.subtract(locked));
        stock.setTotalAmount(after.multiply(costPrice).setScale(2, RoundingMode.HALF_UP));
        recalcAfterAdjust(stock);
        this.updateById(stock);
        drugStockCacheService.evictCache(stock.getDrugId());
        insertLog(stock, diffQuantity.compareTo(BigDecimal.ZERO) > 0 ? 5 : 6, diffQuantity, before, after,
                "stocktake", stocktakeId, stocktakeNo, operatorName, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizDrugStock deductStockBatch(StockBatchMoveDTO move) {
        if (move == null || move.getStockId() == null) {
            throw new BusinessException("缺少库存批次");
        }
        DrugStockChangeTypeEnum changeType = requireChangeType(move.getChangeType());
        if (!changeType.outbound()) {
            throw new BusinessException("扣减库存的流水类型必须是出库类（当前：" + changeType.getLabel() + "）");
        }
        BigDecimal quantity = requirePositive(move.getQuantity());
        BizDrugStock stock = baseMapper.selectByIdForUpdate(move.getStockId());
        if (stock == null) {
            throw new BusinessException("库存批次不存在或已被删除（批次#" + move.getStockId() + "）");
        }
        String label = batchLabel(stock);
        if (move.getStockRoom() != null && !move.getStockRoom().equals(stock.getStockRoom())) {
            // 库位闸门：调拨单写着"从药房退回药库"，点到的批次却在药库，说明单据和现实已经不一致，
            // 硬扣下去就是把另一层的账搬空——这种不一致必须当场响，不能静默按批次的实际库位扣。
            throw new BusinessException(label + " 实际在「" + StockRoomEnum.getText(stock.getStockRoom())
                    + "」，与本单指定的「" + StockRoomEnum.getText(move.getStockRoom()) + "」不符，请重新选择批次");
        }
        BigDecimal available = nvl(stock.getAvailableQuantity());
        if (available.compareTo(quantity) < 0) {
            throw new BusinessException(label + " 可用量只有 " + plain(available) + "，不够扣 " + plain(quantity)
                    + "（已锁定 " + plain(nvl(stock.getLockedQuantity())) + " 是已开方未发药的量，不许调拨也不许退货）");
        }
        BigDecimal before = nvl(stock.getQuantity());
        stock.setQuantity(before.subtract(quantity));
        stock.setAvailableQuantity(available.subtract(quantity));
        stock.setTotalAmount(stock.getQuantity().multiply(nvl(stock.getCostPrice())).setScale(2, RoundingMode.HALF_UP));
        recalcStockStatus(stock);
        this.updateById(stock);
        drugStockCacheService.evictCache(stock.getDrugId());
        insertLog(stock, changeType.getCode(), quantity.negate(), before, stock.getQuantity(),
                move.getSourceType(), move.getSourceId(), move.getSourceNo(), move.getOperatorName(), move.getReason());
        return stock;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizDrugStock addStockToRoom(StockBatchMoveDTO move) {
        // C 类：入参由调拨/退货 service 现造的内部指令对象，不经 HTTP 绑定，注解不生效
        if (move == null || move.getDrugId() == null) {
            throw new BusinessException("缺少药品");
        }
        DrugStockChangeTypeEnum changeType = requireChangeType(move.getChangeType());
        if (changeType.outbound()) {
            throw new BusinessException("增加库存的流水类型必须是入库类（当前：" + changeType.getLabel() + "）");
        }
        BigDecimal quantity = requirePositive(move.getQuantity());
        // C 类：同上，接收落位的批号与效期来自内部指令，缺了就建不了批次，只能在 service 挡
        if (!StringUtils.hasText(move.getBatchNo())) {
            throw new BusinessException("批号不能为空（不知道批号就落不了批次，事后无从追溯这批药是哪来的）");
        }
        if (move.getExpiryDate() == null) {
            throw new BusinessException("有效期不能为空（没有效期建不了批次）");
        }
        Integer room = move.getStockRoom();
        if (StockRoomEnum.fromCode(room) == null) {
            throw new BusinessException("库存地点只能是 1-药库 / 2-药房");
        }
        BigDecimal costPrice = nvl(move.getCostPrice());

        BizDrugStock exist = baseMapper.selectByDrugAndBatchForUpdate(move.getDrugId(), move.getBatchNo(), room);
        if (exist != null) {
            BigDecimal before = nvl(exist.getQuantity());
            // 成本按"货随价走"累加再加权：调拨本身不是新进货，但同一批号两侧成本价可能已被
            // 各自的入库加权拉出差异，直接沿用接收方旧成本会把这批货的真实成本差抹掉。
            exist.setQuantity(before.add(quantity));
            exist.setAvailableQuantity(nvl(exist.getAvailableQuantity()).add(quantity));
            exist.setTotalAmount(nvl(exist.getTotalAmount()).add(quantity.multiply(costPrice))
                    .setScale(2, RoundingMode.HALF_UP));
            if (exist.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                exist.setCostPrice(exist.getTotalAmount().divide(exist.getQuantity(), 2, RoundingMode.HALF_UP));
            }
            recalcAfterAdjust(exist);
            this.updateById(exist);
            drugStockCacheService.evictCache(exist.getDrugId());
            insertLog(exist, changeType.getCode(), quantity, before, exist.getQuantity(),
                    move.getSourceType(), move.getSourceId(), move.getSourceNo(), move.getOperatorName(), move.getReason());
            return exist;
        }

        BizDrugStock stock = new BizDrugStock();
        stock.setDrugId(move.getDrugId());
        stock.setBatchNo(move.getBatchNo());
        stock.setProductionDate(move.getProductionDate());
        stock.setExpiryDate(move.getExpiryDate());
        stock.setCostPrice(costPrice);
        stock.setSupplier(move.getSupplier());
        stock.setSupplierId(move.getSupplierId());
        stock.setStockRoom(room);
        stock.setQuantity(quantity);
        stock.setLockedQuantity(BigDecimal.ZERO);
        stock.setAvailableQuantity(quantity);
        stock.setTotalAmount(quantity.multiply(costPrice).setScale(2, RoundingMode.HALF_UP));
        stock.setStockStatus(StockStatusEnum.NORMAL.getCode());
        stock.setCreateBy(move.getOperatorName());
        stock.setUpdateBy(move.getOperatorName());
        stock.setRemark(cut(move.getReason(), 500));
        this.save(stock);
        drugStockCacheService.evictCache(stock.getDrugId());
        insertLog(stock, changeType.getCode(), quantity, BigDecimal.ZERO, quantity,
                move.getSourceType(), move.getSourceId(), move.getSourceNo(), move.getOperatorName(), move.getReason());
        return stock;
    }

    private DrugStockChangeTypeEnum requireChangeType(Integer changeType) {
        DrugStockChangeTypeEnum type = DrugStockChangeTypeEnum.fromCode(changeType);
        if (type == null) {
            throw new BusinessException("库存流水类型不合法：" + changeType);
        }
        return type;
    }

    private BigDecimal requirePositive(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("数量必须大于0");
        }
        return quantity.setScale(2, RoundingMode.HALF_UP);
    }

    /** 报错文案里的批次标识：药名 + 批号 + 库位，三者缺一都没法去现场找那一箱药 */
    private String batchLabel(BizDrugStock stock) {
        String drugName = baseMapper.selectDrugNameById(stock.getDrugId());
        StringBuilder sb = new StringBuilder(drugName == null ? "药品#" + stock.getDrugId() : drugName);
        if (StringUtils.hasText(stock.getBatchNo())) {
            sb.append("（批号 ").append(stock.getBatchNo()).append("）");
        }
        sb.append("，库位 ").append(StockRoomEnum.getText(stock.getStockRoom()));
        return sb.toString();
    }

    @Override
    public PageResult<BizDrugStockLogVO> selectStockLogPage(String drugName, Integer changeType,
                                                            int pageNum, int pageSize) {
        Page<BizDrugStockLogVO> page = stockLogMapper.selectLogPageWithDrug(
                new Page<>(pageNum, pageSize), drugName, changeType);
        page.getRecords().forEach(this::fillLogText);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    /** 流水的类型与库位文字都在服务端算（前端抄映射必漂移） */
    private void fillLogText(BizDrugStockLogVO vo) {
        vo.fillTexts();
    }

    /**
     * 批次库存状态回算：数量归零 → 缺货；可用归零 → 预警（全部在途锁定）；只做下探不回改
     */
    private void recalcStockStatus(BizDrugStock stock) {
        if (stock.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            stock.setStockStatus(3); // 缺货
        } else if (stock.getAvailableQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            stock.setStockStatus(2); // 预警（全部在途锁定）
        }
    }

    /**
     * 盘点过账后的批次状态回算。与 {@link #recalcStockStatus} 的「只下探不回改」不同：
     * 盘盈的目的就是让缺货的批次重新有货，状态不跟着抬回去，库存预警列表会继续说谎。
     * <br>4-过期是效期事实，不因盘盈而消失，原样保留。
     */
    private void recalcAfterAdjust(BizDrugStock stock) {
        if (stock.getStockStatus() != null && stock.getStockStatus() == 4) {
            return;
        }
        if (stock.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            stock.setStockStatus(StockStatusEnum.OUT_OF_STOCK.getCode());
        } else if (stock.getAvailableQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            stock.setStockStatus(StockStatusEnum.WARNING.getCode());
        } else {
            stock.setStockStatus(StockStatusEnum.NORMAL.getCode());
        }
    }

    private void insertLog(BizDrugStock batch, int changeType, BigDecimal changeQuantity,
                           BigDecimal before, BigDecimal after, String sourceType,
                           Long sourceId, String sourceNo, String operatorName) {
        insertLog(batch, changeType, changeQuantity, before, after, sourceType, sourceId, sourceNo, operatorName, null);
    }

    private void insertLog(BizDrugStock batch, int changeType, BigDecimal changeQuantity,
                           BigDecimal before, BigDecimal after, String sourceType,
                           Long sourceId, String sourceNo, String operatorName, String remark) {
        BizDrugStockLog log = new BizDrugStockLog();
        log.setStockId(batch.getId());
        log.setDrugId(batch.getDrugId());
        log.setBatchNo(batch.getBatchNo());
        log.setChangeType(changeType);
        log.setChangeQuantity(changeQuantity);
        log.setQuantityBefore(before);
        log.setQuantityAfter(after);
        log.setSourceType(sourceType);
        log.setSourceId(sourceId);
        log.setSourceNo(sourceNo);
        log.setOperatorName(operatorName);
        // remark 列宽 500：把盘点差异说明原样拼进来可能超长，超长会把「过账」升级成 500（AGENTS §3）
        log.setRemark(cut(remark, 500));
        stockLogMapper.insert(log);
    }

    /** 截到列宽（列宽是事实，入参层不做长度校验，避免把「说明写长了」变成请求失败） */
    private static String cut(String text, int max) {
        if (text == null) {
            return null;
        }
        String t = text.trim();
        return t.length() <= max ? t : t.substring(0, max);
    }

    private static String trimToNull(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    /** 数量显示：5.00 → 5，2.50 → 2.5（报错文案里不想看到一串尾零） */
    private static String plain(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }
}
