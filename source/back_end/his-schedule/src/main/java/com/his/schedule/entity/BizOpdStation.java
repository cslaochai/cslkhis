package com.his.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门诊公共工位字典
 *
 * <p>位先定义存在：总导诊/分诊台/输液室/注射室/采血室这些位全院通用、不挂具体科室；
 * 派岗只是把人写进位里，见 {@link BizOpdStationAssignment}。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_opd_station")
public class BizOpdStation extends BaseEntity {

    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;

    /** 工位编码（全局唯一） */
    private String stationCode;

    /** 工位名称（如：门诊总导诊台） */
    private String stationName;

    /** 工位类别（1-总导诊 2-门诊分诊台 3-输液室 4-注射室 5-采血室 9-其他） */
    private Integer stationKind;

    /** 应到岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他 7-护工，默认2护理） */
    private Integer requiredStaffType;

    /** 建议同时在岗人数 */
    private Integer capacity;

    /** 工位电话 */
    private String phone;

    /** 排序号（小的在前） */
    private Integer sortNo;

    /** 状态（0-停用 1-启用） */
    private Integer status;
}
