package com.his.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 科室月度成本实体（同科室同月唯一，重复录入拒绝）。
 */
@Data
@TableName("biz_dept_cost_month")
public class BizDeptCostMonth {

    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 科室名称（快照） */
    private String deptName;

    /** yyyy-MM */
    private String costMonth;

    /** 人力成本（元） */
    private BigDecimal laborCost;

    /** 药品成本（元） */
    private BigDecimal drugCost;

    /** 耗材成本（元） */
    private BigDecimal materialCost;

    /** 设备折旧（元） */
    private BigDecimal depreciation;

    /** 其他成本（元） */
    private BigDecimal otherCost;

    /** 写入时计算 */
    private BigDecimal totalCost;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 删除标志（0-正常 1-删除） */
    private Integer delFlag;

    /** 备注 */
    private String remark;
}
