package com.his.outpatient.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 门诊公共工位字典新增/修改入参
 */
@Data
public class OpdStationUpsertDTO {

    /** 主键ID（空=新增） */
    private Long id;

    /** 工位编码（全局唯一） */
    @NotBlank(message = "工位编码不能为空")
    private String stationCode;

    /** 工位名称 */
    @NotBlank(message = "工位名称不能为空")
    private String stationName;

    /** 工位类别（1-总导诊 2-门诊分诊台 3-输液室 4-注射室 5-采血室 9-其他） */
    @NotNull(message = "工位类别不能为空")
    @AllowedValues(values = {"1", "2", "3", "4", "5", "9"}, message = "工位类别取值不合法")
    private Integer stationKind;

    /** 应到岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他 7-护工，空=护理） */
    @AllowedValues(values = {"1", "2", "3", "4", "5", "6", "7"}, message = "岗位类别不合法")
    private Integer requiredStaffType;

    /** 建议同时在岗人数（空=1） */
    private Integer capacity;

    /** 工位电话 */
    private String phone;

    /** 排序号（空=0） */
    private Integer sortNo;

    /** 状态（0-停用 1-启用，空=启用） */
    @AllowedValues(values = {"0", "1"}, message = "状态取值不合法")
    private Integer status;

    /** 备注 */
    private String remark;
}
