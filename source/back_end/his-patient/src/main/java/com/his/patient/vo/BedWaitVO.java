package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 等床队列条目（列表与详情共用）
 */
@Data
public class BedWaitVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 等待号
     */
    private String waitNo;

    /**
     * 来源住院证ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionOrderId;

    /**
     * 来源住院证号（无证登记为 null）
     */
    private String admissionOrderNo;

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
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    private String genderText;

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
     * 期望病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long expectWardId;

    /**
     * 需求床型
     */
    private String bedType;

    private String bedTypeText;

    /**
     * 优先级（1-普通 2-急 3-危重）
     */
    private Integer priority;

    private String priorityText;

    /**
     * 性别限制（0-不限 1-限男床 2-限女床）
     */
    private Integer genderLimit;

    private String genderLimitText;

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

    private String waitStatusText;

    /**
     * 排队位次（等待中队列内的顺序，1 起；非等待中为 0）
     */
    private Integer seq;

    /**
     * 登记排队时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTime;

    /**
     * 等待时长（小时，查询时算）
     */
    private Long waitHours;

    /**
     * 等待时长文案
     */
    private String waitDurationText;

    /**
     * 是否超出最长等待天数（查询时算，不改库）
     */
    private Boolean expired;

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
     * 已安排床位所属科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long assignedDeptId;

    /**
     * 已安排床位所属科室名称
     */
    private String assignedDeptName;

    /**
     * 是否跨科调配（床位所属科室 ≠ 拟收治科室）
     */
    private Boolean crossDept;

    /**
     * 安排床位时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedTime;

    /**
     * 安排人
     */
    private String assignedBy;

    /**
     * 收治后回填的入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    private String admissionNo;

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

    /**
     * 备注
     */
    private String remark;

    // 操作可用性（后端判，前端不自判）

    /**
     * 可安排/改派床位（等待中或已安排均可改派）
     */
    private Boolean canAssign;

    /**
     * 可退回队列（已安排床位时才有意义）
     */
    private Boolean canRelease;

    /**
     * 可取消排队（等待中/已安排可取消；已收治绝对不可 —— 人已经在院里了）
     */
    private Boolean canCancel;

    /**
     * 可办理入院（必须已安排床位）
     */
    private Boolean canAdmit;
}
