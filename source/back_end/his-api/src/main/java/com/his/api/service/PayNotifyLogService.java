package com.his.api.service;

import com.alibaba.fastjson.JSON;
import com.his.common.enums.PayChannelEnum;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.util.TextUtil;
import com.his.api.enums.PayNotifyResultEnum;
import com.his.api.enums.PayNotifySceneEnum;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 渠道异步通知留痕。
 */
public interface PayNotifyLogService {

    /**
     * 留痕入参（内部指令，不是 HTTP 入参，字段级校验由实现方收口）。
     *
     * <p>报文里能自取的事实（单号/渠道交易号/状态/金额/应用ID）一律由工厂方法从原文里提，
     * 不让每个通知出口各抄一遍键名 —— 抄了就会漂移。
     *
     * @param signVerified 验签结果（1-通过 0-未通过）
     * @param notifyResult 处理结果，取值见 {@link PayNotifyResultEnum}
     * @param ackBody      本方应答原文（渠道协议字段，决定它是否重试）
     */
    record NotifyRecord(Integer channel, Integer notifyScene, String payNo, String tradeNo, String tradeStatus,
                        BigDecimal totalAmount, String appId, Integer signVerified, Integer notifyResult,
                        String ackBody, String rawBody, String errorMsg) {

        /**
         * 支付宝表单参数通知：单号取 out_trade_no，交易号取 trade_no，金额取 total_amount。
         */
        public static NotifyRecord ofAlipay(PayNotifySceneEnum scene, PayNotifyResultEnum result, String ackBody,
                                           boolean signVerified, String errorMsg, Map<String, String> params) {
            Map<String, String> safe = params == null ? Map.of() : params;
            return new NotifyRecord(PayChannelEnum.ALIPAY.getCode(), scene.getCode(),
                    safe.get("out_trade_no"), safe.get("trade_no"), safe.get("trade_status"),
                    parseAmount(safe.get("total_amount")), safe.get("app_id"),
                    signVerified ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode(),
                    result.getCode(), ackBody, toJson(safe), errorMsg);
        }

        /**
         * 非表单报文（微信 V3 是加密 JSON，解密前无从取字段）：原文整体留痕，字段一律留空。
         */
        public static NotifyRecord ofRawText(PayNotifySceneEnum scene, PayNotifyResultEnum result, String ackBody,
                                             Integer channel, boolean signVerified, String errorMsg, String rawText) {
            return new NotifyRecord(channel, scene.getCode(), null, null, null, null, null,
                    signVerified ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode(),
                    result.getCode(), ackBody, TextUtil.trimToEmpty(rawText), errorMsg);
        }

        private static BigDecimal parseAmount(String text) {
            if (!TextUtil.hasText(text)) {
                return null;
            }
            try {
                return new BigDecimal(TextUtil.trim(text));
            } catch (NumberFormatException e) {
                // 金额解析不出来照样要留痕（原文还在），只是这一列空着，不能因为一个脏数字丢掉整条通知
                return null;
            }
        }

        private static String toJson(Map<String, String> params) {
            return JSON.toJSONString(params);
        }
    }

    /**
     * 落一条通知留痕。实现方保证<b>不抛异常</b>：留痕是旁路证据，写不进去只该留 warn，
     * 不能让「证据没记下」把已经收到钱的入账链路打断。
     */
    void recordNotify(NotifyRecord record);
}
