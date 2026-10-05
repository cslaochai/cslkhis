package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 不良事件实体（全院上报 + PDCA 四态留痕）
 * <p>
 * 状态机：1 已上报待处理 → 2 处理中 → 3 已整改 → 4 已结案（不可逆）。
 * 状态 ≥2 后上报内容与删除均被锁定（留痕完整性是评审底线）。
 */
@Data
@TableName("biz_adverse_event")
public class BizAdverseEvent {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 事件等级（字典 his_adverse_event_level，SAC 分级）
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
     * 发生病区ID（sql/168，服务端按患者当时在住的住院记录补写，前端不传）。
     *
     * <p>只有科室的 {@code occur_dept_id} 算不出护理质控的千床日率 —— 跌倒/压疮的发生率
     * 必须除以「该病区该月的实际占用床日」，分子分母要在同一个病区上对齐。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long occurWardId;

    /**
     * 发生病区名称（快照）
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String occurWardName;

    /**
     * 来源（字典 his_adverse_acquired，1-院内获得 2-入院带入）；压疮发生率只算 1
     */
    private Integer acquiredFlag;

    /**
     * 发生时间
     */
    private LocalDateTime occurTime;

    /**
     * 关联患者（可空：非患者事件）
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
     * 上报人员工ID（服务端取当前用户）
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
     * 状态（字典 his_adverse_event_status）
     */
    private Integer status;

    /**
     * 处理人员工ID（D）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long handlerId;

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
     * 整改人员工ID（C）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rectifyById;

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
     * 结案人员工ID（A）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long closeById;

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
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
