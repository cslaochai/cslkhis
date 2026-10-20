package com.his.schedule.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 急诊固定医师占比（评审口径：《急诊科建设与管理指南(试行)》卫医政发〔2009〕50号
 * —— 固定急诊医师应不低于在岗急诊医师的 75%）。
 * 按急诊内科/外科/儿科分别统计：当日已发布（2/4）在岗医生中，档案标记固定（is_fixed=1）的占比。
 */
@Data
public class ErFixedRatioVO {
    /** 急诊科室ID */
    private Long deptId;
    /** 科室名称 */
    private String deptName;
    /** 当日发布在岗医师数（distinct 员工） */
    private Integer onDutyDoctors;
    /** 其中固定医师数（is_fixed=1） */
    private Integer fixedDoctors;
    /** 固定占比（0~1，四位小数；无人排班为 null） */
    private BigDecimal fixedRatio;
}
