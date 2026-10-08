package com.his.pharmacy.vo;

import lombok.Data;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 医嘱开立科室（摆药明细上不带科室快照，记账行的归科依据只能回医嘱取）。
 */
@Data
public class WardDispenseOrderDeptVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private String deptName;
}
