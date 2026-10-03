package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 扣款处理留痕行。
 */
@Data
public class DeductLogVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 扣款通知ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long noticeId;

    /**
     * 动作（字典 his_yb_deduct_action）
     */
    private Integer action;

    /**
     * 动作详情/备注
     */
    private String detail;

    private BigDecimal amount;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operateTime;
}
