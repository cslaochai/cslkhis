package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对照工作台分页查询（itemType 必填：四张院内表结构不同，必须分类型查）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class YbMappingQueryPageDTO extends PageParam {

    /**
     * 院内项目类型（1-药品 2-诊疗项目 3-检验项目 4-耗材）
     */
    @jakarta.validation.constraints.NotNull(message = "项目类型不能为空")
    private Integer itemType;

    /**
     * 关键字（院内编码/名称、医保编码/目录名模糊）
     */
    private String keyword;

    /**
     * 对照状态（0-未对照 1-已对照；空=全部）
     */
    private Integer mapStatus;

    /**
     * 院内项目状态（0-停用 1-启用；空=全部）
     */
    private Integer itemStatus;
}
