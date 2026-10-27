package com.his.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 病区-科室多对多关系：一行 = 「某病区可以收治某科室的患者」。
 *
 * <p>为什么需要这张表：科室（行政建制 + 成本核算 + 执业科目映射）与病区（护理单元 + 物理空间）
 * 是两个正交维度，一个病区可以收多个科室的患者（跨科收治 / 全院一张床），
 * 一个科室也可以有多个病区。这与主流 HIS CDR 住院登记表 ryksdm(入院科室) / rybqdm(入院病区)
 * 两字段并存是同构的。
 *
 * <p>与 {@code sys_ward.dept_id} 的关系：后者保留为「本位科室（默认收治科室）」，
 * 因为 ICU / PIVAS / 摆药 / 排班 / 工作台统计共 6 处数据权限判定依赖 ward_id → dept_id
 * 单值折算。本位科室必须同时是本病区在 {@code sys_ward_dept} 里 isPrimary=1 的那一条。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ward_dept")
public class SysWardDept extends BaseEntity {

    /**
     * 病区ID（sys_ward.ward_id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 科室ID（sys_department.id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 是否本位科室：1-是 0-否。每个病区有且仅有一条为 1，且必须与 sys_ward.dept_id 一致
     */
    private Integer isPrimary;

    /**
     * 该科室在本病区的专科优先床数（NULL=不限额，剩余为机动床）
     */
    private Integer bedQuota;

    /**
     * 排序号（越小越靠前）
     */
    private Integer sortOrder;
}
