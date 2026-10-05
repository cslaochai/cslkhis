package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 检查项目下拉选择出参
 */
@Data
public class SysInspectionItemSelectListVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 项目编码（唯一）
     */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 项目类型（1-放射检查 2-超声检查 3-心电图 4-内镜检查 5-其他）
     */
    private Integer itemType;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 检查价格
     */
    private BigDecimal price;

    /**
     * 检查前准备
     */
    private String preparation;
}
