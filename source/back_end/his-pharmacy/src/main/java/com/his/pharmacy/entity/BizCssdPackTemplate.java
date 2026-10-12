package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

/**
 * CSSD 器械包模板目录实体（99 号脚本新增）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_cssd_pack_template")
public class BizCssdPackTemplate extends BaseEntity {

    /**
     * 包编码
     */
    private String templateCode;
    /**
     * 器械包名称
     */
    private String packName;

    /**
     * 默认灭菌方式（1-高压蒸汽 2-环氧乙烷 3-低温等离子）
     */
    private Integer sterilizeMethod;

    /**
     * 状态（1-启用 0-停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
}
