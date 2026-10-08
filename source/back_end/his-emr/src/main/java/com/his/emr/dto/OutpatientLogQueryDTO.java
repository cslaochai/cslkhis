package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门诊日志（法规台账）查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OutpatientLogQueryDTO extends PageParam {

    /**
     * 就诊日期起（yyyy-MM-dd，含）
     */
    private String startDate;

    /**
     * 就诊日期止（yyyy-MM-dd，含）
     */
    private String endDate;

    /**
     * 科室ID
     */
    private Long deptId;

    /**
     * 医生（员工ID）筛选
     */
    private Long doctorId;

    /**
     * 关键词：患者姓名 / 病历号 / 诊断名称
     */
    private String keyword;

    /**
     * 只看发热（体温 ≥ 37.3℃）
     */
    private Boolean feverOnly;

    /**
     * 法定可报病筛选：true 只看诊断 ICD 命中法定传染病字典的行
     */
    private Boolean reportableOnly;

    /**
     * 上报情况：1 未上报（漏报风险） 2 已上报；仅在 reportableOnly=true 时有意义
     */
    private Integer reportedFilter;
}
