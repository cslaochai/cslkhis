package com.mis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典类型新增/修改入参
 */
@Data
public class DictTypeUpsertDTO implements Serializable {

    private Long id;

    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    private String remark;
}
