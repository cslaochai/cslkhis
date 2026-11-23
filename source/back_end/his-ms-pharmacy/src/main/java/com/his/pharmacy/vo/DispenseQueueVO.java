package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 门诊取药队列出参
 */
@Data
public class DispenseQueueVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 取药日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate queueDate;

    /**
     * 取药号
     */
    private String queueNo;

    /**
     * 处方ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /**
     * 处方号
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
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 发药窗口ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long windowId;

    /**
     * 发药窗口名称
     */
    private String windowName;

    /**
     * 队列状态：1-待取药 2-已发药 3-已过号 4-已取消
     */
    private Integer queueStatus;

    /**
     * 到面签到时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signInTime;

    /**
     * 签到方式：1-身份证 2-就诊卡 3-扫码 4-手工
     */
    private Integer signInType;

    /**
     * 叫号次数
     */
    private Integer callCount;

    /**
     * 最近叫号时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime callTime;

    /**
     * 整单发药完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dispenseTime;

    /**
     * 过号或取消原因
     */
    private String cancelReason;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
