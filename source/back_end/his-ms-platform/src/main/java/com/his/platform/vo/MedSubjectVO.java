package com.his.platform.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 诊疗科目（树形节点）。
 */
@Data
public class MedSubjectVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 科目代码（03 / 03.01）
     */
    private String subjectCode;

    /**
     * 科目名称
     */
    private String subjectName;

    /**
     * 上级科目代码
     */
    private String parentCode;

    /**
     * 层级：1-一级 2-二级
     */
    private Integer subjectLevel;

    /**
     * 是否临床科目：1-是 0-否
     */
    private Integer isClinical;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

    private Integer sortOrder;

    private String remark;

    /**
     * 已映射科室数（只算 is_primary=1 的，即「有多少科室把它当作上报主科目」）
     */
    private Integer mappedDeptCount;

    /**
     * 下级科目（一级科目才有）
     */
    private List<MedSubjectVO> children;
}
