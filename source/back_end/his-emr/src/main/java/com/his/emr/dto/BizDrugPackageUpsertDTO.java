package com.his.emr.dto;

import lombok.Data;

import java.util.List;

/**
 * 药品套餐保存入参
 */
@Data
public class BizDrugPackageUpsertDTO {
    /**
     * 套餐ID，新增时为空
     */
    private Long id;

    /**
     * 所属医生ID（后端以当前登录用户覆盖）
     */
    private Long doctorId;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 套餐类型（1-药品套餐 2-检查套餐 3-综合套餐）
     */
    private Integer packageType;

    /**
     * 套餐明细列表
     */
    private List<BizDrugPackageDetailUpsertDTO> details;
}
