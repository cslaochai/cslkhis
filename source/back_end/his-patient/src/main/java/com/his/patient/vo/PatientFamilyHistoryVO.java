package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 患者家族史出参
 */
@Data
public class PatientFamilyHistoryVO {
    /**
     * 家族史ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
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
     * 亲属年龄
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
     * 传染性疾病
     */
    private String infectiousDisease;
}
