package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 治疗项目字典。
 *
 * <p>这张表**没有 unit / spec / 频次 / 剂量**列（AGENTS 里记过：裸 SQL 猜列名会运行时炸），
 * 所以治疗费只能按「次」计：数量恒为 1、单价取单价、单位写死「次」。
 * {@code deptId} 老库里只有 101/105 两个值且在科室中不存在，只能当"建议执行科室"留档。
 */
@Data
@TableName("sys_treatment_item")
public class SysTreatmentItem implements Serializable {

    /** 主键ID */
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 项目编码（唯一） */
    private String itemCode;

    /** 项目名称 */
    private String itemName;

    /** 项目类型（1-注射 2-输液 3-换药 4-拆线 5-其他） */
    private Integer itemType;

    /** 执行科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 治疗价格 */
    private BigDecimal price;

    /** 治疗时长（分钟） */
    private Integer duration;

    /** 使用方法 */
    private String usageMethod;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /** 删除标志（0-正常 1-删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;
}
