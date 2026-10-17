package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.DrugStockChangeTypeEnum;
import com.his.common.enums.DrugTransferStatusEnum;
import com.his.common.enums.DrugTransferTypeEnum;
import com.his.common.enums.StockRoomEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.pharmacy.dto.DrugTransferActionDTO;
import com.his.pharmacy.dto.DrugTransferQueryPageDTO;
import com.his.pharmacy.dto.DrugTransferUpsertDTO;
import com.his.pharmacy.dto.StockBatchMoveDTO;
import com.his.pharmacy.entity.BizDrugStock;
import com.his.pharmacy.entity.BizDrugTransfer;
import com.his.pharmacy.entity.BizDrugTransferItem;
import com.his.pharmacy.mapper.BizDrugStockLogMapper;
import com.his.pharmacy.mapper.BizDrugStockMapper;
import com.his.pharmacy.mapper.BizDrugTransferItemMapper;
import com.his.pharmacy.mapper.BizDrugTransferMapper;
import com.his.pharmacy.service.DrugTransferService;
import com.his.pharmacy.service.PharmacyService;
import com.his.pharmacy.vo.BizDrugStockLogVO;
import com.his.pharmacy.vo.BizDrugStockVO;
import com.his.pharmacy.vo.DrugTransferVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 药品调拨服务实现（sql/154）
 */
@Service
@RequiredArgsConstructor
public class DrugTransferServiceImpl extends ServiceImpl<BizDrugTransferMapper, BizDrugTransfer> implements DrugTransferService {

    /**
     * 库存流水的来源单据类型（与药品库存流水的「来源单据类型」取值一致）
     */
    private static final String SOURCE_TYPE = "drugTransfer";

    private final BizDrugTransferMapper bizDrugTransferMapper;
    private final BizDrugTransferItemMapper bizDrugTransferItemMapper;
    private final BizDrugStockMapper bizDrugStockMapper;
    private final BizDrugStockLogMapper bizDrugStockLogMapper;
    private final PharmacyService pharmacyService;
    private final RedisSequenceService redisSequenceService;

    /**
     * 抽屉里的流水要带库位与类型文案，和流水台账同一口径（前端不许自己映射）
     */
    private static List<BizDrugStockLogVO> logsWithTexts(List<BizDrugStockLogVO> logs) {
        logs.forEach(BizDrugStockLogVO::fillTexts);
        return logs;
    }

    private static String batchLabel(BizDrugStockVO batch) {
        String label = TextUtil.hasText(batch.getDrugName()) ? batch.getDrugName() : "药品#" + batch.getDrugId();
        return TextUtil.hasText(batch.getBatchNo()) ? label + "（批号 " + batch.getBatchNo() + "）" : label;
    }

    @Override
    public PageResult<DrugTransferVO> listPage(DrugTransferQueryPageDTO query) {
        Page<DrugTransferVO> page = bizDrugTransferMapper.selectTransferPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                TextUtil.trimToNull(query.getTransferNo()), query.getTransferType(), query.getStatus(),
                TextUtil.trimToNull(query.getKeyword()), TextUtil.trimToNull(query.getDateStart()), TextUtil.trimToNull(query.getDateEnd()));
        page.getRecords().forEach(this::fillText);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public DrugTransferVO getDetailById(Long id) {
        DrugTransferVO vo = bizDrugTransferMapper.selectTransferById(id);
        if (vo == null) {
            throw new BusinessException("调拨单不存在或已删除");
        }
        fillText(vo);
        vo.setItems(bizDrugTransferItemMapper.selectByTransferId(id));
        vo.setLogs(logsWithTexts(bizDrugStockLogMapper.selectBySource(SOURCE_TYPE, id)));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTransferVO upsert(DrugTransferUpsertDTO dto) {
        DrugTransferTypeEnum type = DrugTransferTypeEnum.fromCode(dto.getTransferType());
        String reason = TextUtil.cut(TextUtil.requireTrimmed(dto.getReason(), "调拨事由不能为空"), 200);
        String operator = UserUtils.getCurrentUser().getRealName();
        LocalDateTime now = TimeUtil.nowSeconds();
        List<BizDrugTransferItem> rows = buildItems(dto.getItems(), type, operator);

        if (dto.getId() == null) {
            BizDrugTransfer head = new BizDrugTransfer();
            head.setTransferNo(redisSequenceService.generateDrugTransferNo());
            head.setTransferType(type.getCode());
            head.setFromRoom(type.getFromRoom().getCode());
            head.setToRoom(type.getToRoom().getCode());
            head.setReason(reason);
            head.setStatus(DrugTransferStatusEnum.PENDING_OUT.getCode());
            head.setTotalItems(0);
            head.setTotalQuantity(BigDecimal.ZERO);
            head.setOutQuantity(BigDecimal.ZERO);
            head.setInQuantity(BigDecimal.ZERO);
            head.setTotalAmount(BigDecimal.ZERO);
            head.setRemark(TextUtil.cut(dto.getRemark(), 500));
            head.setCreateBy(operator);
            head.setUpdateBy(operator);
            head.setCreateTime(now);
            head.setUpdateTime(now);
            if (bizDrugTransferMapper.insert(head) != 1) {
                throw new BusinessException("生成调拨单失败");
            }
            rows.forEach(row -> row.setTransferId(head.getId()));
            rows.forEach(bizDrugTransferItemMapper::insert);
            refreshSummary(head.getId(), operator, now);
            return getDetailById(head.getId());
        }

        BizDrugTransfer cur = lock(dto.getId());
        requireStatus(cur, DrugTransferStatusEnum.PENDING_OUT, "修改调拨明细");
        // 整单替换明细：uk_transfer_stock(transfer_id, stock_id) 不含 del_flag，软删再插必撞键
        bizDrugTransferItemMapper.purgeByTransferId(cur.getId());
        bizDrugTransferMapper.update(null, Wrappers.<BizDrugTransfer>lambdaUpdate()
                .eq(BizDrugTransfer::getId, cur.getId())
                .set(BizDrugTransfer::getTransferType, type.getCode())
                .set(BizDrugTransfer::getFromRoom, type.getFromRoom().getCode())
                .set(BizDrugTransfer::getToRoom, type.getToRoom().getCode())
                .set(BizDrugTransfer::getReason, reason)
                .set(BizDrugTransfer::getRemark, TextUtil.cut(dto.getRemark(), 500))
                .set(BizDrugTransfer::getUpdateBy, operator)
                .set(BizDrugTransfer::getUpdateTime, now));
        rows.forEach(row -> row.setTransferId(cur.getId()));
        rows.forEach(bizDrugTransferItemMapper::insert);
        refreshSummary(cur.getId(), operator, now);
        return getDetailById(cur.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTransferVO confirmOut(DrugTransferActionDTO dto) {
        BizDrugTransfer head = lock(dto.getId());
        requireStatus(head, DrugTransferStatusEnum.PENDING_OUT, "确认发出");
        String operator = UserUtils.getCurrentUser().getRealName();
        LocalDateTime now = TimeUtil.nowSeconds();
        List<BizDrugTransferItem> items = listItems(head.getId());
        if (items.isEmpty()) {
            throw new BusinessException("这张调拨单没有明细，先把批次选进来再发出");
        }
        // 逐批扣减 + 逐批落 type=7 流水；任一批次被闸门挡下就整单回滚，不留"搬了一半"的账
        for (BizDrugTransferItem item : items) {
            StockBatchMoveDTO move = new StockBatchMoveDTO();
            move.setStockId(item.getStockId());
            move.setStockRoom(head.getFromRoom());
            move.setQuantity(item.getApplyQuantity());
            move.setChangeType(DrugStockChangeTypeEnum.TRANSFER_OUT.getCode());
            move.setSourceType(SOURCE_TYPE);
            move.setSourceId(head.getId());
            move.setSourceNo(head.getTransferNo());
            move.setOperatorName(operator);
            move.setReason(transferLabel(head, item, "发出"));
            pharmacyService.deductStockBatch(move);
            bizDrugTransferItemMapper.markOut(item.getId(), operator, now);
        }
        bizDrugTransferMapper.update(null, Wrappers.<BizDrugTransfer>lambdaUpdate()
                .eq(BizDrugTransfer::getId, head.getId())
                .set(BizDrugTransfer::getStatus, DrugTransferStatusEnum.PENDING_IN.getCode())
                .set(BizDrugTransfer::getOutBy, operator)
                .set(BizDrugTransfer::getOutTime, now)
                .set(BizDrugTransfer::getUpdateBy, operator)
                .set(BizDrugTransfer::getUpdateTime, now));
        refreshSummary(head.getId(), operator, now);
        return getDetailById(head.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTransferVO confirmIn(DrugTransferActionDTO dto) {
        BizDrugTransfer head = lock(dto.getId());
        requireStatus(head, DrugTransferStatusEnum.PENDING_IN, "确认接收");
        String operator = UserUtils.getCurrentUser().getRealName();
        LocalDateTime now = TimeUtil.nowSeconds();
        List<BizDrugTransferItem> items = listItems(head.getId());
        long pending = items.stream().filter(i -> !Integer.valueOf(1).equals(i.getInFlag())).count();
        if (pending == 0) {
            throw new BusinessException("本单批次已全部接收，无需重复操作");
        }
        for (BizDrugTransferItem item : items) {
            if (Integer.valueOf(1).equals(item.getInFlag())) {
                continue;
            }
            // 供应商随货走：接收方新建批次时要带上原批次的供应商，否则这一层库存从此无从退货。
            // 原批次此时数量为 0 但行还在（发完的批次不删），查不到只可能是被人物理删了。
            BizDrugStock from = bizDrugStockMapper.selectById(item.getStockId());
            if (from == null) {
                throw new BusinessException("发出方批次已不存在（" + item.getDrugName() + " 批号 "
                        + item.getBatchNo() + "），无法确定随货供应商，请先核对该批次的出入库流水");
            }
            StockBatchMoveDTO move = new StockBatchMoveDTO();
            move.setStockId(item.getStockId());
            move.setDrugId(item.getDrugId());
            move.setBatchNo(item.getBatchNo());
            move.setProductionDate(item.getProductionDate());
            move.setExpiryDate(item.getExpiryDate());
            move.setCostPrice(item.getCostPrice());
            move.setSupplier(from.getSupplier());
            move.setSupplierId(from.getSupplierId());
            move.setStockRoom(head.getToRoom());
            move.setQuantity(item.getApplyQuantity());
            move.setChangeType(DrugStockChangeTypeEnum.TRANSFER_IN.getCode());
            move.setSourceType(SOURCE_TYPE);
            move.setSourceId(head.getId());
            move.setSourceNo(head.getTransferNo());
            move.setOperatorName(operator);
            move.setReason(transferLabel(head, item, "接收"));
            BizDrugStock landed = pharmacyService.addStockToRoom(move);
            bizDrugTransferItemMapper.markIn(item.getId(), landed.getId(), operator, now);
        }
        bizDrugTransferMapper.update(null, Wrappers.<BizDrugTransfer>lambdaUpdate()
                .eq(BizDrugTransfer::getId, head.getId())
                .set(BizDrugTransfer::getStatus, DrugTransferStatusEnum.DONE.getCode())
                .set(BizDrugTransfer::getInBy, operator)
                .set(BizDrugTransfer::getInTime, now)
                .set(BizDrugTransfer::getUpdateBy, operator)
                .set(BizDrugTransfer::getUpdateTime, now));
        refreshSummary(head.getId(), operator, now);
        return getDetailById(head.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTransferVO cancel(DrugTransferActionDTO dto) {
        BizDrugTransfer head = lock(dto.getId());
        requireStatus(head, DrugTransferStatusEnum.PENDING_OUT, "作废");
        // ①条件必填：作废原因只在取消入口必填（同一动作 DTO 被确认入口复用），注解一刀切会挡掉合法的确认
        String reason = TextUtil.cut(TextUtil.requireTrimmed(dto.getReason(), "作废原因不能为空"), 200);
        String operator = UserUtils.getCurrentUser().getRealName();
        LocalDateTime now = TimeUtil.nowSeconds();
        // 待发出=一行库存都没动过，作废只是把单子关掉；一旦发出就成了在途，作废会让药凭空消失，
        // 想收回去只有一条路：开一张反向调拨单，让流水把它讲清楚。
        bizDrugTransferMapper.update(null, Wrappers.<BizDrugTransfer>lambdaUpdate()
                .eq(BizDrugTransfer::getId, head.getId())
                .set(BizDrugTransfer::getStatus, DrugTransferStatusEnum.CANCELLED.getCode())
                .set(BizDrugTransfer::getCancelBy, operator)
                .set(BizDrugTransfer::getCancelTime, now)
                .set(BizDrugTransfer::getCancelReason, reason)
                .set(BizDrugTransfer::getUpdateBy, operator)
                .set(BizDrugTransfer::getUpdateTime, now));
        return getDetailById(head.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        BizDrugTransfer head = lock(id);
        DrugTransferStatusEnum status = DrugTransferStatusEnum.fromCode(head.getStatus());
        if (status != DrugTransferStatusEnum.PENDING_OUT && status != DrugTransferStatusEnum.CANCELLED) {
            throw new BusinessException("只有「待发出」或「已作废」的调拨单可以删除（当前："
                    + DrugTransferStatusEnum.getText(head.getStatus()) + "）；已发出请先完成接收，"
                    + "已完成是留档凭证不能抹");
        }
        bizDrugTransferItemMapper.purgeByTransferId(head.getId());
        if (bizDrugTransferMapper.purgeById(head.getId()) != 1) {
            throw new BusinessException("删除调拨单失败");
        }
    }

    /**
     * 校验并组装明细：批次存在、库位对得上、数量合法、可用量够。
     *
     * <p>可用量在建单就挡一道（发出时 {@code deductStockBatch} 还会再挡一道）：
     * 建单时的报错是「你选的这批货不够」，发出时的报错是一整单回滚，后者对用户难得多。
     */
    private List<BizDrugTransferItem> buildItems(List<DrugTransferUpsertDTO.Item> rows, DrugTransferTypeEnum type,
                                                 String operator) {
        Set<Long> seen = new HashSet<>();
        List<Long> stockIds = new ArrayList<>(rows.size());
        for (DrugTransferUpsertDTO.Item row : rows) {
            if (row.getStockId() == null || !seen.add(row.getStockId())) {
                throw new BusinessException("同一条明细缺少批次，或同一批次在一单里重复出现（批次#"
                        + row.getStockId() + "）");
            }
            stockIds.add(row.getStockId());
        }
        Map<Long, BizDrugStockVO> byId = bizDrugStockMapper.selectBatchSnapshots(stockIds).stream()
                .collect(Collectors.toMap(BizDrugStockVO::getId, Function.identity(), (a, b) -> a));
        List<BizDrugTransferItem> items = new ArrayList<>(rows.size());
        for (DrugTransferUpsertDTO.Item row : rows) {
            BizDrugStockVO batch = byId.get(row.getStockId());
            if (batch == null) {
                throw new BusinessException("库存批次不存在或已被删除（批次#" + row.getStockId() + "）");
            }
            if (!Objects.equals(batch.getStockRoom(), type.getFromRoom().getCode())) {
                throw new BusinessException(batchLabel(batch) + " 实际在「" + StockRoomEnum.getText(batch.getStockRoom())
                        + "」，与本单方向「" + type.getLabel() + "」的发出库位不符，请重新选择批次");
            }
            BigDecimal quantity = NumUtil.scale(row.getApplyQuantity(), 2);
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(batchLabel(batch) + " 的调拨数量必须大于 0");
            }
            BigDecimal available = NumUtil.orZero(batch.getAvailableQuantity());
            if (available.compareTo(quantity) < 0) {
                throw new BusinessException(batchLabel(batch) + " 可用量只有 " + NumUtil.plain(NumUtil.orZero(available)) + "，不够调 "
                        + NumUtil.plain(NumUtil.orZero(quantity)) + "（已锁定 " + NumUtil.plain(NumUtil.orZero(NumUtil.orZero(batch.getLockedQuantity())))
                        + " 是已开方未发药的量，不许调拨）");
            }
            BizDrugTransferItem item = new BizDrugTransferItem();
            item.setStockId(batch.getId());
            item.setDrugId(batch.getDrugId());
            item.setDrugCode(batch.getDrugCode());
            item.setDrugName(batch.getDrugName());
            item.setSpecification(batch.getSpecification());
            item.setUnit(batch.getUnit());
            item.setBatchNo(batch.getBatchNo());
            item.setProductionDate(batch.getProductionDate());
            item.setExpiryDate(batch.getExpiryDate());
            item.setCostPrice(NumUtil.orZero(batch.getCostPrice()));
            item.setApplyQuantity(quantity);
            item.setLockedQuantity(NumUtil.orZero(batch.getLockedQuantity()));
            item.setOutFlag(0);
            item.setInFlag(0);
            item.setRemark(TextUtil.cut(row.getRemark(), 500));
            item.setCreateBy(operator);
            item.setUpdateBy(operator);
            items.add(item);
        }
        return items;
    }

    /**
     * 把明细汇总的五个口径数回写主单（列表页直接读主单，不再逐单算明细）
     */
    private void refreshSummary(Long transferId, String operator, LocalDateTime now) {
        BizDrugTransfer sum = bizDrugTransferItemMapper.selectSummary(transferId);
        bizDrugTransferMapper.update(null, Wrappers.<BizDrugTransfer>lambdaUpdate()
                .eq(BizDrugTransfer::getId, transferId)
                .set(BizDrugTransfer::getTotalItems, sum.getTotalItems())
                .set(BizDrugTransfer::getTotalQuantity, NumUtil.orZero(sum.getTotalQuantity()))
                .set(BizDrugTransfer::getOutQuantity, NumUtil.orZero(sum.getOutQuantity()))
                .set(BizDrugTransfer::getInQuantity, NumUtil.orZero(sum.getInQuantity()))
                .set(BizDrugTransfer::getTotalAmount, NumUtil.orZero(sum.getTotalAmount()))
                .set(BizDrugTransfer::getUpdateBy, operator)
                .set(BizDrugTransfer::getUpdateTime, now));
    }

    private List<BizDrugTransferItem> listItems(Long transferId) {
        return bizDrugTransferItemMapper.selectList(Wrappers.<BizDrugTransferItem>lambdaQuery()
                .eq(BizDrugTransferItem::getTransferId, transferId)
                .orderByAsc(BizDrugTransferItem::getId));
    }

    private BizDrugTransfer lock(Long id) {
        BizDrugTransfer head = bizDrugTransferMapper.selectByIdForUpdate(id);
        if (head == null) {
            throw new BusinessException("调拨单不存在或已删除");
        }
        return head;
    }

    private void requireStatus(BizDrugTransfer head, DrugTransferStatusEnum expected, String action) {
        if (Objects.equals(head.getStatus(), expected.getCode())) {
            return;
        }
        // 待发出/待接收之间的误操作最常见，直接把出路写在报错里
        String hint = Objects.equals(head.getStatus(), DrugTransferStatusEnum.PENDING_IN.getCode())
                ? "；货已在途，只能继续接收，要收回来请开一张反向调拨单" : "";
        throw new BusinessException("当前状态不能" + action + "（现在："
                + DrugTransferStatusEnum.getText(head.getStatus()) + "）" + hint);
    }

    /**
     * 方向、单号、批次、数量都写进流水备注：只写「调拨」两字，事后对着流水想不起来搬的是什么
     */
    private String transferLabel(BizDrugTransfer head, BizDrugTransferItem item, String stage) {
        return DrugTransferTypeEnum.getText(head.getTransferType()) + stage + "：" + head.getTransferNo()
                + " " + item.getDrugName() + " 批号" + item.getBatchNo()
                + " 数量" + NumUtil.plain(NumUtil.orZero(item.getApplyQuantity())) + "；事由：" + head.getReason();
    }

    private void fillText(DrugTransferVO vo) {
        vo.setTransferTypeText(DrugTransferTypeEnum.getText(vo.getTransferType()));
        vo.setFromRoomText(StockRoomEnum.getText(vo.getFromRoom()));
        vo.setToRoomText(StockRoomEnum.getText(vo.getToRoom()));
        vo.setStatusText(DrugTransferStatusEnum.getText(vo.getStatus()));
    }
}
