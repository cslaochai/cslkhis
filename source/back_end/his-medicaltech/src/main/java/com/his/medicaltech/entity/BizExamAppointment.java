package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 检查预约单—— 分时段占号的凭证。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_exam_appointment")
public class BizExamAppointment extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 有效标记：1-在办（占着号源），NULL-已终结；与 apply_id 组成唯一索引，
     * 由数据库保证「同一申请单只有一条有效预约」（改约=先终结旧的再占新的）
     */
    private Integer activeFlag;

    /**
     * 预约单号
     */
    private String apptNo;

    /**
     * 检查申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 申请单号
     */
    private String applyNo;

    /**
     * 预约前的申请状态：取消时按它精确回退，不靠猜
     */
    private Integer prevApplyStatus;

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
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 申请科室ID
     */
    private Long applyDeptId;

    /**
     * 申请科室
     */
    private String applyDeptName;

    /**
     * 申请医生ID
     */
    private Long doctorId;

    /**
     * 申请医生
     */
    private String doctorName;

    /**
     * 检查项目ID
     */
    private Long itemId;

    /**
     * 项目编码
     */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 本次占用时长（分钟），服务端算定后固化
     */
    private Integer examMinutes;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 检查科室ID
     */
    private Long examDeptId;

    /**
     * 检查科室名称
     */
    private String examDeptName;

    /**
     * 检查室
     */
    private String roomName;

    /**
     * 检查日期
     */
    private LocalDate examDate;

    /**
     * 开始时间（HH:mm）
     */
    private String startTime;

    /**
     * 结束 HH:mm = 开始 + examMinutes（服务端算，不接受前端传）
     */
    private String endTime;

    /**
     * 是否急诊（0-否 1-是）
     */
    private Integer isEmergency;

    /**
     * 状态（1-已预约 2-已到检 3-已完成 4-已取消 5-爽约）
     */
    private Integer status;

    /**
     * 预约操作人
     */
    private String bookBy;

    /**
     * 预约操作时间
     */
    private LocalDateTime bookTime;

    /**
     * 到检时间
     */
    private LocalDateTime arriveTime;

    /**
     * 检查完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 爽约判定时间
     */
    private LocalDateTime noshowTime;
}
