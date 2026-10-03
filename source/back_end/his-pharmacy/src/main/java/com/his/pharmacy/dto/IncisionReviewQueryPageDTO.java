package com.his.pharmacy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** I 类切口预防用药点评分页入参 */
@Data
public class IncisionReviewQueryPageDTO {

    /** 患者姓名/手术名称模糊 */
    private String keyword;

    /** 点评结论（1合理 2不合理） */
    @Min(value = 1, message = "点评结论非法")
    @Max(value = 2, message = "点评结论非法")
    private Integer reviewResult;

    /** 页码 */
    @Min(value = 1, message = "页码非法")
    private Integer pageNum = 1;

    /** 每页条数 */
    @Min(value = 1, message = "每页条数非法")
    private Integer pageSize = 10;
}
