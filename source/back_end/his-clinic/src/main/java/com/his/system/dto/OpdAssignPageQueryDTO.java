package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 门诊工位派岗记录分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OpdAssignPageQueryDTO extends PageParam {

    /** 开始日期（含） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 结束日期（含） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 工位ID */
    private Long stationId;

    /** 工位类别（1总导诊 2分诊台 3输液室 4注射室 5采血室 9其他） */
    private Integer stationKind;

    /** 是否借调（0-否 1-是） */
    private Integer isLoan;

    /** 关键词（员工姓名/工号模糊） */
    private String keyword;
}
