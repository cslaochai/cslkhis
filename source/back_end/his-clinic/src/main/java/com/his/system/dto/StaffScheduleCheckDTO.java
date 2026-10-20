package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 员工排班区间规则校验入参（全区岗位通用：工时 / 连续夜班 / 连续上班 / 人力配置）。
 * 告警明细不阻断保存，只列出来给排班员自查。
 */
@Data
public class StaffScheduleCheckDTO {

    /**
     * 开始日期（含）
     */
    @NotNull(message = "开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期（含）
     */
    @NotNull(message = "结束日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 排班单元类型（1-科室 2-病区 3-全院），空=全部单元
     */
    private Integer orgType;

    /**
     * 排班单元ID，空=该类型全部单元
     */
    private Long orgId;

    /**
     * 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他），空=全部岗位
     */
    private Integer staffType;
}
