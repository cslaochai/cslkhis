package com.his.patient.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 医嘱模板明细（含明细行），套用模板与模板预览都用它。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InpatientOrderTemplateDetailVO extends InpatientOrderTemplateListVO implements Serializable {

    /** 明细项集合 */
    private List<InpatientOrderTemplateItemVO> items;
}
