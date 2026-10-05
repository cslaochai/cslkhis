package com.his.charge.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 在院欠费患者榜查询入参。
 */
@Data
public class ArrearsBoardQueryDTO {

    /**
     * 患者姓名/患者号模糊
     */
    private String keyword;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Min(value = 1, message = "每页条数不能小于1")
    private Integer pageSize = 10;
}
