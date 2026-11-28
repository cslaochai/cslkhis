package com.his.iot.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 配送任务单分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RobotDispatchPageQueryDTO extends PageParam implements Serializable {

    /** 状态：1-待调度 2-已派单 3-配送中 4-已签收 5-已取消 6-异常，空为全部 */
    private Integer status;

    /** 优先级：1-常规 2-批量补货 3-急送 */
    private Integer priority;

    /** 发起来源 */
    private String sourceType;

    /** 机器人编号 */
    private String robotNo;

    /** 单号/物品/取货点/收货站点/发起人模糊 */
    private String keyword;
}
