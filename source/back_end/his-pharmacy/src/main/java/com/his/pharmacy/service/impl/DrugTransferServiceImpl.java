package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.base.RedisSequenceService;
import com.his.common.enums.DrugStockChangeTypeEnum;
import com.his.common.enums.DrugTransferStatusEnum;
import com.his.common.enums.DrugTransferTypeEnum;
import com.his.common.enums.StockRoomEnum;
import com.his.common.exception.BusinessException;
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
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 药品调拨服务实现（sql/154）
 *
 * <p>三条口径值得先说清楚：
 * <ul>
 *   <li><b>库位是硬闸门，不是提示</b>：建单时批次不在本单「发出库位」就直接拒绝，
 *       发出时再校验一次。放行会把药库的整件货当成药房在架量搬走，两侧账都说不清。</li>
 *   <li><b>一张单两行流水、合计为 0</b>：type=7 为负、type=8 为正，所以详情里的流水
 *       是这张单「账实相符」的证据；只有一行就说明货还在途。</li>
 *   <li><b>成本随货走、同批号按到货额加权</b>：接收方新增批次直接沿用发出方的成本价（调拨不是
 *       一笔新进货，不该产生价差）；若该批号在接收库位已有批次，则按「原金额+到货金额」重算加权
 *       成本（addStockToRoom 与采购入库同一口径），不把两侧已形成的成本差硬洗掉。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class DrugTransferServiceImpl implements DrugTransferService {

    /** 库存流水的来源单据类型（与药品库存流水的「来源单据类型」取值一致） */
    private static final String SOURCE_TYPE = "drugTransfer";

    private final BizDrugTransferMapper transferMapper;
    private final BizDrugTransferItemMapper itemMapper;
    private final BizDrugStockMapper stockMapper;
    private final BizDrugStockLogMapper stockLogMapper;
    private final PharmacyService pharmacyService;
    private final RedisSequenceService redisSequenceService;

    @Override
    public PageResult<DrugTransferVO> listPage(DrugTransferQueryPageDTO query) {
        DrugTransferQueryPageDTO q = query == null ? new DrugTransferQueryPageDTO() : query;
        Page<DrugTransferVO> page = transferMapper.selectTransferPage(
                new Page<>(q.getPageNum(), q.getPageSize()),
                trimToNull(q.getTransferNo()), q.getTransferType(), q.getStatus(),
                trimToNull(q.getKeyword()), trimToNull(q.getDateStart()), trimToNull(q.getDateEnd()));
        page.getRecords().forEach(this::fillText);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public DrugTransferVO getDetailById(Long id) {
        DrugTransferVO vo = transferMapper.selectTransferById(id);
        if (vo == null) {
            throw new BusinessException("调拨单不存在或已删除");
        }
        fillText(vo);
        vo.setItems(itemMapper.selectByTransferId(id));
        vo.setLogs(logsWithTexts(stockLogMapper.selectBySource(SOURCE_TYPE, id)));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTransferVO upsert(DrugTransferUpsertDTO dto) {
        DrugTransferTypeEnum type = DrugTransferTypeEnum.fromCode(dto.getTransferType());
        String reason = requireText(dto.getReason(), "调拨事由", 200);
        String operator = UserUtils.getCurrentEmployeeName();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
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
            head.setRemark(cut(dto.getRemark(), 500));
            head.setCreateBy(operator);
            head.setUpdateBy(operator);
            head.setCreateTime(now);
            head.setUpdateTime(now);
            if (transferMapper.insert(head) != 1) {
                throw new BusinessException("生成调拨单失败");
            }
            rows.forEach(row -> row.setTransferId(head.getId()));
            rows.forEach(itemMapper::insert);
            refreshSummary(head.getId(), operator, now);
            return getDetailById(head.getId());
        }

        BizDrugTransfer cur = lock(dto.getId());
        requireStatus(cur, DrugTransferStatusEnum.PENDING_OUT, "修改调拨明细");
        // 整单替换明细：uk_transfer_stock(transfer_id, stock_id) 不含 del_flag，软删再插必撞键
        itemMapper.purgeByTransferId(cur.getId());
        transferMapper.update(null, Wrappers.<BizDrugTransfer>lambdaUpdate()
                .eq(BizDrugTransfer::getId, cur.getId())
                .set(BizDrugTransfer::getTransferType, type.getCode())
                .set(BizDrugTransfer::getFromRoom, type.getFromRoom().getCode())
                .set(BizDrugTransfer::getToRoom, type.getToRoom().getCode())
                .set(BizDrugTransfer::getReason, reason)
                .set(BizDrugTransfer::getRemark, cut(dto.getRemark(), 500))
                .set(BizDrugTransfer::getUpdateBy, operator)
                .set(BizDrugTransfer::getUpdateTime, now));
        rows.forEach(row -> row.setTransferId(cur.getId()));
        rows.forEach(itemMapper::insert);
        refreshSummary(cur.getId(), operator, now);
        return getDetailById(cur.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTransferVO confirmOut(DrugTransferActionDTO dto) {
        BizDrugTransfer head = lock(dto.getId());
        requireStatus(head, DrugTransferStatusEnum.PENDING_OUT, "确认发出");
        String operator = UserUtils.getCurrentEmployeeName();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
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
            itemMapper.markOut(item.getId(), operator, now);
        }
        transferMapper.update(null, Wrappers.<BizDrugTransfer>lambdaUpdate()
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
        String operator = UserUtils.getCurrentEmployeeName();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
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
            BizDrugStock from = stockMapper.selectById(item.getStockId());
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
            itemMapper.markIn(item.getId(), landed.getId(), operator, now);
        }
        transferMapper.update(null, Wrappers.<BizDrugTransfer>lambdaUpdate()
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
        String reason = requireText(dto.getReason(), "作废原因", 200);
        String operator = UserUtils.getCurrentEmployeeName();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        // 待发出=一行库存都没动过，作废只是把单子关掉；一旦发出就成了在途，作废会让药凭空消失，
        // 想收回去只有一条路：开一张反向调拨单，让流水把它讲清楚。
        transferMapper.update(null, Wrappers.<BizDrugTransfer>lambdaUpdate()
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
        itemMapper.purgeByTransferId(head.getId());
        if (transferMapper.purgeById(head.getId()) != 1) {
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
        Map<Long, BizDrugStockVO> byId = stockMapper.selectBatchSnapshots(stockIds).stream()
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
            BigDecimal quantity = scale(row.getApplyQuantity());
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(batchLabel(batch) + " 的调拨数量必须大于 0");
            }
            BigDecimal available = nvl(batch.getAvailableQuantity());
            if (available.compareTo(quantity) < 0) {
                throw new BusinessException(batchLabel(batch) + " 可用量只有 " + plain(available) + "，不够调 "
                        + plain(quantity) + "（已锁定 " + plain(nvl(batch.getLockedQuantity()))
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
            item.setCostPrice(nvl(batch.getCostPrice()));
            item.setApplyQuantity(quantity);
            item.setLockedQuantity(nvl(batch.getLockedQuantity()));
            item.setOutFlag(0);
            item.setInFlag(0);
            item.setRemark(cut(row.getRemark(), 500));
            item.setCreateBy(operator);
            item.setUpdateBy(operator);
            items.add(item);
        }
        return items;
    }

    /** 把明细汇总的五个口径数回写主单（列表页直接读主单，不再逐单算明细） */
    private void refreshSummary(Long transferId, String operator, LocalDateTime now) {
        BizDrugTransfer sum = itemMapper.selectSummary(transferId);
        transferMapper.update(null, Wrappers.<BizDrugTransfer>lambdaUpdate()
                .eq(BizDrugTransfer::getId, transferId)
                .set(BizDrugTransfer::getTotalItems, sum.getTotalItems())
                .set(BizDrugTransfer::getTotalQuantity, nvl(sum.getTotalQuantity()))
                .set(BizDrugTransfer::getOutQuantity, nvl(sum.getOutQuantity()))
                .set(BizDrugTransfer::getInQuantity, nvl(sum.getInQuantity()))
                .set(BizDrugTransfer::getTotalAmount, nvl(sum.getTotalAmount()))
                .set(BizDrugTransfer::getUpdateBy, operator)
                .set(BizDrugTransfer::getUpdateTime, now));
    }

    private List<BizDrugTransferItem> listItems(Long transferId) {
        return itemMapper.selectList(Wrappers.<BizDrugTransferItem>lambdaQuery()
                .eq(BizDrugTransferItem::getTransferId, transferId)
                .orderByAsc(BizDrugTransferItem::getId));
    }

    private BizDrugTransfer lock(Long id) {
        BizDrugTransfer head = transferMapper.selectByIdForUpdate(id);
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

    /** 方向、单号、批次、数量都写进流水备注：只写「调拨」两字，事后对着流水想不起来搬的是什么 */
    private String transferLabel(BizDrugTransfer head, BizDrugTransferItem item, String stage) {
        return DrugTransferTypeEnum.getText(head.getTransferType()) + stage + "：" + head.getTransferNo()
                + " " + item.getDrugName() + " 批号" + item.getBatchNo()
                + " 数量" + plain(item.getApplyQuantity()) + "；事由：" + head.getReason();
    }

    /** 抽屉里的流水要带库位与类型文案，和流水台账同一口径（前端不许自己映射） */
    private static List<BizDrugStockLogVO> logsWithTexts(List<BizDrugStockLogVO> logs) {
        logs.forEach(BizDrugStockLogVO::fillTexts);
        return logs;
    }

    private void fillText(DrugTransferVO vo) {        vo.setTransferTypeText(DrugTransferTypeEnum.getText(vo.getTransferType()));
        vo.setFromRoomText(StockRoomEnum.getText(vo.getFromRoom()));
        vo.setToRoomText(StockRoomEnum.getText(vo.getToRoom()));
        vo.setStatusText(DrugTransferStatusEnum.getText(vo.getStatus()));
    }

    private static String batchLabel(BizDrugStockVO batch) {
        String label = StringUtils.hasText(batch.getDrugName()) ? batch.getDrugName() : "药品#" + batch.getDrugId();
        return StringUtils.hasText(batch.getBatchNo()) ? label + "（批号 " + batch.getBatchNo() + "）" : label;
    }

    /** 必填文本：trim + 截到列宽（超长会把业务失败升级成 500，AGENTS §3） */
    private static String requireText(String text, String label, int max) {
        // B 类：作废原因只在取消入口必填（同一动作 DTO 被确认入口复用），条件必填留 service；事由一侧注解已兜住，此处只兼做截断
        if (!StringUtils.hasText(text)) {
            throw new BusinessException(label + "不能为空");
        }
        return cut(text, max);
    }

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

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /** 数量显示：5.00 → 5，2.50 → 2.5（报错文案里不想看到一串尾零） */
    private static String plain(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }
}
