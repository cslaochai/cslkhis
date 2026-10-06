package com.his.equipment.dto;

import com.his.common.base.PageParam;
import com.his.common.validation.InEnum;
import com.his.equipment.enums.MaintainTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 维保记录分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MaintainQueryPageDTO extends PageParam implements Serializable {

    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;

    /**
     * 维保类型（1-保养 2-维修 3-巡检）；不筛则不传
     */
    @InEnum(value = MaintainTypeEnum.class, message = "维保类型取值不合法（1-保养 2-维修 3-巡检）")
    private Integer maintainType;
}
