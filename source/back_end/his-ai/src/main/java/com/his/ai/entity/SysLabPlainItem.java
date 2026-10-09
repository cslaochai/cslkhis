package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 检验项目白话词典（建表见 sql/218）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_lab_plain_item")
public class SysLabPlainItem extends BaseEntity {

    /**
     * 所属分组（血常规 / 肝功能 / 肾功能 / 血糖 / 血脂 / 炎症 / 凝血 / 心肌 / 电解质 / 尿常规 / 大便）
     */
    private String groupName;

    /**
     * 检验项目名称，与检验结果里的项目名称精确匹配
     */
    private String itemName;

    /**
     * 白话名（血色素 / 坏胆固醇 / 心肌损伤指标 …）
     */
    private String plainName;

    /**
     * 这项查什么（给患者看的一句话）
     */
    private String whatIsIt;

    /**
     * 结果偏高时的白话说明
     */
    private String highText;

    /**
     * 结果偏低时的白话说明
     */
    private String lowText;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
