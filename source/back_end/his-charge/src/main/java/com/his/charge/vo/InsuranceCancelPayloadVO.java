package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 医保 2305 撤销报文（{@code InsuranceSettlementServiceImpl#sendCancel}）。
 *
 * <p>报文是<b>发给医保局的外部契约</b>，字段名/key 一个都不能动 ——
 * 改一个 key 就是对方的必填项校验不过，报盘直接被拒，且这个错误只在联调时暴露。
 *
 * <p>本类是G7 样例报文（真实对接时按医保前置机 2305 规范做字段映射，
 * 替换 {@code InsuranceChannelService} 即可），序列化由 Jackson ，
 * 字段声明顺序即 JSON 键顺序。
 */
@Data
public class InsuranceCancelPayloadVO implements Serializable {

    /**
     * 报文类型，固定 2305（撤销）
     */
    private String msgType;

    /**
     * 本次交易流水号
     */
    private String tradeNo;

    /**
     * 被撤销的上传交易流水号
     */
    private String origTradeNo;

    /**
     * 结算清单号
     */
    private String settlementNo;

    /**
     * 账单号
     */
    private String billNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 撤销原因
     */
    private String cancelReason;

    /**
     * 发送时间（{@code yyyy-MM-dd HH:mm:ss}）
     */
    private String sendTime;

    /**
     * 备注（G7 样例报文说明）
     */
    private String note;
}