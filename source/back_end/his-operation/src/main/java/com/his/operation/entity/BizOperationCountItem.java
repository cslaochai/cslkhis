package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 手术清点明细。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_operation_count_item")
public class BizOperationCountItem extends BaseEntity {

    /**
     * 清点单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long countId;

    /**
     * 行号
     */
    private Integer seqNo;

    /**
     * 类别（纱布/纱垫）（1-器械 2-敷料 3-缝针 4-刀片 5-其他）
     */
    private Integer itemCategory;

    /**
     * 名称（如：止血钳 / 纱布块 / 圆针）
     */
    private String itemName;

    /**
     * 规格
     */
    private String spec;

    /**
     * 术前数量
     */
    private Integer beforeQty;

    /**
     * 关体前数量
     */
    private Integer closureQty;

    /**
     * 关体后数量
     */
    private Integer finalQty;
}
