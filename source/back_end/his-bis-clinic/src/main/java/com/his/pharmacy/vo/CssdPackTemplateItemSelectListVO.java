package com.his.pharmacy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 器械包模板组成明细下拉出参（编辑器按名称带出规格/单位）。
 */
@Data
@Schema(name = "CssdPackTemplateItemSelectListVO", description = "器械包明细名称下拉出参")
public class CssdPackTemplateItemSelectListVO implements Serializable {

    /**
     * 器械名称
     */
    private String itemName;

    /**
     * 规格
     */
    private String spec;

    /**
     * 单位
     */
    private String unit;
}
