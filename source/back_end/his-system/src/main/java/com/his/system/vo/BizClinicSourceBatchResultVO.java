package com.his.system.vo;

import lombok.Data;

/**
 * 排班批量写入结果（批量新增/复刻共用）
 */
@Data
public class BizClinicSourceBatchResultVO {

    /**
     * 实际创建的排班条数
     */
    private Integer created;

    /**
     * 跳过的条数（目标日期已过、与既有排班重叠等）
     */
    private Integer skipped;

    /**
     * 结论话术，前端直接展示
     */
    private String message;
}
