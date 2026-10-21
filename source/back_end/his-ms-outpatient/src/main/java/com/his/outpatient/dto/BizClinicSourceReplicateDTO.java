package com.his.outpatient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 排班复刻入参：把源日期区间内的正常排班整体后移 dayOffset 天，展开与落库在后端单事务完成
 */
@Data
public class BizClinicSourceReplicateDTO {

    /**
     * 源区间开始日期
     */
    @NotNull(message = "请选择复刻的起始日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 源区间结束日期
     */
    @NotNull(message = "请选择复刻的结束日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 后移天数（默认 7=下一周）
     */
    @Min(value = 1, message = "复刻偏移至少 1 天")
    @Max(value = 31, message = "复刻偏移最多 31 天")
    private Integer dayOffset;

    /**
     * 科室过滤（空=当前数据范围内全部科室），与排班面板的筛选口径一致
     */
    private Long deptId;

    /**
     * 岗位类别过滤（空=当前范围内全部岗位）
     */
    @AllowedValues(values = {"1", "2", "3", "4", "5", "6", "7"}, message = "岗位类别不合法")
    private Integer staffType;
}
