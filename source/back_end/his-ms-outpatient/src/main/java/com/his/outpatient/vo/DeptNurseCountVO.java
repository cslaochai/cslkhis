package com.his.outpatient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 科室在册护理人员数（护理业务统计后缀，不含科室/病区主数据名称）。
 *
 * <p>科室、病区候选清单分别由科室宿主（selectTree）与病区宿主（ward selectList）提供，
 * 本 VO 只回护理序列在册人数，供候选行叠加「在册 N 人」标注与默认选中判定。
 */
@Data
public class DeptNurseCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 科室ID（病区按本位科室折算）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 在册护士/护师人数
     */
    private Integer nurseCount;
}
