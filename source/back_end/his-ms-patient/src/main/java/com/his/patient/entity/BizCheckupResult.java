package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 体检结果明细实体（体检结果明细，登记时按套餐项目预生成空行）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_checkup_result")
public class BizCheckupResult extends BaseEntity {

    /**
     * 体检登记ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 项目来源（1-检验项目 2-检查项目 3-一般检查）
     */
    private Integer itemSource;

    /**
     * 来源目录项目ID（sys_laboratory_item / sys_inspection_item / sys_treatment_item）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /**
     * 来源目录项目编码（结果回写与报告对码用）
     */
    private String itemCode;

    /**
     * 项目名称（取目录项目名，冗余快照）
     */
    private String itemName;

    /**
     * 项目类别（1-检验 2-检查 3-一般）
     */
    private Integer itemType;

    /**
     * 参考范围
     */
    private String refStandard;

    /**
     * 结果值/所见
     */
    private String resultValue;

    /**
     * 异常标志（0-正常 1-异常 2-待查）
     */
    private Integer abnormalFlag;

    /**
     * 单项小结/建议
     */
    private String summaryText;

    /**
     * 检查/检验医师
     */
    private String checkerName;

    /**
     * 结果录入时间
     */
    private LocalDateTime resultTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
