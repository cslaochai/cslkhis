package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交班前试算出参（G8 班结预览，不落库）。
 *
 * <p>存在理由：交班单上的「系统现金」原先只在提交那一刻才算得出来，收费员点完钞、
 * 填完实交现金，按下确认才被「现金差异 X 元，必须填写差异说明」打回来 ——
 * 先拒一次再猜着写原因。本 VO 让收费员在弹框里就看到账，差异在提交前就摆平。
 *
 * <p>口径与 {@code FinanceSettlementServiceImpl.handover} 逐字对齐（区间起点、
 * 行集定义），预览与落单之间只差「确认」这一个动作。
 */
@Data
public class CashierHandoverPreviewVO {

    /**
     * 统计区间起（不含）= 上次交班时刻；首次交班回落到当日 00:00:00
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodBegin;

    /**
     * 统计区间止（含）= 本次试算时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodEnd;

    /**
     * 本班收费笔数（未认领的成功收款流水）
     */
    private Integer chargeCount;

    /**
     * 本班收费金额合计（含非现金渠道；现金清点只需看 {@link #cashAmount}）
     */
    private BigDecimal chargeAmount;

    /**
     * 本班退费笔数
     */
    private Integer refundCount;

    /**
     * 本班退费金额（正数）
     */
    private BigDecimal refundAmount;

    /**
     * 本班系统现金（支付方式 1-现金的收款合计）—— 收费员点钞要对的「账」
     */
    private BigDecimal cashAmount;
}
