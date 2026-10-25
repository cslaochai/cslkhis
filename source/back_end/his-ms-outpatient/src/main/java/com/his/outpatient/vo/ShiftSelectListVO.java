package com.his.outpatient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 班次下拉出参：选班次时要认名字，排班表单还要用它带出上下班时间。
 */
@Data
@Schema(name = "ShiftSelectListVO", description = "班次下拉出参")
public class ShiftSelectListVO implements Serializable {

    /** 班次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 班次名称
     */
    private String shiftName;

    /**
     * 开始时间（HH:mm）
     */
    private String startTime;

    /**
     * 结束时间（HH:mm）
     */
    private String endTime;

    /**
     * 适用域（ShiftUseScopeEnum）：1-门诊/急诊排班 2-病区护理排班 3-全院值守 4-全院通用
     * 下拉选项按此拼「班次名称-适用域」，同名班次在不同册并存时避免选错册
     */
    private Integer useScope;

    /** 班次缩写（排班格子小标签显示） */
    private String shortName;

    /** 排班图背景色（HEX，如 #67C23A；空=前端按班别兜底） */
    private String color;
}
