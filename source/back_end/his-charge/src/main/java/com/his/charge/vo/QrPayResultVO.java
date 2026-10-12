package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 扫码收款轮询结果：前端只认 payStatus，其余字段用来把账单余额同步刷新。
 */
@Data
public class QrPayResultVO {

    private String payNo;

    /**
     * 支付单状态（0-待支付 1-已支付 2-已关闭 3-已退款）
     */
    private Integer payStatus;

    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    /**
     * 账单当前仍欠多少（收款入账后由流水现算，供前端刷新列表而不是再点一次收款）
     */
    private BigDecimal unpaidAmount;
}
