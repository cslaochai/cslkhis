package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 患者标签查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPatientTagQueryDTO extends PageParam {

    /**
     * 标签名称，如：高血压、VIP、医保
     */
    private String tagName;

}