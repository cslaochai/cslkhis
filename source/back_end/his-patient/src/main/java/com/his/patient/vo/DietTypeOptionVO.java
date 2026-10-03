package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 饮食类型目录下拉（营养师登记方案时的选项）。
 *
 * <p>热量/蛋白目标、餐次、是否走食堂订餐都从目录带出并在前端显示，
 * 但<b>落库值由服务端按 code 重算</b> —— 下拉里的默认值是"建议"，不是"事实"。
 */
@Data
public class DietTypeOptionVO implements Serializable {

    private String code;
    /** 名称 */
    private String name;

    /** 类别 */
    private Integer category;
    private String categoryText;

    private Integer route;
    private String routeText;

    /** 该途径是否需要食堂订餐 */
    private Boolean needsMeal;

    private Integer calorieTarget;
    private Integer proteinTarget;

    /** 默认供应餐次（"1,2,3"） */
    private String mealTypes;
    private String mealTypesText;

    private String desc;
}
