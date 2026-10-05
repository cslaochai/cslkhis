package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 胶片规格下拉（sql/138）。
 *
 * <p>下拉里必须带单价：登记员选规格的时候就要知道这张片多少钱，
 * 等提交完再跳到别处看价格，等于让人先做决定再告诉他代价。
 */
@Data
public class FilmSpecSelectListVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 规格编码
     */
    private String specCode;

    /**
     * 规格名称
     */
    private String specName;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 计价单位
     */
    private String unit;
}
