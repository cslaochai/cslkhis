package com.his.emr.dto;

import lombok.Data;

/**
 * 处理质控问题入参
 */
@Data
public class QcHandleDTO {

    /**
     * 质控记录ID
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
