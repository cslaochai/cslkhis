package com.his.miniapp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 常见问题列表项。
 *
 * <p>答案整段返回：条目是几十条量级、单条答案不到 100 字，
 * 让前端自己折叠远比后端截断后再点开取详情省一次往返。
 */
@Data
@Schema(name = "FaqListVO", description = "常见问题列表项")
public class FaqListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 常见问题编号 */
    private String faqNo;

    /** 分类编码 */
    private String categoryCode;

    /** 分类名称 */
    private String categoryName;

    /** 问题 */
    private String question;

    /** 答案 */
    private String answer;

    /** 热门（0-否 1-是） */
    private Integer hotFlag;

    /** 查看次数 */
    private Integer viewCount;
}
