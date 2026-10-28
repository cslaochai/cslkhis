package com.his.charge.service.impl;

import com.his.charge.config.InsuranceProperties;
import com.his.charge.service.InsuranceChannelService;
import com.his.charge.support.InsuranceGatewayClient;
import com.his.charge.support.InsuranceGatewayClient.CancelInput;
import com.his.charge.support.InsuranceGatewayClient.DayBillItem;
import com.his.charge.support.InsuranceGatewayClient.EnvelopeException;
import com.his.charge.support.InsuranceGatewayClient.InsReply;
import com.his.charge.support.InsuranceGatewayClient.PersonData;
import com.his.charge.support.InsuranceGatewayClient.PersonQueryInput;
import com.his.charge.support.InsuranceGatewayClient.ReceiptData;
import com.his.charge.support.InsuranceGatewayClient.ReplyException;
import com.his.charge.support.InsuranceGatewayClient.SettleData;
import com.his.charge.support.InsuranceGatewayClient.TransportException;
import com.his.common.enums.InsuredPersonStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 医保前置系统出口（HTTP 实现）：HIS → csmig前置(8091/mig) → csmis医保局端(8090/mss)。
 *
 * <p>本类只声明「交易码 ↔ 接口路径 ↔ 出入参报文类型」，再把类型化回执落成领域对象；HTTP 传输、
 * 国密信封（SM2 签名 + SM4 加密 + csssl 证书链）与报文类型绑定收口在 {@link InsuranceGatewayClient}。
 *
 * <p>参保核验的业务判定留在本类：身份证是实名金标准、姓名与参保卡号双向比对、
 * 平台不可用按未参保处理（fail-closed，不因专线抖动放行医保）。
 *
 * <p>与本地 mock 实现二选一，由配置 {@code his.insurance.channel.mode=remote} 激活。
 * 上游契约见 csmig 的 GatewayController（/gateway/{msgType}），csmig 透传到 csmis。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "his.insurance.channel.mode", havingValue = "remote")
public class RemoteInsuranceChannelServiceImpl implements InsuranceChannelService {

    /**
     * 上送路径配置里的报文类型占位符，发出前替换为具体 msgType
     */
    private static final String MSG_TYPE_PLACEHOLDER = "{msgType}";

    /**
     * 2305 结算信息撤销的医保接口编号（对方交易码，与院内报盘类型码值无关）
     */
    private static final String MSG_TYPE_CANCEL = "2305";

    /**
     * 本端失败约定码：请求未发出或没拿到有效回执，与对方 infcode 体系（0=医保侧接受）区分
     */
    private static final Integer INFCODE_LOCAL_FAIL = -1;

    private final InsuranceGatewayClient insuranceGatewayClient;
    private final InsuranceProperties.Paths paths;

    public RemoteInsuranceChannelServiceImpl(InsuranceGatewayClient insuranceGatewayClient,
                                             InsuranceProperties properties) {
        this.insuranceGatewayClient = insuranceGatewayClient;
        this.paths = properties.getChannel().getPaths();
    }

    @Override
    public Receipt send(OutboundMessage message) {
        String path = paths.getSend().replace(MSG_TYPE_PLACEHOLDER, message.getMsgType());
        String payload = message.getPayload() == null ? "" : message.getPayload();
        InsReply<ReceiptData> reply;
        try {
            reply = insuranceGatewayClient.post(path, payload, ReceiptData.class);
        } catch (TransportException e) {
            // 平台连不上不抛断业务链路：落一条失败回执，由台账体现，便于重试（与本地 mock 失败语义一致）
            return new Receipt(false, null, null, "医保平台连接失败：" + e.getMessage());
        } catch (EnvelopeException e) {
            return new Receipt(false, null, e.getRawBody(), "医保平台回执安全校验失败：" + e.getMessage());
        } catch (ReplyException e) {
            return new Receipt(false, null, e.getRawBody(), "医保平台回执解析失败：" + e.getMessage());
        }
        ReceiptData data = reply.data();
        if (data == null) {
            // 前置机自身报错时回 data=null：失败原因取对方 message，原口径只能报「infcode=-1」把真因吞掉
            return new Receipt(false, null, reply.rawBusiness(),
                    failReason(reply, "医保平台返回失败"));
        }
        if (data.confirmed()) {
            return new Receipt(true, data.receiptNo(), reply.rawData(), null);
        }
        return new Receipt(false, data.receiptNo(), reply.rawData(), dataFailReason(data, "医保平台返回失败"));
    }

    @Override
    public List<RemoteSettlement> queryDayBill(LocalDate billDate) {
        List<DayBillItem> items;
        try {
            items = insuranceGatewayClient.getForList(paths.getDayBill(), billDate, DayBillItem.class);
        } catch (TransportException e) {
            // 对账必须让使用方感知平台不可用，不能静默返回空（空会被误判成“本地多出差笔”）
            throw new BusinessException("医保平台对账接口不可用：" + e.getMessage());
        } catch (ReplyException e) {
            throw new BusinessException("医保平台对账结果解析失败：" + e.getMessage());
        }
        List<RemoteSettlement> list = new ArrayList<>(items.size());
        for (DayBillItem item : items) {
            list.add(new RemoteSettlement(item.tradeNo(), item.settlementNo(),
                    item.totalAmount(), item.insurancePay(), item.receiptNo()));
        }
        return list;
    }

    @Override
    public InsuredStatus checkInsured(String insuranceNo, String idCard, String name) {
        // 实名核验口径：主档有身份证 → 身份证是金标准，按身份证查参保（查无即未参保）；
        // 主档无身份证 → 退按医保卡号查参保，命中后比姓名防冒用他人卡号
        boolean byIdCard = TextUtil.hasText(idCard);
        InsReply<PersonData> reply;
        try {
            reply = insuranceGatewayClient.post(paths.getPersonQuery(),
                    new PersonQueryInput(byIdCard ? idCard : null, byIdCard ? null : insuranceNo),
                    PersonData.class);
        } catch (TransportException e) {
            // 参保状态不可知，按「不可用」返回，结算侧据此拒医保
            return new InsuredStatus(false, false, null, null, "医保平台连接失败：" + e.getMessage());
        } catch (EnvelopeException e) {
            return new InsuredStatus(false, false, null, null, "医保平台回执安全校验失败：" + e.getMessage());
        } catch (ReplyException e) {
            return new InsuredStatus(false, false, null, null, "医保平台参保查询结果解析失败：" + e.getMessage());
        } catch (IllegalArgumentException e) {
            return new InsuredStatus(false, false, null, null, "医保平台参保查询请求构造失败：" + e.getMessage());
        }
        if (!reply.accepted()) {
            return new InsuredStatus(false, false, null, null, failReason(reply, "医保平台参保查询失败"));
        }
        PersonData data = reply.data();
        if (data == null) {
            return byIdCard
                    ? new InsuredStatus(false, false, null, null,
                    "按身份证未查询到参保记录（未参保登记），请核对身份证/医保卡号或改按自费结算")
                    : new InsuredStatus(false, false, null, null,
                    "参保库查无此卡号（" + insuranceNo + "），未参保登记");
        }
        // 按卡号查询时做姓名比对，防止冒用他人医保卡号
        if (!byIdCard && TextUtil.hasText(name) && !name.equals(data.name())) {
            return new InsuredStatus(false, false, null, null,
                    "参保人身份与患者档案不符（姓名不一致），请核对医保卡号或改按自费结算");
        }
        // 按身份证查到参保人后，再比对其参保卡号与患者主档卡号：不一致 = 卡号填错/冒用他人卡号
        String insuredNo = data.insuranceNo();
        if (byIdCard && TextUtil.hasText(insuredNo) && !insuredNo.equals(insuranceNo)) {
            return new InsuredStatus(false, false, null, null,
                    "医保卡号与参保人身份不符（按身份证查到的参保卡号为 " + insuredNo + "），"
                            + "可能填错或冒用他人卡号，请核对后改按自费结算");
        }
        boolean active = InsuredPersonStatusEnum.isNormal(data.personStatus());
        return new InsuredStatus(true, active, data.insuranceType(), data.name(),
                active ? null : "参保状态为停保（" + (TextUtil.hasText(insuredNo) ? insuredNo : insuranceNo) + "）");
    }

    @Override
    public SettleSplit preSettle(SettleRequest request) {
        return callSettle(paths.getPreSettle(), request);
    }

    @Override
    public SettleSplit formalSettle(SettleRequest request) {
        return callSettle(paths.getFormalSettle(), request);
    }

    /**
     * 实时结算（2201/2301 共用）：SettleRequest 交客户端加签加密上送，回执按 SettleData 绑定后转成费用拆分。
     * 平台连不上/返回非 200 时，返回 infcode=-1 且带 errMsg，由调用方降级为「自费收」。
     */
    private SettleSplit callSettle(String path, SettleRequest request) {
        InsReply<SettleData> reply;
        try {
            reply = insuranceGatewayClient.post(path, request, SettleData.class);
        } catch (TransportException e) {
            boolean uncertain = e.isUncertain();
            SettleSplit err = settleError(request, uncertain
                    ? "医保平台响应超时/连接中断（结果不确定，须先冲正确认再降级）：" + e.getMessage()
                    : "医保平台连接失败：" + e.getMessage());
            err.setUncertain(uncertain);
            return err;
        } catch (EnvelopeException e) {
            return settleError(request, "医保平台回执安全校验失败：" + e.getMessage());
        } catch (ReplyException e) {
            return settleError(request, "医保平台响应解析失败");
        } catch (IllegalArgumentException e) {
            return settleError(request, "医保结算请求序列化失败：" + e.getMessage());
        }
        SettleData d = reply.data();
        if (!reply.accepted() || d == null) {
            return settleError(request, failReason(reply, "医保平台返回失败"));
        }
        SettleSplit split = new SettleSplit();
        split.setInfcode(d.infcode());
        split.setTradeNo(d.tradeNo());
        split.setReceiptNo(d.receiptNo());
        split.setInsuranceType(d.insuranceType());
        split.setName(d.name());
        split.setTotal(d.total());
        split.setInsurancePay(d.insurancePay());
        split.setPersonalAccount(d.personalAccount());
        split.setCashPay(d.cashPay());
        split.setSelfPay(d.selfPay());
        split.setDeductible(d.deductible());
        split.setAccountBalanceAfter(d.accountBalanceAfter());
        split.setWarnMsg(d.warnMsg());
        split.setErrMsg(d.errMsg());
        return split;
    }

    @Override
    public Receipt cancelRealtimeSettle(String origTradeNo, String tradeNo, String reason) {
        CancelInput input = new CancelInput(MSG_TYPE_CANCEL, tradeNo, origTradeNo,
                reason == null ? "HIS 不确定态自动冲正" : reason,
                LocalDateTime.now().format(DateFormats.DATETIME));
        InsReply<ReceiptData> reply;
        try {
            reply = insuranceGatewayClient.post(paths.getCancel(), input, ReceiptData.class);
        } catch (TransportException e) {
            return new Receipt(false, null, null, "医保平台连接失败：" + e.getMessage());
        } catch (EnvelopeException e) {
            return new Receipt(false, null, e.getRawBody(), "医保平台冲正回执安全校验失败：" + e.getMessage());
        } catch (ReplyException | IllegalArgumentException e) {
            return new Receipt(false, null, null, "医保平台冲正回执解析失败：" + e.getMessage());
        }
        ReceiptData data = reply.data();
        boolean confirmed = reply.accepted() && data != null && data.confirmed();
        if (confirmed) {
            return new Receipt(true, data.receiptNo(), reply.rawBusiness(), null);
        }
        String errMsg = data == null || data.errMsg() == null ? reply.message() : data.errMsg();
        return new Receipt(false, data == null ? null : data.receiptNo(), reply.rawBusiness(),
                TextUtil.hasText(errMsg) ? errMsg
                        : "医保平台冲正失败（infcode=" + (data == null ? INFCODE_LOCAL_FAIL : data.infcode()) + "）");
    }

    /** 本端未拿到有效回执时的失败结果：infcode 置约定码，tradeNo 回填供调用方对账 */
    private SettleSplit settleError(SettleRequest request, String errMsg) {
        SettleSplit err = new SettleSplit();
        err.setInfcode(INFCODE_LOCAL_FAIL);
        err.setTradeNo(request.getTradeNo());
        err.setErrMsg(errMsg);
        return err;
    }

    /** 通道层失败原因：优先用对方 message，缺失时退回本端缺省文案（带 code 便于排查） */
    private String failReason(InsReply<?> reply, String defaultMsg) {
        return TextUtil.hasText(reply.message())
                ? reply.message() : defaultMsg + "（code=" + reply.code() + "）";
    }

    /** 业务层失败原因：data 段的 errMsg 优先，没有就带出 infcode（0 才是医保侧接受） */
    private String dataFailReason(ReceiptData data, String defaultMsg) {
        return TextUtil.hasText(data.errMsg())
                ? data.errMsg() : defaultMsg + "（infcode=" + data.infcode() + "）";
    }
}
