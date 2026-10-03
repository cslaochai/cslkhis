package com.his.emr.dto;

import lombok.Data;

/**
 * 处理临床规则校验问题入参
 */
@Data
public class RuleCheckHandleDTO {

    /**
     * 校验记录ID
     */
    private Long id;

    /**
     * 是否忽略该问题
     */
    private Boolean ignore;

    /**
     * 处理备注
     */
    private String remark;

}
