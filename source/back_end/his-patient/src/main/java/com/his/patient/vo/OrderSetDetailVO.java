package com.his.patient.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 医嘱组套模板明细（含明细行），编辑回显与预览共用。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderSetDetailVO extends OrderSetListVO implements Serializable {

    /**
     * 核查项码值
     */
    private List<InpatientOrderTemplateItemVO> items;
}
