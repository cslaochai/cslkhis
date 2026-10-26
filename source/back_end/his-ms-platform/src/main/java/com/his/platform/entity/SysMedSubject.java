package com.his.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 诊疗科目主数据（国家卫健委《医疗机构诊疗科目名录》）。
 *
 * <p>为什么不能直接用 sys_department：科室是**行政建制**，dept_code 是本院自编码；
 * 而病案首页、卫生统计直报、医保结算清单要填的是**诊疗科目代码**（如 03.01 呼吸内科专业）。
 * 两者不是一一对应：一个行政科室可能覆盖多个科目，一个科目也可能由多个科室共同承担。
 * 所以科目单独成表，用 sys_dept_subject_map 做对照，病案首页仍存院内 dept_id。
 *
 * <p>名录会修订，科目一律数据化维护，不在代码里写死枚举——写死的话每次卫健委调整都得改代码发版。
 *
 * <p>无 delFlag：subject_code 上有唯一键，逻辑删除会占着唯一键导致"删了再建同码"报 500，
 * 与 sys_ward / sys_med_group / sys_menu 口径一致，一律物理删除。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_med_subject")
public class SysMedSubject extends BaseEntity {

    /**
     * 诊疗科目代码（一级两位如 03，二级形如 03.01）
     */
    private String subjectCode;

    /**
     * 诊疗科目名称（如「内科」「呼吸内科专业」）
     */
    private String subjectName;

    /**
     * 上级科目代码（一级科目为空）
     */
    private String parentCode;

    /**
     * 科目层级：1-一级科目 2-二级科目
     */
    private Integer subjectLevel;

    /**
     * 是否临床科目：1-是 0-否。检验/病理/影像等医技科目记 0，用于区分「临床科室」与「平台科室」。
     */
    private Integer isClinical;

    /**
     * 状态：0-停用 1-启用
     */
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
