package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 麻醉期间生命体征采样（麻醉期间生命体征）—— 只增不改。
 *
 * <p><b>不继承 {@code BaseEntity}</b>：这张表没有 remark/update_by/update_time 列
 * （见 sql/83），多一列 Jackson 无关但 MyBatis-Plus 全表 select 会直接 500 ——
 * 跟"实体字段与表完全对齐"那条铁律是同一件事。
 *
 * <p>业务唯一键是 {@code (record_id, sample_time)}：同一时刻两组不同的血压是<b>数据错误</b>，
 * 不是"备注里说明一下就行" —— 麻醉单上的每个时间点只能有一个真值。
 */
@Data
@TableName("biz_anesthesia_vital")
public class BizAnesthesiaVital implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 麻醉记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 采样时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sampleTime;

    /**
     * 收缩压（mmHg）
     */
    private Integer systolic;

    /**
     * 舒张压（mmHg）
     */
    private Integer diastolic;

    /**
     * 心率（次/分）
     */
    private Integer heartRate;

    /**
     * 呼吸频率（次/分）
     */
    private Integer respiration;

    /**
     * 体温（℃）
     */
    private BigDecimal temperature;

    /**
     * 脉搏血氧饱和度（%）
     */
    private Integer spo2;

    /**
     * 呼气末二氧化碳分压（mmHg）
     */
    private Integer etco2;

    /**
     * 备注
     */
    private String remark;

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
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
}
