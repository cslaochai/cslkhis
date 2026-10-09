package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.FieldFill;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 药品字典的只读视图（his-ai 侧）。
 */
@Data
@TableName("sys_drug")
public class SysDrugGuide {
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
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String drugCode;

    private String drugName;

    private String specification;

    private String dosageForm;

    private String unit;

    /**
     * 储存条件
     */
    private String storageCondition;

    /**
     * 是否冷链药品（0-否 1-是）
     */
    private Integer isColdChain;

    /**
     * 特殊管理分类（0-普通 1-麻醉 2-一类精神 3-二类精神 4-毒性）
     */
    private Integer specialFlag;

    /**
     * 抗菌药物分级（0-非抗菌 1-非限制 2-限制 3-特殊）
     */
    private Integer antibioticLevel;

    /**
     * 说明书用法用量（成人常规，仅供参考）
     */
    private String usageDosage;
}
