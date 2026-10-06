package com.his.equipment.dto;

import com.his.common.base.PageParam;
import com.his.common.validation.InEnum;
import com.his.equipment.enums.MeteringTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 计量记录分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MeteringQueryPageDTO extends PageParam implements Serializable {

    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;

    /**
     * 计量类型（1-强检 2-校准）；不筛则不传
     */
    @InEnum(value = MeteringTypeEnum.class, message = "计量类型取值不合法（1-强检 2-校准）")
    private Integer meteringType;
}
