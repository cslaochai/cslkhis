package com.his.appoint.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 停诊影响名单项：该班次在挂的患者（停诊前必须处置）
 */
@Data
public class StopImpactItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 挂号来源（1-窗口挂号 2-自助机挂号 3-网上挂号 4-预约挂号）
     */
    private Integer registSource;

    /**
     * 挂号状态（1-已挂号 2-已签到 3-已接诊 4-已就诊 5-已退号 6-已过号 7-爽约 8-未就诊）
     */
    private Integer registStatus;

    /**
     * 就诊时段（HH:mm，可能为空）
     */
    private String slotTime;
}
