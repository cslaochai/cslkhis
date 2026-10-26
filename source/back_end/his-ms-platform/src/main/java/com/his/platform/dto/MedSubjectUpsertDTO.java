package com.his.platform.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 诊疗科目新增/修改入参。
 */
@Data
public class MedSubjectUpsertDTO {

    /**
     * 科目ID，新增时为空，修改时必填
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 科目代码，如 03 / 03.01。二级科目必须形如「一级代码.两位序号」
     */
    @NotBlank(message = "科目代码不能为空")
    private String subjectCode;

    /**
     * 科目名称，如「呼吸内科专业」
     */
    @NotBlank(message = "科目名称不能为空")
    private String subjectName;

    /**
     * 上级科目代码。二级科目必填且必须是一级科目
     */
    private String parentCode;

    /**
     * 科目层级：1-一级 2-二级
     */
    @NotNull(message = "科目层级不能为空")
    @AllowedValues(values = {"1", "2"}, message = "科目层级取值不合法")
    private Integer subjectLevel;

    /**
     * 是否临床科目：1-是 0-否
     */
    @AllowedValues(values = {"0", "1"}, message = "是否临床科目取值不合法")
    private Integer isClinical;

    /**
     * 状态：0-停用 1-启用
     */
    @AllowedValues(values = {"0", "1"}, message = "状态取值不合法")
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}
