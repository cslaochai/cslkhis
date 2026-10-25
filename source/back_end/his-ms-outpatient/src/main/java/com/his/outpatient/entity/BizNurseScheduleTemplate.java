package com.his.outpatient.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 护理排班模板（周模板：按星期几给护士配置固定出勤，是长期资产；真实排班按周「套用」生成）。
 * 与门诊号源模板 {@code BizClinicSourceTemplate} 同处门诊模块，但字段只保留护理相关子集，
 * 不继承号源/诊室/挂号费那套。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nurse_schedule_template")
public class BizNurseScheduleTemplate extends BaseEntity {

    /** 逻辑删除标志（0 未删除 1 已删除；手动维护：列表/统计按 delFlag 过滤，删除走置位更新，便于「已删除模板」统计且不撞真实排班唯一键） */
    private Integer delFlag;

    /** 备注 */
    private String remark;

    /** 模板名称（如「内科一组周模板」） */
    private String name;

    /** 排班单元类型（1-病区 2-门诊科室；空按 1-病区） */
    private Integer unitType;

    /** 排班单元ID（unitType=1 取病区ID，=2 取门诊科室ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /** 排班单元名称（快照，生成排班与适用记录时原样带回） */
    private String wardName;

    /** 单双周（0-每周 1-单周 2-双周，按 ISO 周号奇偶） */
    private Integer weekParity;

    /** 生效起始日期（空=不限） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validFrom;

    /** 生效截止日期（空=不限） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    /** 共享范围（0-个人 1-权限组共享；权限组模板全院护理排班员可见可套用） */
    private Integer scope;

    /** 状态（0-停用 1-启用） */
    private Integer status;
}
