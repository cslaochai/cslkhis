package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 门诊治疗按次执行流水（治疗执行记录）—— 一次治疗一行。
 *
 * <p>sql/11 建这张表时它是"执行记录"，但既没有"第几次"也没有"计划哪天做"，
 * 也没有任何计费痕迹列，所以只能事后记一笔流水，撑不起疗程。G19 把它升级为按次流水：
 * 排期与打卡共用同一行，{@code (applyId, execSeq)} 唯一键由数据库兜住重复打卡。
 *
 * <p>两个状态列各管各的，不要混：
 * <ul>
 *   <li>{@code execStatus}：这一针/这一次做没做（0-待执行 1-已执行 2-已取消）。</li>
 *   <li>{@code recordStatus}：做完之后患者反应如何（0-异常 1-正常），打卡时一并录。</li>
 *   <li>{@code chargeStatus}：钱记上没有（0-未计费 1-已计费 2-计费失败 3-无需计费）。
 *       打卡成功但计费失败时行仍在，状态是 1 + 2 —— 业务事实与账分开，谁也不能把谁吞掉。</li>
 * </ul>
 *
 * <p>本表同样**没有 del_flag**（老表，且不继承 BaseEntity）：未执行的流水在删除申请时物理删除，
 * 已执行的流水是收费凭据的来源，只能取消不能删。
 *
 * <p>⚠ 老库把 {@code execute_time} / 记录状态建成了 NOT NULL 带默认值：
 * 待执行的排期行不写这两列，MySQL 会填上"当前时间 + 1-正常"。因此这两个值**只在
 * {@code execStatus=1} 时才有意义**，VO 与页面都必须按状态取用，不能拿默认值当事实展示。
 */
@Data
@TableName("biz_treatment_record")
public class BizTreatmentRecord implements Serializable {

    /**
     * charge_fail_reason 列宽：写库前必须截断，超长会让整条 update 失败（"记账失败"升级成 500）
     */
    public static final int REASON_MAX = 500;

    /**
     * 治疗记录ID
     */
    @TableId(value = "record_id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 治疗记录编号
     */
    private String recordNo;

    /**
     * 治疗申请ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 治疗项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long treatmentItemId;

    /**
     * 第几次执行（1 起）
     */
    private Integer execSeq;

    /**
     * 计划执行日期（排期）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;

    /**
     * 执行状态（0-待执行 1-已执行 2-已取消）
     */
    private Integer execStatus;

    /**
     * 执行医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long executeDoctorId;

    /**
     * 执行护士ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nurseId;

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
     * 记录状态（0-异常 1-正常）
     */
    private Integer recordStatus;

    /**
     * 治疗结果描述
     */
    private String result;

    /**
     * 计费状态（0-未计费 1-已计费 2-计费失败 3-无需计费）
     */
    private Integer chargeStatus;

    /**
     * 计费时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime chargeTime;

    /**
     * 记账单号
     */
    private String feeNo;

    /**
     * 记账行ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeRecordId;

    /**
     * 本次计费金额
     */
    private BigDecimal chargeAmount;

    /**
     * 未计费/失败原因
     */
    private String chargeFailReason;

    /**
     * 备注
     */
    private String remark;
}
