package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 排班模板实体（周模板：按星期几配置医生的固定班次，是长期资产；排班是按周生成的产物）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_clinic_source_template")
public class BizClinicSourceTemplate extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



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
     * 排班人员ID（语义泛化，见 {@code BizClinicSource#doctorId}；模板生成排班时原样带给排班信息）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 排班人员姓名（同上）
     */
    private String doctorName;

    /**
     * 岗位类别（1医生 2护理 3医技 4药学 5收费 6行政其他，见 {@code StaffTypeEnum}）：
     * 只有 1-医生的模板才配号源/诊室/挂号费，其余岗位是纯出勤模板。
     */
    private Integer staffType;

    /**
     * 星期几（1-周一 ... 7-周日，与 java DayOfWeek.getValue 一致）
     */
    private Integer weekDay;

    /**
     * 单双周（0-每周 1-单周 2-双周，按 ISO 周号奇偶）
     */
    private Integer weekParity;

    /**
     * 生效起始日期（空=不限）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validFrom;

    /**
     * 生效截止日期（空=不限）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    /**
     * 开始时间（如08:00）
     */
    private String startTime;

    /**
     * 结束时间（如12:00）
     */
    private String endTime;

    /**
     * 班次ID（班次字典的ID）——模板归属哪个班次的唯一事实，时间段与班别都从它带出。
     * 生成排班时原样带给排班信息。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 号源总数
     */
    private Integer totalSource;

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
     * 挂号费。
     *
     * <p><b>已废止</b>：湘医保发〔2025〕58号（长沙市医保局同文，2026-01-01 执行）废止挂号费，
     * 合并为门诊诊查费。本字段仅保留作历史兼容，整改后恒为 0，计价与出账都不读它。
     */
    private BigDecimal registFee;

    /**
     * 门诊诊查费（医保编码 011102020010000）。
     *
     * <p>湘医保发〔2025〕58号：支付分类=丙类，医保不报、100% 自付，不进职工门诊统筹。
     *
     * <p>🔑 价格快照含职级加收：普通 9 / 副主任 22 / 主任 31 / 知名专家 70。
     */
    private BigDecimal diagnosisFee;

    /**
     * 门诊诊查费职级加收档位（1普通 2副主任医师 3主任医师 4知名专家）。
     *
     * <p>对应 {@link com.his.common.enums.ClinicDiagnosisTierEnum}。模板按档位带出价格默认值，
     * 生成实际号源时一并落库（价格以生成当时为准，模板后续改档不影响已生成的号源）。
     */
    private Integer diagnosisTier;

    /**
     * 是否专家号（0-否 1-是）
     *
     * @deprecated 定价已由 {@link #diagnosisTier} + {@link #diagnosisFee} 承担，本列仅留作展示标记。
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
     * 是否开放预约（0-否 1-是）
     */
    private Integer isAppointment;

    /**
     * 预约号源数（0-未划池，全部现场可挂）
     */
    private Integer appointmentSource;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
