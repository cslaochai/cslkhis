package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 排班号源摘要：跨模块需要「这一班次还剩多少号、归哪个科室哪位医生、什么时段、收多少钱」时的出参。
 *
 * <p>只交出挂号、急诊、队列三个域真正要读的列；号源的生成、放号、停诊编排留在排班域内部。
 */
@Data
@NoArgsConstructor
public class ClinicSourceBriefVO {

    /**
     * 号源ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 来源岗位排班ID；为 null 表示这条号源不是从岗位排班生成的
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long staffScheduleId;

    /**
     * 排班日期（就诊日）
     */
    private LocalDate scheduleDate;

    /**
     * 坐诊科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 坐诊科室名称快照
     */
    private String deptName;

    /**
     * 诊室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomId;

    /**
     * 诊室名称快照
     */
    private String roomName;

    /**
     * 出诊医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 出诊医生姓名快照
     */
    private String doctorName;

    /**
     * 岗位类型，取值见 StaffTypeEnum
     */
    private Integer staffType;

    /**
     * 接诊开始时间 HH:mm
     */
    private String startTime;

    /**
     * 接诊结束时间 HH:mm
     */
    private String endTime;

    /**
     * 班次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 剩余可挂号源数
     */
    private Integer availableSource;

    /**
     * 挂号费
     */
    private BigDecimal registFee;

    /**
     * 诊查费总额（含专家加收）
     */
    private BigDecimal diagnosisFee;

    /**
     * 职级档位码（1-普通 2-副主任 3-主任 4-知名专家，见 TitleTierMapping）
     */
    private Integer diagnosisTier;

    /**
     * 是否专家号源：1-是 0-否
     */
    private Integer isExpert;
}
