package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 患者端用药说明的单药行：一名药一行，只给患者看得懂的白话字段。
 */
@Data
@NoArgsConstructor
public class MedicationGuideItemVO {

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 规格
     */
    private String specification;

    /**
     * 剂型
     */
    private String dosageForm;

    /**
     * 发药数量文本（含单位）
     */
    private String quantityText;

    /**
     * 单次服用量文本；医嘱没写时为 null，由前端显示「按医生交代服用」
     */
    private String dosageText;

    /**
     * 频次白话，例如「每天 3 次」
     */
    private String frequencyText;

    /**
     * 用药途径，例如「口服」
     */
    private String routeText;

    /**
     * 疗程，例如「连服 7 天」
     */
    private String courseText;

    /**
     * 注意事项（按药品客观属性逐条列，不合并、不推断）
     */
    private List<String> cautions;

    /**
     * 说明书常规用法（成人），仅供参考
     */
    private String specText;
}
