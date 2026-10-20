package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 门诊公共工位字典出参。
 */
@Data
public class OpdStationVO {

    /** 工位ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 工位编码 */
    private String stationCode;
    /** 工位名称 */
    private String stationName;

    /** 工位类别（1总导诊 2分诊台 3输液室 4注射室 5采血室 9其他） */
    private Integer stationKind;
    /** 工位类别文案 */
    private String stationKindText;

    /** 应到岗位类别 */
    private Integer requiredStaffType;
    /** 应到岗位类别文案 */
    private String requiredStaffTypeName;

    /** 建议同时在岗人数 */
    private Integer capacity;
    /** 工位电话 */
    private String phone;
    /** 排序号 */
    private Integer sortNo;
    /** 状态（1启用 0停用） */
    private Integer status;
    /** 备注 */
    private String remark;
}
