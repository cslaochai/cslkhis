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
 * 门诊治疗申请（治疗申请单）—— 一次「疗程」，可含多次执行。
 *
 * <p>本表主键列名是申请ID 且不叫 id，因此**不继承 BaseEntity**；
 * 同时它**没有 del_flag / create_by**（sql/11 建表就没有，his-report 的 CDR 裸 SQL 与
 * his-patient 的患者索引都按"这表没有 del_flag"写的）—— 取消用 {@code apply_status=2} 表达。
 *
 * <p>三处口径：
 * <ol>
 *   <li>{@code applyStatus} 只有 0-待执行 / 1-已执行 / 2-已取消三档（字典没变），
 *       疗程做到第几次看 {@code doneTimes}/{@code totalTimes}，**不新增"执行中"中间态**。
 *       规则：doneTimes=0 → 0；doneTimes&gt;0 且未取消 → 1。</li>
 *   <li>单价与项目/患者/科室同为**开单时快照**：疗程中途价表调价不能让已打的卡跟着变。</li>
 *   <li>{@code executeTime} 沿用老列语义，写"最近一次执行时间"（不是首次），与老铺底数据一致。</li>
 * </ol>
 */
@Data
@TableName("biz_treatment_apply")
public class BizTreatmentApply implements Serializable {

    /**
     * 治疗申请ID
     */
    @TableId(value = "apply_id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 治疗申请单号
     */
    private String applyNo;

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
     * 就诊次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long visitId;

    /**
     * 挂号ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 挂号单号
     */
    private String registNo;

    /**
     * 开单医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 开单医生姓名
     */
    private String doctorName;

    /**
     * 开单科室ID（收入归科依据，取自挂号单）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 开单科室名称
     */
    private String deptName;

    /**
     * 建议执行科室ID（治疗项目字典上的科室，老字典里是孤儿引用时为空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long execDeptId;

    /**
     * 建议执行科室名称
     */
    private String execDeptName;

    /**
     * 治疗项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long treatmentItemId;

    /**
     * 治疗项目编码
     */
    private String itemCode;

    /**
     * 治疗项目名称
     */
    private String itemName;

    /**
     * 治疗项目类别（1-注射 2-输液 3-换药 4-拆线 5-其他）
     */
    private Integer itemType;

    /**
     * 单价快照（元/次），NULL = 开单时没取到价，计费按「未计费」留痕而不是猜价
     */
    private BigDecimal price;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /**
     * 最近一次执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executeTime;

    /**
     * 申请状态（0-待执行 1-已执行 2-已取消）
     */
    private Integer applyStatus;

    /**
     * 疗程总次数
     */
    private Integer totalTimes;

    /**
     * 已完成次数
     */
    private Integer doneTimes;

    /**
     * 疗程计划开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 相邻两次执行的间隔天数
     */
    private Integer intervalDays;

    /**
     * 备注
     */
    private String remark;
}
