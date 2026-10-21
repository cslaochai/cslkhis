package com.his.outpatient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 门诊工位下拉出参（派岗弹框选「排到哪个位」）。
 */
@Data
public class OpdStationSelectVO {

    /** 工位ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 工位编码 */
    private String stationCode;
    /** 工位名称 */
    private String stationName;

    /** 工位类别 */
    private Integer stationKind;
    /** 工位类别文案 */
    private String stationKindText;

    /** 建议同时在岗人数 */
    private Integer capacity;
    /** 工位电话 */
    private String phone;
}
