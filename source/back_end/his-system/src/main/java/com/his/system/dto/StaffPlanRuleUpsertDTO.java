package com.his.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 人力配置标准新增/修改入参。
 */
@Data
public class StaffPlanRuleUpsertDTO {

    /**
     * 主键ID（空=新增）
     */
    private Long id;

    /**
     * 排班单元类型（1-科室 2-病区 3-全院）
     */
    @NotNull(message = "排班单元类型不能为空")
    private Integer orgType;

    /**
     * 排班单元ID（全院级不传）
     */
    private Long orgId;

    /**
     * 标准班次ID（空或 0=该单元全部班次共用）
     */
    private Long shiftId;

    /**
     * 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）
     */
    @NotNull(message = "岗位类别不能为空")
    private Integer staffType;

    /**
     * 最低在岗人数
     */
    private Integer minStaff;

    /**
     * 最高在岗人数（空或 0=不限）
     */
    private Integer maxStaff;

    /**
     * 单周工时上限
     */
    private BigDecimal maxWeekHours;

    /**
     * 连续夜班天数上限
     */
    private Integer maxConsecutiveNightDays;

    /**
     * 连续上班天数上限
     */
    private Integer maxConsecutiveWorkDays;

    /**
     * 状态（0-停用 1-启用，空=启用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
