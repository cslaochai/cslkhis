package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 挂号详情VO
 */
@Data
public class BizAppointInfoListVO {

    /**
     * 挂号记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 挂号单号（唯一）
     */
    private String registNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（0-未知 1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 排班ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /**
     * 时间片段ID（排班时段号源的ID；历史挂号无段为 NULL）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long slotId;

    /**
     * 就诊时段开始快照 HH:mm
     */
    private String slotStart;

    /**
     * 就诊时段结束快照 HH:mm
     */
    private String slotEnd;

    /**
     * 挂号类型（1-普通号 2-专家号 3-急诊号 4-免费号）
     */
    private Integer registType;

    /**
     * 挂号来源（1-窗口挂号 2-自助机挂号 3-网上挂号 4-预约挂号）
     */
    private Integer registSource;

    /**
     * 挂号时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registTime;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 到达时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arriveTime;

    /**
     * 时间段（1-上午 2-下午 3-全天 4-凌晨）
     */
    private Integer scheduleType;

    /**
     * 结算方式（1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-商业保险）
     */
    private Integer settlementType;

    /**
     * 医保类型（如：在职职工、退休职工、城乡居民等）
     */
    private String medicalInsuranceType;

    /**
     * 医保卡号
     */
    private String medicalInsuranceNo;

    /**
     * 挂号状态（1-已挂号 2-已签到 3-已接诊 4-已就诊 5-已退号 6-已过号）
     */
    private Integer registStatus;

    /**
     * 就诊类型（号别）（1-初诊 2-复诊）
     */
    private Integer visitType;

    /**
     * 复诊来源（1-当日回诊 2-医嘱复诊预约 3-患者自助复诊 4-随访计划复诊），仅复诊有值
     */
    private Integer revisitSource;

    /**
     * 复诊关联的原病历ID（批次E/E6；仅 visitType=2 有值）。
     * <p>只表示引用关系，原病历本身不改。雪花ID → 字符串，避免前端精度丢失。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long revisitRecordId;

    /**
     * 账单状态（字典 {@code his_bill_status}：1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费）。
     * 空 = 这张号没有挂号账单（免收，或建单时收费模块缺席）。
     */
    private Integer billStatus;

    /**
     * 应缴合计 —— 来自账单 payable_amount（医保统筹/个账已扣掉），不是收费表的实收
     */
    private BigDecimal totalFee;

    /**
     * 退号时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;

    /**
     * 退号原因
     */
    private String refundReason;

    /**
     * 挂号费结算账单ID（免收时为 NULL）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    /**
     * 账单号（快照）
     */
    private String billNo;

    /**
     * 队列记录ID（未签到时为空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long queueId;

    /**
     * 排队号（未签到时为空）
     */
    private String queueNo;

    /**
     * 队列状态（2-候诊中 3-就诊中 4-已就诊 5-已退号 6-已过号）
     * <p>未签到时为空 —— 前端据此区分「待签到」。注意它与 registStatus（挂号状态，1~6 另一套码值）
     * 不是一回事，历史上前端把 registStatus 当 queueStatus 渲染，导致状态列整体串位。
     */
    private Integer queueStatus;
}
