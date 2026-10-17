package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 住院欠费管控策略实体（单行，id 固定 1）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_arrears_policy")
public class BizArrearsPolicy extends BaseEntity {

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
}
