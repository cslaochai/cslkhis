package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 家族史
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_patient_family_history")
public class BizPatientFamilyHistory extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;


    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /**
     * 与患者关系（父亲/母亲/兄弟/姐妹/祖父/祖母/子女）
     */
    private String relationship;
    /**
     * 亲属姓名
     */
    private String name;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 是否在世（0-已故 1-在世）
     */
    private Integer isAlive;
    /**
     * 死亡原因
     */
    private String causeOfDeath;
    /**
     * 健康状况描述
     */
    private String healthStatus;
    /**
     * 遗传性疾病（如：高血压、糖尿病、肿瘤等）
     */
    private String hereditaryDisease;
    /**
     * 传染病史
     */
    private String infectiousDisease;
}
