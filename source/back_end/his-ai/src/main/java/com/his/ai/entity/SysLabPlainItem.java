package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检验项目白话词典（建表见 {@code sql/218}）。
 * <p>
 * 患者端报告解读的<b>规则层地基</b>：把「这项查什么 / 偏高通常意味着什么 / 偏低通常意味着什么」
 * 落成人能审、能改的固定文本，而不是让模型现编。
 * <p>
 * <b>为什么必须人工维护：</b>模型顺着「血红蛋白偏低」会顺手补一句「可能是缺铁性贫血，建议补铁」，
 * 这句话读起来完全通顺、但没有任何医生背书 —— 患者会信。而词典里这句话根本不会被写出来，
 * 维护者（包括 AI）写进医嘱类词汇，会被种子脚本的硬断言挡住。
 * <p>
 * 匹配键是 {@code itemName} 不是项目编码：库里 {@code laboratory_item_code} 与项目名是多对多
 * （JY001 同时对应红细胞、白细胞、血红蛋白），编码不可靠。
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
     * 检验项目名称，与 biz_lab_result.laboratory_item_name 精确匹配
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
}
