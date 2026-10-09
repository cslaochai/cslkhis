package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * ICD-10 诊断编码（ICD-10 诊断编码）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_icd10")
public class SysIcd10 extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * ICD编码
     */
    private String icdCode;
    /**
     * 疾病名称
     */
    private String icdName;
    /**
     * 分类
     */
    private String icdCategory;
    /**
     * 排序
     */
    private Integer sortOrder;
    /**
     * 状态（0-停用 1-正常）
     */
    private Integer status;
}
