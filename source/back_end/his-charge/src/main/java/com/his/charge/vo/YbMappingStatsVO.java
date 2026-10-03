package com.his.charge.vo;

import lombok.Data;

/**
 * 对照率统计行（item_type 1-药品 2-诊疗 3-检验 4-耗材）。
 */
@Data
public class YbMappingStatsVO {

    /**
     * 院内项目类型
     */
    private Integer itemType;

    /**
     * 项目总数（del_flag=0 全部，含停用）
     */
    private Long total;

    /**
     * 已对照数
     */
    private Long mapped;
}
