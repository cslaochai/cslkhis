package com.his.miniapp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 智能导诊症状科室映射。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_triage_rule")
public class BizTriageRule extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 症状编码
     */
    private String symptomCode;

    /**
     * 症状名称
     */
    private String symptomName;

    /**
     * 匹配关键词（顿号分隔）
     */
    private String keywords;

    /**
     * 推荐科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 推荐科室名称
     */
    private String deptName;

    /**
     * 推荐权重（越大越靠前）
     */
    private Integer weight;

    /**
     * 急症信号（0-否 1-是）
     */
    private Integer urgentFlag;

    /**
     * 就诊提示
     */
    private String advice;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
