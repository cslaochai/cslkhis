package com.his.appoint.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 挂号新增/修改入参
 */
@Data
public class AppointUpsertDTO {
    /**
     * 挂号记录ID，新增时为空，修改时必填
     */
    private Long id;

    /**
     * 患者主键ID
     */
    @NotNull(message = "患者主键不能为空")
    private Long patientId;

    /**
     * 排班ID
     */
    private Long scheduleId;

    /**
     * 就诊类型（号别）（1-初诊 2-复诊）
     */
    @NotNull(message = "就诊类型不能为空")
    @AllowedValues(values = {1, 2}, message = "就诊类型只能是1-初诊或2-复诊")
    private Integer visitType;

    /**
     * 结算方式（1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-商业保险）
     */
    @NotNull(message = "结算方式不能为空")
    @AllowedValues(values = {1, 2, 3, 4, 5}, message = "结算方式只能是1-自费/2-城镇职工医保/3-城乡居民医保/4-公费/5-商业保险")
    private Integer settlementType;

    /**
     * 挂号来源（1-窗口挂号 2-自助机挂号 3-网上挂号 4-预约挂号；为空默认窗口 1）
     */
    private Integer registSource;

    /**
     * 就诊时段（HH:mm，30 分钟粒度；可选，校验必须落在所选班次时段内）
     */
    private String slotTime;

    /**
     * 时间片段ID（排班时段号源的ID，可选）：传了则按段扣号源并写段快照
     */
    private Long slotId;

    /**
     * 医保类型（如：在职职工、退休职工、城乡居民等）
     */
    private String medicalInsuranceType;
    /**
     * 医保卡号
     */
    private String medicalInsuranceNo;

    /**
     * 复诊关联的原病历ID
     */
    private Long revisitRecordId;

    /**
     * 复诊来源（1-当日回诊 2-医嘱复诊预约 3-患者自助复诊 4-随访计划复诊）。
     */
    private Integer revisitSource;
}
