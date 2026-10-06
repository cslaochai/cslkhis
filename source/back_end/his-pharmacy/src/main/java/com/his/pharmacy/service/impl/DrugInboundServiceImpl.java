package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.service.RedisSequenceService;
import com.his.common.exception.BusinessException;
import com.his.pharmacy.dto.DrugInboundCancelDTO;
import com.his.pharmacy.dto.DrugInboundCreateDTO;
import com.his.pharmacy.dto.DrugInboundIdDTO;
import com.his.pharmacy.dto.DrugInboundItemDTO;
import com.his.pharmacy.dto.DrugInboundQueryPageDTO;
import com.his.pharmacy.entity.BizDrugInbound;
import com.his.pharmacy.entity.BizDrugInboundDetail;
import com.his.pharmacy.entity.BizDrugStock;
import com.his.pharmacy.mapper.BizDrugInboundDetailMapper;
import com.his.pharmacy.mapper.BizDrugInboundMapper;
import com.his.pharmacy.service.DrugInboundService;
import com.his.pharmacy.service.PharmacyService;
import com.his.pharmacy.vo.DrugBriefVO;
import com.his.pharmacy.vo.DrugInboundDetailVO;
import com.his.pharmacy.vo.DrugInboundVO;
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
 * 药品入库单服务实现
 *
 * 口径：
 *   · 药品编码/名称/规格/单位在生成时从药品字典 **快照**写入明细，不靠联表回显
 *     （入库单是凭证，字典改名不能改变历史单据的记载）
 *   · 金额全部服务端重算（明细 amount = 数量 × 成本价，单头 = Σ明细）
 *   · 只有**入库**（2→3）才动库存：按明细「药品+批号」建/加批次并落库存流水，来源写 drugInbound + 入库单号
 *   · 取消只允许 1/2 状态；已入库的冲销要走退货入库（会反向增加/减少批次），不能靠改状态抹掉
 */
@Service
@RequiredArgsConstructor
public class DrugInboundServiceImpl extends ServiceImpl<BizDrugInboundMapper, BizDrugInbound>
    @Autowired
    private DictCacheService dictText;
        implements DrugInboundService {

    private final BizDrugInboundMapper inboundMapper;
    private final BizDrugInboundDetailMapper detailMapper;
    private final PharmacyService pharmacyService;
    private final RedisSequenceService redisSequenceService;

    @Override
    public PageResult<DrugInboundVO> page(DrugInboundQueryPageDTO queryDTO) {
        Page<DrugInboundVO> page = inboundMapper.selectInboundPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                emptyToNull(queryDTO.getInboundNo()),
                queryDTO.getInboundType(),
                queryDTO.getInboundStatus(),
                emptyToNull(queryDTO.getPurchaseOrderNo()),
                emptyToNull(queryDTO.getDateStart()),
                emptyToNull(queryDTO.getDateEnd()));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public DrugInboundVO getDetailById(Long inboundId) {
        DrugInboundVO vo = inboundMapper.selectInboundById(inboundId);
        if (vo == null) {
            throw new BusinessException("入库单不存在或已删除");
        }
        vo.setItems(detailMapper.selectByInboundId(inboundId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugInboundVO createInbound(DrugInboundCreateDTO dto) {
        // C 类：采购订单生成入库单时由本模块 service 直调本方法，那条路径不过 Bean Validation
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("入库明细不能为空，至少要有一条");
        }
        // 一张采购订单只允许一张「未取消」的入库单：否则同一批采购能被反复入库，库存凭空翻倍
        if (dto.getPurchaseOrderId() != null && inboundMapper.countActiveByPurchaseOrder(dto.getPurchaseOrderId()) > 0) {
            throw new BusinessException("该采购订单已生成过入库单（未取消），不能重复生成；如原单有误请先取消");
        }

        String operator = UserUtils.getCurrentEmployeeName();
        String inboundNo = redisSequenceService.generateInboundNo();

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;
        List<BizDrugInboundDetail> details = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (DrugInboundItemDTO item : dto.getItems()) {
            String batchNo = item.getBatchNo() == null ? "" : item.getBatchNo().trim();
            String key = item.getDrugId() + "|" + batchNo;
            if (!seen.add(key)) {
                throw new BusinessException("同一张入库单里，同一药品同一批号只能出现一次（药品#" + item.getDrugId() + "，批号 " + batchNo + "）");
            }
            DrugBriefVO drug = detailMapper.selectDrugBrief(item.getDrugId());
            if (drug == null || !StringUtils.hasText(drug.getDrugName())) {
                throw new BusinessException("药品不存在或已停用，请从药品字典重新选择（药品#" + item.getDrugId() + "）");
            }
            if (item.getProductionDate() != null && item.getExpiryDate() != null
                    && item.getExpiryDate().isBefore(item.getProductionDate())) {
                throw new BusinessException("有效期不能早于生产日期（药品 " + drug.getDrugName() + "，批号 " + batchNo + "）");
            }

            BizDrugInboundDetail d = new BizDrugInboundDetail();
            d.setDrugId(item.getDrugId());
            d.setDrugCode(drug.getDrugCode());
            d.setDrugName(drug.getDrugName());
            d.setSpecification(drug.getSpecification());
            d.setUnit(StringUtils.hasText(drug.getUnit()) ? drug.getUnit() : "—");
            d.setBatchNo(batchNo);
            d.setProductionDate(item.getProductionDate());
            d.setExpiryDate(item.getExpiryDate());
            d.setQuantity(item.getQuantity());
            d.setCostPrice(item.getCostPrice());
            d.setAmount(item.getQuantity().multiply(item.getCostPrice()).setScale(2, RoundingMode.HALF_UP));
            d.setDetailStatus(1);
            d.setRemark(item.getRemark());
            d.setCreateBy(operator);
            d.setUpdateBy(operator);
            details.add(d);

            totalAmount = totalAmount.add(d.getAmount());
            totalQuantity = totalQuantity.add(d.getQuantity());
        }

        BizDrugInbound entity = new BizDrugInbound();
        entity.setInboundNo(inboundNo);
        entity.setInboundType(dto.getInboundType());
        entity.setPurchaseOrderId(dto.getPurchaseOrderId());
        entity.setPurchaseOrderNo(dto.getPurchaseOrderNo());
        entity.setSupplier(dto.getSupplier());
        entity.setTotalAmount(totalAmount);
        entity.setTotalQuantity(totalQuantity);
        entity.setInboundStatus(1); // 待审核
        entity.setRemark(dto.getRemark());
        entity.setCreateBy(operator);
        entity.setUpdateBy(operator);
        if (!this.save(entity)) {
            throw new BusinessException("生成入库单失败");
        }

        for (BizDrugInboundDetail d : details) {
            d.setId(null);
            d.setInboundId(entity.getId());
            d.setInboundNo(inboundNo);
            detailMapper.insert(d);
        }
        return getDetailById(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(DrugInboundIdDTO dto) {
        BizDrugInbound in = inboundMapper.selectByIdForUpdate(dto.getInboundId());
        if (in == null) {
            throw new BusinessException("入库单不存在或已删除");
        }
        if (in.getInboundStatus() == null || in.getInboundStatus() != 1) {
            throw new BusinessException("只有待审核的入库单可以审核（当前：" + DrugInboundStatusEnum.labelOrUnknown(in.getInboundStatus()) + "）");
        }
        if (detailMapper.countActiveByInbound(in.getId()) == 0) {
            throw new BusinessException("入库单没有有效明细，不能审核");
        }
        String operator = UserUtils.getCurrentEmployeeName();
        this.lambdaUpdate()
                .eq(BizDrugInbound::getId, in.getId())
                .set(BizDrugInbound::getInboundStatus, 2)
                .set(BizDrugInbound::getAuditBy, operator)
                .set(BizDrugInbound::getAuditTime, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .set(BizDrugInbound::getUpdateBy, operator)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugInboundVO stockIn(DrugInboundIdDTO dto) {
        // 行锁：入库是「判状态 → 建批次 → 回写状态」三步，并发下不锁行会同一张单入两次库
        BizDrugInbound in = inboundMapper.selectByIdForUpdate(dto.getInboundId());
        if (in == null) {
            throw new BusinessException("入库单不存在或已删除");
        }
        if (in.getInboundStatus() == null || in.getInboundStatus() != 2) {
            throw new BusinessException("只有已审核的入库单可以入库（当前：" + DrugInboundStatusEnum.labelOrUnknown(in.getInboundStatus()) + "）");
        }

        List<DrugInboundDetailVO> details = detailMapper.selectByInboundId(in.getId());
        List<DrugInboundDetailVO> active = details.stream()
                .filter(d -> d.getDetailStatus() == null || d.getDetailStatus() != 3)
                .toList();
        if (active.isEmpty()) {
            throw new BusinessException("入库单没有有效明细，不能入库");
        }

        String operator = UserUtils.getCurrentEmployeeName();
        for (DrugInboundDetailVO d : active) {
            BizDrugStock batchInfo = new BizDrugStock();
            batchInfo.setDrugId(d.getDrugId());
            batchInfo.setBatchNo(d.getBatchNo());
            batchInfo.setProductionDate(d.getProductionDate());
            batchInfo.setExpiryDate(d.getExpiryDate());
            batchInfo.setCostPrice(d.getCostPrice());
            batchInfo.setSupplier(in.getSupplier());
            // 库存流水来源 = 入库单（不是采购单）：实际操作发生在这里，追溯链采购单→入库单→批次靠本表 purchase_order_id
            pharmacyService.inboundByBatch(batchInfo, d.getQuantity(),
                    "drugInbound", in.getId(), in.getInboundNo(), operator);
        }

        detailMapper.markStockedIn(in.getId());
        this.lambdaUpdate()
                .eq(BizDrugInbound::getId, in.getId())
                .set(BizDrugInbound::getInboundStatus, 3)
                .set(BizDrugInbound::getInboundBy, operator)
                .set(BizDrugInbound::getInboundTime, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .set(BizDrugInbound::getUpdateBy, operator)
                .update();
        return getDetailById(in.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(DrugInboundCancelDTO dto) {
        BizDrugInbound in = inboundMapper.selectByIdForUpdate(dto.getInboundId());
        if (in == null) {
            throw new BusinessException("入库单不存在或已删除");
        }
        if (in.getInboundStatus() == null || (in.getInboundStatus() != 1 && in.getInboundStatus() != 2)) {
            throw new BusinessException("只有待审核或已审核的入库单可以取消（当前：" + DrugInboundStatusEnum.labelOrUnknown(in.getInboundStatus())
                    + "）；已入库的入库单要冲销请走退货入库");
        }
        String operator = UserUtils.getCurrentEmployeeName();
        detailMapper.markCancelled(in.getId());
        this.lambdaUpdate()
                .eq(BizDrugInbound::getId, in.getId())
                .set(BizDrugInbound::getInboundStatus, 4)
                .set(BizDrugInbound::getCancelBy, operator)
                .set(BizDrugInbound::getCancelTime, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .set(BizDrugInbound::getCancelReason, dto.getCancelReason())
                .set(BizDrugInbound::getUpdateBy, operator)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long inboundId) {
        BizDrugInbound in = this.getById(inboundId);
        if (in == null) {
            throw new BusinessException("入库单不存在或已删除");
        }
        if (in.getInboundStatus() != null && in.getInboundStatus() == 3) {
            throw new BusinessException("已入库的入库单不能删除（批次与库存流水已生成）");
        }
        detailMapper.markCancelled(inboundId);
        if (!this.removeById(inboundId)) {
            throw new BusinessException("删除入库单失败");
        }
    }

    private static String emptyToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }
}