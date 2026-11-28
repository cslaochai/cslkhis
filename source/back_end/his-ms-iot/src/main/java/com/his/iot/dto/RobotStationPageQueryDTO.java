package com.his.iot.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 机器人站点分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RobotStationPageQueryDTO extends PageParam implements Serializable {

    /** 站点编码/名称/归属科室 模糊 */
    private String keyword;

    /** 站点类型 */
    private Integer stationType;

    /** 状态 */
    private Integer enabled;
}
