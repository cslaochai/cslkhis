package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 处方点评批次 VO
 */
@Data
public class RxReviewBatchVO {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 批次号
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
     * 专项主题
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
     * 已点评数
     */
    private Integer reviewedCount;

    /**
     * 批次状态（1-进行中 2-已完成）
     */
    private Integer status;

    /**
     * 点评人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reviewerId;

    /**
     * 点评人姓名
     */
    private String reviewerName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String remark;
}
