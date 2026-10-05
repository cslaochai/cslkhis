package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 手术清点明细。
 *
 * <p>一行 = 一件名称，三个数量列分别对应术前 / 关体前 / 关体后。
 * 之所以把三个数字摊在同一行而不是拆成三张明细表：清点的<b>比对对象就是同一件东西</b>，
 * 拆成三张表要靠 item_name 去 JOIN —— 名字改了、字打错了，对不上的账会显示成对上了。
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
