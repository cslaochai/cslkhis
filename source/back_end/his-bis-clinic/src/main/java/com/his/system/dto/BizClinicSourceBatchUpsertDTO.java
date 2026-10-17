package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 批量新增排班入参：一次提交「共用表头 × 多位人员 × 多个日期」的笛卡尔积，
 * 展开与落库全部在后端单事务内完成，前端禁止循环调用单条接口。
 */
@Data
public class BizClinicSourceBatchUpsertDTO {

    /**
     * 科室ID（整批共用）
     */
    @NotNull(message = "科室不能为空")
    private Long deptId;

    /**
     * 科室名称（整批共用）
     */
    @NotBlank(message = "科室不能为空")
    private String deptName;

    /**
     * 岗位类别（1医生 2护理 3医技 4药学 5收费 6行政其他，见 {@code StaffTypeEnum}，整批共用）
     */
    @NotNull(message = "请选择岗位类别")
    @AllowedValues(values = {"1", "2", "3", "4", "5", "6", "7"}, message = "岗位类别不合法")
    private Integer staffType;

    /**
     * 班次ID（整批共用）：时间段/班别的唯一来源，后端按班次覆盖，入参不接收时间
     */
    @NotNull(message = "请选择班次")
    private Long shiftId;

    /**
     * 状态（0-停诊 1-正常，整批共用）
     */
    @NotNull(message = "状态不能为空")
    @AllowedValues(values = {"0", "1"}, message = "状态取值不合法")
    private Integer status;

    /**
     * 排班日期列表（整批共用）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotEmpty(message = "排班日期不能为空")
    @Size(max = 31, message = "一次最多排 31 天，跨月请分批排")
    private List<LocalDate> scheduleDates;

    /**
     * 人员明细（每人一套号源/费用/诊室）
     */
    @Valid
    @NotEmpty(message = "请选择排班人员")
    @Size(max = 100, message = "一次最多排 100 位人员")
    private List<BizClinicSourceBatchItemDTO> items;
}
