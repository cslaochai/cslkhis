package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.service.RedisSequenceService;
import com.his.common.exception.BusinessException;
import com.his.pharmacy.dto.DrugInboundCreateDTO;
import com.his.pharmacy.dto.DrugInboundItemDTO;
import com.his.pharmacy.dto.PurchaseOrderAuditDTO;
import com.his.pharmacy.dto.PurchaseOrderDetailUpsertDTO;
import com.his.pharmacy.dto.PurchaseOrderQueryPageDTO;
import com.his.pharmacy.dto.PurchaseOrderUpsertDTO;
import com.his.pharmacy.entity.BizPurchaseOrder;
import com.his.pharmacy.entity.BizPurchaseOrderDetail;
import com.his.pharmacy.entity.SysSupplier;
import com.his.pharmacy.mapper.BizDrugInboundMapper;
import com.his.pharmacy.mapper.BizPurchaseOrderDetailMapper;
import com.his.pharmacy.mapper.BizPurchaseOrderMapper;
import com.his.pharmacy.mapper.SysSupplierMapper;
import com.his.pharmacy.service.DrugInboundService;
import com.his.pharmacy.service.PurchaseOrderService;
import com.his.pharmacy.vo.DrugInboundVO;
import com.his.pharmacy.vo.PurchaseOrderDetailVO;
import com.his.pharmacy.vo.PurchaseOrderVO;
import com.his.system.utils.UserUtils;
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
import java.util.Set;
import com.his.system.service.DictCacheService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 采购订单服务实现
 *
 * 状态机（两个状态各自独立）：
 *   approval_status 0待审批 → 1已通过 / 2已驳回；只有 0 可审批；已通过不可改（要先驳回）
 *   inbound_status  0未入库 → 1已入库；入库前置 = 审批通过且未入库；入库后不可再入、不可删
 *
 * 金额口径：明细 amount = 数量 × 单价，订单 total_amount = Σ明细 amount，**全部服务端重算**，
 *          前端传来的 amount / totalAmount 一律不采信（否则前端能把总额改成任意值）。
 */
@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl extends ServiceImpl<BizPurchaseOrderMapper, BizPurchaseOrder>
        implements PurchaseOrderService {
    @Autowired
    private DictCacheService dictText;

    private final BizPurchaseOrderMapper orderMapper;
    private final BizPurchaseOrderDetailMapper detailMapper;
    private final SysSupplierMapper supplierMapper;
    private final BizDrugInboundMapper inboundMapper;
    private final DrugInboundService drugInboundService;
    private final RedisSequenceService redisSequenceService;

    @Override
    public PageResult<PurchaseOrderVO> page(PurchaseOrderQueryPageDTO queryDTO) {
        Page<PurchaseOrderVO> page = orderMapper.selectOrderPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                emptyToNull(queryDTO.getOrderNo()),
                queryDTO.getSupplierId(),
                queryDTO.getApprovalStatus(),
                queryDTO.getInboundDone(),
                emptyToNull(queryDTO.getDateStart()),
                emptyToNull(queryDTO.getDateEnd()));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public PurchaseOrderVO getDetailById(Long orderId) {
        PurchaseOrderVO vo = orderMapper.selectOrderById(orderId);
        if (vo == null) {
            throw new BusinessException("采购订单不存在或已删除");
        }
        vo.setItems(detailMapper.selectDetailWithDrug(orderId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsert(PurchaseOrderUpsertDTO dto) {
        SysSupplier supplier = supplierMapper.selectById(dto.getSupplierId());
        if (supplier == null) {
            throw new BusinessException("供应商不存在或已删除");
        }
        if (supplier.getStatus() == null || supplier.getStatus() != 1) {
            throw new BusinessException("供应商「" + supplier.getSupplierName() + "」已停用，不能下单");
        }

        List<BizPurchaseOrderDetail> details = buildDetails(dto.getItems());
        BigDecimal total = details.stream()
                .map(BizPurchaseOrderDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String operator = UserUtils.getCurrentEmployeeName();
        LocalDateTime orderTime = (dto.getOrderTime() != null ? dto.getOrderTime() : LocalDateTime.now())
                .truncatedTo(ChronoUnit.SECONDS);

        if (dto.getOrderId() == null) {
            BizPurchaseOrder entity = new BizPurchaseOrder();
            entity.setOrderNo(redisSequenceService.generatePurchaseNo());
            entity.setSupplierId(dto.getSupplierId());
            entity.setOrderTime(orderTime);
            entity.setTotalAmount(total);
            entity.setApprovalStatus(0);
            entity.setRemark(dto.getRemark());
            entity.setCreateBy(operator);
            entity.setUpdateBy(operator);
            if (!this.save(entity)) {
                throw new BusinessException("新增采购订单失败");
            }
            insertDetails(entity.getOrderId(), details, operator);
            return entity.getOrderId();
        }

        BizPurchaseOrder exist = this.getById(dto.getOrderId());
        if (exist == null) {
            throw new BusinessException("采购订单不存在或已删除");
        }
        if (exist.getApprovalStatus() != null && exist.getApprovalStatus() == 1) {
            throw new BusinessException("订单已审批通过，不能修改；确需修改请先驳回");
        }
        if (inboundMapper.countActiveByPurchaseOrder(exist.getOrderId()) > 0) {
            throw new BusinessException("订单已生成入库单，不能修改；如入库单有误请先取消");
        }
        // 修改 = 重新提交：审批状态回到待审批，并清空上一轮审批人。
        // ⚠ MP 的 updateById 会跳过 null 字段，所以这里用 UpdateWrapper 显式 set（否则 approverId 清不掉）
        this.lambdaUpdate()
                .eq(BizPurchaseOrder::getOrderId, exist.getOrderId())
                .set(BizPurchaseOrder::getSupplierId, dto.getSupplierId())
                .set(BizPurchaseOrder::getOrderTime, orderTime)
                .set(BizPurchaseOrder::getTotalAmount, total)
                .set(BizPurchaseOrder::getRemark, dto.getRemark())
                .set(BizPurchaseOrder::getApprovalStatus, 0)
                .set(BizPurchaseOrder::getApproverId, null)
                .set(BizPurchaseOrder::getUpdateBy, operator)
                .update();
        // 明细全量替换：物理删旧（表上 UK 不含 del_flag，逻辑删会撞键）
        detailMapper.deleteByOrderIdPhysically(exist.getOrderId());
        insertDetails(exist.getOrderId(), details, operator);
        return exist.getOrderId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(PurchaseOrderAuditDTO dto) {
        BizPurchaseOrder order = orderMapper.selectByIdForUpdate(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("采购订单不存在或已删除");
        }
        if (order.getApprovalStatus() == null || order.getApprovalStatus() != 0) {
            throw new BusinessException("只有待审批的订单可以审批（当前：" + dictText.getDicDataLabel("biz_pharmacy_purchaseApprovalStatusEnum", order.getApprovalStatus()) + "）");
        }
        // B 类：驳回原因只在 approvalStatus=2 时必填，条件必填不能下沉成 @NotBlank
        if (dto.getApprovalStatus() == 2 && !StringUtils.hasText(dto.getRemark())) {
            throw new BusinessException("驳回订单必须填写驳回原因");
        }
        if (detailMapper.countByOrder(order.getOrderId()) == 0) {
            throw new BusinessException("订单没有明细，不能审批");
        }
        String operator = UserUtils.getCurrentEmployeeName();
        this.lambdaUpdate()
                .eq(BizPurchaseOrder::getOrderId, order.getOrderId())
                .set(BizPurchaseOrder::getApprovalStatus, dto.getApprovalStatus())
                .set(BizPurchaseOrder::getApproverId, UserUtils.getCurrentEmployeeId())
                .set(BizPurchaseOrder::getRemark, StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : order.getRemark())
                .set(BizPurchaseOrder::getUpdateBy, operator)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugInboundVO generateInbound(Long orderId) {
        // 行锁：防并发对同一张订单生成两张入库单
        BizPurchaseOrder order = orderMapper.selectByIdForUpdate(orderId);
        if (order == null) {
            throw new BusinessException("采购订单不存在或已删除");
        }
        if (order.getApprovalStatus() == null || order.getApprovalStatus() != 1) {
            throw new BusinessException("只有审批通过的采购订单才能生成入库单（当前审批：" + dictText.getDicDataLabel("biz_pharmacy_purchaseApprovalStatusEnum", order.getApprovalStatus()) + "）");
        }

        List<PurchaseOrderDetailVO> details = detailMapper.selectDetailWithDrug(orderId);
        if (details.isEmpty()) {
            throw new BusinessException("订单没有明细，不能生成入库单");
        }

        SysSupplier supplier = supplierMapper.selectById(order.getSupplierId());
        String supplierName = supplier != null ? supplier.getSupplierName() : null;

        DrugInboundCreateDTO dto = new DrugInboundCreateDTO();
        dto.setInboundType(1); // 1-采购入库
        dto.setPurchaseOrderId(order.getOrderId());
        dto.setPurchaseOrderNo(order.getOrderNo());
        dto.setSupplier(supplierName);
        dto.setRemark("由采购订单 " + order.getOrderNo() + " 生成");
        dto.setItems(details.stream().map(d -> {
            DrugInboundItemDTO item = new DrugInboundItemDTO();
            item.setDrugId(d.getDrugId());
            item.setBatchNo(d.getBatchNo());
            item.setProductionDate(d.getProductionDate());
            item.setExpiryDate(d.getExpiryDate());
            item.setQuantity(d.getQuantity());
            item.setCostPrice(d.getUnitPrice());
            return item;
        }).toList());

        // 「同一采购订单只能有一张未取消的入库单」的校验在入库单侧（它才知道自己有没有单）
        return drugInboundService.createInbound(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long orderId) {
        BizPurchaseOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("采购订单不存在或已删除");
        }
        if (inboundMapper.countActiveByPurchaseOrder(orderId) > 0) {
            throw new BusinessException("订单已生成入库单，不能删除（删除会留下孤儿入库单）；请先取消入库单");
        }
        detailMapper.deleteByOrderIdPhysically(orderId);
        if (!this.removeById(orderId)) {
            throw new BusinessException("删除采购订单失败");
        }
    }


    /**
     * 明细校验 + 金额重算
     */
    private List<BizPurchaseOrderDetail> buildDetails(List<PurchaseOrderDetailUpsertDTO> items) {
        List<BizPurchaseOrderDetail> list = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (PurchaseOrderDetailUpsertDTO i : items) {
            String batchNo = i.getBatchNo() == null ? "" : i.getBatchNo().trim();
            String key = i.getDrugId() + "|" + batchNo;
            if (!seen.add(key)) {
                throw new BusinessException("同一张订单里，同一药品同一批号只能出现一次（药品#" + i.getDrugId() + "，批号 " + batchNo + "）");
            }
            if (detailMapper.countDrugById(i.getDrugId()) == 0) {
                throw new BusinessException("药品不存在或已停用，请从药品字典重新选择（药品#" + i.getDrugId() + "）");
            }
            if (i.getProductionDate() != null && i.getExpiryDate() != null
                    && i.getExpiryDate().isBefore(i.getProductionDate())) {
                throw new BusinessException("有效期不能早于生产日期（药品#" + i.getDrugId() + "，批号 " + batchNo + "）");
            }
            BizPurchaseOrderDetail d = new BizPurchaseOrderDetail();
            d.setDrugId(i.getDrugId());
            d.setQuantity(i.getQuantity());
            d.setUnitPrice(i.getUnitPrice());
            d.setAmount(i.getQuantity().multiply(i.getUnitPrice()).setScale(2, RoundingMode.HALF_UP));
            d.setBatchNo(batchNo);
            d.setProductionDate(i.getProductionDate());
            d.setExpiryDate(i.getExpiryDate());
            d.setRemark(i.getRemark());
            list.add(d);
        }
        return list;
    }

    private void insertDetails(Long orderId, List<BizPurchaseOrderDetail> details, String operator) {
        for (BizPurchaseOrderDetail d : details) {
            d.setId(null);
            d.setOrderId(orderId);
            d.setCreateBy(operator);
            d.setUpdateBy(operator);
            detailMapper.insert(d);
        }
    }

    private static String emptyToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }
}