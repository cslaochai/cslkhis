package com.his.operation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日间手术登记单 VO。
 *
 * <p>三条派生字段一律服务端算、不落库（落库就会停在错误的值上）：
 * <ul>
 *   <li>overdue —— 术后观察超过该术式 maxStayHours 即超期；</li>
 *   <li>followDue —— 离院时间 + 24h（随访时限）；</li>
 *   <li>followOverdue —— 已过随访时限且随访次数为 0。</li>
 * </ul>
 * 按钮可用性 can* 同样服务端派生，前端不按 status 码值 switch。
 */
@Data
public class DaySurgeryApplyVO implements Serializable {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 登记单号
     */
    private String applyNo;

    /**
     * 准入术式ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /**
     * 术式编码
     */
    private String itemCode;

    /**
     * 术式名称
     */
    private String itemName;

    /**
     * 最长滞留小时数
     */
    private Integer maxStayHours;

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
     * 手术科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 手术科室名称
     */
    private String deptName;

    /**
     * 手术医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 手术医生姓名
     */
    private String doctorName;

    /**
     * 计划手术日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planSurgeryDate;

    /**
     * 状态（1-待评估 2-评估通过 3-已安排 4-术后观察 5-已出院 6-已取消 7-已转住院）
     */
    private Integer status;

    /**
     * 术前评估结论（1-通过 2-不通过）
     */
    private Integer evalResult;

    /**
     * 评估人
     */
    private String evalBy;

    /**
     * 评估时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime evalTime;

    /**
     * 评估意见/禁忌筛查结果
     */
    private String evalRemark;

    /**
     * 手术开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime surgeryTime;

    /**
     * 手术间
     */
    private String operatingRoom;

    /**
     * 台次
     */
    private Integer seqNo;

    /**
     * 实际麻醉方式
     */
    private Integer anesthesiaType;

    /**
     * 主刀医生姓名
     */
    private String surgeon;

    /**
     * 安排人
     */
    private String arrangeBy;

    /**
     * 安排时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arrangeTime;

    /**
     * 手术结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime surgeryEndTime;

    /**
     * 离院方式（1-按时离院 2-转普通住院 3-非计划再入院）
     */
    private Integer leaveType;

    /**
     * 离院时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dischargeTime;

    /**
     * 离院登记人
     */
    private String dischargeBy;

    /**
     * 出院评估结论/医嘱交代
     */
    private String dischargeRemark;

    /**
     * 转住院的住院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long transferAdmissionId;

    /**
     * 转住院原因
     */
    private String transferRemark;

    /**
     * 随访次数
     */
    private Integer followCount;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 随访台账（仅详情回填）
     */
    private List<DaySurgeryFollowVO> follows;

    /**
     * 术后滞留超期（术后观察超 maxStayHours）
     */
    private Boolean overdue;

    /**
     * 随访时限（离院 + 24h）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followDue;

    /**
     * 已过随访时限且一次都没随访
     */
    private Boolean followOverdue;

    /**
     * 仅待评估可改
     */
    private Boolean canEdit;

    /**
     * 待评估可评估
     */
    private Boolean canEvaluate;

    /**
     * 评估通过才可安排
     */
    private Boolean canArrange;

    /**
     * 已安排才可登记完成
     */
    private Boolean canFinish;

    /**
     * 术后观察才可出院
     */
    private Boolean canDischarge;

    /**
     * 术后观察才可转住院
     */
    private Boolean canTransfer;

    /**
     * 已出院/已转住院才可随访
     */
    private Boolean canFollow;

    /**
     * 非终态可取消
     */
    private Boolean canCancel;

    /**
     * 仅待评估且无随访可删
     */
    private Boolean canDelete;
}
