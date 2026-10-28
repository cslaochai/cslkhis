package com.his.outpatient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 急诊记录出参
 */
@Data
public class BizEmergencyVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 急诊号
     */
    private String emergencyNo;

    /**
     * 急诊号挂号单ID（登记时同步创建的 appoint_info，regist_type=急诊）。
     * 医生工作站开病历/处方按它关联就诊次；没有挂号单（老数据/旁路插入）时为 null
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 分诊队列ID（登记时同步创建的 queue，按 regist_id 关联）。
     * 病历/处方 DTO 强校验 queueId，医生站开单必须带上；老数据没有队列时为 null
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long queueId;

    /**
     * 关联队列行状态（biz_queue.queue_status 快照：2-候诊中 3-就诊中 4-已就诊 6-已过号）。
     * 急诊的临床态以 emergencyStatus 为准；queue_status 只供医生站叫号操作显按钮
     * （过号行显「回队」、候诊行显「呼叫/过号」），两套状态不能混用
     */
    private Integer queueStatus;

    /**
     * 关联队列行过号标记（biz_queue.is_overdue）：1=已过号待回队。
     * 过号只落在队列行上，不改急诊记录的临床态——候诊中过号的患者临床上仍在候诊
     */
    private Integer isOverdue;

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
     * 联系电话
     */
    private String phone;

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 分诊级别：1-一级(濒危) 2-二级(危重) 3-三级(急症) 4-四级(非急症)
     */
    private Integer triageLevel;

    /**
     * 分区：red-红区 green-绿区
     */
    private String zone;

    /**
     * 绿色通道（胸痛中心/卒中中心/创伤中心/无）
     */
    private String greenChannel;

    /**
     * 接诊科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 接诊科室名称
     */
    private String deptName;

    /**
     * 接诊医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 接诊医生姓名
     */
    private String doctorName;

    /**
     * 派单方式（0-历史未记 1-登记指定 2-系统派单 3-入池待派单）
     */
    private Integer assignType;

    /**
     * 派单方式文案
     */
    private String assignTypeText;

    /**
     * 未派单原因（assignType=3 时有值）
     */
    private String unassignedReason;

    /**
     * 应接诊时限（分钟，登记时刻快照；0=即刻）
     */
    private Integer targetSeeMinutes;

    /**
     * 已候诊分钟数（候诊中算到当下，已离开候诊状态算到开始诊治那一刻，停表）
     */
    private Long waitMinutes;

    /**
     * 超时档位：0-未超时 1-超时 2-严重超时（只对候诊中有意义）
     */
    private Integer overdueLevel;

    /**
     * 超时档位文案（未超时为空串，前端据此决定是否渲染红标）
     */
    private String overdueText;

    /**
     * 生命体征
     */
    private String vitalSigns;

    /**
     * 意识状态（1-清楚 2-对声音有反应 3-对疼痛有反应 4-无反应）
     */
    private Integer consciousness;

    /**
     * 意识状态文案（未采时明确说「未采」，不按 0 分悄悄算完）
     */
    private String consciousnessText;

    /**
     * MEWS 评分（0-14；登记/复评时快照，不是读时现算——级别定了分就定了，事后翻账要能复核）
     */
    private Integer mewsScore;

    /**
     * 最近一次分诊复评时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReevalTime;

    /**
     * 分诊复评次数
     */
    private Integer reevalCount;

    /**
     * 待复评标记（true = 候诊超时应接诊时限且又超过一个时限未复评；只对候诊中有意义）
     */
    private Boolean reevalDue;

    /**
     * 初步诊断
     */
    private String diagnosis;

    /**
     * 处理措施
     */
    private String treatment;

    /**
     * 急诊状态（1-候诊 2-诊治中 3-留观 4-转住院 5-离院 6-死亡）
     */
    private Integer emergencyStatus;

    /**
     * 留观床号
     */
    private String observationBed;

    /**
     * 留观病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long observationWardId;

    /**
     * 留观床位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long observationBedId;

    /**
     * 开始留观时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime observationStartTime;

    /**
     * 结束留观时间（转住院/离院/死亡时写入）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime observationEndTime;

    /**
     * 已留观小时数（向下取整；在观算到当下，结束留观算到结束那一刻，两者都缺则不报时长）
     */
    private Long obsHours;

    /**
     * 留观档位：0-未达预警 1-超预警时限 2-超上限时限（只有"还在观"的行才会 ≥1）
     */
    private Integer obsLevel;

    /**
     * 留观档位文案（超预警 / 超上限；不带具体小时数，阈值在系统参数里可改）
     */
    private String obsLevelText;

    /**
     * 转住院产生的入院记录ID（入院记录回指）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 入科时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime admissionTime;

    /**
     * 开始诊治时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime diagnosisTime;

    /**
     * 患者名下有挂账/欠费账单（bill_status=6）时为 true（医生站患者行欠费标；
     * 先救治后付费口径下诊治期间的待缴账单不打扰，只有挂账欠费才亮标）
     */
    private Boolean hasArrears;

    /**
     * 本次就诊已出报告的检查/检验申请单数（apply_status=5 合计；医生站患者行「有新报告」提醒）
     */
    private Integer reportReadyCount;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;
}
