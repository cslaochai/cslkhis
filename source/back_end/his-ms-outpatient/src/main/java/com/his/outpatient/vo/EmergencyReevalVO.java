package com.his.outpatient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 急诊分诊复评流水出参（一条 = 一次复评的事实）
 */
@Data
public class EmergencyReevalVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 急诊记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long emergencyId;

    /**
     * 急诊号
     */
    private String emergencyNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 复评前分诊级别
     */
    private Integer triageLevelBefore;

    /**
     * 复评前分诊级别文案
     */
    private String triageLevelBeforeText;

    /**
     * 复评后分诊级别
     */
    private Integer triageLevelAfter;

    /**
     * 复评后分诊级别文案
     */
    private String triageLevelAfterText;

    /**
     * 是否上调（后 &lt; 前；只升不降，恒为 true 或级别不变）
     */
    private Boolean upgraded;

    /**
     * 复评时 MEWS 评分（0-14）
     */
    private Integer mewsScore;

    /**
     * 复评时意识状态码值
     */
    private Integer consciousness;

    /**
     * 复评时意识状态文案
     */
    private String consciousnessText;

    /**
     * 复评时生命体征（JSON）
     */
    private String vitalSigns;

    /**
     * 复评原因
     */
    private String reason;

    /**
     * 操作人（创建人）
     */
    private String createBy;

    /**
     * 复评时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
