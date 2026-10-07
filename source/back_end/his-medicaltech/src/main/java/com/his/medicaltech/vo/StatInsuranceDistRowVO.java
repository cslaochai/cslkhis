package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 险种构成（{@code StatReportAggMapper#insuranceDist} 一行）。
 *
 * <p>{@code insuranceType} 是<b>名称</b>不是编码（该列VARCHAR 直接存"在职职工"这类文本），
 * 所以不能拿它去 join 字典表翻码 —— 这一条与项目里其他"码值列"的处理方式相反，改动时留意。
 */
@Data
public class StatInsuranceDistRowVO implements Serializable {

    /**
     * 险种名称（未登记时为「未登记」）
     */
    private String insuranceType;

    /**
     * 该险种结算单笔数
     */
    private Long cnt;

    /**
     * 该险种费用总额
     */
    private BigDecimal amount;
}