package com.his.appoint.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 排班模板分页查询入参
 * <p>
 * 关键词命中范围：科室名 / 医生名 / 诊室名 / 备注（四列 OR 模糊）——模板页只有这些是可读文本，
 * 号源、费用、星期几都是码值，用关键词搜没有意义。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleTemplateQueryPageDTO extends PageParam {

    /**
     * 关键词（科室名/医生名/诊室名/备注，模糊匹配）
     */
    private String keyword;

    /**
     * 科室ID（空=全部）
     */
    private Long deptId;

    /**
     * 星期几（1-周一 ... 7-周日，空=全部）
     */
    private Integer weekDay;

    /**
     * 状态：0-停用 1-启用（空=全部）
     */
    private Integer status;

    /**
     * 岗位类别（1医生 2护理 3医技 4药学 5收费 6行政其他）：空=全部岗位
     */
    private Integer staffType;
}
