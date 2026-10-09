package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DRG 排除表：主诊断下某诊断丧失 CC/MCC 资格（3.0 强化，防止虚挂并发症抬高权重）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_drg_exclusion")
public class SysDrgExclusion extends BaseEntity {

    /**
     * 主诊断编码（ICD-10）
     */
    private String mainDiagCode;

    /**
     * 被排除的 CC/MCC 诊断编码
     */
    private String excludedCode;

    /**
     * 分组方案版本（2.0/3.0）
     */
    private String version;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
