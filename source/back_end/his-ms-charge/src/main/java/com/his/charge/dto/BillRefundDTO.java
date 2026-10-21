package com.his.charge.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 按账单退费入参：应收侧红冲记账行 + 资金侧按原收款流水逐笔退回，同一个事务。
 */
@Data
public class BillRefundDTO {

    /**
     * 结算账单ID
     */
    @NotNull(message = "缺少账单")
    private Long billId;

    /**
     * 要退的记账行ID（费用记账流水的ID）；为空 = 本账单全部行（整单退）。
     *
     * <p>行级数量退费走 {@link #lines}，这里只作旧调用方（收费处直退/退号联动）的整行退入口。
     */
    private List<Long> feeIds;

    /**
     * 行级退费明细：勾了哪几行、各退多少数量。给了就以它为准（不再看 {@link #feeIds}）；
     * 没给则回退 {@link #feeIds}（整行退），两者都空 = 整单全退。
     */
    @Valid
    private List<RefundLine> lines;

    /**
     * 原因
     */
    @NotBlank(message = "缺少退费原因")
    private String reason;

    /**
     * 流水来源（字典 his_txn_source）：必填；收费处直退传 5，退费申请执行传 4，退号联动传 6
     */
    @NotNull(message = "请选择流水来源")
    @AllowedValues(values = {"1", "2", "3", "4", "5", "6", "7", "8", "9"}, message = "流水来源不合法")
    private Integer sourceType;

    /**
     * 来源退费申请ID（直退留空）
     */
    private Long applyId;

    /**
     * 来源退费申请号
     */
    private String applyNo;

    /**
     * 一条退费行：记账行 + 本次退费数量。
     *
     * <p>{@code quantity} 为空 = 整行退（剩余量全退）；给了数量 = 数量级部分退
     * （共 5 盒退 2 盒）。服务端按该行剩余可冲数量兜底，不接受超量。
     */
    @Data
    public static class RefundLine {

        @NotNull(message = "缺少记账行")
        private Long feeId;

        private BigDecimal quantity;
    }
}
