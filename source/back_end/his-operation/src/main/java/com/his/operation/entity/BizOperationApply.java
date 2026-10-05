package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 住院手术申请单—— 手术闭环的主单。
 *
 * <p>字段与表 <b>一一对应</b>（多一个库里没有的列 → 全表 select 直接 500）。
 *
 * <p>这条链要回答的问题，按发生顺序：
 * <ol>
 *   <li>谁、什么时候、为什么决定开这一刀 → {@code applyDoctorId / applyTime / operationReason}；</li>
 *   <li>手术室把它排在哪个房间、哪个时段、谁主刀 → {@code operationRoom / plannedStartTime / surgeonId}；</li>
 *   <li>术前谁核对了哪几项 → {@code preopCheckItems / preopCheckDoctorId / preopCheckTime}；</li>
 *   <li>实际做了什么（与拟施可能不同）、什么时候开什么时候关 → {@code actualOperationName / operationStartTime / operationEndTime}；</li>
 *   <li>结果落到哪两份正式文书上 → {@code operationId}（病案首页手术明细）、{@code recordId}（record_type=5 手术记录）。</li>
 * </ol>
 *
 * <p><b>{@code isMain} 是"主要手术"标记，同一次住院只允许一条</b>：
 * 首页主要手术只能有 1 条，这与"主要诊断必须且只能 1 条"是同一条口径。
 *
 * <p>{@code operationId} / {@code recordId} 是**链是否断了的唯一证据**：
 * 状态是"已完成"但这两个是空的，就是"说做了、却没有下文"，属于必须拦住的假数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_operation_apply")
public class BizOperationApply extends BaseEntity {

    /**
     * 手术申请单号（SS + yyyyMMdd + 4位序号）
     */
    private String applyNo;

    /**
     * 入院ID（入院记录的入院ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 入院号（快照）
     */
    private String admissionNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 性别（快照）（1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄（快照）
     */
    private Integer age;

    // 申请方

    /**
     * 申请科室ID（= 患者当前科室，服务端推导）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDeptId;

    /**
     * 申请科室名称（快照）
     */
    private String applyDeptName;

    /**
     * 申请时所在病区名称（快照）
     */
    private String applyWardName;

    /**
     * 申请时床号（快照）
     */
    private String applyBedNo;

    /**
     * 申请医生ID（员工ID，不是用户的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDoctorId;

    /**
     * 申请医生姓名（快照）
     */
    private String applyDoctorName;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    // 拟施手术

    /**
     * 拟施手术编码（ICD-9-CM-3）
     */
    private String plannedOperationCode;

    /**
     * 拟施手术名称
     */
    private String plannedOperationName;

    /**
     * 手术级别（1-一级 2-二级 3-三级 4-四级）
     */
    private Integer operationLevel;

    /**
     * 切口等级（0-0类 1-Ⅰ类 2-Ⅱ类 3-Ⅲ类）
     */
    private Integer incisionLevel;

    /**
     * 麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）
     */
    private Integer anesthesiaType;

    /**
     * 术前诊断（回写病历的"术前诊断"要素）
     */
    private String preopDiagnosis;

    /**
     * 手术指征/理由（为什么要开这一刀）
     */
    private String operationReason;

    /**
     * 是否急诊手术（0-否 1-是）
     */
    private Integer isEmergency;

    /**
     * 是否主要手术（0-否 1-是；同一次住院只允许一条主要手术申请）
     */
    private Integer isMain;

    // 排台

    /**
     * 手术间
     */
    private String operationRoom;

    /**
     * 计划开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime plannedStartTime;

    /**
     * 计划结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime plannedEndTime;

    /**
     * 主刀医师ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long surgeonId;

    /**
     * 主刀医师姓名（快照）
     */
    private String surgeonName;

    /**
     * 助手姓名（多人逗号分隔）
     */
    private String assistantName;

    /**
     * 麻醉医师ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long anesthetistId;

    /**
     * 麻醉医师姓名（快照）
     */
    private String anesthetistName;

    /**
     * 排台操作人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleDoctorId;

    /**
     * 排台操作人姓名（快照）
     */
    private String scheduleDoctorName;

    /**
     * 排台时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduleTime;

    /**
     * 排台备注
     */
    private String scheduleRemark;

    // 术前核对

    /**
     * 术前核对要点码（逗号分隔，如 1,2,3,4）
     */
    private String preopCheckItems;

    /**
     * 术前核对补充说明（异常项必须写在这里）
     */
    private String preopNote;

    /**
     * 术前核对人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long preopCheckDoctorId;

    /**
     * 术前核对人姓名（快照）
     */
    private String preopCheckDoctorName;

    /**
     * 术前核对时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime preopCheckTime;

    // 术中 / 术后

    /**
     * 实际手术编码（可能与拟施不同，首页记的是这个）
     */
    private String actualOperationCode;

    /**
     * 实际手术名称
     */
    private String actualOperationName;

    /**
     * 实际开始时间（切皮）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationStartTime;

    /**
     * 实际结束时间（关腹/关胸）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationEndTime;

    /**
     * 术中出血量（ml）
     */
    private Integer bloodLoss;

    /**
     * 术中所见
     */
    private String intraopFindings;

    /**
     * 手术经过/操作步骤
     */
    private String intraopProcedure;

    /**
     * 术后处理与注意事项
     */
    private String postopNote;

    /**
     * 标本送检（无则写"无"）
     */
    private String specimenSent;

    // 完成 / 回写锚点

    /**
     * 完成录入人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long finishDoctorId;

    /**
     * 完成录入人姓名（快照）
     */
    private String finishDoctorName;

    /**
     * 手术完成时间（= 完成录入时刻）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    /**
     * 回写病案首页手术明细ID（病案首页手术明细的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operationId;

    /**
     * 回写住院病历ID（住院病历文书的ID，record_type=5 手术记录）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 状态（0-待排期 1-已排期 2-术前核对完成 3-已完成 4-已取消）
     */
    private Integer operationStatus;

    /**
     * 取消原因（仅待排期/已排期可取消）
     */
    private String cancelReason;

    /**
     * 取消人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long cancelDoctorId;

    /**
     * 取消人姓名（快照）
     */
    private String cancelDoctorName;

    /**
     * 取消时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;
}
