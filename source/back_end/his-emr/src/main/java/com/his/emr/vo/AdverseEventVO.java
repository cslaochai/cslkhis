package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 不良事件 VO（列表与详情共用）
 */
@Data
public class AdverseEventVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 事件编号 AE+yyyyMMdd+4位
     */
    private String eventNo;

    /**
     * 事件类型（字典 his_adverse_event_type）
     */
    private Integer eventType;

    /**
     * 事件等级（字典 his_adverse_event_level）
     */
    private Integer eventLevel;

    /**
     * 发生科室的ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long occurDeptId;

    /**
     * 发生科室名称（快照）
     */
    private String occurDeptName;

    /**
     * 发生病区（服务端按患者当时在住记录补写，可空：门诊/非患者事件没有病区）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long occurWardId;

    /**
     * 发生病区名称（快照）
     */
    private String occurWardName;

    /**
     * 来源（字典 his_adverse_acquired，1-院内获得 2-入院带入），护理质控压疮发生率只算 1
     */
    private Integer acquiredFlag;

    /**
     * 发生时间
     */
    private LocalDateTime occurTime;

    /**
     * 关联患者（可空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名（快照，可空）
     */
    private String patientName;

    /**
     * 关联就诊挂号单的ID（可空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long visitId;

    /**
     * 事件摘要
     */
    private String title;

    /**
     * 事件详细经过
     */
    private String description;

    /**
     * 即时处置措施
     */
    private String immediateAction;

    /**
     * 上报人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reporterId;

    /**
     * 上报人姓名（快照）
     */
    private String reporterName;

    /**
     * 上报时间
     */
    private LocalDateTime reportTime;

    /**
     * 状态（1-已上报待处理 2-处理中 3-已整改 4-已结案）
     */
    private Integer status;

    /**
     * 处理人姓名（快照）
     */
    private String handlerName;

    /**
     * 处理意见（D）
     */
    private String handleRemark;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 整改人姓名（快照）
     */
    private String rectifyByName;

    /**
     * 整改措施（C）
     */
    private String rectifyMeasures;

    /**
     * 整改时间
     */
    private LocalDateTime rectifyTime;

    /**
     * 结案人姓名（快照）
     */
    private String closeByName;

    /**
     * 验证结论（A）
     */
    private String verifyRemark;

    /**
     * 结案时间
     */
    private LocalDateTime closeTime;

    /**
     * 上报时间（创建）
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
