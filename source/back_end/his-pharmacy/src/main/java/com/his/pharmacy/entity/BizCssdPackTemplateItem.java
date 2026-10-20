package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

/**
 * CSSD 器械包模板组成明细实体（99 号脚本新增）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_cssd_pack_template_item")
public class BizCssdPackTemplateItem extends BaseEntity {

    /**
     * 模板ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /**
     * 器械/耗材名称
     */
    private String itemName;
    /**
     * 规格
     */
    private String spec;
    /**
     * 计量单位
     */
    private String unit;
    /**
     * 基数（数量）
     */
    private Integer quantity;
    /**
     * 排序
     */
    private Integer sortNo;
}
