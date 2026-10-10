package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 膳食方案登记/修改（营养师手工登记，或修正医嘱派生方案的饮食类型）。
 */
@Data
public class DietPlanUpsertDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 入院ID
     */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /**
     * 饮食类型码
     */
    @NotNull(message = "饮食类型不能为空")
    private String dietCode;

    /**
     * 饮食名称（可覆盖目录默认名，如"个体化糖尿病饮食"）
     */
    private String dietName;

    /**
     * 管饲/输注方式说明（route=2/3 时应写清途径与泵速）
     */
    private String feedWay;

    /**
     * 每日热量目标 kcal
     */
    private Integer calorieTarget;

    /**
     * 每日蛋白目标 g
     */
    private Integer proteinTarget;

    /**
     * 每日液体量 ml
     */
    private Integer fluidTarget;

    /**
     * 供应餐次（his_meal_type 值逗号分隔，如 "1,2,3,4"）；为空取目录默认
     */
    private String mealTypes;

    /**
     * 开始时间，为空取当前时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 备注
     */
    private String remark;
}
