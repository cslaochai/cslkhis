package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 门诊取药队列（患者缴费后到药房签到才产生）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_dispense_queue")
public class BizDispenseQueue extends BaseEntity {

    /**
     * 取药日期
     */
    private LocalDate queueDate;

    /**
     * 取药号（当日全药房流水）
     */
    private String queueNo;

    /**
     * 处方ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /**
     * 处方号（快照）
     */
    private String prescriptionNo;

    /**
     * 处方明细条数
     */
    private Integer prescriptionItemCount;

    /**
     * 已发药明细条数
     */
    private Integer dispensedItemCount;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号（快照）
     */
    private String patientNo;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 发药窗口ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long windowId;

    /**
     * 发药窗口名称（快照）
     */
    private String windowName;

    /**
     * 队列状态（1-待取药 2-已发药 3-已过号 4-已取消）
     */
    private Integer queueStatus;

    /**
     * 到面签到时间
     */
    private LocalDateTime signInTime;

    /**
     * 签到方式（1-身份证 2-就诊卡 3-扫码 4-手工）
     */
    private Integer signInType;

    /**
     * 叫号次数
     */
    private Integer callCount;

    /**
     * 最近叫号时间
     */
    private LocalDateTime callTime;

    /**
     * 整单发药完成时间
     */
    private LocalDateTime dispenseTime;

    /**
     * 过号或取消原因
     */
    private String cancelReason;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
