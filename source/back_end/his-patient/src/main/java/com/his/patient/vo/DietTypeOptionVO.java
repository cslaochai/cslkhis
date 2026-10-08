package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 饮食类型目录下拉（营养师登记方案时的选项）。
 */
@Data
public class DietTypeOptionVO implements Serializable {

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

    private Integer route;
    private String routeText;

    /**
     * 该途径是否需要食堂订餐
     */
    private Boolean needsMeal;

    private Integer calorieTarget;
    private Integer proteinTarget;

    /**
     * 默认供应餐次（"1,2,3"）
     */
    private String mealTypes;
    private String mealTypesText;

    private String desc;
}
