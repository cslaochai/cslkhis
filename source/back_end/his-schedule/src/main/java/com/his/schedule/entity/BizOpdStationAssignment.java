package com.his.schedule.entity;

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
 * 门诊工位派岗记录
 *
 * <p>只把「当天已排上班的人」挪到公共位上，本身不产出出勤事实，不进工时/缺勤闭环；
 * is_loan=1 表示跨单元借调（人从别的科室/病区借来顶这个公共工位）。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_opd_station_assignment")
public class BizOpdStationAssignment extends BaseEntity {

    /**
     * 上岗日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate assignDate;

    /**
     * 工位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stationId;

    /**
     * 工位名称快照
     */
    private String stationName;

    /**
     * 工位类别快照（1总导诊 2分诊台 3输液室 4注射室 5采血室 9其他）
     */
    private Integer stationKind;

    /**
     * 上岗员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 上岗员工姓名快照
     */
    private String employeeName;

    /**
     * 员工工号快照
     */
    private String empCode;

    /**
     * 上岗人岗位类别快照（取该人当天排班事实，不接受手填）
     */
    private Integer staffType;

    /**
     * 班次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 班次开始时间快照 HH:mm
     */
    private String startTime;

    /**
     * 班次结束时间快照 HH:mm
     */
    private String endTime;

    /**
     * 是否跨单元借调（0-否 1-是）
     */
    private Integer isLoan;

    /**
     * 借出方单元类型（借调填：1科室 2病区 3全院）
     */
    private Integer sourceOrgType;

    /**
     * 借出方单元ID（借调填）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceOrgId;

    /**
     * 借出方单元名称快照（借调填）
     */
    private String sourceOrgName;

    /**
     * 派岗方式（1-排班员指定）
     */
    private Integer assignMode;

    /**
     * 状态（1-在岗 0-撤销）
     */
    private Integer status;

    /**
     * 逻辑删除标志（0 未删除 1 已删除）；本表唯一键不含 del_flag，撤销/删除走物理 purge
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
