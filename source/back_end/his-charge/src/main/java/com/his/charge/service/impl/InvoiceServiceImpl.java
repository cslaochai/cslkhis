package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.dto.InvoiceIssueDTO;
import com.his.charge.entity.BizInvoice;
import com.his.charge.entity.BizPaymentTxn;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.mapper.BizInvoiceMapper;
import com.his.charge.service.InvoiceService;
import com.his.charge.service.PaymentService;
import com.his.charge.service.SettlementBillService;
import com.his.charge.vo.BizInvoiceVO;
import com.his.common.base.PageResult;
import com.his.common.enums.BillStatusEnum;
import com.his.common.enums.InvoiceStatusEnum;
import com.his.common.enums.PayDirectionEnum;
import com.his.common.enums.PayTxnStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.system.service.RedisSequenceService;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 发票服务实现（L4 票据）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl extends ServiceImpl<BizInvoiceMapper, BizInvoice> implements InvoiceService {

    private static final int AMOUNT_SCALE = 2;

    private static final int W_INVOICE_NO = 32;
    private static final int W_BILL_NO = 32;
    private static final int W_PATIENT_NO = 32;
    private static final int W_PATIENT_NAME = 50;
    private static final int W_VOID_REASON = 200;
    private static final int W_REMARK = 500;

    private final SettlementBillService settlementBillService;
    private final PaymentService paymentService;
    private final RedisSequenceService redisSequenceService;

    private static List<BizInvoiceVO> toVOList(List<BizInvoice> entities) {
        List<BizInvoiceVO> vos = new ArrayList<>(entities.size());
        for (BizInvoice entity : entities) {
            vos.add(toVO(entity));
        }
        return vos;
    }

    private static BizInvoiceVO toVO(BizInvoice entity) {
        if (entity == null) {
            return null;
        }
        BizInvoiceVO vo = new BizInvoiceVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizInvoiceVO issueByBill(InvoiceIssueDTO dto) {
        if (dto.getBillId() == null) {
            throw new BusinessException("缺少结算账单");
        }
        BizSettlementBill bill = settlementBillService.getById(dto.getBillId());
        if (bill == null) {
            throw new BusinessException("结算账单不存在");
        }
        if (BillStatusEnum.VOIDED.getCode().equals(bill.getBillStatus())) {
            throw new BusinessException("账单已作废，没有收过钱，无票可开");
        }
        BigDecimal net = netReceived(bill.getId());
        if (net.signum() == 0) {
            throw new BusinessException("该账单净实收为 0（统筹后付或全额减免），没有需要开票的钱，结算凭证用账单本身");
        }
        List<BizInvoice> live = this.list(new LambdaQueryWrapper<BizInvoice>()
                .eq(BizInvoice::getBillId, bill.getId())
                .in(BizInvoice::getInvoiceStatus, InvoiceStatusEnum.ISSUED.getCode(), InvoiceStatusEnum.PRINTED.getCode())
                .orderByDesc(BizInvoice::getId));
        if (!live.isEmpty()) {
            BizInvoice old = live.get(0);
            if (NumUtil.scale(old.getTotalAmount(), AMOUNT_SCALE).compareTo(net) != 0) {
                // 票比钱多是常事：开完票又退了一笔。这时候第二张票只会把差额藏得更深
                throw new BusinessException("该账单已有有效发票 " + old.getInvoiceNo()
                        + "（票面 " + old.getTotalAmount().toPlainString() + "，当前净实收 " + net.toPlainString()
                        + "），请先作废原票再重开");
            }
            throw new BusinessException("该账单已开具发票 " + old.getInvoiceNo() + "，一票对一账单，不重复出票");
        }

        BizInvoice invoice = new BizInvoice();
        invoice.setInvoiceNo(TextUtil.cut(redisSequenceService.generateInvoiceNo(), W_INVOICE_NO));
        invoice.setInvoiceType(dto.getInvoiceType() == null ? 1 : dto.getInvoiceType());
        invoice.setBillId(bill.getId());
        invoice.setBillNo(TextUtil.cut(bill.getBillNo(), W_BILL_NO));
        invoice.setPatientId(bill.getPatientId());
        invoice.setPatientNo(TextUtil.cut(bill.getPatientNo(), W_PATIENT_NO));
        invoice.setPatientName(TextUtil.cut(bill.getPatientName(), W_PATIENT_NAME));
        invoice.setTotalAmount(net);
        invoice.setInvoiceStatus(InvoiceStatusEnum.ISSUED.getCode());
        invoice.setInvoiceTime(LocalDateTime.now());
        invoice.setCreateBy(TextUtil.cut(UserUtils.getCurrentUser().getRealName(), 64));
        invoice.setCreateTime(LocalDateTime.now());
        invoice.setRemark(TextUtil.cut(dto.getRemark(), W_REMARK));
        // 作废后重开：新票指回被作废的那张，红冲链在票这一层也留痕
        BizInvoice lastVoided = this.getOne(new LambdaQueryWrapper<BizInvoice>()
                .eq(BizInvoice::getBillId, bill.getId())
                .in(BizInvoice::getInvoiceStatus, InvoiceStatusEnum.VOIDED.getCode(), InvoiceStatusEnum.REVERSED.getCode())
                .orderByDesc(BizInvoice::getId)
                .last("LIMIT 1"));
        if (lastVoided != null) {
            invoice.setOrigInvoiceId(lastVoided.getId());
            lastVoided.setInvoiceStatus(InvoiceStatusEnum.REVERSED.getCode());
            this.updateById(lastVoided);
        }
        this.save(invoice);
        log.info("[出票] 发票 {} 账单 {} 票面 ¥{}（净实收）换开自={}", invoice.getInvoiceNo(), bill.getBillNo(),
                net.toPlainString(), invoice.getOrigInvoiceId());
        return toVO(invoice);
    }

    @Override
    public PageResult<BizInvoiceVO> selectInvoicePage(Long patientId, Long billId, Integer invoiceStatus,
                                                      String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<BizInvoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizInvoice::getPatientId, patientId)
                .eq(billId != null, BizInvoice::getBillId, billId)
                .eq(invoiceStatus != null, BizInvoice::getInvoiceStatus, invoiceStatus)
                .and(TextUtil.hasText(keyword), w -> w.like(BizInvoice::getInvoiceNo, keyword)
                        .or().like(BizInvoice::getBillNo, keyword)
                        .or().like(BizInvoice::getChargeNo, keyword)
                        .or().like(BizInvoice::getPatientName, keyword))
                // create_time 大面积重复，必须补 id 二级键，否则分页会重复/漏行
                .orderByDesc(BizInvoice::getInvoiceTime)
                .orderByDesc(BizInvoice::getId);

        Page<BizInvoice> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), toVOList(page.getRecords()));
    }

    @Override
    public BizInvoiceVO getInvoiceDetail(Long invoiceId) {
        return toVO(this.getById(invoiceId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean printInvoice(Long invoiceId) {
        BizInvoice invoice = requireInvoice(invoiceId);
        if (InvoiceStatusEnum.fromCode(invoice.getInvoiceStatus()) != InvoiceStatusEnum.ISSUED) {
            throw new BusinessException("只有「已开具」的票能标记打印，当前是「"
                    + InvoiceStatusEnum.descOf(invoice.getInvoiceStatus()) + "」");
        }
        invoice.setInvoiceStatus(InvoiceStatusEnum.PRINTED.getCode());
        invoice.setPrintTime(LocalDateTime.now());
        return this.updateById(invoice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean voidInvoice(Long invoiceId, String reason) {
        BizInvoice invoice = requireInvoice(invoiceId);
        InvoiceStatusEnum status = InvoiceStatusEnum.fromCode(invoice.getInvoiceStatus());
        if (status == InvoiceStatusEnum.VOIDED) {
            return true;
        }
        if (status == InvoiceStatusEnum.REVERSED) {
            throw new BusinessException("该票已红冲换开，不能重复作废");
        }
        if (!TextUtil.hasText(reason)) {
            throw new BusinessException("缺少作废原因");
        }
        invoice.setInvoiceStatus(InvoiceStatusEnum.VOIDED.getCode());
        invoice.setVoidTime(LocalDateTime.now());
        invoice.setVoidReason(TextUtil.cut(reason, W_VOID_REASON));
        this.updateById(invoice);
        log.info("[作废发票] 发票 {} 票面 ¥{} 原因：{}", invoice.getInvoiceNo(),
                invoice.getTotalAmount().toPlainString(), reason);
        return true;
    }

    /**
     * 净实收：Σ 成功收款 − Σ 成功退款。取流水不取账单的 paid/refund 镜像列 ——
     * 镜像列本身就是从流水算出来的，票面再信镜像等于把同一个数当两个独立证据。
     */
    private BigDecimal netReceived(Long billId) {
        BigDecimal net = BigDecimal.ZERO;
        for (BizPaymentTxn txn : paymentService.listByBill(billId)) {
            if (!PayTxnStatusEnum.SUCCESS.getCode().equals(txn.getTxnStatus()) || txn.getAmount() == null) {
                continue;
            }
            net = net.add(PayDirectionEnum.REFUND.getCode().equals(txn.getDirection())
                    ? txn.getAmount().abs().negate() : txn.getAmount());
        }
        return NumUtil.scale(net, AMOUNT_SCALE);
    }

    private BizInvoice requireInvoice(Long invoiceId) {
        if (invoiceId == null) {
            throw new BusinessException("缺少发票");
        }
        BizInvoice invoice = this.getById(invoiceId);
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        return invoice;
    }
}
