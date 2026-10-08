package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * VTE 事件登记行
 */
@Data
public class VteEventVO {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 事件编号
     */
    private String eventNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

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
     * 患者编号
     */
    private String patientNo;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 事件类型（1-深静脉血栓DVT 2-肺栓塞PE 3-预防相关出血）
     */
    private Integer eventType;

    private String eventTypeText;

    /**
     * 发生时机（1-院内发生 2-入院时已存在）
     */
    private Integer onsetType;

    private String onsetTypeText;

    /**
     * 确诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate diagnoseDate;

    /**
     * 诊断依据（1-超声 2-CT肺动脉造影 3-静脉造影 4-临床诊断 5-其他）
     */
    private Integer diagnosisBasis;

    private String diagnosisBasisText;

    /**
     * 血栓部位
     */
    private String thrombusSite;

    /**
     * 转归（1-好转 2-未愈 3-死亡 4-未知）
     */
    private Integer outcome;

    private String outcomeText;

    /**
     * 事件发生时是否正在药物预防（0-否 1-是）
     */
    private Integer drugPreventFlag;

    /**
     * 登记人姓名
     */
    private String reporterName;

    /**
     * 登记时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTime;

    /**
     * 是否计入院内 VTE 发生率（event_type IN (1,2) 且 onset_type=1）
     */
    private Boolean counted;

    /**
     * 备注
     */
    private String remark;
}
