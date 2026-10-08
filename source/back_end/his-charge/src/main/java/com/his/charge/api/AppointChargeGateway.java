package com.his.charge.api;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * 挂号侧对收费能力的引用点（SPI）。
 */
public interface AppointChargeGateway {

    /**
     * 批量查询账单简要信息（挂号看板装配缴费状态用，禁止在循环里单条查）。
     *
     * @param billIds 账单ID集合
     * @return billId -> 简要信息
     */
    Map<Long, BillBrief> mapBillsByIds(Collection<Long> billIds);

    /**
     * 查询单张账单简要信息
     *
     * @return 不存在时返回 null
     */
    BillBrief getBill(Long billId);

    /**
     * 挂号费结算：记账（挂号费 + 诊查费）并立即出账。
     *
     * @return 已落库的账单简要信息；<b>免收时返回 null</b>（没有应收，也就没有账单）
     */
    BillBrief createRegistCharge(RegistChargeCommand command);

    /**
     * 挂号作废时联动处理账单 —— <b>退号不退钱是本系统里最容易漏掉的一环</b>。
     *
     * <p>口径：整单撤销 = 收进来的钱按原收款流水逐路退回 + 本账单的记账行全额红冲 + 账单作废，
     * 三件事必须在<b>同一个事务</b>里。只置挂号状态不处理钱，会留下「已退号 + 账单仍是已支付」
     * 的对账缺口；反过来只退钱不冲应收，收费台上就永远挂着这张收不到的账。
     *
     * @return true=本次确实撤销；false=没有账单或账单早已关闭（幂等跳过）
     */
    boolean cancelRegistCharge(CancelCommand command);

/**
 * 账单撤销命令
 */
    @Data
    class CancelCommand {
        /**
         * 账单ID
         */
        private Long billId;
        /**
         * 退号原因（同时写进退款原因）
         */
        private String reason;
        /**
         * 操作人，服务端从当前登录用户取，不用前端传
         */
        private String operator;
    }

/**
 * 账单简要信息。
 */
    @Data
    class BillBrief {
        private Long billId;
        private String billNo;
        /**
         * 账单状态，字典 {@code his_bill_status}：1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费
         */
        private Integer billStatus;
        /**
         * 个人应缴（统筹与个账已扣掉）
         */
        private BigDecimal payableAmount;
        /**
         * 净已收（收款合计 - 退款合计）
         */
        private BigDecimal paidAmount;
    }

/**
 * 挂号费记账出账命令
 */
    @Data
    class RegistChargeCommand {
        private Long registId;
        private String registNo;
        private Long patientId;
        private String patientNo;
        private String patientName;
        private Long deptId;
        private String deptName;
        private Long doctorId;
        private String doctorName;
        private BigDecimal registFee;
        private BigDecimal diagnosisFee;
        /**
         * 免收挂号费（复诊号）：<b>不记账也不出账单</b>，本方法返回 null。
         *
         * <p>签到门禁的口径是「这张号没有未结清的挂号账单」，所以免收不需要任何假单据兜底；
         * 旧模型里"0 元收费单直接置已收费"那套已经随收费单一起退役。
         */
        private boolean waived;
        /**
         * 为什么免（命中的复诊收费策略名 + 免了哪几项）。部分免收时写进账单备注，
         * 医保/审计追问「这张号为什么少收」时必须答得出来。
         */
        private String waiveReason;
    }
}
