package com.his.appoint.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 挂号信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_appoint_info")
public class BizAppointInfo extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;


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
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 手机号码
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
     * 排班时间片段ID（排班时段号源的ID；历史挂号无段，为 NULL）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long slotId;

    /**
     * 就诊时段开始快照 HH:mm（挂号单自证当时挂的哪段，不靠 join）
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
     * 时间段（1-上午 2-下午 3-全天 4-凌晨）
     */
    private Integer scheduleType;

    /**
     * 就诊时段（HH:mm，30 分钟粒度；轻量分时，号源仍按池扣）
     */
    private String slotTime;

    /**
     * 挂号时间
     */
    private LocalDateTime registTime;

    /**
     * 就诊日期
     */
    private LocalDate visitDate;

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
     * 挂号状态（1-待签到 2-候诊中 3-就诊中 4-已完成 5-已取消 6-已过号）
     */
    private Integer registStatus;

    /**
     * 到达时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arriveTime;

    /**
     * 就诊类型（号别）（1-初诊 2-复诊）
     */
    private Integer visitType;

    /**
     * 复诊来源（1-当日回诊 2-医嘱复诊预约 3-患者自助复诊 4-随访计划复诊），仅复诊有值。
     * <p>决定占不占号源、按哪条收费策略算钱，见 RevisitSourceEnum。
     */
    private Integer revisitSource;

    /**
     * 复诊关联的病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long revisitRecordId;

    /**
     * 退号时间
     */
    private LocalDateTime refundTime;

    /**
     * 退号原因
     */
    private String refundReason;

    /**
     * 挂号费结算账单ID（四层模型 L2，{@code bill_type=1}）。
     * 免收时恒为 NULL —— 没有应收就没有账单，签到门禁据此放行。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    /**
     * 账单号
     */
    private String billNo;
}
