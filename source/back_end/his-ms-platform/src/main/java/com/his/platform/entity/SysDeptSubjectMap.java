package com.his.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 科室 → 诊疗科目 映射（主流 HIS 里叫「病案科室对应设置」）。
 *
 * <p>只做对照，不替换：病案首页 biz_inpatient_summary 仍然存院内 dept_id（历史数据与数据权限
 * 都依赖它），上报时才用本表折算标准科目代码。
 *
 * <p>is_primary 是「该科室上报时用哪个科目」的指定，同一科室有且只有一条为 1。
 * 一个科室可以对应多个科目（如「急诊内科」同时归 20 急诊医学科 与 03 内科）。
 *
 * <p>无 delFlag：uk_dept_subject 上有唯一键，逻辑删除会占着唯一键，一律物理删除。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept_subject_map")
public class SysDeptSubjectMap extends BaseEntity {

    /**
     * 院内科室ID（sys_department.id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 诊疗科目代码（sys_med_subject.subject_code）
     */
    private String subjectCode;

    /**
     * 是否上报主科目：1-是（病案首页取这个）0-否
     */
    private Integer isPrimary;

    /**
     * 备注
     */
    private String remark;
}
