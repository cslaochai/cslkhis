package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 不良事件分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdverseEventQueryPageDTO extends PageParam {

    /** 事件编号 AE+yyyyMMdd+4位 */
    private String eventNo;

    /** 事件类型（字典 his_adverse_event_type） */
    private Integer eventType;

    /** 事件等级（字典 his_adverse_event_level） */
    private Integer eventLevel;

    /** 状态（字典 his_adverse_event_status） */
    private Integer status;

    /** 发生科室的ID */
    private Long occurDeptId;

    /** 关键词（摘要/经过/患者姓名模糊） */
    private String keyword;

    /** 上报日期起（yyyy-MM-dd，按 report_time 自然日，含当天） */
    private String dateStart;

    /** 上报日期止（yyyy-MM-dd，含当天） */
    private String dateEnd;
}
