package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 处方点评批次（卫医管发〔2010〕28号，事后专项点评；评审必查台账）。
 *
 * <p>建批抽样 → 药师逐张点评 → 完成/手动关闭归档。无 del_flag，删除走物理删
 * （uk_batch_no 不含 del_flag，软删行会占键）。
 */
@Data
@TableName("biz_rx_review_batch")
public class BizRxReviewBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 批次号（RXRB+yyyyMMdd+4位序号）
     */
    private String batchNo;

    /**
     * 批次名称
     */
    private String batchName;

    /**
     * 点评类型（1-常规点评 2-专项点评）
     */
    private Integer reviewType;

    /**
     * 专项主题（review_type=2 必填）
     */
    private String specialty;

    /**
     * 处方就诊日期起
     */
    private LocalDate dateStart;

    /**
     * 处方就诊日期止
     */
    private LocalDate dateEnd;

    /**
     * 抽样处方数
     */
    private Integer sampleCount;

    /**
     * 已点评数（冗余维护，提交点评 +1）
     */
    private Integer reviewedCount;

    /**
     * 批次状态（1-进行中 2-已完成）
     */
    private Integer status;

    /**
     * 点评人员工ID
     */
    private Long reviewerId;

    /**
     * 点评人姓名
     */
    private String reviewerName;

    /**
     * 创建人
     */
    private String createBy;

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
