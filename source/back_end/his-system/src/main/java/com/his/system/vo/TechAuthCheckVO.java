package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 技术授权准入判定结果（跨模块：开手术单/排台/内镜执行时的闸门回参）
 */
@Data
public class TechAuthCheckVO {

    /**
     * 是否有权限（本人确实有覆盖该级别的生效授权）
     */
    private boolean authorized;

    /**
     * 是否放行 = 有权限或急诊越权已登记。调用方只看这一个，两个语义分开才回答得出「有没有越权」
     */
    private boolean passed;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    private String employeeName;

    /**
     * 授权类别（1-手术 2-麻醉 3-内镜与介入）
     */
    private Integer authCategory;

    private String authCategoryText;

    /**
     * 要求的级别上限
     */
    private Integer requiredLevel;

    /**
     * 该人当时的授权级别上限（null=该类别完全没有生效授权）
     */
    private Integer heldLevel;

    /**
     * 因急诊放行时写入的越权登记ID（未放行=空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long overrideId;

    /**
     * 面向医生的说明（拒单时直接作为 400 的 message 展示）
     */
    private String message;
}
