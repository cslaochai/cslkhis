package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** I 类切口预防用药点评分页入参 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IncisionReviewQueryPageDTO extends PageParam {

    /** 患者姓名/手术名称模糊 */
    private String keyword;

    /** 点评结论（1合理 2不合理） */
    @Min(value = 1, message = "点评结论非法")
    @Max(value = 2, message = "点评结论非法")
    private Integer reviewResult;
}
