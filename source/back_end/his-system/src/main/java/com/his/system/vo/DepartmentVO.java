package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 科室信息出参
 */
@Data
public class DepartmentVO {

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 科室编码（唯一）
     */
    private String deptCode;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他）
     */
    private Integer deptType;

    /**
     * 上级科室ID，顶级科室为0
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    /**
     * 排序号，越小越靠前
     */
    private Integer sortOrder;

    /**
     * 科室图标
     */
    private String deptIcon;

    /**
     * 科室描述
     */
    private String deptDesc;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 地理位置/地址
     */
    private String location;

    /**
     * 是否开放：0-否 1-是
     */
    private Integer isOpen;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

    /**
     * 子科室列表
     */
    private List<DepartmentVO> children;
}
