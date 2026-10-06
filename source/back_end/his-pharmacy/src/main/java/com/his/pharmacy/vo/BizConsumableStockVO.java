package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 耗材库存VO（JOIN 耗材字典带出字典信息）
 */
@Data
public class BizConsumableStockVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
    /**
     * 耗材ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long consumableId;
    private String consumableCode;
    private String consumableName;
    /**
     * 类别
     */
    private Integer category;
    private String specification;
    /**
     * 单位
     */
    private String unit;
    private BigDecimal retailPrice;
    private String manufacturer;
    /**
     * 批号
     */
    private String batchNo;
    /**
     * 生产日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate productionDate;
    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    /**
     * 库存数量
     */
    private BigDecimal quantity;
    /**
     * 成本价
     */
    private BigDecimal costPrice;
    /**
     * 库存金额
     */
    private BigDecimal totalAmount;
    /**
     * 存放位置
     */
    private String location;
    /**
     * 供应商
     */
    private String supplier;
    /**
     * 库存状态（1-正常 2-预警 3-缺货 4-过期）
     */
    private Integer stockStatus;
}
