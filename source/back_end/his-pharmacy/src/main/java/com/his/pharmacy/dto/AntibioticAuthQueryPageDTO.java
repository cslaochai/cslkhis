package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 抗菌药物处方权授权分页入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AntibioticAuthQueryPageDTO extends PageParam {

    /**
     * 医师姓名/科室模糊
     */
    private String keyword;

    /**
     * 授权级别（1-非限制使用级 2-限制使用级 3-特殊使用级）
     */
    private Integer authLevel;

    /**
     * 状态（1-有效 2-暂停 3-取消）
     */
    private Integer status;

    /**
     * 只看当前可用（状态有效且未过期）
     */
    private Boolean onlyEffective;
}
