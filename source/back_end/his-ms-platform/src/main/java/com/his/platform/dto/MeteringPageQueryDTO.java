package com.his.platform.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 计量记录分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MeteringPageQueryDTO extends PageParam implements Serializable {

    /** 设备ID */
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;

    /**
     * 计量类型（1-强检 2-校准）；不筛则不传
     */
    private Integer meteringType;
}
