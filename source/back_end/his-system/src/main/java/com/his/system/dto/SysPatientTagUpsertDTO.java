package com.his.system.dto;

import lombok.Data;

/**
 * 患者标签新增/修改入参
 */
@Data
public class SysPatientTagUpsertDTO {

    /**
     * 标签ID，新增时为空，修改时必填
     */
    private Long tagId;

    /**
     * 标签名称，如：高血压、VIP、医保
     */
    private String tagName;

    /** 标签缩写用于展示 */
    private String shortName;

    /**
     * 标签颜色(十六进制)，如：#FF5722
     */
    private String tagColor;
}
