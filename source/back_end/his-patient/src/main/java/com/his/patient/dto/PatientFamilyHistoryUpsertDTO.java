package com.his.patient.dto;

import com.his.common.validation.InEnum;
import com.his.patient.enums.IsAliveEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 患者家族史新增/修改入参
 */
@Data
public class PatientFamilyHistoryUpsertDTO {
    /**
     * 家族史ID，新增时为空，修改时必填
     */
    private Long id;
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 与患者关系（父亲/母亲/兄弟/姐妹/祖父/祖母/子女）。
     *
     * <p>这里存的是**亲属称谓文案**，不是患者关系字典的数字码值 ——
     * 与联系人那一组相反，两者别互相套用。
     */
    @NotBlank(message = "与患者关系不能为空（如：父亲、母亲、兄弟）")
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
    @InEnum(value = IsAliveEnum.class, message = "是否在世取值不合法（0-已故 1-在世）")
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
