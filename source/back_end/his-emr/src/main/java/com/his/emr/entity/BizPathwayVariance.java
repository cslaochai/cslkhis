package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 临床路径变异登记（临床路径变异登记）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pathway_variance")
public class BizPathwayVariance extends BaseEntity implements Serializable {

    /**
     * 入径记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long enrollId;

    /**
     * 发生路径日（1..total_days）
     */
    private Integer dayNo;

    /**
     * 变异类型:1-医嘱变动 2-检查检验变动 3-手术操作变动 4-用药变动 5-出院延期 6-其他
     */
    private Integer varianceType;

    /**
     * 变异原因（必填，落库前截到 200）
     */
    private String varianceReason;

    /**
     * 处理措施
     */
    private String handling;

    /**
     * 变异发生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate occurredDate;

    /**
     * 登记人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recorderId;

    /**
     * 登记人姓名
     */
    private String recorderName;

    /**
     * 登记时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;
}
