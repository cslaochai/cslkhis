package com.his.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * 门诊号源表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_clinic_source")
public class BizClinicSource extends BaseEntity {
    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 排班id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long staffScheduleId;

    /**
     * 排班日期
     */
    private LocalDate scheduleDate;

    /**
     * 星期（1-周一 2-周二 3-周三 4-周四 5-周五 6-周六 7-周日）
     */
    private Integer weekDay;

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
     * 诊室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomId;

    /**
     * 诊室名称
     */
    private String roomName;

    /**
     * 排班人员ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 排班人员姓名（同 {@link #doctorId} 的泛化说明）
     */
    private String doctorName;

    /**
     * 岗位类别（1医生 2护理 3医技 4药学 5收费 6行政其他
     */
    private Integer staffType;

    /**
     * 开始时间（如08:00）
     */
    private String startTime;

    /**
     * 结束时间（如12:00）
     */
    private String endTime;

    /**
     * 班次ID（班次字典的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 总号源数
     */
    private Integer totalSource;

    /**
     * 已挂号数
     */
    private Integer usedSource;

    /**
     * 剩余号源数
     */
    private Integer availableSource;

    /**
     * 预约池已用号源（预约渠道扣减，退号按渠道还池）
     */
    private Integer usedAppointmentSource;

    /**
     * 挂号费。
     *
     * <p><b>已废止</b>：湘医保发〔2025〕58号（长沙市医保局同文，2026-01-01 执行）废止挂号费等 71 个
     * 价格项目，合并为门诊诊查费。本字段仅保留作历史数据兼容，整改后恒为 0，计价与出账都不读它。
     */
    private BigDecimal registFee;

    /**
     * 门诊诊查费（医保编码 011102020010000）。
     *
     * <p>湘医保发〔2025〕58号：支付分类=丙类，医保不报、100% 自付，不进职工门诊统筹。
     *
     * <p>🔑 这是<b>号源价格快照</b>，含职级加收：普通 9 / 副主任 22 / 主任 31 / 知名专家 70。
     * 医生事后升职称<b>不会</b>回溯改已排出的号源价格——改了等于让已挂号的患者改约时被加价。
     */
    private BigDecimal diagnosisFee;

    /**
     * 门诊诊查费职级加收档位（1普通 2副主任医师 3主任医师 4知名专家）。
     *
     * <p>对应 {@link com.his.common.enums.ClinicDiagnosisTierEnum}。
     * 政策把加收拆成四个国家编码（011102020010000~3），本列记录本号源适用哪一档，
     * 用于出账留痕与医保对账；<b>计价仍读 {@link #diagnosisFee}</b>（金额快照）。
     */
    private Integer diagnosisTier;

    /**
     * 是否专家号（0-否 1-是）
     *
     * @deprecated 定价已由 {@link #diagnosisTier} + {@link #diagnosisFee} 承担，本列仅留作
     * 「专家号」展示标记，不再参与计价。定价比人高的号一律以 diagnosisTier=4（知名专家）为准。
     */
    @Deprecated
    private Integer isExpert;

    /**
     * 专家号费用
     *
     * @deprecated 58号文后已废止独立专家费，加收并入门诊诊查费。本列恒为 0，不参与计价。
     */
    @Deprecated
    private BigDecimal expertFee;

    /**
     * 是否可预约（0-否 1-是）
     */
    private Integer isAppointment;

    /**
     * 预约号源数
     */
    private Integer appointmentSource;

    /**
     * 累计加号数（加号同时加 total/available，本列留痕审计）
     */
    private Integer addedSource;

    /**
     * 状态（0-停诊 1-正常 2-已满 3-已过期）—— 号源可用性，与发布态是两回事
     */
    private Integer status;

    /**
     * 发布态（0-草稿 1-待审核 2-已发布 3-调班中 4-已变更）；仅已发布/已变更 才出号
     */
    private Integer publishStatus;

    /**
     * 就诊状态（0-待开始 1-接诊中 2-暂停）
     */
    private Integer consultStatus;
}
