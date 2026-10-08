package com.his.miniapp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 患者端常见问题。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_faq")
public class SysFaq extends BaseEntity {

    /**
     * 常见问题编号
     */
    private String faqNo;

    /**
     * 分类编码
     */
    private String categoryCode;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 问题
     */
    private String question;

    /**
     * 答案
     */
    private String answer;

    /**
     * 检索关键词（顿号分隔，含口语同义词）
     */
    private String keywords;

    /**
     * 热门（0-否 1-是）
     */
    private Integer hotFlag;

    /**
     * 查看次数
     */
    private Integer viewCount;

    /**
     * 有帮助次数
     */
    private Integer helpfulCount;

    /**
     * 没帮助次数
     */
    private Integer uselessCount;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
