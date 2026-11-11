package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 患者端-检查预约单（小程序「我的检查」列表与预约成功回执）。
 */
@Data
public class PatientExamApptVO {

    /**
     * 预约单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long apptId;

    /**
     * 预约单号
     */
    private String apptNo;

    /**
     * 检查申请单号
     */
    private String applyNo;

    /**
     * 检查项目名称
     */
    private String itemName;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 检查室
     */
    private String roomName;

    /**
     * 检查科室
     */
    private String examDeptName;

    /**
     * 检查日期（yyyy-MM-dd）
     */
    private String examDate;

    /**
     * 开始时间（HH:mm）
     */
    private String startTime;

    /**
     * 结束时间（HH:mm）
     */
    private String endTime;

    /**
     * 时段文案（HH:mm-HH:mm）
     */
    private String timeRange;

    /**
     * 预约状态码（1-已预约 2-已到检 3-已完成 4-已取消 5-爽约）
     */
    private Integer status;

    /**
     * 预约状态文案
     */
    private String statusText;

    /**
     * 在办标志（null = 已终结）
     */
    private Integer activeFlag;

    /**
     * 是否急诊（1-是 0-否）
     */
    private Integer isEmergency;

    /**
     * 预约时间（yyyy-MM-dd HH:mm:ss）
     */
    private String bookTime;

    /**
     * 预约人（患者自助预约时落就诊人姓名）
     */
    private String bookBy;
}
