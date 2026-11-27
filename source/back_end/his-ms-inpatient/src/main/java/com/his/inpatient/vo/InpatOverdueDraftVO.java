package com.his.inpatient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 住院文书草稿超时催办候选行（时限基线与超时时长由服务层按文书类型口径计算）。
 */
@Data
public class InpatOverdueDraftVO implements Serializable {

    /**
     * 文书ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 文书号
     */
    private String recordNo;

    /**
     * 文书类型（1-入院记录 2-首次病程 5-手术记录 7-出院记录 10-转科记录）
     */
    private Integer recordType;

    /**
     * 文书标题
     */
    private String recordTitle;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 科室ID（书写人不在临床岗位时按它找回接手医生）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 书写医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 书写医生姓名
     */
    private String doctorName;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 时限基线时刻（入院/手术/出院/转入时刻）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime baselineTime;

    /**
     * 规范时限（小时）
     */
    private Integer deadlineHours;

    /**
     * 已超时时长（小时，服务层计算）
     */
    private Long overdueHours;
}
