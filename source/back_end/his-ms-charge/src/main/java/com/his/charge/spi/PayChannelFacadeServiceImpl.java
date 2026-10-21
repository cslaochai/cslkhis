package com.his.charge.spi;

import com.his.common.enums.PayChannelEnum;
import com.his.common.enums.PayNotifyResultEnum;
import com.his.common.enums.PayNotifySceneEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.charge.service.AlipayChannelService;
import com.his.charge.service.ChargePayChannelService;
import com.his.charge.service.PayNotifyLogService;
import com.his.charge.service.PayOrderService;
import com.his.charge.service.WxPayChannelService;
import com.his.charge.vo.PayOrderVO;
import com.his.spi.dto.ChannelNotifyRecordDTO;
import com.his.spi.service.PayChannelFacadeService;
import com.his.spi.vo.ChannelTradeStateVO;
import com.his.spi.vo.MicroPayResultVO;
import com.his.spi.vo.MiniPayUnifiedResultVO;
import com.his.spi.vo.PayOrderInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付渠道端口实现：把渠道侧动作与通知留痕原样透出到跨模块契约。
 */
@Service
@RequiredArgsConstructor
public class PayChannelFacadeServiceImpl implements PayChannelFacadeService {

    private final ChargePayChannelService chargePayChannelService;

    private final AlipayChannelService alipayChannelService;

    private final WxPayChannelService wxPayChannelService;

    private final PayNotifyLogService payNotifyLogService;

    private final PayOrderService payOrderService;

    @Override
    public MicroPayResultVO microPay(String authCode, String orderNo, BigDecimal amount, String subject, Integer channel) {
        ChargePayChannelService.PayResult result =
                chargePayChannelService.microPay(authCode, orderNo, amount, subject, channel);
        MicroPayResultVO vo = new MicroPayResultVO();
        vo.setSuccess(result.success());
        vo.setChannelTxnNo(result.channelTxnNo());
        vo.setErrMsg(result.errMsg());
        return vo;
    }

    @Override
    public boolean verifyAlipayNotify(Map<String, String> params) {
        return alipayChannelService.verifyNotify(params);
    }

    @Override
    public boolean isAlipayPaidStatus(String tradeStatus) {
        return alipayChannelService.isPaidStatus(tradeStatus);
    }

    @Override
    public ChannelTradeStateVO queryAlipayTrade(String payNo) {
        return toTradeStateVO(alipayChannelService.queryTrade(payNo));
    }

    @Override
    public boolean isWxPaidStatus(String tradeStatus) {
        return wxPayChannelService.isPaidStatus(tradeStatus);
    }

    @Override
    public ChannelTradeStateVO queryWxTrade(String payNo) {
        return toTradeStateVO(wxPayChannelService.queryTrade(payNo));
    }

    @Override
    public void recordChannelNotify(ChannelNotifyRecordDTO dto) {
        // 内部指令入口，Bean Validation 不跑，判空只在这里做，缺内容直接报错不兜底
        if (dto == null) {
            throw new BusinessException("通知留痕内容不能为空");
        }
        PayNotifySceneEnum scene = PayNotifySceneEnum.fromCode(dto.getNotifyScene());
        if (scene == null) {
            throw new BusinessException("通知场景取值不合法");
        }
        PayNotifyResultEnum result = PayNotifyResultEnum.fromCode(dto.getNotifyResult());
        if (result == null) {
            throw new BusinessException("通知处理结果取值不合法");
        }
        boolean signVerified = Boolean.TRUE.equals(dto.getSignVerified());
        // 报文形态决定取哪一种留痕：表单参数走「报文里自取字段」，非表单报文整体留原文。
        // 键名提取只在渠道层的工厂方法里发生，本类不抄一份，否则两处口径迟早漂移
        if (dto.getNotifyParams() != null) {
            payNotifyLogService.recordNotify(PayNotifyLogService.NotifyRecord.ofAlipay(
                    scene, result, dto.getAckBody(), signVerified, dto.getErrorMsg(), dto.getNotifyParams()));
            return;
        }
        payNotifyLogService.recordNotify(PayNotifyLogService.NotifyRecord.ofRawText(
                scene, result, dto.getAckBody(), dto.getChannel(), signVerified, dto.getErrorMsg(), dto.getRawBody()));
    }

    @Override
    public MiniPayUnifiedResultVO unifiedJsapiOrder(PayOrderInfoVO order, Integer channel) {
        MiniPayUnifiedResultVO result = new MiniPayUnifiedResultVO();
        if (order == null || !TextUtil.hasText(order.getPayNo())) {
            result.setErrMsg("缺少支付单，无法预下单");
            return result;
        }
        PayChannelEnum channelEnum = PayChannelEnum.fromCode(channel);
        if (channelEnum == null) {
            result.setErrMsg("支付渠道不合法：" + PayChannelEnum.getLabelOrUnknown(channel));
            return result;
        }
        // 金额与业务类型以服务端落库事实为准，不采信调用方快照上的值
        PayOrderVO internal = payOrderService.getByPayNo(order.getPayNo());
        if (internal == null) {
            result.setErrMsg("支付单不存在：" + order.getPayNo());
            return result;
        }
        try {
            WxPayChannelService.PayUnifiedResult unified = switch (channelEnum) {
                case WECHAT -> wxPayChannelService.unifiedOrder(internal);
                case ALIPAY -> alipayChannelService.unifiedOrder(internal);
                case UNIONPAY -> WxPayChannelService.PayUnifiedResult.fail("云闪付暂不支持小程序支付");
            };
            result.setPayParams(unified.payParams());
            result.setErrMsg(unified.errMsg());
        } catch (RuntimeException e) {
            // 配置缺失/签名/网络失败一律收成失败原因：渠道没给出参数就是没出成收银台，不能误报成功
            result.setErrMsg("预下单失败：" + e.getMessage());
        }
        return result;
    }

    private ChannelTradeStateVO toTradeStateVO(AlipayChannelService.TradeState state) {
        if (state == null) {
            return null;
        }
        ChannelTradeStateVO vo = new ChannelTradeStateVO();
        vo.setTradeNo(state.tradeNo());
        vo.setTradeStatus(state.tradeStatus());
        vo.setTotalAmount(state.totalAmount());
        return vo;
    }

    private ChannelTradeStateVO toTradeStateVO(WxPayChannelService.TradeState state) {
        if (state == null) {
            return null;
        }
        ChannelTradeStateVO vo = new ChannelTradeStateVO();
        vo.setTradeNo(state.tradeNo());
        vo.setTradeStatus(state.tradeStatus());
        vo.setTotalAmount(state.totalAmount());
        return vo;
    }
}
