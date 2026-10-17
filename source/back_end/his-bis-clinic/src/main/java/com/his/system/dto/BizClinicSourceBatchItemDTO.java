package com.his.system.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 批量新增排班的人员明细（每人一套号源/费用/诊室，日期与班次取批量表头）
 */
@Data
public class BizClinicSourceBatchItemDTO {

    /**
     * 排班人员ID
     */
    @NotNull(message = "请选择排班人员")
    private Long doctorId;

    /**
     * 排班人员姓名
     */
    @NotBlank(message = "排班人员姓名不能为空")
    private String doctorName;

    /**
     * 诊室ID（仅医生岗有意义，是否必填由 service 按岗位+状态收口）
     */
    private Long roomId;

    /**
     * 诊室名称（仅医生岗有意义）
     */
    private String roomName;

    /**
     * 总号源数
     */
    private Integer totalSource;

    /**
     * 挂号费，单位：元
     */
    private BigDecimal registFee;

    /**
     * 诊查费，单位：元
     */
    private BigDecimal diagnosisFee;

    /**
     * 是否专家号：0-否 1-是
     */
    @AllowedValues(values = {"0", "1"}, message = "是否专家号取值不合法")
    private Integer isExpert;

    /**
     * 专家费，单位：元
     */
    private BigDecimal expertFee;

    /**
     * 是否可预约：0-否 1-是
     */
    @AllowedValues(values = {"0", "1"}, message = "是否可预约取值不合法")
    private Integer isAppointment;

    /**
     * 预约号源数
     */
    private Integer appointmentSource;
}
