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

    /**
     * 支付码过期时刻；倒计时走完后为已过期（服务端会顺带把过期未付的单关掉，payStatus 变 2-已关闭）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 服务端当前时间：与 expireTime 一起给前端锚定倒计时（每次轮询都刷新，纠客户端时钟漂移）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serverTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    /**
     * 账单当前仍欠多少（收款入账后由流水现算，供前端刷新列表而不是再点一次收款）
     */
    private BigDecimal unpaidAmount;
}
