package com.his.pay.service.impl;

import com.his.common.util.TextUtil;
import com.his.pay.entity.BizPayNotifyLog;
import com.his.pay.mapper.BizPayNotifyLogMapper;
import com.his.pay.service.PayNotifyLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 渠道异步通知留痕实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayNotifyLogServiceImpl implements PayNotifyLogService {

    private static final int PAY_NO_MAX_LENGTH = 32;
    private static final int TRADE_NO_MAX_LENGTH = 64;
    private static final int TRADE_STATUS_MAX_LENGTH = 32;
    private static final int APP_ID_MAX_LENGTH = 32;
    private static final int ACK_MAX_LENGTH = 16;
    private static final int ERROR_MAX_LENGTH = 500;
    /**
     * 通知原文的落库上限：支付宝一条表单通知含签名约 1~2KB，超出只可能是伪造或异常报文，
     * 截断保住整行（含处理结果）比让超长把整条 insert 顶掉更有排查价值。
     */
    private static final int RAW_MAX_LENGTH = 8000;

    private final BizPayNotifyLogMapper bizPayNotifyLogMapper;

    /**
     * 留痕必须活在入账事务之外，两件事都在这里：
     * ① 入账链路后续步骤抛异常时外层整体回滚，同事务写的留痕会跟着消失 ——
     * 而「收到了通知、入账却失败」正是最需要留下证据的那一条；
     * ② 通知出口是无登录态入口，留痕写不写得成取决于表上这些列可空，
     * 但无论落成什么样，它都只是旁路证据，绝不能反过来把已经收到钱的入账链路打断，所以异常一律吞成 warn。
     * 因此本方法不开事务（NOT_SUPPORTED 会把外层事务挂起，语句按自动提交落地）。
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void recordNotify(NotifyRecord record) {
        // 内部指令入参：不经过参数绑定，Bean Validation 跑不到，只能在这里收口
        if (record == null || record.notifyScene() == null || record.notifyResult() == null) {
            log.warn("[通知留痕] 缺少通知入口或处理结果，不落库");
            return;
        }
        try {
            BizPayNotifyLog row = new BizPayNotifyLog();
            row.setPayNo(TextUtil.cutToNull(record.payNo(), PAY_NO_MAX_LENGTH));
            row.setChannel(record.channel());
            row.setNotifyScene(record.notifyScene());
            row.setTradeNo(TextUtil.cutToNull(record.tradeNo(), TRADE_NO_MAX_LENGTH));
            row.setTradeStatus(TextUtil.cutToNull(record.tradeStatus(), TRADE_STATUS_MAX_LENGTH));
            row.setTotalAmount(record.totalAmount());
            row.setAppId(TextUtil.cutToNull(record.appId(), APP_ID_MAX_LENGTH));
            row.setSignVerified(record.signVerified());
            row.setNotifyResult(record.notifyResult());
            row.setAckBody(TextUtil.cut(record.ackBody(), ACK_MAX_LENGTH));
            row.setRawBody(TextUtil.cut(record.rawBody(), RAW_MAX_LENGTH));
            row.setErrorMsg(TextUtil.cutToNull(record.errorMsg(), ERROR_MAX_LENGTH));
            bizPayNotifyLogMapper.insert(row);
        } catch (Exception e) {
            log.warn("[通知留痕] 落库失败（不影响收款流程）payNo={} err={}", record.payNo(), e.getMessage(), e);
        }
    }
}
