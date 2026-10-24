package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 床旁结算出账结果 = 住院结算单 + 亮码需要的账单锚点。
 *
 * <p>为什么单独包一层：{@link InpatientSettlementVO} 不带 billId，
 * 而床旁下一步就是按账单出聚合码，让前端拿 admissionId 再绕一次查询纯属浪费。
 */
@Data
public class BedsideSettleResultVO {

    @Schema(description = "住院结算单")
    private InpatientSettlementVO settlement;

    @Schema(description = "出院结算账单ID（亮码用）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    @Schema(description = "出院结算账单号")
    private String billNo;

    @Schema(description = "未付清金额（>0 才需要亮码；余额已抵/已退差后可能为 0）")
    private BigDecimal unpaidAmount;
}
