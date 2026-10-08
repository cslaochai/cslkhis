package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 科室名映射行（对应 {@code BizReferralMapper.selectDeptMap}）。
 *
 * <p>转诊单要从/from/to 科室 ID 换出科室名，科室量级小所以一次性取全，
 * 服务层再组装成 {@code Map<Long, String>} 回填（那个 Map 是本地字典，不是数据契约）。
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
