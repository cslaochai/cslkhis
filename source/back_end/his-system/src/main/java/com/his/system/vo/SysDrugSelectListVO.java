package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 药品下拉选择出参
 */
@Data
public class SysDrugSelectListVO {

    /**
     * 药品ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 药品编码（唯一）
     */
    private String drugCode;

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 药品类型：1-西药 2-中成药 3-中药饮片
     */
    private Integer drugType;

    /**
     * 规格
     */
    private String specification;

    /**
     * 剂型（片剂、胶囊、注射剂等）
     */
    private String dosageForm;

    /**
     * 单位（片、粒、支等）
     */
    private String unit;

    /**
     * 零售价
     */
    private BigDecimal retailPrice;

    /**
     * 每最小库存单位含多少克（中药饮片换算率，sql/139；为空=不按克开方）。
     * 开方页要按它把「元/kg」折成「元/g」显示预估金额，缺了就得上后端才被拒。
     */
    private BigDecimal gramPerUnit;

    /**
     * 用法用量
     */
    private String usageDosage;

    /**
     * 特殊管理分类：0-普通 1-麻醉药品 2-第一类精神药品 3-第二类精神药品 4-毒性药品
     * <p>
     * 开方侧靠它给医生提示「这是麻精药品，有处方限量」——不带给前端，
     * 医生在选药那一刻根本不知道自己开的是管制药品。
     */
    private Integer specialFlag;
}
