package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 患者端待缴账单（账单头 + 逐条明细），对应
 * {@code SettlementBillService#pendingBillViews}。
 *
 * <p>患者端小程序缴费入口的列表数据源：只回答"该收多少、每行是什么"，
 * <b>不回答钱到没到</b> —— 已收以 L3 支付流水为准。
 *
 * <p>明细里带上 {@code poolAmount/accountAmount/selfAmount} 三个医保拆分列是刻意的：
 * 患者端「自付为什么这么多」需要行级快照自证，只给一个合计数患者只会认为算错了。
 *
 * <p>⚠ 本结构目前<b>不直接对外序列化</b>：{@code SettlementBillService#pendingBillsForPatient}
 * 因跨模块调用方（his-miniapp）仍按 {@code Map} 取值而保留旧签名，由实现类转换成
 * {@code Map<String, Object>} 出参。键名与取值形态必须与本 VO 字段一一对应，改一个就要改另一个。
 */
@Data
public class PendingBillVO implements Serializable {

    /**
     * 账单ID（BigINT，序列化成字符串避免前端精度丢失）
     */
    private String id;

    /**
     * 账单号
     */
    private String billNo;

    /**
     * 就诊流水号
     */
    private String encounterNo;

    /**
     * 本次应收合计（元）
     */
    private BigDecimal payableAmount;

    /**
     * 出账时间
     */
    private java.time.LocalDateTime billTime;

    /**
     * 账单状态（1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费）
     */
    private Integer billStatus;

    /**
     * 摊行明细
     */
    private List<PendingBillItemVO> details;

    /**
     * 医保统筹合计（元，由明细汇总）
     */
    private BigDecimal poolAmount;

    /**
     * 医保个账合计（元，由明细汇总）
     */
    private BigDecimal accountAmount;

    /**
     * 自费合计（元，由明细汇总）
     */
    private BigDecimal selfAmount;
}