package com.his.miniapp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 常见问题后台列表项（比患者端多出启用状态与反馈计数 —— 这些是维护者要看的）。
 */
@Data
@Schema(name = "FaqAdminVO", description = "常见问题后台列表项")
public class FaqAdminVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String faqNo;

    private String categoryCode;

    private String categoryName;

    private String question;

    private String answer;

    private String keywords;

    private Integer hotFlag;

    private Integer viewCount;

    private Integer helpfulCount;

    private Integer uselessCount;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    private Integer sortOrder;

    private String createTime;
}
