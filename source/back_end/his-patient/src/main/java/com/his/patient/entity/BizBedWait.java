package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 等床队列
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_bed_wait")
public class BizBedWait extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 等待号（DC + yyyyMMdd + 3位序号）
     */
    private String waitNo;

    /**
     * 来源住院证ID：无证手工登记（急诊/院外转入）时为 null
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionOrderId;

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
     * 性别（快照，性别字典口径）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 拟收治科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDeptId;

    /**
     * 拟收治科室名称
     */
    private String applyDeptName;

    /**
     * 期望病区ID（可空 = 服从调配）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long expectWardId;

    /**
     * 需求床型：normal / ICU / VIP
     */
    private String bedType;

    /**
     * 优先级（1-普通 2-急 3-危重）
     */
    private Integer priority;

    /**
     * 性别限制（0-不限 1-限男床 2-限女床）
     */
    private Integer genderLimit;

    /**
     * 隔离需求（0-否 1-是）
     */
    private Integer isolationFlag;

    /**
     * 预计入院日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectAdmitDate;

    /**
     * 拟诊名称
     */
    private String diagnosisName;

    /**
     * 状态（0-等待中 1-已安排床位 2-已收治 3-已取消）
     */
    private Integer waitStatus;

    /**
     * 登记排队时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTime;

    /**
     * 已安排的床位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long assignedBedId;

    /**
     * 已安排床位号
     */
    private String assignedBedNo;

    /**
     * 已安排床位所在病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long assignedWardId;

    /**
     * 已安排病区名称
     */
    private String assignedWardName;

    /**
     * 已安排床位所属科室ID（跨科调配时 ≠ applyDeptId）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long assignedDeptId;

    /**
     * 已安排床位所属科室名称
     */
    private String assignedDeptName;

    /**
     * 安排床位时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedTime;

    /**
     * 安排人（员工姓名快照）
     */
    private String assignedBy;

    /**
     * 收治后回填的入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 实际收治时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime admitTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;
}
