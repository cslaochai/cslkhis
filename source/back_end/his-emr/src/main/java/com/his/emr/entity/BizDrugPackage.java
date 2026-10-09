package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 药品耗材套餐
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_drug_package")
public class BizDrugPackage extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;
    /**
     * 套餐名称
     */
    private String packageName;
    /**
     * 套餐类型（1-药品套餐 2-检查套餐 3-综合套餐）
     */
    private Integer packageType;

    @TableField(exist = false)
    private List<BizDrugPackageDetail> details;
}
