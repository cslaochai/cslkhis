package com.his.appoint.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 按模板生成排班入参
 */
@Data
public class ScheduleTemplateGenerateDTO {

    /**
     * 周偏移（0=本周 1=下周 -1=上周，默认 1=下周）
     */
    @Min(value = -4, message = "周偏移不合法")
    @Max(value = 8, message = "周偏移不合法")
    private Integer weekOffset;

    /**
     * 科室过滤（空=全部科室模板）
     */
    private Long deptId;

    /**
     * 岗位类别过滤（1医生 2护理 3医技 4药学 5收费 6行政其他）：空=全部岗位。
     * 排班员通常按岗位分批生成（先排医生出诊，再排窗口/护理出勤），两类模板规则不同。
     */
    private Integer staffType;
}
