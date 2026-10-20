package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 住院日清单（按天汇总）。
 */
@Data
public class DailyBillVO implements Serializable {

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 住院号
     */
    private String admissionNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 起始日期
     */
    private String beginDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 合计金额
     */
    private BigDecimal totalAmount;

    /**
     * 明细条数
     */
    private Integer itemCount;

    /**
     * 按天汇总（日期升序）
     */
    private List<DailyBillDayVO> days;
}
