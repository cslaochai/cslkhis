package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 科室下拉统一入口出参。
 *
 * <p>只带下拉需要的字段，不带创建人 / 逻辑删除标记这类管理端字段。
 *
 * <p>字段名统一用**驼峰**（{@code deptName}）而不是下划线：全仓 20+ 处页面已经在写
 * {@code d.deptName}。若这里回科室名称，所有页面都得跟着改，
 * 而"改一处漏一处"正是这轮要消灭的东西。
 */
@Data
public class DepartmentSelectListVO {

    /** 科室ID（序列化成字符串，避免 JS 精度丢失） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 科室编码（唯一） */
    private String deptCode;

    /** 科室名称 */
    private String deptName;

    /**
     * 科室类型：1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他
     * （口径见列注释与字典 {@code his_dept_type}）
     */
    private Integer deptType;

    /** 上级科室ID，顶级为 0 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;
}
