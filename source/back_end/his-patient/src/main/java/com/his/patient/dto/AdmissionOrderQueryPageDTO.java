package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 住院证查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdmissionOrderQueryPageDTO extends PageParam {

    /** 状态：1-待收治 2-已收治 3-已作废 4-已过期；null-全部 */
    private Integer orderStatus;

    /** 拟收治科室ID */
    private Long applyDeptId;

    /** 开证科室ID */
    private Long sourceDeptId;

    /** 患者姓名 / 住院证号模糊查询 */
    private String keyword;

    /** 来源挂号ID（按门诊线索反查） */
    private Long registId;

    /** 来源挂号号 */
    private String registNo;

    /** 患者ID */
    private Long patientId;

    /** 开证时间起（含），yyyy-MM-dd */
    private String beginDate;

    /** 开证时间止（含当日），yyyy-MM-dd */
    private String endDate;

    /**
     * 是否只查「待收治且未过期」：1-是，其它/不传-否。
     * <p>刻意用 Integer 而不是 Boolean：MySQL 里 boolean 会被 JDBC 绑成 1/0，
     * 写 {@code #{q.onlyPending} = 0} 这种判断在布尔语义下很容易读错，用整数最直白。
     */
    private Integer onlyPending;
}
