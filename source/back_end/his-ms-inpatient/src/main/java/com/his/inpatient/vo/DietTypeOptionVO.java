package com.his.inpatient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 饮食类型目录下拉（营养师登记方案时的选项）。
 */
@Data
public class DietTypeOptionVO implements Serializable {

    /** 膳食类型编码 */
    private String code;
    /**
     * 名称
     */
    private String name;

    /**
     * 类别
     */
    private Integer category;
    private String categoryText;

    /** 摄入途径（1-口服 2-管饲 3-静脉（肠外）） */
    private Integer route;
    /** 摄入途径文案 */
    private String routeText;

    /**
     * 该途径是否需要食堂订餐
     */
    private Boolean needsMeal;

    /** 每日热量目标 kcal */
    private Integer calorieTarget;
    /** 每日蛋白目标 g */
    private Integer proteinTarget;

    /**
     * 默认供应餐次（"1,2,3"）
     */
    private String mealTypes;
    /** 默认供应餐次文案 */
    private String mealTypesText;

    /** 描述 */
    private String desc;
}
