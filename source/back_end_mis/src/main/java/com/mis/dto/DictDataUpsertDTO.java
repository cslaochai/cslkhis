package com.mis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据新增/修改入参
 */
@Data
public class DictDataUpsertDTO implements Serializable {

    private Long id;

    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    @NotBlank(message = "字典值不能为空")
    private String dictValue;

    private Integer dictSort;

    /** 是否默认（0-否 1-是） */
    private Integer isDefault;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    private String remark;
}
