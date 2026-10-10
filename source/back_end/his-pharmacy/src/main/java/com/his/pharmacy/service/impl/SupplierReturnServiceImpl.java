package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.DrugStockChangeTypeEnum;
import com.his.common.enums.StockRoomEnum;
import com.his.common.enums.SupplierReturnStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.pharmacy.dto.StockBatchMoveDTO;
import com.his.pharmacy.dto.SupplierReturnActionDTO;
import com.his.pharmacy.dto.SupplierReturnQueryPageDTO;
import com.his.pharmacy.dto.SupplierReturnUpsertDTO;
import com.his.pharmacy.entity.BizDrugSupplierReturn;
import com.his.pharmacy.entity.BizDrugSupplierReturnItem;
import com.his.pharmacy.mapper.BizDrugStockLogMapper;
import com.his.pharmacy.mapper.BizDrugStockMapper;
import com.his.pharmacy.mapper.BizDrugSupplierReturnItemMapper;
import com.his.pharmacy.mapper.BizDrugSupplierReturnMapper;
import com.his.pharmacy.service.PharmacyService;
import com.his.pharmacy.service.SupplierReturnService;
import com.his.pharmacy.vo.BizDrugStockLogVO;
import com.his.pharmacy.vo.BizDrugStockVO;
import com.his.pharmacy.vo.SupplierReturnItemVO;
import com.his.pharmacy.vo.SupplierReturnVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 药品供应商退货服务实现（sql/154 ③级）
 */
@Service
@RequiredArgsConstructor
public class SupplierReturnServiceImpl extends ServiceImpl<BizDrugSupplierReturnMapper, BizDrugSupplierReturn> implements SupplierReturnService {

    /**
     * 库存流水的来源单据类型（与药品库存流水的「来源单据类型」取值一致）
     */
    private static final String SOURCE_TYPE = "supplierReturn";

    private final BizDrugSupplierReturnMapper bizDrugSupplierReturnMapper;
    private final BizDrugSupplierReturnItemMapper bizDrugSupplierReturnItemMapper;
    private final BizDrugStockMapper bizDrugStockMapper;
    private final BizDrugStockLogMapper bizDrugStockLogMapper;
    private final PharmacyService pharmacyService;
    private final RedisSequenceService redisSequenceService;

    private static String batchLabel(BizDrugStockVO batch) {
        String label = TextUtil.hasText(batch.getDrugName()) ? batch.getDrugName() : "药品#" + batch.getDrugId();
        return TextUtil.hasText(batch.getBatchNo()) ? label + "（批号 " + batch.getBatchNo() + "）" : label;
    }

    @Override
    public PageResult<SupplierReturnVO> listPage(SupplierReturnQueryPageDTO query) {
        Page<SupplierReturnVO> page = bizDrugSupplierReturnMapper.selectReturnPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                TextUtil.trimToNull(query.getReturnNo()), query.getSupplierId(), query.getStatus(),
                TextUtil.trimToNull(query.getKeyword()), TextUtil.trimToNull(query.getDateStart()), TextUtil.trimToNull(query.getDateEnd()));
        page.getRecords().forEach(this::fillText);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public SupplierReturnVO getDetailById(Long id) {
        SupplierReturnVO vo = bizDrugSupplierReturnMapper.selectReturnById(id);
        if (vo == null) {
            throw new BusinessException("退货单不存在或已删除");
        }
        fillText(vo);
        List<SupplierReturnItemVO> items = bizDrugSupplierReturnItemMapper.selectByReturnId(id);
        items.forEach(i -> i.setStockRoomText(StockRoomEnum.getText(i.getStockRoom())));
        vo.setItems(items);
        List<BizDrugStockLogVO> logs = bizDrugStockLogMapper.selectBySource(SOURCE_TYPE, id);
        logs.forEach(BizDrugStockLogVO::fillTexts);
        vo.setLogs(logs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierReturnVO upsert(SupplierReturnUpsertDTO dto) {
        String supplierName = bizDrugStockMapper.selectSupplierNameById(dto.getSupplierId());
        if (!TextUtil.hasText(supplierName)) {
            throw new BusinessException("供应商不存在或已停用（供应商#" + dto.getSupplierId() + "）");
        }
        String reason = TextUtil.cut(TextUtil.requireTrimmed(dto.getReturnReason(), "退货原因不能为空"), 200);
        String operator = UserUtils.getCurrentUser().getRealName();
        LocalDateTime now = TimeUtil.nowSeconds();
        List<BizDrugSupplierReturnItem> rows = buildItems(dto.getItems(), dto.getSupplierId(), supplierName, operator);

        if (dto.getId() == null) {
            BizDrugSupplierReturn head = new BizDrugSupplierReturn();
            head.setReturnNo(redisSequenceService.generateSupplierReturnNo());
            head.setSupplierId(dto.getSupplierId());
            head.setSupplierName(TextUtil.cut(supplierName, 128));
            head.setReturnReason(reason);
            head.setSrcRefNo(TextUtil.cut(dto.getSrcRefNo(), 64));
            head.setStatus(SupplierReturnStatusEnum.PENDING.getCode());
            head.setTotalItems(0);
            head.setTotalQuantity(BigDecimal.ZERO);
            head.setTotalAmount(BigDecimal.ZERO);
            head.setRemark(TextUtil.cut(dto.getRemark(), 500));
            head.setCreateBy(operator);
            head.setUpdateBy(operator);
            head.setCreateTime(now);
            head.setUpdateTime(now);
            if (bizDrugSupplierReturnMapper.insert(head) != 1) {
                throw new BusinessException("生成退货单失败");
            }
            rows.forEach(row -> row.setReturnId(head.getId()));
            rows.forEach(bizDrugSupplierReturnItemMapper::insert);
            refreshSummary(head.getId(), operator, now);
            return getDetailById(head.getId());
        }

        BizDrugSupplierReturn cur = lock(dto.getId());
        requireStatus(cur, SupplierReturnStatusEnum.PENDING, "修改退货明细");
        // 整单替换明细：uk_sreturn_stock(return_id, stock_id) 不含 del_flag，软删再插必撞键
        bizDrugSupplierReturnItemMapper.purgeByReturnId(cur.getId());
        bizDrugSupplierReturnMapper.update(null, Wrappers.<BizDrugSupplierReturn>lambdaUpdate()
                .eq(BizDrugSupplierReturn::getId, cur.getId())
                .set(BizDrugSupplierReturn::getSupplierId, dto.getSupplierId())
                .set(BizDrugSupplierReturn::getSupplierName, TextUtil.cut(supplierName, 128))
                .set(BizDrugSupplierReturn::getReturnReason, reason)
                .set(BizDrugSupplierReturn::getSrcRefNo, TextUtil.cut(dto.getSrcRefNo(), 64))
                .set(BizDrugSupplierReturn::getRemark, TextUtil.cut(dto.getRemark(), 500))
                .set(BizDrugSupplierReturn::getUpdateBy, operator)
                .set(BizDrugSupplierReturn::getUpdateTime, now));
        rows.forEach(row -> row.setReturnId(cur.getId()));
        rows.forEach(bizDrugSupplierReturnItemMapper::insert);
        refreshSummary(cur.getId(), operator, now);
        return getDetailById(cur.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierReturnVO confirmReturn(SupplierReturnActionDTO dto) {
        BizDrugSupplierReturn head = lock(dto.getId());
        requireStatus(head, SupplierReturnStatusEnum.PENDING, "确认退货");
        String operator = UserUtils.getCurrentUser().getRealName();
        LocalDateTime now = TimeUtil.nowSeconds();
        List<BizDrugSupplierReturnItem> items = bizDrugSupplierReturnItemMapper.selectList(
                Wrappers.<BizDrugSupplierReturnItem>lambdaQuery()
                        .eq(BizDrugSupplierReturnItem::getReturnId, head.getId())
                        .orderByAsc(BizDrugSupplierReturnItem::getId));
        if (items.isEmpty()) {
            throw new BusinessException("这张退货单没有明细，先把批次选进来再确认");
        }
        for (BizDrugSupplierReturnItem item : items) {
            StockBatchMoveDTO move = new StockBatchMoveDTO();
            move.setStockId(item.getStockId());
            move.setStockRoom(item.getStockRoom());
            move.setQuantity(item.getQuantity());
            move.setChangeType(DrugStockChangeTypeEnum.SUPPLIER_RETURN_OUT.getCode());
            move.setSourceType(SOURCE_TYPE);
            move.setSourceId(head.getId());
            move.setSourceNo(head.getReturnNo());
            move.setOperatorName(operator);
            move.setReason("退给供应商 " + head.getSupplierName() + "：" + item.getDrugName()
                    + " 批号" + item.getBatchNo() + " 数量" + NumUtil.plain(NumUtil.orZero(item.getQuantity()))
                    + " 金额¥" + NumUtil.plain(NumUtil.orZero(item.getAmount())) + "；原因：" + head.getReturnReason());
            pharmacyService.deductStockBatch(move);
        }
        bizDrugSupplierReturnMapper.update(null, Wrappers.<BizDrugSupplierReturn>lambdaUpdate()
                .eq(BizDrugSupplierReturn::getId, head.getId())
                .set(BizDrugSupplierReturn::getStatus, SupplierReturnStatusEnum.DONE.getCode())
                .set(BizDrugSupplierReturn::getReturnBy, operator)
                .set(BizDrugSupplierReturn::getReturnTime, now)
                .set(BizDrugSupplierReturn::getUpdateBy, operator)
                .set(BizDrugSupplierReturn::getUpdateTime, now));
        refreshSummary(head.getId(), operator, now);
        return getDetailById(head.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierReturnVO cancel(SupplierReturnActionDTO dto) {
        BizDrugSupplierReturn head = lock(dto.getId());
        requireStatus(head, SupplierReturnStatusEnum.PENDING, "作废");
        // ①条件必填：作废原因只在取消入口必填（同一动作 DTO 被确认退货复用），注解一刀切会挡掉合法的确认
        String reason = TextUtil.cut(TextUtil.requireTrimmed(dto.getReason(), "作废原因不能为空"), 200);
        String operator = UserUtils.getCurrentUser().getRealName();
        LocalDateTime now = TimeUtil.nowSeconds();
        bizDrugSupplierReturnMapper.update(null, Wrappers.<BizDrugSupplierReturn>lambdaUpdate()
                .eq(BizDrugSupplierReturn::getId, head.getId())
                .set(BizDrugSupplierReturn::getStatus, SupplierReturnStatusEnum.CANCELLED.getCode())
                .set(BizDrugSupplierReturn::getCancelBy, operator)
                .set(BizDrugSupplierReturn::getCancelTime, now)
                .set(BizDrugSupplierReturn::getCancelReason, reason)
                .set(BizDrugSupplierReturn::getUpdateBy, operator)
                .set(BizDrugSupplierReturn::getUpdateTime, now));
        return getDetailById(head.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        BizDrugSupplierReturn head = lock(id);
        SupplierReturnStatusEnum status = SupplierReturnStatusEnum.fromCode(head.getStatus());
        if (status != SupplierReturnStatusEnum.PENDING && status != SupplierReturnStatusEnum.CANCELLED) {
            throw new BusinessException("只有「待退货」或「已作废」的退货单可以删除（当前："
                    + SupplierReturnStatusEnum.getText(head.getStatus()) + "）；已退货药已出库，是留档凭证不能抹");
        }
        bizDrugSupplierReturnItemMapper.purgeByReturnId(head.getId());
        if (bizDrugSupplierReturnMapper.purgeById(head.getId()) != 1) {
            throw new BusinessException("删除退货单失败");
        }
    }

    /**
     * 校验并组装明细：批次存在、确实挂在这个供应商名下、库位与数量合法。
     */
    private List<BizDrugSupplierReturnItem> buildItems(List<SupplierReturnUpsertDTO.Item> rows, Long supplierId,
                                                       String supplierName, String operator) {
        Set<Long> seen = new HashSet<>();
        List<Long> stockIds = new ArrayList<>(rows.size());
        for (SupplierReturnUpsertDTO.Item row : rows) {
            if (row.getStockId() == null || !seen.add(row.getStockId())) {
                throw new BusinessException("同一条明细缺少批次，或同一批次在一单里重复出现（批次#"
                        + row.getStockId() + "）");
            }
            stockIds.add(row.getStockId());
        }
        Map<Long, BizDrugStockVO> byId = bizDrugStockMapper.selectBatchSnapshots(stockIds).stream()
                .collect(Collectors.toMap(BizDrugStockVO::getId, Function.identity(), (a, b) -> a));
        List<BizDrugSupplierReturnItem> items = new ArrayList<>(rows.size());
        for (SupplierReturnUpsertDTO.Item row : rows) {
            BizDrugStockVO batch = byId.get(row.getStockId());
            if (batch == null) {
                throw new BusinessException("库存批次不存在或已被删除（批次#" + row.getStockId() + "）");
            }
            if (batch.getSupplierId() == null) {
                throw new BusinessException(batchLabel(batch) + " 没有关联供应商档案，退无可退。"
                        + "请先在药品库存里把该批次的供应商补成「" + supplierName + "」再来建单"
                        + "（库存上的供应商文本多是生产厂家名，不能当退货对象）");
            }
            if (!Objects.equals(batch.getSupplierId(), supplierId)) {
                throw new BusinessException(batchLabel(batch) + " 属于供应商#" + batch.getSupplierId()
                        + "，不能退给「" + supplierName + "」，请换一个供应商或重选批次");
            }
            BigDecimal quantity = NumUtil.scale(row.getQuantity(), 2);
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(batchLabel(batch) + " 的退货数量必须大于 0");
            }
            BigDecimal available = NumUtil.orZero(batch.getAvailableQuantity());
            if (available.compareTo(quantity) < 0) {
                throw new BusinessException(batchLabel(batch) + " 可用量只有 " + NumUtil.plain(NumUtil.orZero(available)) + "，不够退 "
                        + NumUtil.plain(NumUtil.orZero(quantity)) + "（已锁定 " + NumUtil.plain(NumUtil.orZero(NumUtil.orZero(batch.getLockedQuantity())))
                        + " 是已开方未发药的量，不许退货）");
            }
            BizDrugSupplierReturnItem item = new BizDrugSupplierReturnItem();
            item.setStockId(batch.getId());
            item.setDrugId(batch.getDrugId());
            item.setDrugCode(batch.getDrugCode());
            item.setDrugName(batch.getDrugName());
            item.setSpecification(batch.getSpecification());
            item.setUnit(batch.getUnit());
            item.setBatchNo(batch.getBatchNo());
            item.setExpiryDate(batch.getExpiryDate());
            item.setStockRoom(batch.getStockRoom());
            item.setSupplierId(batch.getSupplierId());
            item.setCostPrice(NumUtil.orZero(batch.getCostPrice()));
            item.setQuantity(quantity);
            item.setAmount(quantity.multiply(NumUtil.orZero(batch.getCostPrice())).setScale(2, RoundingMode.HALF_UP));
            item.setRemark(TextUtil.cut(row.getRemark(), 500));
            item.setCreateBy(operator);
            item.setUpdateBy(operator);
            items.add(item);
        }
        return items;
    }

    private void refreshSummary(Long returnId, String operator, LocalDateTime now) {
        BizDrugSupplierReturn sum = bizDrugSupplierReturnItemMapper.selectSummary(returnId);
        bizDrugSupplierReturnMapper.update(null, Wrappers.<BizDrugSupplierReturn>lambdaUpdate()
                .eq(BizDrugSupplierReturn::getId, returnId)
                .set(BizDrugSupplierReturn::getTotalItems, sum.getTotalItems())
                .set(BizDrugSupplierReturn::getTotalQuantity, NumUtil.orZero(sum.getTotalQuantity()))
                .set(BizDrugSupplierReturn::getTotalAmount, NumUtil.orZero(sum.getTotalAmount()))
                .set(BizDrugSupplierReturn::getUpdateBy, operator)
                .set(BizDrugSupplierReturn::getUpdateTime, now));
    }

    private BizDrugSupplierReturn lock(Long id) {
        BizDrugSupplierReturn head = bizDrugSupplierReturnMapper.selectByIdForUpdate(id);
        if (head == null) {
            throw new BusinessException("退货单不存在或已删除");
        }
        return head;
    }

    private void requireStatus(BizDrugSupplierReturn head, SupplierReturnStatusEnum expected, String action) {
        if (Objects.equals(head.getStatus(), expected.getCode())) {
            return;
        }
        String hint = Objects.equals(head.getStatus(), SupplierReturnStatusEnum.DONE.getCode())
                ? "；药已出库，只能按正常采购入库把货补回来" : "";
        throw new BusinessException("当前状态不能" + action + "（现在："
                + SupplierReturnStatusEnum.getText(head.getStatus()) + "）" + hint);
    }

    private void fillText(SupplierReturnVO vo) {
        vo.setStatusText(SupplierReturnStatusEnum.getText(vo.getStatus()));
    }
}
