package com.his.supplies.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 耗材科室领用台账VO
 */
@Data
public class BizConsumableConsumeVO {
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
    /** 领用单号 */
    private String consumeNo;
    /** 耗材ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long consumableId;
    /** 耗材名称（快照） */
    private String consumableName;
    /** 规格（快照） */
    private String specification;
    /** 单位（快照） */
    private String unit;
    /** 领用数量 */
    private BigDecimal quantity;
    /** 领用科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /** 领用科室名称（快照） */
    private String deptName;
    /** 用途 */
    private String purpose;
    /** 领用时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consumeTime;
    /** 经办人 */
    private String operatorName;
    /** 领用前该耗材全部批次合计 */
    private BigDecimal stockBefore;
    /** 领用后该耗材全部批次合计 */
    private BigDecimal stockAfter;
}
