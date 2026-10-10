package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 处方信息出参
 */
@Data
public class BizPrescriptionVO {
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
     * 处方号
     */
    private String prescriptionNo;

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
     * 性别
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 挂号ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 病历号
     */
    private String recordNo;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 处方类型（1-西药处方 2-中成药处方 3-中药饮片处方）
     */
    private Integer prescriptionType;

    /**
     * 处方来源（1-门诊处方 2-急诊处方 3-住院处方）
     */
    private Integer prescriptionSource;

    /**
     * 总金额，单位：元
     */
    private BigDecimal totalAmount;

    /**
     * 药品数量
     */
    private Integer drugCount;

    /**
     * 用法说明
     */
    private String usageInstruction;

    /**
     * 中药饮片剂数（1~30，仅处方类型=3 有值）
     */
    private Integer doseCount;

    /**
     * 中药煎服方式（1-代煎 2-自煎，字典 his_tcm_decoct_flag；仅处方类型=3 有值）
     */
    private Integer decoctFlag;

    /**
     * 诊断
     */
    private String diagnosis;

    /**
     * 处方状态（1-草稿 2-已提交 3-已审核 4-已发药 5-已取消 6-已退药）
     */
    private Integer prescriptionStatus;

    /**
     * 缴费状态（0-未缴费 1-已缴费 2-已退费）
     */
    private Integer paymentStatus;

    /**
     * 缴费时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    /**
     * 实付金额，单位：元
     */
    private BigDecimal payAmount;

    /**
     * 支付方式（1-现金 2-微信 3-支付宝 4-医保卡 5-余额）
     */
    private Integer payMethod;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /**
     * 是否加急（0-否 1-是）
     */
    private Integer isUrgent;

    /**
     * 开方医师签名ID（为空 = 未签开方名）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorSignId;

    /**
     * 开方签名时刻
     */
    private LocalDateTime doctorSignedTime;

    /**
     * 审方药师签名ID（为空 = 未审方）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditSignId;

    /**
     * 审方签名时刻
     */
    private LocalDateTime auditSignedTime;

    /**
     * 审核人（审方药师姓名）
     */
    private String auditBy;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核结果（1通过/2退回；未审为 NULL）
     */
    private Integer auditResult;

    /**
     * 最近一次审方退回原因
     */
    private String returnReason;

    /**
     * 最近一次退回时间
     */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnTime;

    /**
     * 累计被退回次数
     */
    private Integer returnCount;

    /**
     * 处方明细列表
     */
    private List<BizPrescriptionDetailVO> details;
}
