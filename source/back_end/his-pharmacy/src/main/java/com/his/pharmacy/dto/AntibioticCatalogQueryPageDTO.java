package com.his.pharmacy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 抗菌药物分级目录分页入参（条件下推后端，前端不做切片） */
@Data
public class AntibioticCatalogQueryPageDTO {

    /** 药品名称/通用名/编码模糊 */
    private String keyword;

    /** 分级过滤（0-非抗菌药物 1/2/3-对应级别；不传=全部） */
    @Min(value = 0, message = "分级非法")
    @Max(value = 3, message = "分级非法")
    private Integer levelFilter;

    /** 页码 */
    @Min(value = 1, message = "页码非法")
    private Integer pageNum = 1;

    /** 每页条数 */
    @Min(value = 1, message = "每页条数非法")
    private Integer pageSize = 10;
}
