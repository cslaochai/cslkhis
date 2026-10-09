package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 处方点评明细（处方快照 + 点评结论 + 公示状态）。
 */
@Data
@TableName("biz_rx_review_item")
public class BizRxReviewItem implements Serializable {
    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 批次ID
     */
    private Long batchId;

    /**
     * 批次号（快照，导出台账用）
     */
    private String batchNo;

    /**
     * 处方ID
     */
    private Long prescriptionId;

    /**
     * 处方号
     */
    private String prescriptionNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 开方科室
     */
    private String deptName;

    /**
     * 开方医生ID
     */
    private Long doctorId;

    /**
     * 开方医生姓名
     */
    private String doctorName;

    /**
     * 就诊日期（快照，点评率统计锚）
     */
    private LocalDate visitDate;

    /**
     * 诊断
     */
    private String diagnosis;

    /**
     * 药品数量
     */
    private Integer drugCount;

    /**
     * 处方金额
     */
    private BigDecimal totalAmount;

    /**
     * 处方类型（1-西药 2-中成药 3-中药饮片）
     */
    private Integer prescriptionType;

    /**
     * 处方来源（1-门诊 2-急诊 3-住院）
     */
    private Integer prescriptionSource;

    /**
     * 点评状态（0-待点评 1-已点评）
     */
    private Integer reviewStatus;

    /**
     * 点评结论（1-合理 2-不规范处方 3-用药不适宜处方 4-超常处方）
     */
    private Integer reviewResult;

    /**
     * 问题码（11-15不规范 21-27不适宜 31-34超常）
     */
    private String problemTypes;

    /**
     * 点评意见（不合理时必填）
     */
    private String reviewOpinion;

    /**
     * 点评人员工ID
     */
    private Long reviewerId;

    /**
     * 点评人姓名
     */
    private String reviewerName;

    /**
     * 点评时间
     */
    private LocalDateTime reviewTime;

    /**
     * 公示状态（0未公示 1已公示；只增不可撤）
     */
    private Integer publicityStatus;

    /**
     * 公示操作人
     */
    private String publicityBy;

    /**
     * 公示时间
     */
    private LocalDateTime publicityTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;
}
