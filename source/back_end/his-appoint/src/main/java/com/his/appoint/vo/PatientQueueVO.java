package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 患者端「我的排队」出参（小程序排队页专用）。
 */
@Data
public class PatientQueueVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long queueId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    private String registNo;

    /**
     * 患者姓名
     */
    private String patientName;

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
     * 医生姓名
     */
    private String doctorName;

    private String roomName;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 就诊时段快照（HH:mm，来自挂号号源，不靠 join）
     */
    private String slotStart;

    private String slotEnd;

    /**
     * 排队状态码（口径同 QueueStatusEnum：2候诊中 3就诊中 4已就诊 5已退号 6已过号 7已失效）
     */
    private Integer queueStatus;

    private String queueStatusText;

    /**
     * 我的顺序号
     */
    private Integer sequenceNo;

    /**
     * 当前已叫到的序号（诊室大屏口径）
     */
    private Integer currentCalledNo;

    /**
     * 前方还有多少人候诊（仅候诊中有意义；就诊中/已就诊为 0）
     */
    private Integer aheadCount;

    /**
     * 是否已签到入队（false = 待缴费或未签到，页面引导去签到）
     */
    private Boolean checkedIn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arriveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime callTime;
}
