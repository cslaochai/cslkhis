package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 患者端预问诊记录（G-05）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_previsit_record")
public class BizPrevisitRecord extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 挂号ID（一次挂号一份问卷）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 就诊科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 就诊科室名称
     */
    private String deptName;

    /**
     * 主症状
     */
    private String mainSymptom;

    /**
     * 问答明细JSON（题目与作答回显）
     */
    private String answersJson;

    /**
     * 患者补充描述
     */
    private String freeText;

    /**
     * 病史摘要（模型凝练或规则模板）
     */
    private String summaryAi;

    /**
     * 摘要来源（1-模型 2-规则）
     */
    private Integer summarySource;
}
