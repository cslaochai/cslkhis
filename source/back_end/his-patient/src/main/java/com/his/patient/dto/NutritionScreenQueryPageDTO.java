package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

/**
 * 营养风险筛查记录分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NutritionScreenQueryPageDTO extends PageParam {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 科室ID
     */
    private Long deptId;

    /**
     * 病区ID
     */
    private Long wardId;

    /**
     * 在院状态：1-在院 0-已出院；空 = 全部
     */
    private Integer admitStatus;

    /**
     * 量表（1-NRS2002 2-PG-SGA 3-MNA）
     */
    private Integer screenType;

    /**
     * 判定：0-无营养风险 1-有营养风险
     */
    private Integer riskFlag;

    /**
     * 筛查时机（1-入院48小时内 2-病情变化复筛 3-术后复筛 4-定期复筛）
     */
    private Integer screenSource;

    /**
     * 只看到了复筛日期还没复筛的（下次筛查日期 <= 今天）：1-是
     */
    private Integer dueOnly;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 关键字：患者姓名 / 患者编号 / 住院号 / 筛查单号
     */
    private String keyword;

    /**
     * 服务端填入的科室数据权限集合，前端传了也不生效
     */
    private List<Long> scopeDeptIds;
}
