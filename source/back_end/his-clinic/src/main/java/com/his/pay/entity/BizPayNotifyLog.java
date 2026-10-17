package com.his.pay.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 渠道异步通知留痕（一条通知一行，只追加不改）。
 */
@Data
@TableName("biz_pay_notify_log")
public class BizPayNotifyLog extends BaseEntity {

    /**
     * 支付单号（通知报文里的商户订单号）
     */
    private String payNo;

    /**
     * 支付渠道（1-微信 2-支付宝 3-云闪付）
     */
    private Integer channel;

    /**
     * 通知入口（1-收费台出码 2-患者端小程序）
     */
    private Integer notifyScene;

    /**
     * 渠道交易号
     */
    private String tradeNo;

    /**
     * 渠道交易状态原文
     */
    private String tradeStatus;

    /**
     * 渠道报文金额（元）
     */
    private BigDecimal totalAmount;

    /**
     * 渠道应用ID
     */
    private String appId;

    /**
     * 验签结果（1-通过 0-未通过）
     */
    private Integer signVerified;

    /**
     * 处理结果（1-已入账 2-重复通知 3-未付款通知 4-验签失败 5-支付单不存在 6-渠道不符 7-金额非法 8-通知不受理 9-入账异常）
     */
    private Integer notifyResult;

    /**
     * 应答内容（success-已受理 failure-要求重试）
     */
    private String ackBody;

    /**
     * 通知原文（表单参数序列化）
     */
    private String rawBody;

    /**
     * 拒收/异常原因
     */
    private String errorMsg;

    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;
}
