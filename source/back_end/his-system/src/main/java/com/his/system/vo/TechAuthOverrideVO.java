package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 技术越权登记出参（急诊/抢救越权的事后追认台账）
 */
@Data
public class TechAuthOverrideVO {

    /**
     * 主键（雪花ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 来源单据类型（1-手术申请 2-日间手术 3-住院医嘱 4-内镜记录）
     */
    private Integer sourceType;

    private String sourceTypeText;

    /**
     * 来源单据ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceId;

    /**
     * 来源单据号
     */
    private String sourceNo;

    /**
     * 越权操作者（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 越权操作者姓名
     */
    private String employeeName;

    /**
     * 涉及授权类别
     */
    private Integer authCategory;

    private String authCategoryText;

    /**
     * 该操作要求的级别
     */
    private Integer requiredLevel;

    private String requiredLevelText;

    /**
     * 越权者当时的授权级别上限（null=完全没有该类别授权）
     */
    private Integer heldLevel;

    private String heldLevelText;

    /**
     * 越权原因
     */
    private String reason;

    /**
     * 越权发生时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;

    /**
     * 状态（1-待上级确认 2-已确认）
     */
    private Integer overrideStatus;

    private String overrideStatusText;

    /**
     * 上级确认人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supervisorId;

    /**
     * 上级确认人姓名
     */
    private String supervisorName;

    /**
     * 确认时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;

    /**
     * 确认意见
     */
    private String confirmOpinion;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 可确认（仅待上级确认）
     */
    private Boolean canConfirm;
}
