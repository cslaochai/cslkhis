package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 检验项目字典
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_laboratory_item")
public class SysLaboratoryItem extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;


    /**
     * 项目编码（唯一）
     */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 项目类型（1-血液检验 2-尿液检验 3-生化检验 4-免疫检验 5-微生物检验 6-其他）
     */
    private Integer itemType;

    /**
     * 检验科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 标本类型（血液、尿液、粪便等）
     */
    private String specimenType;

    /**
     * 检验价格
     */
    private BigDecimal price;

    /**
     * 出报告时间（小时）
     */
    private Integer duration;

    /**
     * 参考值范围
     */
    private String referenceValue;

    /**
     * 单位
     */
    private String unit;

    /**
     * 是否需要空腹（0-否 1-是）
     */
    private Integer isFasting;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
