package com.his.platform.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 手术操作编码：ICD-9-CM-3 术式主数据，一行一个编码，供手术申请与手术类医嘱选项目。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_icd9cm3")
public class SysIcd9cm3 extends BaseEntity {

    /**
     * 手术操作编码
     */
    private String opCode;

    /**
     * 手术操作名称
     */
    private String opName;

    /**
     * 章节
     */
    private Integer opCategory;

    /**
     * 编码口径（1-医保版贯标码 0-历史缩写码）
     */
    private Integer codeStd;

    /**
     * 排序，越小越靠前
     */
    private Integer sortOrder;

    /**
     * 状态（0-停用 1-正常）
     */
    private Integer status;

    /**
     * 删除标志（0-正常 1-删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
