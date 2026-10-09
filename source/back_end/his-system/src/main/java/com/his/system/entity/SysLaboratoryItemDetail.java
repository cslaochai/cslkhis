package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 检验项目组套明细（检验项目组套明细）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_laboratory_item_detail")
public class SysLaboratoryItemDetail extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;


    /**
     * 检验大项目ID
     */
    private Long laboratoryItemId;

    /**
     * 明细项目编码
     */
    private String itemCode;

    /**
     * 明细项目名称
     */
    private String itemName;

    /**
     * 单位
     */
    private String unit;

    /**
     * 参考范围
     */
    private String referenceRange;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
