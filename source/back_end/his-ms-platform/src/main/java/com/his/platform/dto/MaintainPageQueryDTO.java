package com.his.platform.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 维保记录分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MaintainPageQueryDTO extends PageParam implements Serializable {

    /** 设备ID */
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;

    /**
     * 维保类型（1-保养 2-维修 3-巡检）；不筛则不传
     */
    private Integer maintainType;
}
