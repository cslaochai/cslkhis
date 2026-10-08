package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 处方中的一个管制品种（麻精毒）—— 给发药窗口看"这张处方里哪些药要特殊处理"。
 */
@Data
public class ControlledDrugVO {

    /**
     * 处方明细ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionDetailId;

    /**
     * 药品ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 管制分类（1-麻醉 2-第一类精神 3-第二类精神 4-毒性）
     */
    private Integer specialFlag;

    /**
     * 剂型（限量档位依据）
     */
    private String dosageForm;

    /**
     * 该明细每张处方允许的最大天数
     */
    private Integer limitDays;

    /**
     * 本处方实际天数（无法核定时为 null = 发药侧会拦）
     */
    private Integer actualDays;

    /**
     * 是否必须双人复核（麻醉药品 / 第一类精神药品）
     */
    private Boolean requiresDualCheck;

    /**
     * 是否须登记空安瓿回收（麻醉/一类精神的注射剂）
     */
    private Boolean requiresAmpouleTracking;
}
