package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 检验项目下拉选择出参
 */
@Data
public class SysLaboratoryItemSelectListVO {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 项目编码（唯一） */
    private String itemCode;

    /** 项目名称 */
    private String itemName;

    /** 项目类型（1-血液检验 2-尿液检验 3-生化检验 4-免疫检验 5-微生物检验 6-其他） */
    private Integer itemType;

    /** 标本类型（血液、尿液、粪便等） */
    private String specimenType;

    /** 检验价格 */
    private BigDecimal price;

    /** 单位 */
    private String unit;
}
