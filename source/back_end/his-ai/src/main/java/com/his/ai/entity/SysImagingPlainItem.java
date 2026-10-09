package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 影像检查白话词典（建表见 sql/232）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_imaging_plain_item")
public class SysImagingPlainItem extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 所属分组（放射 / 超声 / 心电 / 内镜）
     */
    private String groupName;

    /**
     * 匹配关键词（报告项目名包含即命中，取最长命中）
     */
    private String itemName;

    /**
     * 白话名（胸部CT / B超 / 心电图 …）
     */
    private String plainName;

    /**
     * 这项检查是查什么的（给患者看的一句话）
     */
    private String whatItDoes;

    /**
     * 检查前后的注意事项（白话，可为空）
     */
    private String noticeText;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
