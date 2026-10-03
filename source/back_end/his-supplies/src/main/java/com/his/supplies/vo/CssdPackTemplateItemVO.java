package com.his.supplies.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * CSSD 器械包模板组成明细 VO。
 */
@Data
public class CssdPackTemplateItemVO {

    /** 明细ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 模板ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /** 器械/耗材名称 */
    private String itemName;
    /** 规格 */
    private String spec;
    /** 计量单位 */
    private String unit;
    /** 基数（数量） */
    private Integer quantity;
    /** 排序 */
    private Integer sortNo;
}
