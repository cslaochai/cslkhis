package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 护理排班审批与变更留痕分页查询入参（280）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NurseScheduleLogQueryDTO extends PageParam {

    /** 科室ID（按排班归属过滤，空则不限） */
    private Long deptId;

    /** 排班日期起（yyyy-MM-dd，按快照列） */
    private String startDate;

    /** 排班日期止（yyyy-MM-dd，按快照列） */
    private String endDate;

    /** 动作类型（1-新增 2-修改/调班 3-删除 4-提交 5-审核发布 6-撤回 7-进入调班 8-整周提交 9-整周发布 10-复制上周） */
    private Integer actionType;

    /** 护士姓名模糊（按快照列） */
    private String nurseName;
}
