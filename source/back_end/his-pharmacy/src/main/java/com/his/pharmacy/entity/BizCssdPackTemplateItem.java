package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * CSSD 器械包模板组成明细实体（99 号脚本新增）。
 */
@Data
@TableName("biz_cssd_pack_template_item")
public class BizCssdPackTemplateItem {

    /**
     * 明细ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
