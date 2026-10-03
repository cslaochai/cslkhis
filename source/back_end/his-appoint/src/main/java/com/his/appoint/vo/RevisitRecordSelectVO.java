package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 复诊「原病历」下拉候选项
 *
 * <p>窗口挂号、医生站预约复诊、小程序自助复诊三处都要选「按哪一次就诊来复诊」，
 * 收费策略的同科室/同医生/间隔天数全部以它为基准，所以这里必须把科室与医生的
 * **ID** 一并带出（只有名字会判错：同名医生、科室改名）。
 */
@Data
public class RevisitRecordSelectVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String recordNo;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 该次就诊的类型（1-初诊 2-复诊），见 VisitTypeEnum
     */
    private Integer visitType;

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
     * 诊断名称（选病历主要靠它和日期认人认事）
     */
    private String diagnosisName;
}
