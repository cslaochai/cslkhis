package com.his.pharmacy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 抗菌药物处方权授权分页入参 */
@Data
public class AntibioticAuthQueryPageDTO {

    /** 医师姓名/科室模糊 */
    private String keyword;

    /** 授权级别（1-非限制使用级 2-限制使用级 3-特殊使用级） */
    @Min(value = 1, message = "授权级别非法")
    @Max(value = 3, message = "授权级别非法")
    private Integer authLevel;

    /** 状态（1-有效 2-暂停 3-取消） */
    @Min(value = 1, message = "状态非法")
    @Max(value = 3, message = "状态非法")
    private Integer status;

    /** 只看当前可用（状态有效且未过期） */
    private Boolean onlyEffective;

    /** 页码 */
    @Min(value = 1, message = "页码非法")
    private Integer pageNum = 1;

    /** 每页条数 */
    @Min(value = 1, message = "每页条数非法")
    private Integer pageSize = 10;
}
