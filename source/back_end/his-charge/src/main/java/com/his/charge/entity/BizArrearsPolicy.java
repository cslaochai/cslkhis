package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 住院欠费管控策略实体（单行，id 固定 1）。
 */
@Data
@TableName("biz_arrears_policy")
public class BizArrearsPolicy {
    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 策略ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 预警线（元）：欠费达线发提示，不拦截
     */
    private BigDecimal warnLine;

    /**
     * 停费线（元）：欠费达线拦截择期类新开医嘱
     */
    private BigDecimal stopLine;

    /**
     * 停费管控开关（0-关 1-开）
     */
    private Integer stopEnabled;

    /**
     * 被拦截的医嘱类别（逗号分隔；药品/手术/急救类永不拦截）
     */
    private String stopClasses;

    /**
     * 备注
     */
    private String remark;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
