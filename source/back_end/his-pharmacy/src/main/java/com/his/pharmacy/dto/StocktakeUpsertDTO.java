package com.his.pharmacy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 盘点单建单/改单入参
 */
@Data
public class StocktakeUpsertDTO {

    /** 盘点单ID（null=新建） */
    private Long id;

    /** 盘点主题 */
    @NotBlank(message = "盘点主题不能为空")
    @Size(max = 100, message = "盘点主题不能超过100字")
    private String stocktakeTitle;

    /** 范围-药品类型（null=全部，1-西药 2-中成药 3-中药饮片） */
    @Min(value = 1, message = "药品类型取值不合法（1-西药 2-中成药 3-中药饮片）")
    @Max(value = 3, message = "药品类型取值不合法（1-西药 2-中成药 3-中药饮片）")
    private Integer scopeDrugType;

    /** 范围-药品名称/编码/批号关键字（null=不限） */
    @Size(max = 50, message = "关键字不能超过50字")
    private String scopeKeyword;

    /** 备注（服务端截到列宽 500，入参层不限长度：AGENTS §3） */
    private String remark;
}
