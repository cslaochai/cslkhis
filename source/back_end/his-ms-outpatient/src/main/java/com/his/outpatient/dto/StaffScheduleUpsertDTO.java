package com.his.outpatient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 全院岗位排班新增/修改入参。
 */
@Data
public class StaffScheduleUpsertDTO {

    /**
     * 主键ID（空=新增）
     */
    private Long id;

    /**
     * 排班日期
     */
    @NotNull(message = "排班日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /**
     * 排班单元类型（1-科室 2-病区 3-全院）
     */
    @NotNull(message = "排班单元类型不能为空")
    @AllowedValues(values = {"1", "2", "3"}, message = "排班单元类型取值不合法")
    private Integer orgType;

    /**
     * 排班单元ID（科室/病区必填，全院级不传）
     */
    private Long orgId;

    /**
     * 排班对象（员工）
     */
    @NotNull(message = "排班对象不能为空")
    private Long employeeId;

    /**
     * 所依据的岗位（空=取该员工在这两个科室关系上生效的那条，其次主岗位）
     */
    private Long employeePostId;

    /**
     * 标准班次ID（上班必填；休息/请假/培训/停班不传）
     */
    private Long shiftId;

    /**
     * 出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班）
     */
    @NotNull(message = "出勤状态不能为空")
    @AllowedValues(values = {"1", "2", "3", "4", "5"}, message = "出勤状态不合法")
    private Integer dutyStatus;

    /**
     * 响应形态（1-坐班 2-听班 3-留院值班，空=坐班）
     */
    @AllowedValues(values = {"1", "2", "3"}, message = "响应形态取值不合法")
    private Integer attendMode;

    /**
     * 是否出诊（0-否 1-是，空=否；只有医生岗且非听班才可能为是）
     */
    @AllowedValues(values = {"0", "1"}, message = "是否出诊取值不合法")
    private Integer clinicFlag;

    /**
     * 发布态（0-草稿 1-待审核 2-已发布 3-调班中 4-已变更）。
     *
     * <p>写入侧一般不由前端传：护理/全院排班格子由服务端按「新增=草稿、改单=已变更」定，
     * 医生出诊派生的事实跟随号源行的发布态。留空时不改该列（交给库默认值）。
     */
    @AllowedValues(values = {"0", "1", "2", "3", "4"}, message = "发布态取值不合法")
    private Integer publishStatus;

    /**
     * 备注
     */
    private String remark;
}
