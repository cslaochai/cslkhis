package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 随访任务出参
 */
@Data
public class BizFollowupTaskVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 随访任务号
     */
    private String taskNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 联系电话（明文仅编辑回显接口返回，列表一律置空）
     */
    private String phone;

    /**
     * 脱敏联系电话（列表出参，后端统一遮，前端不许自己遮码）
     */
    private String phoneMasked;

    /**
     * 随访归属科室（sql/164 补列，数据范围与看板下钻靠它）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称（快照）
     */
    private String deptName;

    /**
     * 诊断
     */
    private String diagnosis;

    /** 随访类型（1-复诊提醒 2-慢病随访 3-用药指导 4-术后随访） */
    private Integer followupType;

    /**
     * 随访内容
     */
    private String followupContent;

    /**
     * 计划随访时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followupTime;

    /**
     * 随访状态：1-待随访 2-随访中 3-已完成 4-已取消
     */
    private Integer followupStatus;

    /**
     * 是否逾期未随访（服务端按 followup_time 与当前时间现算，不落列）：
     * 列表要标红、看板要计数，两处必须是同一个口径，所以只能后端给。
     */
    private Boolean overdue;

    /**
     * 执行人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long executorId;

    /**
     * 执行人姓名
     */
    private String executorName;

    /**
     * 执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executeTime;

    /**
     * 随访结果
     */
    private String executeResult;

    /**
     * 患者反馈内容（小程序回写）
     */
    private String patientReply;

    /**
     * 患者反馈时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime patientReplyTime;

    /**
     * 外呼通道（1-人工 2-自动）
     */
    private Integer callChannel;

    /**
     * 外呼状态（0-未外呼 1-待外呼 2-已接通 3-未接通）
     */
    private Integer callStatus;

    /**
     * 最近一次外呼登记时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime callTime;

    /**
     * 累计外呼登记次数
     */
    private Integer callAttempts;

    /**
     * 复诊引用的原病历ID（由本任务生成复诊号时写入；雪花 → 字符串避免前端精度丢失）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long revisitRecordId;

    /**
     * 由本任务生成的复诊挂号ID；为空表示还没生成过复诊号
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long revisitAppointId;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

}
