package com.his.charge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.charge.dto.InpatientSettlementUpsertDTO;
import com.his.charge.dto.PrepayQueryPageDTO;
import com.his.charge.dto.PrepayUpsertDTO;
import com.his.charge.vo.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 住院账务服务（P3）：预交金 / 日清单 / 出院结算 / 欠费提示。
 */
public interface InpatientAccountService {

    /**
     * 预交金流水分页（按收退时间倒序；充值正、退款负）
     */
    IPage<PrepayVO> prepayListPage(PrepayQueryPageDTO query);

    /**
     * 预交金账户（充值合计 / 柜面退款合计 / 当前余额 / 流水笔数）
     */
    PrepayBalanceVO balance(Long admissionId);

    /**
     * 收预交金（充值 / 退款）。
     *
     * <p>返回<b>多行</b>而不是一行：柜面退款按 FIFO 可能摊到好几笔原充值流水上
     * （微信收的退微信、现金收的退现金），强行合成一行就编不出渠道号。
     */
    List<PrepayVO> savePrepay(PrepayUpsertDTO dto);

    /**
     * 住院日清单（按天汇总）
     */
    DailyBillVO dailyBill(Long admissionId, String beginDate, String endDate);

    /**
     * 出院结算试算：L2 草稿的应缴 + 住院账户余额抵扣/退差/欠费。
     *
     * <p>医保类型不作为入参：由 L2 按患者参保号推（两处各判一次就会漂）。
     */
    InpatientSettlementPreviewVO preview(Long admissionId, Integer settleMode);

    /**
     * 办理出院结算（一次住院一张有效账单；欠费也出账单，只是账单留在未付清）
     */
    InpatientSettlementVO settle(InpatientSettlementUpsertDTO dto);

    /**
     * 当前有效的出院结算账单（没有返回 {@code null}；已作废的不算）
     */
    InpatientSettlementVO settlementDetail(Long admissionId);

    /**
     * 住院账务概览（医生站 / 护士站的欠费提示；欠费时留一条预警记录，所以别把它当只读查询用）
     */
    InpatientAccountSummaryVO summary(Long admissionId);

    /**
     * 在院欠费算式的四个数（纯只读，不写告警）。
     *
     * <p>欠费管控 gate 与 {@link #summary} 必须走这里，而不是各抄一份公式 ——
     * 同一个患者在前台显示欠 800、在医嘱开立处显示不欠，就等于没有口径。
     * 高频只读查询（出院前查状态、开医嘱前查管控）不该有写告警的副作用。
     */
    ArrearsView arrearsView(Long admissionId);


    record ArrearsView(BigDecimal chargedNet, BigDecimal collected,
                       BigDecimal prepayBalance, BigDecimal arrearsAmount) {
    }
}
