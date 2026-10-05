package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 随访任务
 */
@Data
@TableName("biz_followup_task")
public class BizFollowupTask {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 任务编号
     */
    private String taskNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /**
     * 患者号
     */
    private String patientNo;
    /**
     * 患者姓名
     */
    private String patientName;
    /**
     * 联系电话
     */
    private String phone;

    /**
     * 随访归属科室（sql/164 补列）：数据范围收口与看板下钻都靠它，没有这列只能看全院
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
    /**
     * 随访类型（1-复诊提醒 2-慢病随访 3-用药指导 4-术后随访）
     */
    private Integer followupType;
    /**
     * 随访内容
     */
    private String followupContent;
    /**
     * 计划随访时间
     */
    private LocalDateTime followupTime;
    /**
     * 随访状态（1-待随访 2-随访中 3-已完成 4-已取消）
     */
    private Integer followupStatus;

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
    private LocalDateTime executeTime;
    /**
     * 执行结果
     */
    private String executeResult;

    /**
     * 患者反馈内容（小程序回写，与执行人的 executeResult 分列不互覆）
     */
    private String patientReply;

    /**
     * 患者反馈时间
     */
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
    private LocalDateTime callTime;

    /**
     * 累计外呼登记次数
     */
    private Integer callAttempts;

    /**
     * 复诊引用的原病历ID（由本任务生成复诊号时写入）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long revisitRecordId;

    /**
     * 由本任务生成的复诊挂号ID（幂等 + 追溯：任务上看不出「已经约过了」就会重复占号源）
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
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
