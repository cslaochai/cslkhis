package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.charge.dto.RefundFlowQueryPageDTO;
import com.his.charge.entity.BizPaymentTxn;
import com.his.charge.entity.BizSettlementBillItem;
import com.his.charge.mapper.BizPaymentTxnMapper;
import com.his.charge.mapper.BizSettlementBillItemMapper;
import com.his.charge.service.RefundFlowService;
import com.his.charge.vo.BizRefundFlowDetailVO;
import com.his.charge.vo.BizRefundFlowItemVO;
import com.his.charge.vo.BizRefundFlowVO;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 退费流水台账实现（只读，数据源为四层支付流水支付资金流水 direction=2）。
 *
 * <p>详见 {@link RefundFlowService} 类注释：M7 的旧退费单/旧退费明细
 * 双写台账已并入 L3 支付流水，本服务不再写任何表，只把 {@code direction=2} 的流水投影成台账 VO。
 */
@Service
@RequiredArgsConstructor
public class RefundFlowServiceImpl implements RefundFlowService {

    private final BizPaymentTxnMapper paymentTxnMapper;
    private final BizSettlementBillItemMapper billItemMapper;

    @Override
    public PageResult<BizRefundFlowVO> selectFlowPage(RefundFlowQueryPageDTO query) {
        LambdaQueryWrapper<BizPaymentTxn> wrapper = new LambdaQueryWrapper<BizPaymentTxn>()
                .eq(BizPaymentTxn::getDirection, 2)
                .eq(BizPaymentTxn::getTxnStatus, 1)
                .eq(query.getRefundMethod() != null, BizPaymentTxn::getRefundMethod, query.getRefundMethod())
                .eq(query.getPatientId() != null, BizPaymentTxn::getPatientId, query.getPatientId())
                // 流水来源：1-退费申请(applyId 有值) / 2-直退·退号联动(applyId 为空)；0-存量在四层不存在
                .and(query.getFlowSource() != null, w -> {
                    if (query.getFlowSource() == 1) {
                        w.isNotNull(BizPaymentTxn::getApplyId);
                    } else if (query.getFlowSource() == 2 || query.getFlowSource() == 3) {
                        w.isNull(BizPaymentTxn::getApplyId);
                    } else {
                        w.apply("1 = 0");
                    }
                })
                .and(StringUtils.hasText(query.getKeyword()), w -> w.like(BizPaymentTxn::getTxnNo, query.getKeyword())
                        .or().like(BizPaymentTxn::getBillNo, query.getKeyword())
                        .or().like(BizPaymentTxn::getPatientName, query.getKeyword()))
                // 同毫秒退多笔翻页会重复/漏行，补 id 作二级键
                .orderByDesc(BizPaymentTxn::getTxnTime)
                .orderByDesc(BizPaymentTxn::getId);
        Page<BizPaymentTxn> page = paymentTxnMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<BizRefundFlowVO> records = page.getRecords().stream()
                .map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public BizRefundFlowDetailVO getFlowDetail(Long refundId) {
        BizPaymentTxn txn = paymentTxnMapper.selectById(refundId);
        if (txn == null) {
            throw new BusinessException("退费流水不存在");
        }
        BizRefundFlowDetailVO vo = new BizRefundFlowDetailVO();
        BeanUtils.copyProperties(toVO(txn), vo);
        List<BizRefundFlowItemVO> items = billItemMapper.selectList(new LambdaQueryWrapper<BizSettlementBillItem>()
                        .eq(BizSettlementBillItem::getBillId, txn.getBillId())
                        .orderByAsc(BizSettlementBillItem::getId))
                .stream().map(this::toItem).collect(Collectors.toList());
        vo.setDetails(items);
        return vo;
    }

    private BizRefundFlowVO toVO(BizPaymentTxn txn) {
        BizRefundFlowVO vo = new BizRefundFlowVO();
        vo.setId(txn.getId());
        // 退款单号复用支付流水号（RT 前缀），前端仅展示
        vo.setRefundNo(txn.getTxnNo());
        vo.setChargeId(txn.getBillId());
        // 原收费单号 = 被退的结算账单号
        vo.setChargeNo(txn.getBillNo());
        vo.setPatientId(txn.getPatientId());
        vo.setPatientNo(txn.getPatientNo());
        vo.setPatientName(txn.getPatientName());
        // 退款金额为负，台账展示取绝对值
        vo.setTotalAmount(txn.getAmount() == null ? BigDecimal.ZERO : txn.getAmount().abs());
        vo.setRefundReason(txn.getReason());
        // 四层流水成功即"已退费"，恒为 3
        vo.setRefundStatus(3);
        vo.setRefundMethod(txn.getRefundMethod());
        vo.setPayMethod(txn.getPayMethod());
        vo.setChannelRefundNo(txn.getReceiptNo());
        vo.setFlowSource(txn.getApplyId() != null ? 1 : 2);
        vo.setApplyId(txn.getApplyId());
        vo.setApplyNo(txn.getApplyNo());
        vo.setInsuranceCancelled(txn.getInsuranceCancelled());
        vo.setRefundBy(txn.getCashierName());
        vo.setRefundTime(txn.getTxnTime());
        vo.setCreateTime(txn.getCreateTime());
        return vo;
    }

    private BizRefundFlowItemVO toItem(BizSettlementBillItem item) {
        BizRefundFlowItemVO vo = new BizRefundFlowItemVO();
        vo.setItemName(item.getItemName());
        vo.setSpecification(item.getSpecification());
        // 整单退：被退数量 = 账单数量，被退金额 = 账单金额
        vo.setRefundQuantity(item.getQuantity());
        vo.setUnit(item.getUnit());
        vo.setPrice(item.getPrice());
        vo.setRefundAmount(item.getAmount());
        vo.setSourceNo(null);
        return vo;
    }
}
