package com.his.pay.spi;

import com.his.pay.service.PayOrderService;
import com.his.pay.vo.PayOrderVO;
import com.his.spi.dto.PayOrderCreateDTO;
import com.his.spi.service.PayOrderFacadeService;
import com.his.spi.vo.PayOrderCreateResultVO;
import com.his.spi.vo.PayOrderInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 支付单端口实现：收款凭证层的建单、取单、实收确认与关单的跨模块出口。
 */
@Service
@RequiredArgsConstructor
public class PayOrderFacadeServiceImpl implements PayOrderFacadeService {

    private final PayOrderService payOrderService;

    @Override
    public PayOrderCreateResultVO createPending(PayOrderCreateDTO dto) {
        PayOrderService.CreateResult created = payOrderService.createPending(new PayOrderService.CreateSpec(
                dto.getBizType(), dto.getBizId(), dto.getPatientId(), dto.getPatientName(),
                dto.getAmount(), dto.getChannel(), dto.getRemark()));
        PayOrderCreateResultVO result = new PayOrderCreateResultVO();
        result.setOrder(toOrderInfo(created.order()));
        result.setReused(created.reused());
        return result;
    }

    @Override
    public PayOrderInfoVO reusablePending(Integer bizType, Long bizId, Integer channel, BigDecimal amount) {
        return toOrderInfo(payOrderService.reusablePending(bizType, bizId, channel, amount));
    }

    @Override
    public PayOrderInfoVO getByPayNo(String payNo) {
        return toOrderInfo(payOrderService.getByPayNo(payNo));
    }

    @Override
    public PayOrderInfoVO confirmPaid(String payNo, Integer channel, BigDecimal channelAmount, String channelTxnNo) {
        return toOrderInfo(payOrderService.confirmPaid(payNo, channel, channelAmount, channelTxnNo));
    }

    @Override
    public void closePending(String payNo) {
        payOrderService.closePending(payNo);
    }

    @Override
    public void closePendingByBiz(Integer bizType, Long bizId) {
        payOrderService.closePendingByBiz(bizType, bizId);
    }

    @Override
    public List<PayOrderInfoVO> listPendingByBiz(Integer bizType, Long bizId) {
        List<PayOrderVO> pending = payOrderService.listPendingByBiz(bizType, bizId);
        List<PayOrderInfoVO> list = new ArrayList<>(pending.size());
        for (PayOrderVO order : pending) {
            list.add(toOrderInfo(order));
        }
        return list;
    }

    @Override
    public void closeExpiredIfPending(String payNo) {
        payOrderService.closeExpiredIfPending(payNo);
    }

    /**
     * 逐字段显式搬运：跨模块契约的宽度只由本端口决定，源类型以后加列不会把窄接口无声撑宽。
     */
    private PayOrderInfoVO toOrderInfo(PayOrderVO order) {
        if (order == null) {
            return null;
        }
        PayOrderInfoVO vo = new PayOrderInfoVO();
        vo.setId(order.getId());
        vo.setPayNo(order.getPayNo());
        vo.setBizType(order.getBizType());
        vo.setBizId(order.getBizId());
        vo.setPatientId(order.getPatientId());
        vo.setPatientName(order.getPatientName());
        vo.setAmount(order.getAmount());
        vo.setChannel(order.getChannel());
        vo.setPayStatus(order.getPayStatus());
        vo.setOutTradeNo(order.getOutTradeNo());
        vo.setPayTime(order.getPayTime());
        vo.setExpireTime(order.getExpireTime());
        vo.setCashierId(order.getCashierId());
        vo.setCashierName(order.getCashierName());
        return vo;
    }
}
