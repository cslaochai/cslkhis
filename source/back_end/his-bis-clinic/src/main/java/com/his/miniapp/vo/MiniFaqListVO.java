package com.his.miniapp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 常见问题列表项（患者端与后台维护端共用这一个出参类）。
 */
@Data
@Schema(name = "MiniFaqListVO", description = "常见问题列表项")
public class MiniFaqListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 热门（0-否 1-是）
     */
    private Integer hotFlag;

    /**
     * 查看次数
     */
    private Integer viewCount;

    /**
     * 检索关键词（后台维护字段，患者端不填）
     */
    private String keywords;

    /**
     * 有帮助次数（后台维护字段，患者端不填）
     */
    private Integer helpfulCount;

    /**
     * 没帮助次数（后台维护字段，患者端不填）
     */
    private Integer uselessCount;

    /**
     * 状态（0-停用 1-启用，后台维护字段，患者端不填）
     */
    private Integer status;

    /**
     * 排序号（后台维护字段，患者端不填）
     */
    private Integer sortOrder;

    /**
     * 创建时间（后台维护字段，患者端不填）
     */
    private String createTime;
}
