package com.his.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医疗废物登记实体（86 号脚本新增）。
 *
 * <p>三态：1已登记 → 2已交接 → 3已处置。已交接后禁删（交接单是对外凭证）。
 */
@Data
@TableName("biz_medical_waste")
public class BizMedicalWaste {

    /**
     * 医废登记ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 医废交接单号
     */
    private String wasteNo;

    /**
     * 医废类别（1-感染性 2-损伤性 3-病理性 4-药物性 5-化学性）
     */
    private Integer wasteType;

    /**
     * 重量（kg）
     */
    private BigDecimal weightKg;

    /**
     * 产生科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /**
     * 产生科室名称
     */
    private String deptName;
    /**
     * 收集时间
     */
    private LocalDateTime collectTime;
    /**
     * 收集人
     */
    private String collectorName;

    /**
     * 状态（1-已登记 2-已交接 3-已处置）
     */
    private Integer status;

    /**
     * 交接人
     */
    private String handoverName;
    /**
     * 交接时间
     */
    private LocalDateTime handoverTime;
    /**
     * 处置公司
     */
    private String disposalCompany;
    /**
     * 处置时间
     */
    private LocalDateTime disposalTime;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
}
