package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 设备台账分页查询。
 *
 * <p>分页字段继承 {@link PageParam}，不在本类重复定义（AGENTS.md 分页 DTO 铁律）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EquipmentQueryPageDTO extends PageParam implements Serializable {

    /**
     * 关键词：设备编码/名称/型号/科室模糊
     */
    private String keyword;

    /**
     * 设备类别（1-大型影像设备 2-检验分析设备 3-生命支持设备 4-手术室设备 5-抢救设备 6-常规诊疗设备 7-其他设备）
     */
    private Integer category;

    /**
     * 状态（1-在用 2-停用 3-维修中 4-报废）
     */
    private Integer status;
}
