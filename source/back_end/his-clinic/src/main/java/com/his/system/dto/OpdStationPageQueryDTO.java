package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门诊公共工位字典分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OpdStationPageQueryDTO extends PageParam {

    /** 工位类别（1总导诊 2分诊台 3输液室 4注射室 5采血室 9其他） */
    private Integer stationKind;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    /** 关键词（编码/名称模糊） */
    private String keyword;
}
