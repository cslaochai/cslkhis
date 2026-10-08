package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 科室名映射行（对应 BizReferralMapper.selectDeptMap）。
 */
@Data
public class DeptMapRowVO implements Serializable {

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 科室名称
     */
    private String deptName;
}
