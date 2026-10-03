package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 医嘱模板分页查询入参（模板管理弹窗用）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InpatientOrderTemplateQueryPageDTO extends PageParam implements Serializable {

    /**
     * 名称/备注模糊
     */
    private String keyword;

    /**
     * 默认医嘱类型：1-长期 2-临时
     */
    private Integer orderType;
}
