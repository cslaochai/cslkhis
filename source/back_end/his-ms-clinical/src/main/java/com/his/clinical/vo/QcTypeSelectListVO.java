package com.his.clinical.vo;

import lombok.Data;

import java.io.Serializable;

/** 质控类型下拉项：质控检查记录筛选框可选的质控类型编码与文案。 */
@Data
public class QcTypeSelectListVO implements Serializable {

    /** 质控类型码值（0-综合 1-完整性检查 2-规范性检查 3-逻辑性检查 4-AI内涵质控） */
    private Integer code;

    /** 质控类型文案（下拉展示） */
    private String text;
}
