package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 影像检查白话词典（建表见 {@code sql/232}）。
 * <p>
 * 患者端影像报告解读的<b>规则层地基</b>：把「这项检查是查什么的 / 检查前后的注意」
 * 落成人能审、能改的固定文本。与 {@code sys_lab_plain_item} 的关键差异：
 * 影像报告的描述/结论文本本身<b>不进词典也不做改写</b>——那是开放文本，交给模型串话 + 硬闸；
 * 词典只负责可穷举的部分（检查项目介绍），模型挂了它照常给出，这就是降级时的底线。
 * <p>
 * 匹配键是**关键词**而非精确名称：报告项目名是自由组合文本
 * （「胸部CT平扫+三维重建」），代码按「报告项目名包含关键词，取最长命中」匹配。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_imaging_plain_item")
public class SysImagingPlainItem extends BaseEntity {

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
