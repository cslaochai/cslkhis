package com.his.supplies.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 耗材出入库流水VO（JOIN 耗材字典带出耗材名）
 */
@Data
public class BizConsumableStockLogVO {
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
    /** 删除标志（0-正常 1-删除） */
    private Integer delFlag;
    /** 备注 */
    private String remark;
    /** 库存批次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stockId;
    /** 耗材ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long consumableId;
    private String consumableName;
    /** 批号 */
    private String batchNo;
    /** 变动类型（1-入库 2-领用出库 3-退回入库 4-其他出库 5-盘盈 6-盘亏） */
    private Integer changeType;
    /** 变动数量 */
    private BigDecimal changeQuantity;
    /** 变动前批次数量 */
    private BigDecimal quantityBefore;
    /** 变动后批次数量 */
    private BigDecimal quantityAfter;
    /** 来源类型 */
    private String sourceType;
    /** 来源单据ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceId;
    /** 来源单据号 */
    private String sourceNo;
    /** 操作人 */
    private String operatorName;
}
