package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 药品库存出参
 */
@Data
public class BizDrugStockVO {
    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 创建人 */
    private String createBy;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /** 更新人 */
    private String updateBy;
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    /** 逻辑删除标记：0-未删除 1-已删除 */
    private Integer delFlag;
    /** 备注 */
    private String remark;
    /** 药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;
    /** 药品编码（药品字典联表带出） */
    private String drugCode;
    /** 药品名称（药品字典联表带出） */
    private String drugName;
    /** 规格（药品字典联表带出） */
    private String specification;
    /** 单位（药品字典联表带出） */
    private String unit;
    /** 药品类型：1-西药 2-中成药 3-中药饮片（药品字典联表带出） */
    private Integer drugType;
    /** 零售价（药品字典联表带出） */
    private BigDecimal retailPrice;
    /** 批号 */
    private String batchNo;
    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate productionDate;
    /** 有效期至 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    /** 库存数量，单位：最小包装单位 */
    private BigDecimal quantity;
    /** 锁定数量，单位：最小包装单位 */
    private BigDecimal lockedQuantity;
    /** 可用数量，单位：最小包装单位 */
    private BigDecimal availableQuantity;
    /** 成本价，单位：元 */
    private BigDecimal costPrice;
    /** 库存总金额，单位：元 */
    private BigDecimal totalAmount;
    /** 库位（货位） */
    private String location;
    /** 库存地点：1-药库 2-药房（sql/154） */
    private Integer stockRoom;
    /** 库存地点文字（服务端算，前端不再抄一份映射） */
    private String stockRoomText;
    /** 供应商ID（可空：历史批次没有结构化供应商） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
    /** 供应商 */
    private String supplier;
    /** 库存状态（1-正常 2-预警 3-缺货 4-过期） */
    private Integer stockStatus;
}
