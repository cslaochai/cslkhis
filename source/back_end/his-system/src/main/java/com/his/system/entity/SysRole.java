package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /**
     * 角色编码（唯一）
     */
    private String roleCode;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 岗位类别（1医生 2护理 3医技 4药学 5收费 6行政其他，见 {@code StaffTypeEnum}，sql/195）
     *
     * <p>角色是权限概念，岗位类别是人事概念——二者是不同的维度，同一个类别下有多个角色
     * （如 1-医生下有医生/急诊医生/放射诊断医师/公卫医师）。排班与人员下拉按<b>类别</b>取人：
     * 「人在哪个科室是什么岗位」由员工岗位(人 × 科室 × 角色) 与本列一起派生。
     * NULL = 该角色不参与排班（如患者角色）。
     */
    private Integer staffType;
    /**
     * 角色类型（1-系统角色 2-自定义角色）
     */
    private Integer roleType;
    /**
     * 数据权限范围（1-全部数据 2-自定义数据 3-本部门数据 4-本部门及以下 5-仅本人数据）
     */
    private Integer dataScope;
    /**
     * 排序号
     */
    private Integer sortOrder;
    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
