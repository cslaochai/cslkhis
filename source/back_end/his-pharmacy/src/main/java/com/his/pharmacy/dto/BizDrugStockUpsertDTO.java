package com.his.pharmacy.dto;

import com.his.common.enums.StockRoomEnum;
import com.his.common.validation.InEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 药品库存新增/修改入参
 */
@Data
public class BizDrugStockUpsertDTO {
    /**
     * 库存ID，新增时为空，修改时必填
     */
    private Long id;
    /**
     * 药品ID
     */
    @NotNull(message = "药品ID不能为空，请从药品字典选择药品")
    private Long drugId;
    /**
     * 批号
     */
    @NotBlank(message = "批号不能为空")
    private String batchNo;
    /**
     * 生产日期
     */
    private LocalDate productionDate;
    /**
     * 有效期至
     */
    @NotNull(message = "有效期不能为空")
    private LocalDate expiryDate;
    /**
     * 库存数量，单位：最小包装单位
     */
    private BigDecimal quantity;
    /**
     * 锁定数量，单位：最小包装单位
     */
    private BigDecimal lockedQuantity;
    /**
     * 可用数量，单位：最小包装单位
     */
    private BigDecimal availableQuantity;
    /**
     * 成本价，单位：元
     */
    private BigDecimal costPrice;
    /**
     * 库存总金额，单位：元
     */
    private BigDecimal totalAmount;
    /**
     * 库位（货位）
     */
    private String location;
    /**
     * 库存地点：1-药库 2-药房（不传=药房，sql/154）
     */
    @InEnum(value = StockRoomEnum.class, message = "库存地点取值不合法（1-药库 2-药房）")
    private Integer stockRoom;
    /**
     * 供应商
     */
    private String supplier;
    /**
     * 供应商ID（供应商主档主键，挂上才能被供应商退货选中）
     */
    private Long supplierId;
    /**
     * 库存状态：1-正常 2-预警 3-缺货 4-过期
     */
    private Integer stockStatus;
}
