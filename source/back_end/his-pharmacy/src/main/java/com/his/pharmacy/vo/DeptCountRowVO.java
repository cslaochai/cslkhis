package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/** 统计口径里的"科室 + 名称"行（按科室生成监测指标时的科室清单，非对外接口 VO） */
@Data
public class DeptCountRowVO {

    /** 被投诉科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 被投诉科室名称（快照） */
    private String deptName;
}
