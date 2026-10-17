package com.his.appoint.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 急诊转住院入参：一次动作完成「入院登记（途径=急诊）+ 急诊记录终态」
 */
@Data
public class EmergencyAdmitDTO {

    /**
     * 急诊记录ID
     */
    @NotNull(message = "急诊记录不能为空")
    @JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long id;

    /**
     * 入院病区ID
     */
    @NotNull(message = "入院病区不能为空")
    @JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long wardId;

    /**
     * 入院床位ID（必须空闲）
     */
    @NotNull(message = "入院床位不能为空")
    @JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long bedId;

    /**
     * 入院医生ID（不传取急诊接诊医生，再取当前登录人）
     */
    @JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long admitDoctorId;

    /**
     * 入院诊断（不传取急诊初步诊断）
     */
    private String diagnosis;
}
