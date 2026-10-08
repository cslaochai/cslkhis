package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 医嘱组套模板分页查询入参（全院组套模板管理页）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderSetQueryPageDTO extends PageParam implements Serializable {

    /**
     * 名称/备注模糊
     */
    private String keyword;

    /**
     * 共享范围：1-个人 2-科室 3-全院（不传=全部可见）
     */
    private Integer scope;

    /**
     * 默认医嘱类型：1-长期 2-临时
     */
    private Integer orderType;
}
