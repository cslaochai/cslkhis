package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 检验项目开单快照：申请单要把项目的编码/名称/归属科室/价格/标本类型抄成自己的列，
 * 供跨模块开单方（门诊病历、批量开单）读取，只带抄快照需要的列。
 */
@Data
public class LabItemVO implements Serializable {

    /**
     * 检验项目 ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 项目编码
     */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 归属科室 ID（申请单落到哪个检验科室）
     */
    private Long deptId;

    /**
     * 项目单价（元）
     */
    private BigDecimal price;

    /**
     * 标本类型（字典值，项目固定一种）
     */
    private String specimenType;
}
