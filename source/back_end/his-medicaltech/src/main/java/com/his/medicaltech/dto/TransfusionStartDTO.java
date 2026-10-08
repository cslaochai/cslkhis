package com.his.medicaltech.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 开始输注入参（含双人核对）。
 */
@Data
public class TransfusionStartDTO implements Serializable {

    /**
     * 输血申请单ID（必填）
     */
    @NotNull(message = "输血申请单ID不能为空")
    private Long applyId;

    /**
     * 输血前核对要点码（必填，逗号分隔，如 "1,2,3,4,5,6"）
     */
    private String checkItems;

    /**
     * 核对补充说明（异常项必须写在这里）
     */
    private String checkNote;

    /**
     * 核对护士1 ID（必填，员工ID）
     */
    @NotNull(message = "核对护士1 不能为空")
    private Long checkNurseId;

    /**
     * 核对护士2 ID（必填，员工ID，不能与核对护士1 相同）
     */
    @NotNull(message = "核对护士2 不能为空（输血必须双人核对）")
    private Long checkNurse2Id;

    /**
     * 输注执行护士ID（员工ID；为空 = 由核对护士1 执行）
     */
    private Long infusionNurseId;

    /**
     * 输注开始时间（必填）。
     *
     * <p><b>{@code @JsonFormat} 不能省</b>：Jackson 对 {@code LocalDateTime} 的默认反序列化
     * 只认 ISO-8601（{@code 2026-09-21T10:00:00}），而前端日期选择器给的是
     * {@code 2026-09-21 10:00:00}（空格分隔）—— 少了这个注解，请求体直接反序列化失败，
     * 表现为"请求体格式不正确"，而<b>不是</b>任何一条业务校验失败（P4.3 首跑 52/68 FAIL 就是这个）。
     */
    @NotNull(message = "输注开始时间不能为空（没有开始时间算不出输注时长）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime infusionStartTime;

    /**
     * 滴速（如 60滴/分；输注前 15 分钟须慢速）
     */
    private String infusionSpeed;

    /**
     * 输注过程观察（生命体征与不良反应）
     */
    private String observation;

    /**
     * 备注
     */
    private String remark;
}
